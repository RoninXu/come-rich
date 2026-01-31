package com.finance.planner.career.service.impl;

import com.finance.planner.career.dto.*;
import com.finance.planner.career.entity.CareerIncome;
import com.finance.planner.career.entity.CareerPlan;
import com.finance.planner.career.repository.CareerIncomeRepository;
import com.finance.planner.career.repository.CareerPlanRepository;
import com.finance.planner.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CareerPlanServiceImpl Unit Tests")
class CareerPlanServiceImplTest {

    @Mock
    private CareerPlanRepository careerPlanRepository;

    @Mock
    private CareerIncomeRepository careerIncomeRepository;

    @InjectMocks
    private CareerPlanServiceImpl careerPlanService;

    private static final Long USER_ID = 1L;
    private static final Long PLAN_ID = 10L;

    private CareerPlan createPlan(Long id, Long userId, String title) {
        CareerPlan plan = new CareerPlan();
        plan.setId(id);
        plan.setUserId(userId);
        plan.setTitle(title);
        plan.setCareerType("freelance");
        plan.setStatus((short) 1);
        plan.setTargetMonthlyIncome(new BigDecimal("5000"));
        plan.setActualMonthlyIncome(BigDecimal.ZERO);
        plan.setStartDate(LocalDate.now());
        return plan;
    }

    // ========== createPlan ==========

    @Test
    @DisplayName("createPlan - success")
    void createPlan_success() {
        CreateCareerPlanRequest request = CreateCareerPlanRequest.builder()
                .title("Freelance Writing")
                .careerType("freelance")
                .targetMonthlyIncome(new BigDecimal("5000"))
                .build();

        when(careerPlanRepository.save(any(CareerPlan.class))).thenAnswer(invocation -> {
            CareerPlan p = invocation.getArgument(0);
            p.setId(PLAN_ID);
            return p;
        });

        CareerPlanDto result = careerPlanService.createPlan(USER_ID, request);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Freelance Writing");
        assertThat(result.getStatus()).isEqualTo((short) 1);
        verify(careerPlanRepository).save(any(CareerPlan.class));
    }

    // ========== listPlans ==========

    @Test
    @DisplayName("listPlans - returns user plans")
    void listPlans_success() {
        CareerPlan p1 = createPlan(1L, USER_ID, "Plan 1");
        CareerPlan p2 = createPlan(2L, USER_ID, "Plan 2");

        when(careerPlanRepository.findByUserIdOrderByCreatedAtDesc(USER_ID))
                .thenReturn(List.of(p1, p2));

        List<CareerPlanDto> result = careerPlanService.listPlans(USER_ID);

        assertThat(result).hasSize(2);
    }

    // ========== getPlan ==========

    @Test
    @DisplayName("getPlan - success with calculated income")
    void getPlan_success() {
        CareerPlan plan = createPlan(PLAN_ID, USER_ID, "Test Plan");

        when(careerPlanRepository.findByIdAndUserId(PLAN_ID, USER_ID)).thenReturn(Optional.of(plan));
        when(careerIncomeRepository.sumAmountByPlanIdAndDateBetween(eq(PLAN_ID), any(), any()))
                .thenReturn(new BigDecimal("2000"));

        CareerPlanDto result = careerPlanService.getPlan(USER_ID, PLAN_ID);

        assertThat(result).isNotNull();
        assertThat(result.getActualMonthlyIncome()).isEqualByComparingTo(new BigDecimal("2000"));
    }

    @Test
    @DisplayName("getPlan - throws when not found")
    void getPlan_notFound() {
        when(careerPlanRepository.findByIdAndUserId(PLAN_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> careerPlanService.getPlan(USER_ID, PLAN_ID))
                .isInstanceOf(BusinessException.class);
    }

    // ========== deletePlan ==========

    @Test
    @DisplayName("deletePlan - success")
    void deletePlan_success() {
        CareerPlan plan = createPlan(PLAN_ID, USER_ID, "Test");

        when(careerPlanRepository.findByIdAndUserId(PLAN_ID, USER_ID)).thenReturn(Optional.of(plan));

        careerPlanService.deletePlan(USER_ID, PLAN_ID);

        verify(careerPlanRepository).delete(plan);
    }

    // ========== addIncome ==========

    @Test
    @DisplayName("addIncome - success")
    void addIncome_success() {
        CareerPlan plan = createPlan(PLAN_ID, USER_ID, "Test");

        when(careerPlanRepository.findByIdAndUserId(PLAN_ID, USER_ID)).thenReturn(Optional.of(plan));
        when(careerIncomeRepository.save(any(CareerIncome.class))).thenAnswer(invocation -> {
            CareerIncome i = invocation.getArgument(0);
            i.setId(100L);
            return i;
        });

        AddCareerIncomeRequest request = AddCareerIncomeRequest.builder()
                .amount(new BigDecimal("1000"))
                .description("Project payment")
                .incomeDate(LocalDate.now())
                .build();

        CareerIncomeDto result = careerPlanService.addIncome(USER_ID, PLAN_ID, request);

        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("1000"));
    }

    // ========== getIncomeHistory ==========

    @Test
    @DisplayName("getIncomeHistory - success")
    void getIncomeHistory_success() {
        CareerPlan plan = createPlan(PLAN_ID, USER_ID, "Test");
        CareerIncome income = new CareerIncome();
        income.setId(1L);
        income.setCareerPlanId(PLAN_ID);
        income.setAmount(new BigDecimal("1000"));
        income.setIncomeDate(LocalDate.now());

        when(careerPlanRepository.findByIdAndUserId(PLAN_ID, USER_ID)).thenReturn(Optional.of(plan));
        when(careerIncomeRepository.findByCareerPlanIdOrderByIncomeDateDesc(PLAN_ID))
                .thenReturn(List.of(income));

        List<CareerIncomeDto> result = careerPlanService.getIncomeHistory(USER_ID, PLAN_ID);

        assertThat(result).hasSize(1);
    }
}
