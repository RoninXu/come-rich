package com.finance.planner.budget.controller;

import com.finance.planner.auth.entity.User;
import com.finance.planner.auth.repository.UserRepository;
import com.finance.planner.budget.dto.*;
import com.finance.planner.budget.service.BudgetAiService;
import com.finance.planner.budget.service.BudgetService;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import com.finance.planner.common.response.ApiResponse;
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
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Tag(name = "Budgets", description = "Budget management and tracking endpoints")
public class BudgetController {

    private final BudgetService budgetService;
    private final BudgetAiService budgetAiService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Set or update a category budget")
    public ApiResponse<BudgetDto> setBudget(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SetBudgetRequest request) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(budgetService.setBudget(userId, request));
    }

    @GetMapping
    @Operation(summary = "Get all budgets for a specific month")
    public ApiResponse<List<BudgetDto>> getBudgets(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Month in YYYY-MM format")
            @RequestParam String yearMonth) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(budgetService.getBudgets(userId, yearMonth));
    }

    @DeleteMapping
    @Operation(summary = "Delete a category budget")
    public ApiResponse<Void> deleteBudget(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long categoryId,
            @Parameter(description = "Month in YYYY-MM format")
            @RequestParam String yearMonth) {
        Long userId = getUserId(userDetails);
        budgetService.deleteBudget(userId, categoryId, yearMonth);
        return ApiResponse.success();
    }

    @PostMapping("/total")
    @Operation(summary = "Set or update the total budget for a month")
    public ApiResponse<BudgetTotalDto> setBudgetTotal(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SetBudgetTotalRequest request) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(budgetService.setBudgetTotal(userId, request));
    }

    @GetMapping("/total")
    @Operation(summary = "Get the total budget for a specific month")
    public ApiResponse<BudgetTotalDto> getBudgetTotal(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Month in YYYY-MM format")
            @RequestParam String yearMonth) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(budgetService.getBudgetTotal(userId, yearMonth));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get budget vs actual spending summary for a month")
    public ApiResponse<BudgetSummaryDto> getBudgetSummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Month in YYYY-MM format")
            @RequestParam String yearMonth) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(budgetService.getBudgetSummary(userId, yearMonth));
    }

    @GetMapping("/trend")
    @Operation(summary = "Get budget trend for recent months")
    public ApiResponse<List<BudgetSummaryDto>> getBudgetTrend(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Number of months to look back")
            @RequestParam(defaultValue = "6") int months) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(budgetService.getBudgetTrend(userId, months));
    }

    @PostMapping("/copy")
    @Operation(summary = "Copy budgets from previous month to target month")
    public ApiResponse<List<BudgetDto>> copyBudgetFromPreviousMonth(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Target month in YYYY-MM format")
            @RequestParam String targetMonth) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(budgetService.copyBudgetFromPreviousMonth(userId, targetMonth));
    }

    @PostMapping("/ai-suggestions")
    @Operation(summary = "Generate AI-powered budget optimization suggestions")
    public ApiResponse<BudgetAiSuggestionDto> generateAiSuggestions(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Month in YYYY-MM format")
            @RequestParam String yearMonth) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(budgetAiService.generateSuggestions(userId, yearMonth));
    }

    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }
}
