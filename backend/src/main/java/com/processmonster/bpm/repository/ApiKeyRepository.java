package com.processmonster.bpm.repository;

import com.processmonster.bpm.entity.ApiKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ApiKey entity
 */
@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    /**
     * Find API key by ID (not deleted)
     */
    Optional<ApiKey> findByIdAndDeletedFalse(Long id);

    /**
     * Find API key by key hash
     */
    Optional<ApiKey> findByKeyHashAndDeletedFalse(String keyHash);

    /**
     * Find API key by key prefix (for display)
     */
    Optional<ApiKey> findByKeyPrefixAndDeletedFalse(String keyPrefix);

    /**
     * Find all API keys (not deleted)
     */
    Page<ApiKey> findByDeletedFalse(Pageable pageable);

    /**
     * Find all API keys (not deleted) - List version
     */
    List<ApiKey> findByDeletedFalse();

    /**
     * Find enabled API keys
     */
    @Query("SELECT ak FROM ApiKey ak WHERE ak.enabled = true AND ak.deleted = false")
    List<ApiKey> findEnabledKeys();

    /**
     * Find active API keys (enabled and not expired)
     */
    @Query("SELECT ak FROM ApiKey ak WHERE ak.enabled = true AND ak.deleted = false " +
           "AND (ak.expiresAt IS NULL OR ak.expiresAt > :now)")
    List<ApiKey> findActiveKeys(@Param("now") LocalDateTime now);

    /**
     * Find expired API keys
     */
    @Query("SELECT ak FROM ApiKey ak WHERE ak.expiresAt IS NOT NULL AND ak.expiresAt <= :now " +
           "AND ak.deleted = false")
    List<ApiKey> findExpiredKeys(@Param("now") LocalDateTime now);

    /**
     * Find API keys by creator
     */
    @Query("SELECT ak FROM ApiKey ak WHERE ak.createdBy = :username AND ak.deleted = false")
    Page<ApiKey> findByCreatedBy(@Param("username") String username, Pageable pageable);

    /**
     * Search API keys by name or description
     */
    @Query("SELECT ak FROM ApiKey ak WHERE ak.deleted = false AND " +
           "(LOWER(ak.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(ak.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ApiKey> searchApiKeys(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Count active API keys
     */
    @Query("SELECT COUNT(ak) FROM ApiKey ak WHERE ak.enabled = true AND ak.deleted = false " +
           "AND (ak.expiresAt IS NULL OR ak.expiresAt > :now)")
    long countActiveKeys(@Param("now") LocalDateTime now);

    /**
     * Check if key hash already exists
     */
    boolean existsByKeyHashAndDeletedFalse(String keyHash);
}
