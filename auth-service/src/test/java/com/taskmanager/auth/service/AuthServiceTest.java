package com.taskmanager.auth.service;

import com.taskmanager.auth.dto.RegisterRequest;
import com.taskmanager.auth.entity.Role;
import com.taskmanager.auth.repository.RefreshTokenRepository;
import com.taskmanager.auth.repository.RoleRepository;
import com.taskmanager.auth.repository.UserRepository;
import com.taskmanager.auth.security.JwtTokenProvider;
import com.taskmanager.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для AuthService
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Регистрация - ошибка: username занят")
    void register_UsernameExists() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setEmail("new@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("уже существует");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Регистрация - ошибка: email занят")
    void register_EmailExists() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("email уже существует");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Проверка что AuthService создаётся корректно")
    void authService_IsNotNull() {
        // Then
        org.junit.jupiter.api.Assertions.assertNotNull(authService);
    }
}
