package com.finance.planner.analysis.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HealthScoreDto Unit Tests")
class HealthScoreDtoTest {

    // ========== calculateGrade ==========

    @Test
    @DisplayName("calculateGrade - grade A for score >= 90")
    void calculateGrade_A() {
        // Given
        HealthScoreDto dto = HealthScoreDto.builder()
                .totalScore(95)
                .build();

        // When
        dto.calculateGrade();

        // Then
        assertThat(dto.getGrade()).isEqualTo("A");
    }

    @Test
    @DisplayName("calculateGrade - grade B for score between 80 and 89")
    void calculateGrade_B() {
        // Given
        HealthScoreDto dto = HealthScoreDto.builder()
                .totalScore(85)
                .build();

        // When
        dto.calculateGrade();

        // Then
        assertThat(dto.getGrade()).isEqualTo("B");
    }

    @Test
    @DisplayName("calculateGrade - grade C for score between 70 and 79")
    void calculateGrade_C() {
        // Given
        HealthScoreDto dto = HealthScoreDto.builder()
                .totalScore(75)
                .build();

        // When
        dto.calculateGrade();

        // Then
        assertThat(dto.getGrade()).isEqualTo("C");
    }

    @Test
    @DisplayName("calculateGrade - grade D for score between 60 and 69")
    void calculateGrade_D() {
        // Given
        HealthScoreDto dto = HealthScoreDto.builder()
                .totalScore(65)
                .build();

        // When
        dto.calculateGrade();

        // Then
        assertThat(dto.getGrade()).isEqualTo("D");
    }

    @Test
    @DisplayName("calculateGrade - grade F for score below 60")
    void calculateGrade_F() {
        // Given
        HealthScoreDto dto = HealthScoreDto.builder()
                .totalScore(45)
                .build();

        // When
        dto.calculateGrade();

        // Then
        assertThat(dto.getGrade()).isEqualTo("F");
    }
}
