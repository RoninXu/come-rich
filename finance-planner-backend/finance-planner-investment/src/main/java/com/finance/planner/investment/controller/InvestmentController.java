package com.finance.planner.investment.controller;

import com.finance.planner.auth.entity.User;
import com.finance.planner.auth.repository.UserRepository;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.response.ApiResponse;
import com.finance.planner.common.exception.BusinessException;
import com.finance.planner.investment.dto.*;
import com.finance.planner.investment.service.InvestmentAdviceService;
import com.finance.planner.investment.service.RiskAssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/investment")
@RequiredArgsConstructor
@Tag(name = "Investment", description = "Investment advice and risk assessment endpoints")
public class InvestmentController {

    private final RiskAssessmentService riskAssessmentService;
    private final InvestmentAdviceService investmentAdviceService;
    private final UserRepository userRepository;

    @GetMapping("/quiz")
    @Operation(summary = "Get risk assessment quiz questions")
    public ApiResponse<List<RiskQuizQuestionDto>> getQuizQuestions() {
        return ApiResponse.success(riskAssessmentService.getQuizQuestions());
    }

    @PostMapping("/quiz")
    @Operation(summary = "Submit risk assessment quiz answers")
    public ApiResponse<RiskAssessmentDto> submitQuiz(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RiskQuizRequest request) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(riskAssessmentService.submitQuiz(userId, request));
    }

    @GetMapping("/assessment")
    @Operation(summary = "Get latest risk assessment result")
    public ApiResponse<RiskAssessmentDto> getLatestAssessment(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(riskAssessmentService.getLatestAssessment(userId));
    }

    @GetMapping("/assessment/history")
    @Operation(summary = "Get risk assessment history")
    public ApiResponse<List<RiskAssessmentDto>> getAssessmentHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(riskAssessmentService.getAssessmentHistory(userId));
    }

    @PostMapping("/recommendations")
    @Operation(summary = "Generate AI investment recommendations")
    public ApiResponse<InvestmentAdviceDto> generateRecommendations(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(investmentAdviceService.generateRecommendations(userId));
    }

    @GetMapping("/recommendations")
    @Operation(summary = "Get active investment recommendations")
    public ApiResponse<List<InvestmentRecommendationDto>> getActiveRecommendations(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(investmentAdviceService.getActiveRecommendations(userId));
    }

    @GetMapping("/allocation")
    @Operation(summary = "Get asset allocation data for chart")
    public ApiResponse<AssetAllocationDto> getAssetAllocation(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(investmentAdviceService.getAssetAllocation(userId));
    }

    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }
}
