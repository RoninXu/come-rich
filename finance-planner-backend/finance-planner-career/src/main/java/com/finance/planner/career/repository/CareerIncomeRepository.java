package com.finance.planner.career.repository;

import com.finance.planner.career.entity.CareerIncome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CareerIncomeRepository extends JpaRepository<CareerIncome, Long> {

    List<CareerIncome> findByCareerPlanIdOrderByIncomeDateDesc(Long careerPlanId);

    @Query("SELECT COALESCE(SUM(ci.amount), 0) FROM CareerIncome ci " +
           "WHERE ci.careerPlanId = :planId AND ci.incomeDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByPlanIdAndDateBetween(@Param("planId") Long planId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);
}
