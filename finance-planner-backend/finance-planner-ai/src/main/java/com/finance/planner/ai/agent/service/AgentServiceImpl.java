package com.finance.planner.ai.agent.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.planner.ai.agent.dto.AgentConfirmationPayload;
import com.finance.planner.ai.agent.dto.AgentToolCall;
import com.finance.planner.ai.agent.dto.AgentToolCallPayload;
import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.Tool;
import com.finance.planner.ai.agent.tool.ToolExecutor;
import com.finance.planner.ai.agent.tool.ToolRegistry;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.util.RelativeDateResolver;
import com.finance.planner.ai.dto.LlmStreamEvent;
import com.finance.planner.ai.dto.ToolCallChunk;
import com.finance.planner.ai.service.ConversationService;
import com.finance.planner.ai.service.LlmClient;
import com.finance.planner.ai.service.RateLimitService;
import com.finance.planner.ai.time.TimeContext;
import com.finance.planner.ai.time.TimeContextProvider;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {

    private static final int MAX_ITERATIONS = 10;

    private final RateLimitService rateLimitService;
    private final ConversationService conversationService;
    private final LlmClient llmClient;
    private final ToolRegistry toolRegistry;
    private final ToolExecutor toolExecutor;
    private final AgentContextBuilder contextBuilder;
    private final AgentSseHelper sseHelper;
    private final ConfirmationStore confirmationStore;
    private final AgentRiskConfigService riskConfigService;
    private final TimeContextProvider timeContextProvider;
    private final ObjectMapper objectMapper;
    private final ToolCacheService toolCacheService;
    private final AgentCleanupService agentCleanupService;
    private final AgentMetricsService agentMetricsService;

    @Override
    public void streamAgentChat(Long userId, String message, String sessionId, SseEmitter emitter) {
        String reservationId = rateLimitService.reserveQuota(userId);

        String finalSessionId = conversationService.createOrGetSessionId(sessionId);
        conversationService.saveMessage(userId, finalSessionId, "user", message, null, "text", null, null);

        Schedulers.boundedElastic().schedule(() -> {
            try {
                runAgentLoop(userId, finalSessionId, message, emitter);
                rateLimitService.commitQuota(reservationId);
            } catch (Exception e) {
                rateLimitService.releaseQuota(reservationId);
                throw e;
            }
        });
    }

    @Override
    public boolean respondToConfirmation(Long userId, String confirmationId, boolean accepted) {
        return confirmationStore.resolve(userId, confirmationId, accepted);
    }

    private void runAgentLoop(Long userId, String sessionId, String userMessage, SseEmitter emitter) {
        try {
            List<Map<String, Object>> messages = contextBuilder.buildMessages(userId, sessionId, userMessage);
            BigDecimal riskThreshold = riskConfigService.getRiskThreshold(userId);

            for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
                ToolCallAccumulator accumulator = new ToolCallAccumulator();
                StringBuilder assistantContent = new StringBuilder();
                String[] finishReasonHolder = new String[1];

                llmClient.streamChatWithTools(messages, toolRegistry.getToolSchemas(), "auto")
                        .publishOn(Schedulers.boundedElastic())
                        .doOnNext(event -> handleStreamEvent(event, sessionId, emitter, assistantContent, accumulator, finishReasonHolder))
                        .doOnError(error -> {
                            log.error("Agent stream error: {}", error.getMessage());
                            sseHelper.sendError(emitter, "AI 服务暂时不可用，请稍后重试");
                        })
                        .blockLast();

                List<AgentToolCall> toolCalls = accumulator.toToolCalls();
                if (!toolCalls.isEmpty()) {
                    String toolCallsJson = objectMapper.writeValueAsString(buildToolCallsPayload(toolCalls));
                    conversationService.saveMessage(userId, sessionId, "assistant", assistantContent.toString(), null, "tool_call", toolCallsJson, null);

                    Map<String, Object> assistantMessage = new LinkedHashMap<>();
                    assistantMessage.put("role", "assistant");
                    assistantMessage.put("content", assistantContent.toString());
                    assistantMessage.put("tool_calls", buildToolCallsPayload(toolCalls));
                    messages.add(assistantMessage);

                    for (AgentToolCall toolCall : toolCalls) {
                        executeToolCall(userId, sessionId, toolCall, userMessage, messages, riskThreshold, emitter);
                    }
                    continue;
                }

                if (assistantContent.length() > 0) {
                    conversationService.saveMessage(userId, sessionId, "assistant", assistantContent.toString(), null, "text", null, null);
                    Map<String, Object> assistantMessage = new LinkedHashMap<>();
                    assistantMessage.put("role", "assistant");
                    assistantMessage.put("content", assistantContent.toString());
                    messages.add(assistantMessage);
                }

                if ("stop".equalsIgnoreCase(finishReasonHolder[0]) || "length".equalsIgnoreCase(finishReasonHolder[0]) || finishReasonHolder[0] == null) {
                    break;
                }
            }

            sseHelper.sendDone(emitter, sessionId);
        } catch (Exception e) {
            log.error("Agent loop failed: {}", e.getMessage(), e);
            agentCleanupService.cleanupSession(userId, sessionId, e.getMessage());
            sseHelper.sendError(emitter, "AI 处理失败，请稍后再试");
        } finally {
            try { emitter.complete(); } catch (Exception ignored) {}
        }
    }

    private void handleStreamEvent(
            LlmStreamEvent event,
            String sessionId,
            SseEmitter emitter,
            StringBuilder assistantContent,
            ToolCallAccumulator accumulator,
            String[] finishReasonHolder
    ) {
        if (event.getContent() != null) {
            assistantContent.append(event.getContent());
            sseHelper.sendContent(emitter, sessionId, event.getContent());
        }
        if (event.getToolCalls() != null) {
            accumulator.addChunks(event.getToolCalls());
        }
        if (event.getFinishReason() != null) {
            finishReasonHolder[0] = event.getFinishReason();
        }
    }

    private void executeToolCall(
            Long userId,
            String sessionId,
            AgentToolCall toolCall,
            String userMessage,
            List<Map<String, Object>> messages,
            BigDecimal riskThreshold,
            SseEmitter emitter
    ) throws Exception {
        long startTime = System.currentTimeMillis();
        boolean wasCached = false;
        ToolResult result = null;

        try {
            Tool tool = toolRegistry.getTool(toolCall.getName());
            if (tool == null) {
                result = ToolResult.failure("未知工具: " + toolCall.getName());
                appendToolResult(userId, sessionId, toolCall, result, messages);
                sseHelper.sendToolCallResult(emitter, buildToolPayload(toolCall, result));
                return;
            }

            Map<String, Object> arguments = new HashMap<>(parseArguments(toolCall.getArguments()));
            applyRelativeDateOverride(toolCall.getName(), userMessage, arguments, userId);

            // Check cache for read-only tools
            Optional<ToolResult> cached = toolCacheService.get(sessionId, toolCall.getName(), arguments);
            if (cached.isPresent()) {
                result = cached.get();
                wasCached = true;
                appendToolResult(userId, sessionId, toolCall, result, messages);
                sseHelper.sendToolCallResult(emitter, buildToolPayload(toolCall, result));
                return;
            }

            RiskLevel riskLevel = toolExecutor.resolveRiskLevel(tool, arguments, riskThreshold);

            sseHelper.sendToolCallStart(emitter, buildToolPayload(toolCall, null));

            if (riskLevel == RiskLevel.HIGH) {
                String confirmationId = confirmationStore.create(userId, toolCall.getId(), toolCall.getName());
                AgentConfirmationPayload payload = AgentConfirmationPayload.builder()
                        .confirmationId(confirmationId)
                        .toolCallId(toolCall.getId())
                        .toolName(toolCall.getName())
                        .riskLevel(riskLevel)
                        .summary("此操作为高风险操作，需要确认后继续执行")
                        .build();
                sseHelper.sendConfirmationRequired(emitter, payload);
                boolean accepted = confirmationStore.awaitDecision(confirmationId, 300);
                Map<String, Object> resolved = new HashMap<>();
                resolved.put("confirmationId", confirmationId);
                resolved.put("toolCallId", toolCall.getId());
                resolved.put("accepted", accepted);
                sseHelper.sendConfirmationResolved(emitter, resolved);
                if (!accepted) {
                    result = ToolResult.failure("用户拒绝了该操作");
                    appendToolResult(userId, sessionId, toolCall, result, messages);
                    sseHelper.sendToolCallResult(emitter, buildToolPayload(toolCall, result));
                    return;
                }
            }

            result = toolExecutor.executeTool(tool, userId, arguments, riskThreshold);

            // Cache successful results for cacheable tools
            toolCacheService.put(sessionId, toolCall.getName(), arguments, result);

            // Invalidate related caches for write operations
            if (tool.getRiskLevel() != RiskLevel.LOW) {
                toolCacheService.invalidateRelated(sessionId, toolCall.getName());
            }

            appendToolResult(userId, sessionId, toolCall, result, messages);
            sseHelper.sendToolCallResult(emitter, buildToolPayload(toolCall, result));
        } finally {
            long latency = System.currentTimeMillis() - startTime;
            boolean success = result != null && result.isSuccess();
            String errorMsg = (result != null && !result.isSuccess()) ? result.getMessage() : null;
            agentMetricsService.recordToolExecution(userId, sessionId, toolCall.getName(),
                    success, latency, errorMsg, wasCached);
        }
    }

    private void appendToolResult(
            Long userId,
            String sessionId,
            AgentToolCall toolCall,
            ToolResult result,
            List<Map<String, Object>> messages
    ) throws Exception {
        String resultJson = objectMapper.writeValueAsString(result);
        conversationService.saveMessage(userId, sessionId, "tool", resultJson, null, "tool", null, toolCall.getId());

        Map<String, Object> toolMessage = new LinkedHashMap<>();
        toolMessage.put("role", "tool");
        toolMessage.put("tool_call_id", toolCall.getId());
        toolMessage.put("content", resultJson);
        messages.add(toolMessage);
    }

    private Map<String, Object> parseArguments(String arguments) {
        if (arguments == null || arguments.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(arguments, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("raw", arguments);
            return fallback;
        }
    }

    private void applyRelativeDateOverride(String toolName, String userMessage, Map<String, Object> arguments, Long userId) {
        if (!"create_transaction".equals(toolName) && !"update_transaction".equals(toolName)) {
            return;
        }
        TimeContext timeContext = timeContextProvider.getTimeContext(userId, null);
        Clock clock = buildClock(timeContext);
        RelativeDateResolver.resolveFromMessage(userMessage, clock)
                .ifPresent(date -> arguments.put("transactionDate", date.toString()));
    }

    private Clock buildClock(TimeContext timeContext) {
        if (timeContext != null && timeContext.getServerTime() != null) {
            try {
                ZonedDateTime zdt = ZonedDateTime.parse(timeContext.getServerTime());
                return Clock.fixed(zdt.toInstant(), zdt.getZone());
            } catch (Exception ignored) {
            }
        }
        return Clock.systemDefaultZone();
    }

    private List<Map<String, Object>> buildToolCallsPayload(List<AgentToolCall> toolCalls) {
        List<Map<String, Object>> payload = new ArrayList<>();
        for (AgentToolCall call : toolCalls) {
            Map<String, Object> function = new LinkedHashMap<>();
            function.put("name", call.getName());
            function.put("arguments", call.getArguments());

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", call.getId());
            entry.put("type", "function");
            entry.put("function", function);
            payload.add(entry);
        }
        return payload;
    }

    private AgentToolCallPayload buildToolPayload(AgentToolCall toolCall, ToolResult result) {
        return AgentToolCallPayload.builder()
                .toolCallId(toolCall.getId())
                .toolName(toolCall.getName())
                .arguments(toolCall.getArguments())
                .result(result)
                .build();
    }

    private static class ToolCallAccumulator {
        private final Map<Integer, AgentToolCall> calls = new LinkedHashMap<>();

        void addChunks(List<ToolCallChunk> chunks) {
            for (ToolCallChunk chunk : chunks) {
                if (chunk.getIndex() == null) {
                    continue;
                }
                AgentToolCall existing = calls.getOrDefault(chunk.getIndex(), new AgentToolCall());
                existing.setId(chunk.getId() != null ? chunk.getId() : existing.getId());
                existing.setName(chunk.getName() != null ? chunk.getName() : existing.getName());
                if (chunk.getArguments() != null) {
                    String existingArgs = existing.getArguments() == null ? "" : existing.getArguments();
                    existing.setArguments(existingArgs + chunk.getArguments());
                }
                calls.put(chunk.getIndex(), existing);
            }
        }

        List<AgentToolCall> toToolCalls() {
            return new ArrayList<>(calls.values());
        }
    }
}
