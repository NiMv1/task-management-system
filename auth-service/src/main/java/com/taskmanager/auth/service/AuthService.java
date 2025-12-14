package com.taskmanager.auth.service;

import com.taskmanager.auth.dto.*;
import com.taskmanager.auth.entity.RefreshToken;
import com.taskmanager.auth.entity.Role;
import com.taskmanager.auth.entity.User;
import com.taskmanager.auth.repository.RefreshTokenRepository;
import com.taskmanager.auth.repository.RoleRepository;
import com.taskmanager.auth.repository.UserRepository;
import com.taskmanager.auth.security.JwtTokenProvider;
import com.taskmanager.auth.security.UserDetailsImpl;
import com.taskmanager.common.exception.BusinessException;
import com.taskmanager.common.exception.ResourceNotFoundException;
import com.taskmanager.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Сервис аутентификации и авторизации
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Регистрация нового пользователя
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Проверка уникальности username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Пользователь с таким именем уже существует");
        }

        // Проверка уникальности email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Пользователь с таким email уже существует");
        }

        // Получение роли по умолчанию
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Роль", "name", "ROLE_USER"));

        // Создание пользователя
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(Set.of(userRole))
                .build();

        user = userRepository.save(user);
        log.info("Зарегистрирован новый пользователь: {}", user.getUsername());

        // Аутентификация и генерация токенов
        return authenticateAndGenerateTokens(request.getUsername(), request.getPassword());
    }

    /**
     * Аутентификация пользователя
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        return authenticateAndGenerateTokens(request.getUsername(), request.getPassword());
    }

    /**
     * Обновление access токена по refresh токену
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Refresh токен не найден"));

        if (refreshToken.isRevoked()) {
            throw new UnauthorizedException("Refresh токен отозван");
        }

        if (refreshToken.isExpired()) {
            throw new UnauthorizedException("Refresh токен истёк");
        }

        User user = refreshToken.getUser();
        String roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.joining(","));

        String accessToken = jwtTokenProvider.generateAccessTokenFromUsername(
                user.getUsername(), user.getId(), user.getEmail(), roles);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .build();
    }

    /**
     * Выход из системы (отзыв refresh токена)
     */
    @Transactional
    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Refresh токен не найден"));
        token.setRevoked(true);
        refreshTokenRepository.save(token);
        log.info("Пользователь {} вышел из системы", token.getUser().getUsername());
    }

    /**
     * Выход из всех сессий
     */
    @Transactional
    public void logoutAll() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", "id", userDetails.getId()));
        
        refreshTokenRepository.revokeAllByUser(user);
        log.info("Пользователь {} вышел из всех сессий", user.getUsername());
    }

    private AuthResponse authenticateAndGenerateTokens(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Генерация access токена
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);

        // Генерация refresh токена
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", "id", userDetails.getId()));

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(
                        jwtTokenProvider.getRefreshTokenExpiration() / 1000))
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
                .userId(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .roles(userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .build();
    }
}
