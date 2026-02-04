package com.finance.planner.career.dto;

import com.finance.planner.career.entity.UserProfile;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileDtoTest {

    @Test
    void fromEntityShouldMapTimezone() {
        UserProfile profile = new UserProfile();
        profile.setId(1L);
        profile.setTimezone("Asia/Shanghai");

        UserProfileDto dto = UserProfileDto.fromEntity(profile);

        assertThat(dto.getTimezone()).isEqualTo("Asia/Shanghai");
    }
}
