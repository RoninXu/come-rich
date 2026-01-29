package com.finance.planner.auth.security;

import com.finance.planner.auth.entity.User;
import com.finance.planner.auth.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl Unit Tests")
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User createUser(String username, String password, Short status) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail("test@example.com");
        user.setStatus(status);
        return user;
    }

    @Test
    @DisplayName("loadUserByUsername - success returns UserDetails with correct authorities")
    void loadUserByUsername_success() {
        User user = createUser("testuser", "encodedPassword", (short) 1);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.getAuthorities())
                .hasSize(1)
                .extracting("authority")
                .contains("ROLE_USER");
    }

    @Test
    @DisplayName("loadUserByUsername - user not found throws UsernameNotFoundException")
    void loadUserByUsername_notFound_throwsUsernameNotFoundException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nonexistent"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found: nonexistent");
    }

    @Test
    @DisplayName("loadUserByUsername - disabled user returns UserDetails with enabled=false")
    void loadUserByUsername_disabledUser_returnsDisabledUserDetails() {
        User user = createUser("disableduser", "encodedPassword", (short) 0);
        when(userRepository.findByUsername("disableduser")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("disableduser");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("disableduser");
        assertThat(userDetails.isEnabled()).isFalse();
        // Other flags should still be true
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
    }
}
