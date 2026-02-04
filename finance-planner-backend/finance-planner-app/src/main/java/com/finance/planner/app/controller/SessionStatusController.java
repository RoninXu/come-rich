package com.finance.planner.app.controller;

import com.finance.planner.app.dto.SessionStatusDto;
import com.finance.planner.app.service.SessionStatusService;
import com.finance.planner.auth.entity.User;
import com.finance.planner.auth.repository.UserRepository;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import com.finance.planner.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI Session", description = "AI session status and diagnostics")
public class SessionStatusController {

    private final SessionStatusService sessionStatusService;
    private final UserRepository userRepository;

    @GetMapping("/session-status")
    @Operation(summary = "Get AI session status with server time context")
    public ApiResponse<SessionStatusDto> getSessionStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Session ID (optional)")
            @RequestParam(required = false) String sessionId) {
        Long userId = getUserId(userDetails);
        SessionStatusDto status = sessionStatusService.buildStatus(userId, sessionId);
        return ApiResponse.success(status);
    }

    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }
}
