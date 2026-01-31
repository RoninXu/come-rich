package com.finance.planner.career.service.impl;

import com.finance.planner.career.dto.*;
import com.finance.planner.career.entity.CareerIncome;
import com.finance.planner.career.entity.CareerPlan;
import com.finance.planner.career.repository.CareerIncomeRepository;
import com.finance.planner.career.repository.CareerPlanRepository;
import com.finance.planner.career.service.CareerPlanService;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CareerPlanServiceImpl implements CareerPlanService {

    private final CareerPlanRepository careerPlanRepository;
    private final CareerIncomeRepository careerIncomeRepository;

    @Override
    @Transactional
    public CareerPlanDto createPlan(Long userId, CreateCareerPlanRequest request) {
        CareerPlan plan = new CareerPlan();
        plan.setUserId(userId);
        plan.setCareerType(request.getCareerType());
        plan.setTitle(request.getTitle());
        plan.setDescription(request.getDescription());
        plan.setMatchScore(request.getMatchScore());
        plan.setStatus((short) 1);
        plan.setTargetMonthlyIncome(request.getTargetMonthlyIncome());
        plan.setActualMonthlyIncome(BigDecimal.ZERO);
        plan.setStartDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now());

        CareerPlan saved = careerPlanRepository.save(plan);
        log.info("Created career plan {} for user {}", saved.getId(), userId);
        return CareerPlanDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CareerPlanDto> listPlans(Long userId) {
        return careerPlanRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(CareerPlanDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CareerPlanDto getPlan(Long userId, Long planId) {
        CareerPlan plan = careerPlanRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAREER_PLAN_NOT_FOUND));

        CareerPlanDto dto = CareerPlanDto.fromEntity(plan);
        // Calculate actual monthly income for current month
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        BigDecimal monthlyIncome = careerIncomeRepository.sumAmountByPlanIdAndDateBetween(
                planId, monthStart, monthEnd);
        dto.setActualMonthlyIncome(monthlyIncome);
        return dto;
    }

    @Override
    @Transactional
    public CareerPlanDto updatePlan(Long userId, Long planId, CreateCareerPlanRequest request) {
        CareerPlan plan = careerPlanRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAREER_PLAN_NOT_FOUND));

        if (request.getTitle() != null) plan.setTitle(request.getTitle());
        if (request.getDescription() != null) plan.setDescription(request.getDescription());
        if (request.getCareerType() != null) plan.setCareerType(request.getCareerType());
        if (request.getTargetMonthlyIncome() != null) plan.setTargetMonthlyIncome(request.getTargetMonthlyIncome());

        CareerPlan saved = careerPlanRepository.save(plan);
        log.info("Updated career plan {} for user {}", saved.getId(), userId);
        return CareerPlanDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deletePlan(Long userId, Long planId) {
        CareerPlan plan = careerPlanRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAREER_PLAN_NOT_FOUND));
        careerPlanRepository.delete(plan);
        log.info("Deleted career plan {} for user {}", planId, userId);
    }

    @Override
    @Transactional
    public CareerIncomeDto addIncome(Long userId, Long planId, AddCareerIncomeRequest request) {
        careerPlanRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAREER_PLAN_NOT_FOUND));

        CareerIncome income = new CareerIncome();
        income.setCareerPlanId(planId);
        income.setAmount(request.getAmount());
        income.setDescription(request.getDescription());
        income.setIncomeDate(request.getIncomeDate());

        CareerIncome saved = careerIncomeRepository.save(income);
        log.info("Added income {} to career plan {} for user {}", saved.getId(), planId, userId);
        return CareerIncomeDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CareerIncomeDto> getIncomeHistory(Long userId, Long planId) {
        careerPlanRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAREER_PLAN_NOT_FOUND));

        return careerIncomeRepository.findByCareerPlanIdOrderByIncomeDateDesc(planId)
                .stream()
                .map(CareerIncomeDto::fromEntity)
                .collect(Collectors.toList());
    }
}
