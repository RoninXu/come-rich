package com.finance.planner.auth.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoginResponse Unit Tests")
class LoginResponseTest {

    @Test
    @DisplayName("of factory method - creates LoginResponse with all fields correctly set")
    void testOfFactoryMethod() {
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .nickname("TestNick")
                .avatarUrl("https://example.com/avatar.png")
                .createdAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .build();

        LoginResponse response = LoginResponse.of("jwt-token-123", 86400000L, userResponse);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token-123");
        assertThat(response.getExpiresIn()).isEqualTo(86400000L);
        assertThat(response.getUser()).isEqualTo(userResponse);
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("of factory method - sets tokenType to Bearer by default")
    void testTokenTypeIsBearerByDefault() {
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .build();

        LoginResponse response = LoginResponse.of("some-token", 3600000L, userResponse);

        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }
}
