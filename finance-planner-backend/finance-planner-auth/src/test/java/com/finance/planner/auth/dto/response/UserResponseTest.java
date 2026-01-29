package com.finance.planner.auth.dto.response;

import com.finance.planner.auth.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserResponse Unit Tests")
class UserResponseTest {

    @Test
    @DisplayName("fromEntity - maps all User entity fields to UserResponse correctly")
    void testFromEntity() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setNickname("TestNick");
        user.setAvatarUrl("https://example.com/avatar.png");
        user.setCreatedAt(LocalDateTime.of(2025, 3, 15, 10, 30, 0));

        UserResponse response = UserResponse.fromEntity(user);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getNickname()).isEqualTo("TestNick");
        assertThat(response.getAvatarUrl()).isEqualTo("https://example.com/avatar.png");
        assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 3, 15, 10, 30, 0));
    }

    @Test
    @DisplayName("fromEntity - handles null optional fields gracefully")
    void testFromEntityWithNullFields() {
        User user = new User();
        user.setId(2L);
        user.setUsername("minimaluser");
        // email, nickname, avatarUrl, createdAt are all null

        UserResponse response = UserResponse.fromEntity(user);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getUsername()).isEqualTo("minimaluser");
        assertThat(response.getEmail()).isNull();
        assertThat(response.getNickname()).isNull();
        assertThat(response.getAvatarUrl()).isNull();
        assertThat(response.getCreatedAt()).isNull();
    }
}
