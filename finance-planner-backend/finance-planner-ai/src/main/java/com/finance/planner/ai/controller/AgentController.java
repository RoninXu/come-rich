package com.finance.planner.ai.controller;

import com.finance.planner.ai.agent.dto.AgentConfirmationRequest;
import com.finance.planner.ai.agent.dto.metrics.AgentErrorMetricDto;
import com.finance.planner.ai.agent.dto.metrics.AgentMetricsOverviewDto;
import com.finance.planner.ai.agent.dto.metrics.AgentMetricsTimelinePointDto;
import com.finance.planner.ai.agent.dto.metrics.AgentToolMetricDto;
import com.finance.planner.ai.agent.service.AgentCleanupService;
import com.finance.planner.ai.agent.service.AgentMetricsQueryService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/ai/agent")
@RequiredArgsConstructor
@Tag(name = "AI Agent", description = "AI agent endpoints")
public class AgentController {

    private final AgentService agentService;
    private final AgentRiskConfigService riskConfigService;
    private final AgentCleanupService agentCleanupService;
    private final AgentMetricsQueryService metricsQueryService;
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

    @GetMapping("/metrics/overview")
    @Operation(summary = "Get agent metrics overview")
    public ApiResponse<AgentMetricsOverviewDto> getMetricsOverview(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "7") Integer days) {
        Long userId = getUserId(userDetails);
        DateRange range = resolveRange(startDate, endDate, days);
        return ApiResponse.success(metricsQueryService.getOverview(userId, range.start(), range.end()));
    }

    @GetMapping("/metrics/tools")
    @Operation(summary = "Get agent tool metrics ranking")
    public ApiResponse<List<AgentToolMetricDto>> getToolMetrics(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "7") Integer days,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        Long userId = getUserId(userDetails);
        DateRange range = resolveRange(startDate, endDate, days);
        return ApiResponse.success(metricsQueryService.getToolMetrics(userId, range.start(), range.end(), limit));
    }

    @GetMapping("/metrics/timeline")
    @Operation(summary = "Get agent metrics timeline")
    public ApiResponse<List<AgentMetricsTimelinePointDto>> getMetricsTimeline(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "7") Integer days) {
        Long userId = getUserId(userDetails);
        DateRange range = resolveRange(startDate, endDate, days);
        return ApiResponse.success(metricsQueryService.getTimeline(userId, range.start(), range.end()));
    }

    @GetMapping("/metrics/errors")
    @Operation(summary = "Get agent error metrics")
    public ApiResponse<List<AgentErrorMetricDto>> getErrorMetrics(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "7") Integer days,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        Long userId = getUserId(userDetails);
        DateRange range = resolveRange(startDate, endDate, days);
        return ApiResponse.success(metricsQueryService.getErrorStats(userId, range.start(), range.end(), limit));
    }

    private DateRange resolveRange(LocalDate startDate, LocalDate endDate, Integer days) {
        if ((startDate == null) != (endDate == null)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "startDate 和 endDate 需要同时提供");
        }

        if (startDate != null) {
            if (endDate.isBefore(startDate)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "endDate 不能早于 startDate");
            }
            return new DateRange(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay().minusNanos(1));
        }

        int effectiveDays = days == null ? 7 : days;
        if (effectiveDays <= 0 || effectiveDays > 365) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "days 必须在 1 到 365 之间");
        }

        LocalDateTime end = LocalDateTime.now();
        return new DateRange(end.minusDays(effectiveDays), end);
    }

    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }

    private record DateRange(LocalDateTime start, LocalDateTime end) {
    }
}
