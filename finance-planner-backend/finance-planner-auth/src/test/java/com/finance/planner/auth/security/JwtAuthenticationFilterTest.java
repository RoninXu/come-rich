package com.finance.planner.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Unit Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("doFilterInternal - valid token sets authentication in SecurityContext")
    void doFilterInternal_validToken_setsAuthentication() throws ServletException, IOException {
        String token = "valid-jwt-token";
        request.addHeader("Authorization", "Bearer " + token);

        UserDetails userDetails = new User(
                "testuser",
                "password",
                true, true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("testuser");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .contains("ROLE_USER");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal - no Authorization header does not set authentication")
    void doFilterInternal_noAuthHeader_doesNotSetAuthentication() throws ServletException, IOException {
        // No Authorization header added to request

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    @DisplayName("doFilterInternal - invalid token does not set authentication")
    void doFilterInternal_invalidToken_doesNotSetAuthentication() throws ServletException, IOException {
        String token = "invalid-jwt-token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtTokenProvider.validateToken(token)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).getUsernameFromToken(anyString());
    }

    @Test
    @DisplayName("doFilterInternal - no Bearer prefix does not set authentication")
    void doFilterInternal_noBearerPrefix_doesNotSetAuthentication() throws ServletException, IOException {
        request.addHeader("Authorization", "Basic some-basic-auth");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    @DisplayName("doFilterInternal - exception during processing continues filter chain")
    void doFilterInternal_exceptionDuringProcessing_continuesFilterChain() throws ServletException, IOException {
        String token = "exception-token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(token)).thenThrow(new RuntimeException("Unexpected error"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        // Filter chain must continue even when exception occurs
        verify(filterChain).doFilter(request, response);
    }
}
