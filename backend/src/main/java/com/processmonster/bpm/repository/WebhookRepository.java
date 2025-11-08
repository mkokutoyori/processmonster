package com.processmonster.bpm.repository;

import com.processmonster.bpm.entity.Webhook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Webhook entity
 */
@Repository
public interface WebhookRepository extends JpaRepository<Webhook, Long> {

    /**
     * Find webhook by ID (not deleted)
     */
    Optional<Webhook> findByIdAndDeletedFalse(Long id);

    /**
     * Find all webhooks (not deleted)
     */
    Page<Webhook> findByDeletedFalse(Pageable pageable);

    /**
     * Find all webhooks (not deleted) - List version
     */
    List<Webhook> findByDeletedFalse();

    /**
     * Find enabled webhooks
     */
    @Query("SELECT w FROM Webhook w WHERE w.enabled = true AND w.deleted = false")
    List<Webhook> findEnabledWebhooks();

    /**
     * Find webhooks by event type
     */
    @Query("SELECT w FROM Webhook w JOIN w.events e WHERE e = :eventType " +
           "AND w.enabled = true AND w.deleted = false")
    List<Webhook> findByEventType(@Param("eventType") String eventType);

    /**
     * Find webhooks by creator
     */
    @Query("SELECT w FROM Webhook w WHERE w.createdBy = :username AND w.deleted = false")
    Page<Webhook> findByCreatedBy(@Param("username") String username, Pageable pageable);

    /**
     * Search webhooks by name, description, or URL
     */
    @Query("SELECT w FROM Webhook w WHERE w.deleted = false AND " +
           "(LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.url) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Webhook> searchWebhooks(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Count enabled webhooks
     */
    long countByEnabledTrueAndDeletedFalse();

    /**
     * Find webhooks with failures
     */
    @Query("SELECT w FROM Webhook w WHERE w.failureCount > 0 AND w.deleted = false " +
           "ORDER BY w.lastFailureAt DESC")
    Page<Webhook> findWebhooksWithFailures(Pageable pageable);
}
