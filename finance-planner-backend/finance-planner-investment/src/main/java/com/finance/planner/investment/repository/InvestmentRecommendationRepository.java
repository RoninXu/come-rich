package com.finance.planner.investment.repository;

import com.finance.planner.investment.entity.InvestmentRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvestmentRecommendationRepository extends JpaRepository<InvestmentRecommendation, Long> {

    List<InvestmentRecommendation> findByUserIdAndStatus(Long userId, Short status);

    List<InvestmentRecommendation> findByRiskAssessmentId(Long riskAssessmentId);
}
