package com.finance.planner.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthScoreDto {

    /**
     * Total health score (0-100)
     */
    private int totalScore;

    /**
     * Score grade: A (90-100), B (80-89), C (70-79), D (60-69), F (<60)
     */
    private String grade;

    /**
     * Individual score components
     */
    private int savingAbility;       // 30 points max - savings rate vs 20% target
    private int balanceRatio;         // 25 points max - expense/income ratio
    private int consumptionStructure; // 20 points max - essential vs discretionary
    private int assetGrowth;          // 15 points max - month-over-month improvement
    private int recordingHabit;       // 10 points max - transaction frequency

    /**
     * Detailed breakdown
     */
    private ScoreDetail savingDetail;
    private ScoreDetail balanceDetail;
    private ScoreDetail consumptionDetail;
    private ScoreDetail growthDetail;
    private ScoreDetail habitDetail;

    /**
     * Improvement suggestions
     */
    private List<String> suggestions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreDetail {
        private String name;
        private int score;
        private int maxScore;
        private String description;
        private String status; // good, average, poor
    }

    /**
     * Calculate grade based on total score
     */
    public void calculateGrade() {
        if (totalScore >= 90) {
            this.grade = "A";
        } else if (totalScore >= 80) {
            this.grade = "B";
        } else if (totalScore >= 70) {
            this.grade = "C";
        } else if (totalScore >= 60) {
            this.grade = "D";
        } else {
            this.grade = "F";
        }
    }
}
