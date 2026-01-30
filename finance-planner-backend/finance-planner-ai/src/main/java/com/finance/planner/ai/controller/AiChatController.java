package com.finance.planner.ai.controller;

import com.finance.planner.ai.dto.ConversationHistoryDto;
import com.finance.planner.ai.service.AiChatService;
import com.finance.planner.ai.service.LlmProviderManager;
import com.finance.planner.auth.entity.User;
import com.finance.planner.auth.repository.UserRepository;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import com.finance.planner.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI Chat", description = "AI conversation and LLM provider management endpoints")
public class AiChatController {

    private final AiChatService aiChatService;
    private final LlmProviderManager llmProviderManager;
    private final UserRepository userRepository;

    @GetMapping(value = "/chat-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream AI chat response via SSE")
    public SseEmitter chatStream(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "User message") @RequestParam String message,
            @Parameter(description = "Session ID (optional, auto-generated if not provided)")
            @RequestParam(required = false) String sessionId) {
        Long userId = getUserId(userDetails);
        SseEmitter emitter = new SseEmitter(120_000L);

        emitter.onCompletion(() -> {});
        emitter.onTimeout(emitter::complete);

        aiChatService.streamChat(userId, message, sessionId, emitter);

        return emitter;
    }

    @GetMapping("/history")
    @Operation(summary = "Get conversation history for a session")
    public ApiResponse<ConversationHistoryDto> getHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Session ID") @RequestParam String sessionId) {
        Long userId = getUserId(userDetails);
        ConversationHistoryDto history = aiChatService.getHistory(userId, sessionId);
        return ApiResponse.success(history);
    }

    @GetMapping("/remaining")
    @Operation(summary = "Get remaining chat count for today")
    public ApiResponse<Integer> getRemainingChats(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        int remaining = aiChatService.getRemainingChats(userId);
        return ApiResponse.success(remaining);
    }

    @GetMapping("/providers")
    @Operation(summary = "List all available LLM providers")
    public ApiResponse<List<String>> listProviders() {
        return ApiResponse.success(llmProviderManager.listProviders());
    }

    @GetMapping("/provider")
    @Operation(summary = "Get current active LLM provider")
    public ApiResponse<String> getCurrentProvider() {
        return ApiResponse.success(llmProviderManager.getActiveProvider());
    }

    @PutMapping("/provider")
    @Operation(summary = "Switch active LLM provider at runtime")
    public ApiResponse<Void> switchProvider(
            @Parameter(description = "Provider name") @RequestParam String name) {
        llmProviderManager.switchProvider(name);
        return ApiResponse.success();
    }

    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }
}
