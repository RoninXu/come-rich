package com.finance.planner.goal.controller;

import com.finance.planner.auth.entity.User;
import com.finance.planner.auth.repository.UserRepository;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import com.finance.planner.common.response.ApiResponse;
import com.finance.planner.goal.dto.*;
import com.finance.planner.goal.service.GoalAiService;
import com.finance.planner.goal.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
@Tag(name = "Goals", description = "Financial goal planning endpoints")
public class GoalController {

    private final GoalService goalService;
    private final GoalAiService goalAiService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create a new financial goal")
    public ApiResponse<GoalDto> createGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateGoalRequest request) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(goalService.createGoal(userId, request));
    }

    @GetMapping
    @Operation(summary = "List goals with optional status filter")
    public ApiResponse<List<GoalDto>> listGoals(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Status filter: 1=active, 2=completed, 3=abandoned")
            @RequestParam(required = false) Short status) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(goalService.listGoals(userId, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single goal with calculated fields")
    public ApiResponse<GoalDto> getGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(goalService.getGoal(userId, id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing goal")
    public ApiResponse<GoalDto> updateGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateGoalRequest request) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(goalService.updateGoal(userId, id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a goal")
    public ApiResponse<Void> deleteGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = getUserId(userDetails);
        goalService.deleteGoal(userId, id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/progress")
    @Operation(summary = "Add a progress record to a goal")
    public ApiResponse<GoalProgressDto> addProgress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody AddProgressRequest request) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(goalService.addProgress(userId, id, request));
    }

    @GetMapping("/{id}/progress")
    @Operation(summary = "Get progress history for a goal")
    public ApiResponse<List<GoalProgressDto>> getProgressHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(goalService.getProgressHistory(userId, id));
    }

    @GetMapping("/active-count")
    @Operation(summary = "Get active goal count for dashboard")
    public ApiResponse<Integer> getActiveGoalCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(goalService.getActiveGoalCount(userId));
    }

    @PostMapping("/{id}/ai-plan")
    @Operation(summary = "Generate AI achievement plan for a goal")
    public ApiResponse<GoalAiPlanDto> generateAiPlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(goalAiService.generateAiPlan(userId, id));
    }

    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }
}
