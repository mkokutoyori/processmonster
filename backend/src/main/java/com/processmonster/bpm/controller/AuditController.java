package com.processmonster.bpm.controller;

import com.processmonster.bpm.dto.audit.AuditLogDTO;
import com.processmonster.bpm.entity.AuditLog;
import com.processmonster.bpm.mapper.AuditMapper;
import com.processmonster.bpm.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for audit log queries.
 * Provides read-only access to audit logs for compliance and monitoring.
 */
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit", description = "Audit log management and compliance")
@SecurityRequirement(name = "bearerAuth")
public class AuditController {

    private final AuditService auditService;
    private final AuditMapper auditMapper;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('AUDIT_READ', 'ROLE_ADMIN', 'ROLE_AUDITOR')")
    @Operation(summary = "Get all audit logs", description = "Retrieve all audit logs (paginated)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<AuditLogDTO>> getAllLogs(Pageable pageable) {
        log.debug("REST request to get all audit logs");

        Page<AuditLog> logs = auditService.getAllLogs(pageable);
        Page<AuditLogDTO> dtos = logs.map(auditMapper::toDTO);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasAnyAuthority('AUDIT_READ', 'ROLE_ADMIN', 'ROLE_AUDITOR')")
    @Operation(summary = "Get logs by username", description = "Retrieve audit logs for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<AuditLogDTO>> getLogsByUsername(
            @PathVariable String username,
            Pageable pageable) {
        log.debug("REST request to get audit logs for user: {}", username);

        Page<AuditLog> logs = auditService.getLogsByUsername(username, pageable);
        Page<AuditLogDTO> dtos = logs.map(auditMapper::toDTO);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/action/{action}")
    @PreAuthorize("hasAnyAuthority('AUDIT_READ', 'ROLE_ADMIN', 'ROLE_AUDITOR')")
    @Operation(summary = "Get logs by action", description = "Retrieve audit logs for a specific action")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<AuditLogDTO>> getLogsByAction(
            @PathVariable String action,
            Pageable pageable) {
        log.debug("REST request to get audit logs for action: {}", action);

        Page<AuditLog> logs = auditService.getLogsByAction(action, pageable);
        Page<AuditLogDTO> dtos = logs.map(auditMapper::toDTO);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAnyAuthority('AUDIT_READ', 'ROLE_ADMIN', 'ROLE_AUDITOR')")
    @Operation(summary = "Get logs by entity", description = "Retrieve audit logs for a specific entity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<AuditLogDTO>> getLogsByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            Pageable pageable) {
        log.debug("REST request to get audit logs for entity: {} {}", entityType, entityId);

        Page<AuditLog> logs = auditService.getLogsByEntity(entityType, entityId, pageable);
        Page<AuditLogDTO> dtos = logs.map(auditMapper::toDTO);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/daterange")
    @PreAuthorize("hasAnyAuthority('AUDIT_READ', 'ROLE_ADMIN', 'ROLE_AUDITOR')")
    @Operation(summary = "Get logs by date range", description = "Retrieve audit logs within a date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date format"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<AuditLogDTO>> getLogsByDateRange(
            @Parameter(description = "Start date (ISO format)", example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)", example = "2024-12-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        log.debug("REST request to get audit logs between {} and {}", startDate, endDate);

        Page<AuditLog> logs = auditService.getLogsByDateRange(startDate, endDate, pageable);
        Page<AuditLogDTO> dtos = logs.map(auditMapper::toDTO);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/security")
    @PreAuthorize("hasAnyAuthority('AUDIT_READ', 'ROLE_ADMIN', 'ROLE_AUDITOR')")
    @Operation(summary = "Get security logs", description = "Retrieve security-related audit logs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Security logs retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<AuditLogDTO>> getSecurityLogs(Pageable pageable) {
        log.debug("REST request to get security audit logs");

        Page<AuditLog> logs = auditService.getSecurityLogs(pageable);
        Page<AuditLogDTO> dtos = logs.map(auditMapper::toDTO);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/failed")
    @PreAuthorize("hasAnyAuthority('AUDIT_READ', 'ROLE_ADMIN', 'ROLE_AUDITOR')")
    @Operation(summary = "Get failed actions", description = "Retrieve audit logs of failed actions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Failed actions retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<AuditLogDTO>> getFailedActions(Pageable pageable) {
        log.debug("REST request to get failed action audit logs");

        Page<AuditLog> logs = auditService.getFailedActions(pageable);
        Page<AuditLogDTO> dtos = logs.map(auditMapper::toDTO);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('AUDIT_READ', 'ROLE_ADMIN', 'ROLE_AUDITOR')")
    @Operation(summary = "Search audit logs", description = "Search audit logs by keyword")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<AuditLogDTO>> searchLogs(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            Pageable pageable) {
        log.debug("REST request to search audit logs: {}", keyword);

        Page<AuditLog> logs = auditService.searchLogs(keyword, pageable);
        Page<AuditLogDTO> dtos = logs.map(auditMapper::toDTO);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/user/{username}/recent")
    @PreAuthorize("hasAnyAuthority('AUDIT_READ', 'ROLE_ADMIN', 'ROLE_AUDITOR')")
    @Operation(summary = "Get recent user logs", description = "Get recent audit logs for a user (last N hours)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recent logs retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<AuditLogDTO>> getRecentUserLogs(
            @PathVariable String username,
            @Parameter(description = "Hours to look back") @RequestParam(defaultValue = "24") int hours) {
        log.debug("REST request to get recent logs for user: {} (last {} hours)", username, hours);

        List<AuditLog> logs = auditService.getRecentLogs(username, hours);
        List<AuditLogDTO> dtos = logs.stream()
                .map(auditMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/failed-logins/{username}")
    @PreAuthorize("hasAnyAuthority('AUDIT_READ', 'ROLE_ADMIN', 'ROLE_AUDITOR')")
    @Operation(summary = "Get failed login count", description = "Get count of failed logins for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Failed login count retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Map<String, Object>> getFailedLoginCount(
            @PathVariable String username,
            @Parameter(description = "Minutes to look back") @RequestParam(defaultValue = "30") int minutes) {
        log.debug("REST request to get failed login count for user: {} (last {} minutes)", username, minutes);

        long count = auditService.getFailedLoginCount(username, minutes);

        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("failedLoginCount", count);
        response.put("windowMinutes", minutes);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyAuthority('AUDIT_READ', 'ROLE_ADMIN', 'ROLE_AUDITOR')")
    @Operation(summary = "Get audit statistics", description = "Get statistics about audit logs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Map<String, Object>> getAuditStats() {
        log.debug("REST request to get audit statistics");

        // For now, basic stats - can be enhanced
        Map<String, Object> stats = new HashMap<>();
        Page<AuditLog> allLogs = auditService.getAllLogs(Pageable.ofSize(1));
        Page<AuditLog> securityLogs = auditService.getSecurityLogs(Pageable.ofSize(1));
        Page<AuditLog> failedActions = auditService.getFailedActions(Pageable.ofSize(1));

        stats.put("totalLogs", allLogs.getTotalElements());
        stats.put("securityLogs", securityLogs.getTotalElements());
        stats.put("failedActions", failedActions.getTotalElements());

        return ResponseEntity.ok(stats);
    }
}
