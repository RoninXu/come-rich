package com.finance.planner.ai.controller;

import com.finance.planner.ai.agent.dto.AgentConfirmationRequest;
import com.finance.planner.ai.agent.service.AgentCleanupService;
import com.finance.planner.ai.agent.service.AgentRiskConfigService;
import com.finance.planner.ai.agent.service.AgentService;
import com.finance.planner.auth.entity.User;
import com.finance.planner.auth.repository.UserRepository;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import com.finance.planner.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/ai/agent")
@RequiredArgsConstructor
@Tag(name = "AI Agent", description = "AI agent endpoints")
public class AgentController {

    private final AgentService agentService;
    private final AgentRiskConfigService riskConfigService;
    private final AgentCleanupService agentCleanupService;
    private final UserRepository userRepository;

    @GetMapping(value = "/chat-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream AI agent response via SSE")
    public SseEmitter chatStream(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "User message") @RequestParam String message,
            @Parameter(description = "Session ID (optional, auto-generated if not provided)")
            @RequestParam(required = false) String sessionId) {
        Long userId = getUserId(userDetails);
        String effectiveSessionId = sessionId != null ? sessionId : "unknown";
        SseEmitter emitter = new SseEmitter(300_000L);
        emitter.onTimeout(() ->
                agentCleanupService.cleanupSession(userId, effectiveSessionId, "SSE timeout"));
        emitter.onError(t ->
                agentCleanupService.cleanupSession(userId, effectiveSessionId, "SSE error: " + t.getMessage()));
        agentService.streamAgentChat(userId, message, sessionId, emitter);
        return emitter;
    }

    @PostMapping("/confirm")
    @Operation(summary = "Confirm or reject a high-risk agent action")
    public ApiResponse<Void> confirm(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AgentConfirmationRequest request) {
        Long userId = getUserId(userDetails);
        boolean resolved = agentService.respondToConfirmation(userId, request.getConfirmationId(), request.getAccepted());
        if (!resolved) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "确认请求不存在或已过期");
        }
        return ApiResponse.success();
    }

    @GetMapping("/risk-threshold")
    @Operation(summary = "Get user risk threshold for high-risk operations")
    public ApiResponse<BigDecimal> getRiskThreshold(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(riskConfigService.getRiskThreshold(userId));
    }

    @PutMapping("/risk-threshold")
    @Operation(summary = "Update user risk threshold for high-risk operations")
    public ApiResponse<Void> setRiskThreshold(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Threshold amount") @RequestParam BigDecimal threshold) {
        Long userId = getUserId(userDetails);
        riskConfigService.setRiskThreshold(userId, threshold);
        return ApiResponse.success();
    }

    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }
}
