package com.processmonster.bpm.service;

import com.processmonster.bpm.dto.auth.AuthResponse;
import com.processmonster.bpm.dto.auth.LoginRequest;
import com.processmonster.bpm.entity.RefreshToken;
import com.processmonster.bpm.entity.User;
import com.processmonster.bpm.exception.BusinessException;
import com.processmonster.bpm.repository.RefreshTokenRepository;
import com.processmonster.bpm.repository.UserRepository;
import com.processmonster.bpm.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Authentication Service
 *
 * Handles user authentication, token generation, and session management.
 *
 * Features:
 * - Login with username/email and password
 * - JWT access and refresh token generation
 * - Token refresh
 * - Logout with token revocation
 * - Brute-force protection
 * - Audit logging
 *
 * @author ProcessMonster Team
 * @version 1.0.0
 * @since 2025-11-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MessageSource messageSource;

    @Value("${app.security.brute-force.max-attempts}")
    private int maxFailedAttempts;

    @Value("${app.security.brute-force.lockout-duration}")
    private long lockoutDuration;

    /**
     * Authenticate user and generate tokens
     */
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String usernameOrEmail = request.getUsernameOrEmail();

        // Find user
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail)
            .orElseThrow(() -> new BadCredentialsException(
                getMessage("auth.login.failed")));

        // Check if account is locked
        if (user.isAccountLocked()) {
            throw new LockedException(getMessage("auth.account.locked"));
        }

        // Check if account is disabled
        if (!user.getEnabled() || user.getDeleted()) {
            throw new LockedException(getMessage("auth.account.disabled"));
        }

        try {
            // Authenticate
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    user.getUsername(),
                    request.getPassword()
                )
            );

            // Reset failed attempts on successful login
            if (user.getFailedLoginAttempts() > 0) {
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);
            }

            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Generate tokens
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            // Save refresh token
            saveRefreshToken(user, refreshToken, httpRequest);

            log.info("User logged in successfully: {}", user.getUsername());

            return buildAuthResponse(user, accessToken, refreshToken);

        } catch (AuthenticationException e) {
            // Handle failed login attempt
            handleFailedLogin(user);
            throw new BadCredentialsException(getMessage("auth.login.failed"));
        }
    }

    /**
     * Refresh access token using refresh token
     */
    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue) {
        // Find refresh token
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
            .orElseThrow(() -> new BusinessException(getMessage("auth.token.invalid")));

        // Validate refresh token
        if (!refreshToken.isValid()) {
            throw new BusinessException(getMessage("auth.token.expired"));
        }

        User user = refreshToken.getUser();

        // Generate new access token
        String newAccessToken = jwtService.generateAccessToken(user);

        // Optionally rotate refresh token
        String newRefreshToken = jwtService.generateRefreshToken(user);
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        // Save new refresh token
        saveRefreshToken(user, newRefreshToken, null);

        log.info("Token refreshed for user: {}", user.getUsername());

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    /**
     * Logout user and revoke tokens
     */
    @Transactional
    public void logout(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException(getMessage("user.notfound")));

        // Revoke all user tokens
        refreshTokenRepository.revokeUserTokens(user, LocalDateTime.now());

        log.info("User logged out: {}", username);
    }

    /**
     * Handle failed login attempt
     */
    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= maxFailedAttempts) {
            // Lock account
            user.setLockedUntil(LocalDateTime.now().plusSeconds(lockoutDuration / 1000));
            log.warn("Account locked due to failed login attempts: {}", user.getUsername());
        }

        userRepository.save(user);
    }

    /**
     * Save refresh token to database
     */
    private void saveRefreshToken(User user, String token, HttpServletRequest request) {
        RefreshToken refreshToken = RefreshToken.builder()
            .token(token)
            .user(user)
            .expiresAt(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpiration() / 1000))
            .revoked(false)
            .ipAddress(request != null ? getClientIp(request) : null)
            .userAgent(request != null ? request.getHeader("User-Agent") : null)
            .build();

        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Build authentication response
     */
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .roles(user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet()))
            .permissions(user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .collect(Collectors.toSet()))
            .build();

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtService.getAccessTokenExpiration() / 1000) // Convert to seconds
            .user(userInfo)
            .build();
    }

    /**
     * Get client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * Get internationalized message
     */
    private String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
