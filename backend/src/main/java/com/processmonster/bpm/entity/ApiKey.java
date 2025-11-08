package com.processmonster.bpm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing an API key for external integrations.
 * API keys are hashed using SHA-256 for security.
 */
@Entity
@Table(name = "api_keys", indexes = {
        @Index(name = "idx_api_key_hash", columnList = "keyHash"),
        @Index(name = "idx_api_key_prefix", columnList = "keyPrefix"),
        @Index(name = "idx_api_key_enabled", columnList = "enabled"),
        @Index(name = "idx_api_key_deleted", columnList = "deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Friendly name for the API key
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * SHA-256 hash of the API key (never store plain text)
     */
    @Column(nullable = false, unique = true, length = 64)
    private String keyHash;

    /**
     * First 8 characters of the key for display purposes (e.g., "pk_live_12345678...")
     */
    @Column(nullable = false, length = 20)
    private String keyPrefix;

    /**
     * Description of what this API key is used for
     */
    @Column(length = 500)
    private String description;

    /**
     * Whether the API key is enabled
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    /**
     * Expiration date (null = never expires)
     */
    private LocalDateTime expiresAt;

    /**
     * Last time this API key was used
     */
    private LocalDateTime lastUsedAt;

    /**
     * IP addresses allowed to use this key (comma-separated, null = any IP)
     */
    @Column(length = 1000)
    private String allowedIps;

    /**
     * Permissions granted to this API key
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "api_key_permissions", joinColumns = @JoinColumn(name = "api_key_id"))
    @Column(name = "permission")
    @Builder.Default
    private Set<String> permissions = new HashSet<>();

    /**
     * Rate limit: max requests per minute (0 = no limit)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer rateLimitPerMinute = 100;

    // Audit fields
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, updatable = false)
    private String createdBy;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String updatedBy;

    // Soft delete
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (createdBy == null) {
            createdBy = "system";
        }
        if (updatedBy == null) {
            updatedBy = createdBy;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if the API key is active (enabled and not expired)
     */
    public boolean isActive() {
        if (!enabled || deleted) {
            return false;
        }
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            return false;
        }
        return true;
    }

    /**
     * Check if the API key is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Update last used timestamp
     */
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }
}
