package com.finance.planner.career.service;

import com.finance.planner.career.dto.*;

import java.util.List;

public interface CareerPlanService {

    CareerPlanDto createPlan(Long userId, CreateCareerPlanRequest request);

    List<CareerPlanDto> listPlans(Long userId);

    CareerPlanDto getPlan(Long userId, Long planId);

    CareerPlanDto updatePlan(Long userId, Long planId, CreateCareerPlanRequest request);

    void deletePlan(Long userId, Long planId);

    CareerIncomeDto addIncome(Long userId, Long planId, AddCareerIncomeRequest request);

    List<CareerIncomeDto> getIncomeHistory(Long userId, Long planId);
}
