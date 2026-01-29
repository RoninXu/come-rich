package com.finance.planner.auth.service.impl;

import com.finance.planner.auth.dto.request.LoginRequest;
import com.finance.planner.auth.dto.request.RegisterRequest;
import com.finance.planner.auth.dto.response.LoginResponse;
import com.finance.planner.auth.dto.response.UserResponse;
import com.finance.planner.auth.entity.User;
import com.finance.planner.auth.repository.UserRepository;
import com.finance.planner.auth.security.JwtTokenProvider;
import com.finance.planner.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserServiceImpl userService;

    // --- Helper methods ---

    private RegisterRequest createRegisterRequest(String username, String password, String email, String nickname) {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setEmail(email);
        request.setNickname(nickname);
        return request;
    }

    private LoginRequest createLoginRequest(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    private User createUser(Long id, String username, String email, String nickname) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword("encodedPassword");
        user.setEmail(email);
        user.setNickname(nickname);
        user.setStatus((short) 1);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    // --- Register Tests ---

    @Test
    @DisplayName("register - success with all fields")
    void register_success() {
        RegisterRequest request = createRegisterRequest("testuser", "password123", "test@example.com", "TestNick");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getNickname()).isEqualTo("TestNick");
        assertThat(savedUser.getStatus()).isEqualTo((short) 1);
    }

    @Test
    @DisplayName("register - duplicate username throws BusinessException")
    void register_duplicateUsername_throwsException() {
        RegisterRequest request = createRegisterRequest("existinguser", "password123", "test@example.com", null);

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("register - duplicate email throws BusinessException")
    void register_duplicateEmail_throwsException() {
        RegisterRequest request = createRegisterRequest("newuser", "password123", "existing@example.com", null);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("register - null email skips email uniqueness check")
    void register_nullEmail_noEmailCheck() {
        RegisterRequest request = createRegisterRequest("testuser", "password123", null, null);

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.register(request);

        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register - uses nickname if provided")
    void register_usesNicknameIfProvided() {
        RegisterRequest request = createRegisterRequest("testuser", "password123", null, "MyNickname");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getNickname()).isEqualTo("MyNickname");
    }

    @Test
    @DisplayName("register - uses username as nickname when nickname is null")
    void register_usesUsernameAsNicknameIfNullNickname() {
        RegisterRequest request = createRegisterRequest("testuser", "password123", null, null);

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getNickname()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("register - encodes password before saving")
    void register_encodesPassword() {
        RegisterRequest request = createRegisterRequest("testuser", "rawPassword", null, null);

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("rawPassword")).thenReturn("$2a$10$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.register(request);

        verify(passwordEncoder).encode("rawPassword");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getPassword()).isEqualTo("$2a$10$encoded");
    }

    // --- Login Tests ---

    @Test
    @DisplayName("login - success returns LoginResponse with token and user info")
    void login_success() {
        LoginRequest request = createLoginRequest("testuser", "password123");
        User user = createUser(1L, "testuser", "test@example.com", "TestNick");
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token-123");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(86400000L);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoginResponse response = userService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token-123");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(86400000L);
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("login - updates lastLoginAt timestamp")
    void login_updatesLastLoginAt() {
        LoginRequest request = createLoginRequest("testuser", "password123");
        User user = createUser(1L, "testuser", "test@example.com", "TestNick");
        assertThat(user.getLastLoginAt()).isNull();

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(86400000L);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.login(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getLastLoginAt()).isNotNull();
    }

    @Test
    @DisplayName("login - user not found after authentication throws BusinessException")
    void login_userNotFound_throwsException() {
        LoginRequest request = createLoginRequest("nonexistent", "password123");
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("User not found");
    }

    // --- GetCurrentUser Tests ---

    @Test
    @DisplayName("getCurrentUser - success returns UserResponse")
    void getCurrentUser_success() {
        User user = createUser(1L, "testuser", "test@example.com", "TestNick");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserResponse response = userService.getCurrentUser("testuser");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getNickname()).isEqualTo("TestNick");
    }

    @Test
    @DisplayName("getCurrentUser - user not found throws BusinessException")
    void getCurrentUser_notFound_throwsException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUser("nonexistent"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("User not found");
    }
}
