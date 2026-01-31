package com.finance.planner.career.controller;

import com.finance.planner.auth.entity.User;
import com.finance.planner.auth.repository.UserRepository;
import com.finance.planner.career.dto.*;
import com.finance.planner.career.service.CareerPlanService;
import com.finance.planner.career.service.CareerRecommendationService;
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

import java.util.List;

@RestController
@RequestMapping("/api/career")
@RequiredArgsConstructor
@Tag(name = "Career", description = "Side hustle recommendations and career planning")
public class CareerController {

    private final CareerPlanService careerPlanService;
    private final CareerRecommendationService careerRecommendationService;
    private final UserRepository userRepository;

    @GetMapping("/recommendations")
    @Operation(summary = "Get AI career recommendations based on user profile")
    public ApiResponse<List<CareerRecommendationDto>> getRecommendations(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(careerRecommendationService.getRecommendations(userId));
    }

    @PostMapping("/plans")
    @Operation(summary = "Create a new career plan (adopt a recommendation)")
    public ApiResponse<CareerPlanDto> createPlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateCareerPlanRequest request) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(careerPlanService.createPlan(userId, request));
    }

    @GetMapping("/plans")
    @Operation(summary = "List all career plans")
    public ApiResponse<List<CareerPlanDto>> listPlans(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(careerPlanService.listPlans(userId));
    }

    @GetMapping("/plans/{id}")
    @Operation(summary = "Get career plan detail")
    public ApiResponse<CareerPlanDto> getPlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(careerPlanService.getPlan(userId, id));
    }

    @PutMapping("/plans/{id}")
    @Operation(summary = "Update a career plan")
    public ApiResponse<CareerPlanDto> updatePlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody CreateCareerPlanRequest request) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(careerPlanService.updatePlan(userId, id, request));
    }

    @DeleteMapping("/plans/{id}")
    @Operation(summary = "Delete a career plan")
    public ApiResponse<Void> deletePlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = getUserId(userDetails);
        careerPlanService.deletePlan(userId, id);
        return ApiResponse.success();
    }

    @PostMapping("/plans/{id}/income")
    @Operation(summary = "Record income for a career plan")
    public ApiResponse<CareerIncomeDto> addIncome(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody AddCareerIncomeRequest request) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(careerPlanService.addIncome(userId, id, request));
    }

    @GetMapping("/plans/{id}/income")
    @Operation(summary = "Get income history for a career plan")
    public ApiResponse<List<CareerIncomeDto>> getIncomeHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(careerPlanService.getIncomeHistory(userId, id));
    }

    @PostMapping("/plans/{id}/startup-plan")
    @Operation(summary = "Generate AI 90-day startup plan")
    public ApiResponse<String> generateStartupPlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(careerRecommendationService.generateStartupPlan(userId, id));
    }

    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }
}
