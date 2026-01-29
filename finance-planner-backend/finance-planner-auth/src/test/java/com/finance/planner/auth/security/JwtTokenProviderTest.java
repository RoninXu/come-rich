package com.finance.planner.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_SECRET = "testSecretKeyForJWTTokenGenerationWhichShouldBeLongEnoughForHS512AlgorithmTesting";
    private static final Long TEST_EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", TEST_EXPIRATION);
    }

    @Test
    @DisplayName("generateToken with username - returns valid JWT string")
    void generateToken_withUsername() {
        String token = jwtTokenProvider.generateToken("testuser");

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
        // JWT tokens have 3 parts separated by dots
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("generateToken with Authentication - extracts username from principal")
    void generateToken_withAuthentication() {
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("authuser");

        String token = jwtTokenProvider.generateToken(authentication);

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
        // Verify the token contains the correct username
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);
        assertThat(extractedUsername).isEqualTo("authuser");
    }

    @Test
    @DisplayName("getUsernameFromToken - extracts correct username")
    void getUsernameFromToken() {
        String token = jwtTokenProvider.generateToken("expectedUser");

        String username = jwtTokenProvider.getUsernameFromToken(token);

        assertThat(username).isEqualTo("expectedUser");
    }

    @Test
    @DisplayName("validateToken - returns true for valid token")
    void validateToken_valid() {
        String token = jwtTokenProvider.generateToken("validuser");

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("validateToken - returns false for invalid/malformed token")
    void validateToken_invalid() {
        boolean isValid = jwtTokenProvider.validateToken("this.is.not.a.valid.jwt");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateToken - returns false for expired token")
    void validateToken_expired() throws InterruptedException {
        // Create a provider with very short expiration
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortLivedProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(shortLivedProvider, "jwtExpiration", 1L);

        String token = shortLivedProvider.generateToken("expireduser");

        // Wait for the token to expire
        Thread.sleep(50);

        boolean isValid = shortLivedProvider.validateToken(token);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("getExpirationMs - returns configured expiration value")
    void getExpirationMs() {
        Long expiration = jwtTokenProvider.getExpirationMs();

        assertThat(expiration).isEqualTo(86400000L);
    }

    @Test
    @DisplayName("validateToken - returns false for empty string")
    void validateToken_emptyString() {
        boolean isValid = jwtTokenProvider.validateToken("");

        assertThat(isValid).isFalse();
    }
}
