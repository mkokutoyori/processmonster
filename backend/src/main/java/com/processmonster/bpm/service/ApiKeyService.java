package com.processmonster.bpm.service;

import com.processmonster.bpm.entity.ApiKey;
import com.processmonster.bpm.exception.BusinessException;
import com.processmonster.bpm.exception.ResourceNotFoundException;
import com.processmonster.bpm.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Set;

/**
 * Service for managing API keys
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private static final String API_KEY_PREFIX = "pk_live_";
    private static final int KEY_LENGTH = 32; // bytes, will be 43 chars when base64 encoded
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a new API key
     */
    public ApiKeyWithPlainText createApiKey(String name, String description, Set<String> permissions,
                                             Integer rateLimitPerMinute, LocalDateTime expiresAt) {
        log.debug("Creating new API key: {}", name);

        String currentUser = getCurrentUsername();

        // Generate secure random key
        String plainKey = generateSecureKey();
        String keyHash = hashKey(plainKey);
        String keyPrefix = API_KEY_PREFIX + plainKey.substring(0, Math.min(8, plainKey.length()));

        // Check if hash already exists (extremely unlikely but good practice)
        if (apiKeyRepository.existsByKeyHashAndDeletedFalse(keyHash)) {
            throw new BusinessException("API key collision detected. Please try again.");
        }

        ApiKey apiKey = ApiKey.builder()
                .name(name)
                .description(description)
                .keyHash(keyHash)
                .keyPrefix(keyPrefix)
                .permissions(permissions != null ? permissions : Set.of())
                .rateLimitPerMinute(rateLimitPerMinute != null ? rateLimitPerMinute : 100)
                .expiresAt(expiresAt)
                .enabled(true)
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .build();

        ApiKey saved = apiKeyRepository.save(apiKey);
        log.info("API key created: {} (ID: {})", saved.getName(), saved.getId());

        // Return both the entity and the plain key (only time we show the plain key!)
        return new ApiKeyWithPlainText(saved, API_KEY_PREFIX + plainKey);
    }

    /**
     * Get API key by ID
     */
    @Transactional(readOnly = true)
    public ApiKey getApiKeyById(Long id) {
        return apiKeyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("API key not found with ID: " + id));
    }

    /**
     * Get all API keys (paginated)
     */
    @Transactional(readOnly = true)
    public Page<ApiKey> getAllApiKeys(Pageable pageable) {
        return apiKeyRepository.findByDeletedFalse(pageable);
    }

    /**
     * Get all API keys (list)
     */
    @Transactional(readOnly = true)
    public List<ApiKey> getAllApiKeys() {
        return apiKeyRepository.findByDeletedFalse();
    }

    /**
     * Get enabled API keys
     */
    @Transactional(readOnly = true)
    public List<ApiKey> getEnabledKeys() {
        return apiKeyRepository.findEnabledKeys();
    }

    /**
     * Get active API keys (enabled and not expired)
     */
    @Transactional(readOnly = true)
    public List<ApiKey> getActiveKeys() {
        return apiKeyRepository.findActiveKeys(LocalDateTime.now());
    }

    /**
     * Search API keys
     */
    @Transactional(readOnly = true)
    public Page<ApiKey> searchApiKeys(String keyword, Pageable pageable) {
        return apiKeyRepository.searchApiKeys(keyword, pageable);
    }

    /**
     * Update API key
     */
    public ApiKey updateApiKey(Long id, String name, String description, Set<String> permissions,
                                Integer rateLimitPerMinute, String allowedIps) {
        log.debug("Updating API key: {}", id);

        ApiKey apiKey = getApiKeyById(id);
        String currentUser = getCurrentUsername();

        if (name != null) {
            apiKey.setName(name);
        }
        if (description != null) {
            apiKey.setDescription(description);
        }
        if (permissions != null) {
            apiKey.setPermissions(permissions);
        }
        if (rateLimitPerMinute != null) {
            apiKey.setRateLimitPerMinute(rateLimitPerMinute);
        }
        if (allowedIps != null) {
            apiKey.setAllowedIps(allowedIps);
        }
        apiKey.setUpdatedBy(currentUser);

        ApiKey updated = apiKeyRepository.save(apiKey);
        log.info("API key updated: {} (ID: {})", updated.getName(), updated.getId());

        return updated;
    }

    /**
     * Enable API key
     */
    public ApiKey enableApiKey(Long id) {
        log.debug("Enabling API key: {}", id);

        ApiKey apiKey = getApiKeyById(id);
        apiKey.setEnabled(true);
        apiKey.setUpdatedBy(getCurrentUsername());

        ApiKey updated = apiKeyRepository.save(apiKey);
        log.info("API key enabled: {} (ID: {})", updated.getName(), updated.getId());

        return updated;
    }

    /**
     * Disable API key
     */
    public ApiKey disableApiKey(Long id) {
        log.debug("Disabling API key: {}", id);

        ApiKey apiKey = getApiKeyById(id);
        apiKey.setEnabled(false);
        apiKey.setUpdatedBy(getCurrentUsername());

        ApiKey updated = apiKeyRepository.save(apiKey);
        log.info("API key disabled: {} (ID: {})", updated.getName(), updated.getId());

        return updated;
    }

    /**
     * Delete API key (soft delete)
     */
    public void deleteApiKey(Long id) {
        log.debug("Deleting API key: {}", id);

        ApiKey apiKey = getApiKeyById(id);
        apiKey.setDeleted(true);
        apiKey.setDeletedAt(LocalDateTime.now());
        apiKey.setUpdatedBy(getCurrentUsername());

        apiKeyRepository.save(apiKey);
        log.info("API key deleted: {} (ID: {})", apiKey.getName(), apiKey.getId());
    }

    /**
     * Authenticate with API key
     */
    @Transactional(readOnly = true)
    public ApiKey authenticateApiKey(String plainKey) {
        if (plainKey == null || plainKey.isEmpty()) {
            throw new BusinessException("API key is required");
        }

        // Remove prefix if present
        String keyWithoutPrefix = plainKey.startsWith(API_KEY_PREFIX)
                ? plainKey.substring(API_KEY_PREFIX.length())
                : plainKey;

        String keyHash = hashKey(keyWithoutPrefix);

        ApiKey apiKey = apiKeyRepository.findByKeyHashAndDeletedFalse(keyHash)
                .orElseThrow(() -> new BusinessException("Invalid API key"));

        if (!apiKey.isActive()) {
            if (apiKey.isExpired()) {
                throw new BusinessException("API key has expired");
            }
            throw new BusinessException("API key is disabled");
        }

        return apiKey;
    }

    /**
     * Update last used timestamp for API key
     */
    public void updateLastUsed(Long id) {
        ApiKey apiKey = apiKeyRepository.findById(id).orElse(null);
        if (apiKey != null) {
            apiKey.updateLastUsed();
            apiKeyRepository.save(apiKey);
        }
    }

    /**
     * Count active API keys
     */
    @Transactional(readOnly = true)
    public long countActiveKeys() {
        return apiKeyRepository.countActiveKeys(LocalDateTime.now());
    }

    /**
     * Find expired keys that need cleanup
     */
    @Transactional(readOnly = true)
    public List<ApiKey> findExpiredKeys() {
        return apiKeyRepository.findExpiredKeys(LocalDateTime.now());
    }

    // Helper methods

    /**
     * Generate a secure random API key
     */
    private String generateSecureKey() {
        byte[] randomBytes = new byte[KEY_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Hash API key using SHA-256
     */
    public static String hashKey(String plainKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainKey.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }

    /**
     * DTO to return API key with plain text (only shown once during creation)
     */
    public static class ApiKeyWithPlainText {
        public final ApiKey apiKey;
        public final String plainKey;

        public ApiKeyWithPlainText(ApiKey apiKey, String plainKey) {
            this.apiKey = apiKey;
            this.plainKey = plainKey;
        }
    }
}
