package com.finance.planner.career.controller;

import com.finance.planner.auth.entity.User;
import com.finance.planner.auth.repository.UserRepository;
import com.finance.planner.career.dto.SaveProfileRequest;
import com.finance.planner.career.dto.UserProfileDto;
import com.finance.planner.career.service.UserProfileService;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import com.finance.planner.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "User profile for career recommendations")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get user profile")
    public ApiResponse<UserProfileDto> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(userProfileService.getProfile(userId));
    }

    @PutMapping
    @Operation(summary = "Save or update user profile")
    public ApiResponse<UserProfileDto> saveProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SaveProfileRequest request) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(userProfileService.saveProfile(userId, request));
    }

    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }
}
