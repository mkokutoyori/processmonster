package com.processmonster.bpm.service;

import com.processmonster.bpm.entity.Webhook;
import com.processmonster.bpm.entity.WebhookDelivery;
import com.processmonster.bpm.exception.BusinessException;
import com.processmonster.bpm.exception.ResourceNotFoundException;
import com.processmonster.bpm.repository.WebhookDeliveryRepository;
import com.processmonster.bpm.repository.WebhookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for managing webhooks and delivering webhook events
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WebhookService {

    private final WebhookRepository webhookRepository;
    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Create a new webhook
     */
    public Webhook createWebhook(String name, String url, String description, Set<String> events,
                                  String secret, Integer timeoutMs, Integer maxRetries) {
        log.debug("Creating new webhook: {}", name);

        String currentUser = getCurrentUsername();

        Webhook webhook = Webhook.builder()
                .name(name)
                .url(url)
                .description(description)
                .events(events != null ? events : Set.of())
                .secret(secret)
                .timeoutMs(timeoutMs != null ? timeoutMs : 30000)
                .maxRetries(maxRetries != null ? maxRetries : 3)
                .enabled(true)
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .build();

        Webhook saved = webhookRepository.save(webhook);
        log.info("Webhook created: {} (ID: {})", saved.getName(), saved.getId());

        return saved;
    }

    /**
     * Get webhook by ID
     */
    @Transactional(readOnly = true)
    public Webhook getWebhookById(Long id) {
        return webhookRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook not found with ID: " + id));
    }

    /**
     * Get all webhooks (paginated)
     */
    @Transactional(readOnly = true)
    public Page<Webhook> getAllWebhooks(Pageable pageable) {
        return webhookRepository.findByDeletedFalse(pageable);
    }

    /**
     * Get all webhooks (list)
     */
    @Transactional(readOnly = true)
    public List<Webhook> getAllWebhooks() {
        return webhookRepository.findByDeletedFalse();
    }

    /**
     * Get enabled webhooks
     */
    @Transactional(readOnly = true)
    public List<Webhook> getEnabledWebhooks() {
        return webhookRepository.findEnabledWebhooks();
    }

    /**
     * Get webhooks for a specific event type
     */
    @Transactional(readOnly = true)
    public List<Webhook> getWebhooksForEvent(String eventType) {
        return webhookRepository.findByEventType(eventType);
    }

    /**
     * Search webhooks
     */
    @Transactional(readOnly = true)
    public Page<Webhook> searchWebhooks(String keyword, Pageable pageable) {
        return webhookRepository.searchWebhooks(keyword, pageable);
    }

    /**
     * Update webhook
     */
    public Webhook updateWebhook(Long id, String name, String url, String description,
                                  Set<String> events, String secret, Integer timeoutMs, Integer maxRetries) {
        log.debug("Updating webhook: {}", id);

        Webhook webhook = getWebhookById(id);
        String currentUser = getCurrentUsername();

        if (name != null) {
            webhook.setName(name);
        }
        if (url != null) {
            webhook.setUrl(url);
        }
        if (description != null) {
            webhook.setDescription(description);
        }
        if (events != null) {
            webhook.setEvents(events);
        }
        if (secret != null) {
            webhook.setSecret(secret);
        }
        if (timeoutMs != null) {
            webhook.setTimeoutMs(timeoutMs);
        }
        if (maxRetries != null) {
            webhook.setMaxRetries(maxRetries);
        }
        webhook.setUpdatedBy(currentUser);

        Webhook updated = webhookRepository.save(webhook);
        log.info("Webhook updated: {} (ID: {})", updated.getName(), updated.getId());

        return updated;
    }

    /**
     * Enable webhook
     */
    public Webhook enableWebhook(Long id) {
        log.debug("Enabling webhook: {}", id);

        Webhook webhook = getWebhookById(id);
        webhook.setEnabled(true);
        webhook.setUpdatedBy(getCurrentUsername());

        Webhook updated = webhookRepository.save(webhook);
        log.info("Webhook enabled: {} (ID: {})", updated.getName(), updated.getId());

        return updated;
    }

    /**
     * Disable webhook
     */
    public Webhook disableWebhook(Long id) {
        log.debug("Disabling webhook: {}", id);

        Webhook webhook = getWebhookById(id);
        webhook.setEnabled(false);
        webhook.setUpdatedBy(getCurrentUsername());

        Webhook updated = webhookRepository.save(webhook);
        log.info("Webhook disabled: {} (ID: {})", updated.getName(), updated.getId());

        return updated;
    }

    /**
     * Delete webhook (soft delete)
     */
    public void deleteWebhook(Long id) {
        log.debug("Deleting webhook: {}", id);

        Webhook webhook = getWebhookById(id);
        webhook.setDeleted(true);
        webhook.setDeletedAt(LocalDateTime.now());
        webhook.setUpdatedBy(getCurrentUsername());

        webhookRepository.save(webhook);
        log.info("Webhook deleted: {} (ID: {})", webhook.getName(), webhook.getId());
    }

    /**
     * Trigger webhook event (async)
     */
    @Async
    public void triggerEvent(String eventType, Map<String, Object> payload) {
        log.debug("Triggering webhook event: {}", eventType);

        List<Webhook> webhooks = getWebhooksForEvent(eventType);
        if (webhooks.isEmpty()) {
            log.debug("No webhooks configured for event: {}", eventType);
            return;
        }

        for (Webhook webhook : webhooks) {
            try {
                deliverWebhook(webhook, eventType, payload);
            } catch (Exception e) {
                log.error("Error triggering webhook {} for event {}: {}",
                        webhook.getId(), eventType, e.getMessage());
            }
        }
    }

    /**
     * Deliver webhook (with retry logic)
     */
    @Async
    public void deliverWebhook(Webhook webhook, String eventType, Map<String, Object> payload) {
        log.debug("Delivering webhook: {} for event: {}", webhook.getId(), eventType);

        // Create delivery record
        WebhookDelivery delivery = WebhookDelivery.builder()
                .webhook(webhook)
                .eventType(eventType)
                .status("PENDING")
                .build();

        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            delivery.setRequestPayload(payloadJson);

            // Build request
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(webhook.getUrl()))
                    .timeout(Duration.ofMillis(webhook.getTimeoutMs()))
                    .header("Content-Type", webhook.getContentType())
                    .header("User-Agent", "ProcessMonster-Webhook/1.0");

            // Add HMAC signature if secret is configured
            if (webhook.getSecret() != null && !webhook.getSecret().isEmpty()) {
                String signature = generateHmacSignature(payloadJson, webhook.getSecret());
                requestBuilder.header("X-Webhook-Signature", signature);
            }

            // Add event type header
            requestBuilder.header("X-Event-Type", eventType);

            // Build request with method and body
            HttpRequest request = requestBuilder
                    .method(webhook.getHttpMethod(), HttpRequest.BodyPublishers.ofString(payloadJson))
                    .build();

            // Execute request
            long startTime = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long duration = System.currentTimeMillis() - startTime;

            // Handle response
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                delivery.markSuccess(response.statusCode(), response.body(), duration);
                webhook.recordSuccess();
                webhookRepository.save(webhook);
                log.info("Webhook delivered successfully: {} (status: {})", webhook.getId(), response.statusCode());
            } else {
                String errorMsg = "HTTP " + response.statusCode() + ": " + response.body();
                handleDeliveryFailure(delivery, webhook, errorMsg);
            }

        } catch (Exception e) {
            log.error("Webhook delivery failed: {} - {}", webhook.getId(), e.getMessage());
            handleDeliveryFailure(delivery, webhook, e.getMessage());
        }

        webhookDeliveryRepository.save(delivery);
    }

    /**
     * Handle delivery failure with retry logic
     */
    private void handleDeliveryFailure(WebhookDelivery delivery, Webhook webhook, String errorMessage) {
        delivery.setErrorMessage(errorMessage);

        if (delivery.getRetryCount() < webhook.getMaxRetries()) {
            // Schedule retry with exponential backoff
            int retryDelayMs = webhook.getRetryDelayMs() * (int) Math.pow(2, delivery.getRetryCount());
            LocalDateTime nextRetry = LocalDateTime.now().plusSeconds(retryDelayMs / 1000);
            delivery.markForRetry(nextRetry);
            log.info("Webhook delivery will be retried: {} (attempt {}/{})",
                    webhook.getId(), delivery.getRetryCount() + 1, webhook.getMaxRetries());
        } else {
            delivery.markFailed(errorMessage);
            webhook.recordFailure(errorMessage);
            webhookRepository.save(webhook);
            log.warn("Webhook delivery failed after {} retries: {}", webhook.getMaxRetries(), webhook.getId());
        }
    }

    /**
     * Process pending retries
     */
    @Transactional
    public void processPendingRetries() {
        List<WebhookDelivery> pendingRetries = webhookDeliveryRepository.findPendingRetries(LocalDateTime.now());

        for (WebhookDelivery delivery : pendingRetries) {
            try {
                Map<String, Object> payload = objectMapper.readValue(
                        delivery.getRequestPayload(),
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)
                );
                deliverWebhook(delivery.getWebhook(), delivery.getEventType(), payload);
            } catch (Exception e) {
                log.error("Error processing retry for delivery {}: {}", delivery.getId(), e.getMessage());
            }
        }
    }

    /**
     * Get delivery history for a webhook
     */
    @Transactional(readOnly = true)
    public Page<WebhookDelivery> getDeliveryHistory(Long webhookId, Pageable pageable) {
        return webhookDeliveryRepository.findByWebhookId(webhookId, pageable);
    }

    /**
     * Get recent deliveries
     */
    @Transactional(readOnly = true)
    public List<WebhookDelivery> getRecentDeliveries(Long webhookId, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return webhookDeliveryRepository.findRecentDeliveries(webhookId, since);
    }

    /**
     * Count webhooks
     */
    @Transactional(readOnly = true)
    public long countEnabledWebhooks() {
        return webhookRepository.countByEnabledTrueAndDeletedFalse();
    }

    /**
     * Test webhook (send test event)
     */
    @Async
    public void testWebhook(Long webhookId) {
        Webhook webhook = getWebhookById(webhookId);

        Map<String, Object> testPayload = new HashMap<>();
        testPayload.put("event", "webhook.test");
        testPayload.put("timestamp", LocalDateTime.now().toString());
        testPayload.put("message", "This is a test webhook delivery");

        deliverWebhook(webhook, "webhook.test", testPayload);
    }

    // Helper methods

    /**
     * Generate HMAC-SHA256 signature
     */
    private String generateHmacSignature(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new BusinessException("Failed to generate HMAC signature: " + e.getMessage());
        }
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }
}
