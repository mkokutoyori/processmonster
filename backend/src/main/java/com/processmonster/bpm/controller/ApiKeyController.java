package com.processmonster.bpm.controller;

import com.processmonster.bpm.dto.apikey.ApiKeyCreatedDTO;
import com.processmonster.bpm.dto.apikey.ApiKeyDTO;
import com.processmonster.bpm.dto.apikey.CreateApiKeyDTO;
import com.processmonster.bpm.entity.ApiKey;
import com.processmonster.bpm.mapper.ApiKeyMapper;
import com.processmonster.bpm.service.ApiKeyService;
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
 * REST Controller for API key management
 */
@RestController
@RequestMapping("/api/v1/api-keys")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "API Keys", description = "API key management for external integrations")
@SecurityRequirement(name = "bearerAuth")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;
    private final ApiKeyMapper apiKeyMapper;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('API_KEY_CREATE', 'ROLE_ADMIN')")
    @Operation(summary = "Create API key", description = "Generate a new API key for external integrations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "API key created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiKeyCreatedDTO> createApiKey(@Valid @RequestBody CreateApiKeyDTO createDTO) {
        log.debug("REST request to create API key: {}", createDTO.getName());

        ApiKeyService.ApiKeyWithPlainText result = apiKeyService.createApiKey(
                createDTO.getName(),
                createDTO.getDescription(),
                createDTO.getPermissions(),
                createDTO.getRateLimitPerMinute(),
                createDTO.getExpiresAt()
        );

        ApiKeyCreatedDTO response = ApiKeyCreatedDTO.builder()
                .apiKey(apiKeyMapper.toDTO(result.apiKey))
                .plainKey(result.plainKey)
                .warning("Save this key securely! It will never be shown again.")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('API_KEY_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get API key by ID", description = "Retrieve API key information by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API key found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "API key not found")
    })
    public ResponseEntity<ApiKeyDTO> getApiKeyById(@PathVariable Long id) {
        log.debug("REST request to get API key: {}", id);

        ApiKey apiKey = apiKeyService.getApiKeyById(id);
        return ResponseEntity.ok(apiKeyMapper.toDTO(apiKey));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('API_KEY_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get all API keys", description = "Retrieve all API keys (paginated)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API keys retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<ApiKeyDTO>> getAllApiKeys(Pageable pageable) {
        log.debug("REST request to get all API keys");

        Page<ApiKey> apiKeys = apiKeyService.getAllApiKeys(pageable);
        Page<ApiKeyDTO> dtos = apiKeys.map(apiKeyMapper::toDTO);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('API_KEY_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get active API keys", description = "Retrieve all active API keys (enabled and not expired)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active API keys retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<ApiKeyDTO>> getActiveApiKeys() {
        log.debug("REST request to get active API keys");

        List<ApiKey> apiKeys = apiKeyService.getActiveKeys();
        List<ApiKeyDTO> dtos = apiKeys.stream()
                .map(apiKeyMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('API_KEY_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Search API keys", description = "Search API keys by name or description")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<ApiKeyDTO>> searchApiKeys(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            Pageable pageable) {
        log.debug("REST request to search API keys: {}", keyword);

        Page<ApiKey> apiKeys = apiKeyService.searchApiKeys(keyword, pageable);
        Page<ApiKeyDTO> dtos = apiKeys.map(apiKeyMapper::toDTO);
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('API_KEY_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Update API key", description = "Update API key configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API key updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "API key not found")
    })
    public ResponseEntity<ApiKeyDTO> updateApiKey(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        log.debug("REST request to update API key: {}", id);

        ApiKey updated = apiKeyService.updateApiKey(
                id,
                (String) updates.get("name"),
                (String) updates.get("description"),
                updates.containsKey("permissions") ? (java.util.Set<String>) updates.get("permissions") : null,
                (Integer) updates.get("rateLimitPerMinute"),
                (String) updates.get("allowedIps")
        );

        return ResponseEntity.ok(apiKeyMapper.toDTO(updated));
    }

    @PutMapping("/{id}/enable")
    @PreAuthorize("hasAnyAuthority('API_KEY_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Enable API key", description = "Enable an API key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API key enabled successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "API key not found")
    })
    public ResponseEntity<ApiKeyDTO> enableApiKey(@PathVariable Long id) {
        log.debug("REST request to enable API key: {}", id);

        ApiKey apiKey = apiKeyService.enableApiKey(id);
        return ResponseEntity.ok(apiKeyMapper.toDTO(apiKey));
    }

    @PutMapping("/{id}/disable")
    @PreAuthorize("hasAnyAuthority('API_KEY_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Disable API key", description = "Disable an API key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API key disabled successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "API key not found")
    })
    public ResponseEntity<ApiKeyDTO> disableApiKey(@PathVariable Long id) {
        log.debug("REST request to disable API key: {}", id);

        ApiKey apiKey = apiKeyService.disableApiKey(id);
        return ResponseEntity.ok(apiKeyMapper.toDTO(apiKey));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('API_KEY_DELETE', 'ROLE_ADMIN')")
    @Operation(summary = "Delete API key", description = "Delete an API key (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "API key deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "API key not found")
    })
    public ResponseEntity<Void> deleteApiKey(@PathVariable Long id) {
        log.debug("REST request to delete API key: {}", id);

        apiKeyService.deleteApiKey(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyAuthority('API_KEY_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get API key statistics", description = "Get statistics about API keys")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Map<String, Object>> getApiKeyStats() {
        log.debug("REST request to get API key statistics");

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalKeys", apiKeyService.getAllApiKeys().size());
        stats.put("activeKeys", apiKeyService.countActiveKeys());
        stats.put("expiredKeys", apiKeyService.findExpiredKeys().size());

        return ResponseEntity.ok(stats);
    }
}
