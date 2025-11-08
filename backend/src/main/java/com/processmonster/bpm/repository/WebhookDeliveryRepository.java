package com.processmonster.bpm.repository;

import com.processmonster.bpm.entity.WebhookDelivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for WebhookDelivery entity
 */
@Repository
public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> {

    /**
     * Find deliveries by webhook ID
     */
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.webhook.id = :webhookId " +
           "ORDER BY wd.createdAt DESC")
    Page<WebhookDelivery> findByWebhookId(@Param("webhookId") Long webhookId, Pageable pageable);

    /**
     * Find deliveries by status
     */
    Page<WebhookDelivery> findByStatus(String status, Pageable pageable);

    /**
     * Find deliveries by event type
     */
    Page<WebhookDelivery> findByEventType(String eventType, Pageable pageable);

    /**
     * Find pending retries (status = RETRYING and nextRetryAt <= now)
     */
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.status = 'RETRYING' " +
           "AND wd.nextRetryAt <= :now ORDER BY wd.nextRetryAt ASC")
    List<WebhookDelivery> findPendingRetries(@Param("now") LocalDateTime now);

    /**
     * Find recent deliveries for a webhook
     */
    @Query("SELECT wd FROM WebhookDelivery wd WHERE wd.webhook.id = :webhookId " +
           "AND wd.createdAt > :since ORDER BY wd.createdAt DESC")
    List<WebhookDelivery> findRecentDeliveries(
            @Param("webhookId") Long webhookId,
            @Param("since") LocalDateTime since);

    /**
     * Count deliveries by webhook and status
     */
    @Query("SELECT COUNT(wd) FROM WebhookDelivery wd WHERE wd.webhook.id = :webhookId " +
           "AND wd.status = :status")
    long countByWebhookIdAndStatus(@Param("webhookId") Long webhookId, @Param("status") String status);

    /**
     * Delete old deliveries (cleanup)
     */
    @Query("DELETE FROM WebhookDelivery wd WHERE wd.createdAt < :cutoffDate")
    void deleteOldDeliveries(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find all deliveries (for admin)
     */
    @Query("SELECT wd FROM WebhookDelivery wd ORDER BY wd.createdAt DESC")
    Page<WebhookDelivery> findAllDeliveries(Pageable pageable);
}
