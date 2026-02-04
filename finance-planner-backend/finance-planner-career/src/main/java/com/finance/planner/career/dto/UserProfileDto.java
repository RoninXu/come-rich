package com.finance.planner.career.dto;

import com.finance.planner.career.entity.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    private Long id;
    private String occupation;
    private String skills;
    private Integer availableHoursPerWeek;
    private BigDecimal incomeExpectation;
    private String interests;
    private String experienceLevel;
    private String timezone;

    public static UserProfileDto fromEntity(UserProfile profile) {
        return UserProfileDto.builder()
                .id(profile.getId())
                .occupation(profile.getOccupation())
                .skills(profile.getSkills())
                .availableHoursPerWeek(profile.getAvailableHoursPerWeek())
                .incomeExpectation(profile.getIncomeExpectation())
                .interests(profile.getInterests())
                .experienceLevel(profile.getExperienceLevel())
                .timezone(profile.getTimezone())
                .build();
    }
}
