package com.processmonster.bpm.controller;

import com.processmonster.bpm.dto.webhook.CreateWebhookDTO;
import com.processmonster.bpm.dto.webhook.UpdateWebhookDTO;
import com.processmonster.bpm.dto.webhook.WebhookDTO;
import com.processmonster.bpm.dto.webhook.WebhookDeliveryDTO;
import com.processmonster.bpm.entity.Webhook;
import com.processmonster.bpm.entity.WebhookDelivery;
import com.processmonster.bpm.mapper.WebhookMapper;
import com.processmonster.bpm.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for webhook management
 */
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhooks", description = "Webhook management for event notifications")
@SecurityRequirement(name = "bearerAuth")
public class WebhookController {

    private final WebhookService webhookService;
    private final WebhookMapper webhookMapper;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('WEBHOOK_CREATE', 'ROLE_ADMIN')")
    @Operation(summary = "Create webhook", description = "Create a new webhook configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Webhook created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<WebhookDTO> createWebhook(@Valid @RequestBody CreateWebhookDTO createDTO) {
        log.debug("REST request to create webhook: {}", createDTO.getName());

        Webhook webhook = webhookService.createWebhook(
                createDTO.getName(),
                createDTO.getUrl(),
                createDTO.getDescription(),
                createDTO.getEvents(),
                createDTO.getSecret(),
                createDTO.getTimeoutMs(),
                createDTO.getMaxRetries()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(webhookMapper.toDTO(webhook));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('WEBHOOK_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get webhook by ID", description = "Retrieve webhook information by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Webhook found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Webhook not found")
    })
    public ResponseEntity<WebhookDTO> getWebhookById(@PathVariable Long id) {
        log.debug("REST request to get webhook: {}", id);

        Webhook webhook = webhookService.getWebhookById(id);
        return ResponseEntity.ok(webhookMapper.toDTO(webhook));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('WEBHOOK_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get all webhooks", description = "Retrieve all webhooks (paginated)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Webhooks retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<WebhookDTO>> getAllWebhooks(Pageable pageable) {
        log.debug("REST request to get all webhooks");

        Page<Webhook> webhooks = webhookService.getAllWebhooks(pageable);
        Page<WebhookDTO> dtos = webhooks.map(webhookMapper::toDTO);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/enabled")
    @PreAuthorize("hasAnyAuthority('WEBHOOK_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get enabled webhooks", description = "Retrieve all enabled webhooks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enabled webhooks retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<WebhookDTO>> getEnabledWebhooks() {
        log.debug("REST request to get enabled webhooks");

        List<Webhook> webhooks = webhookService.getEnabledWebhooks();
        List<WebhookDTO> dtos = webhooks.stream()
                .map(webhookMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('WEBHOOK_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Search webhooks", description = "Search webhooks by name, description, or URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<WebhookDTO>> searchWebhooks(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            Pageable pageable) {
        log.debug("REST request to search webhooks: {}", keyword);

        Page<Webhook> webhooks = webhookService.searchWebhooks(keyword, pageable);
        Page<WebhookDTO> dtos = webhooks.map(webhookMapper::toDTO);
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('WEBHOOK_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Update webhook", description = "Update webhook configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Webhook updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Webhook not found")
    })
    public ResponseEntity<WebhookDTO> updateWebhook(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWebhookDTO updateDTO) {
        log.debug("REST request to update webhook: {}", id);

        Webhook updated = webhookService.updateWebhook(
                id,
                updateDTO.getName(),
                updateDTO.getUrl(),
                updateDTO.getDescription(),
                updateDTO.getEvents(),
                updateDTO.getSecret(),
                updateDTO.getTimeoutMs(),
                updateDTO.getMaxRetries()
        );

        return ResponseEntity.ok(webhookMapper.toDTO(updated));
    }

    @PutMapping("/{id}/enable")
    @PreAuthorize("hasAnyAuthority('WEBHOOK_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Enable webhook", description = "Enable a webhook")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Webhook enabled successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Webhook not found")
    })
    public ResponseEntity<WebhookDTO> enableWebhook(@PathVariable Long id) {
        log.debug("REST request to enable webhook: {}", id);

        Webhook webhook = webhookService.enableWebhook(id);
        return ResponseEntity.ok(webhookMapper.toDTO(webhook));
    }

    @PutMapping("/{id}/disable")
    @PreAuthorize("hasAnyAuthority('WEBHOOK_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Disable webhook", description = "Disable a webhook")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Webhook disabled successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Webhook not found")
    })
    public ResponseEntity<WebhookDTO> disableWebhook(@PathVariable Long id) {
        log.debug("REST request to disable webhook: {}", id);

        Webhook webhook = webhookService.disableWebhook(id);
        return ResponseEntity.ok(webhookMapper.toDTO(webhook));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('WEBHOOK_DELETE', 'ROLE_ADMIN')")
    @Operation(summary = "Delete webhook", description = "Delete a webhook (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Webhook deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Webhook not found")
    })
    public ResponseEntity<Void> deleteWebhook(@PathVariable Long id) {
        log.debug("REST request to delete webhook: {}", id);

        webhookService.deleteWebhook(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/test")
    @PreAuthorize("hasAnyAuthority('WEBHOOK_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Test webhook", description = "Send a test event to the webhook")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Test event queued for delivery"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Webhook not found")
    })
    public ResponseEntity<Map<String, String>> testWebhook(@PathVariable Long id) {
        log.debug("REST request to test webhook: {}", id);

        webhookService.testWebhook(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Test event queued for delivery");
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/{id}/deliveries")
    @PreAuthorize("hasAnyAuthority('WEBHOOK_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get webhook delivery history", description = "Retrieve delivery history for a webhook")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delivery history retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Webhook not found")
    })
    public ResponseEntity<Page<WebhookDeliveryDTO>> getDeliveryHistory(
            @PathVariable Long id,
            Pageable pageable) {
        log.debug("REST request to get delivery history for webhook: {}", id);

        Page<WebhookDelivery> deliveries = webhookService.getDeliveryHistory(id, pageable);
        Page<WebhookDeliveryDTO> dtos = deliveries.map(webhookMapper::toDeliveryDTO);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}/deliveries/recent")
    @PreAuthorize("hasAnyAuthority('WEBHOOK_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get recent deliveries", description = "Get recent webhook deliveries (last N hours)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recent deliveries retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Webhook not found")
    })
    public ResponseEntity<List<WebhookDeliveryDTO>> getRecentDeliveries(
            @PathVariable Long id,
            @Parameter(description = "Hours to look back") @RequestParam(defaultValue = "24") int hours) {
        log.debug("REST request to get recent deliveries for webhook: {} (last {} hours)", id, hours);

        List<WebhookDelivery> deliveries = webhookService.getRecentDeliveries(id, hours);
        List<WebhookDeliveryDTO> dtos = deliveries.stream()
                .map(webhookMapper::toDeliveryDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyAuthority('WEBHOOK_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get webhook statistics", description = "Get statistics about webhooks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Map<String, Object>> getWebhookStats() {
        log.debug("REST request to get webhook statistics");

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalWebhooks", webhookService.getAllWebhooks().size());
        stats.put("enabledWebhooks", webhookService.countEnabledWebhooks());

        return ResponseEntity.ok(stats);
    }
}
