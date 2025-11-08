package com.processmonster.bpm.service;

import com.processmonster.bpm.entity.AuditLog;
import com.processmonster.bpm.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for audit logging.
 * All sensitive operations should be logged through this service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Log an action (async for performance)
     */
    @Async
    public void logAction(String action, String entityType, Long entityId, String entityName,
                          Object oldValue, Object newValue, String result) {
        try {
            AuditLog auditLog = createAuditLog(action, entityType, entityId, entityName,
                    oldValue, newValue, result, null, "INFO", null);
            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} - {} {}", action, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage(), e);
        }
    }

    /**
     * Log an action with custom context
     */
    @Async
    public void logAction(String action, String entityType, Long entityId, String entityName,
                          Object oldValue, Object newValue, String result, Map<String, Object> context) {
        try {
            AuditLog auditLog = createAuditLog(action, entityType, entityId, entityName,
                    oldValue, newValue, result, context, "INFO", null);
            auditLogRepository.save(auditLog);
            log.debug("Audit log created with context: {} - {} {}", action, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage(), e);
        }
    }

    /**
     * Log an error
     */
    @Async
    public void logError(String action, String entityType, Long entityId, String entityName,
                         String errorMessage) {
        try {
            AuditLog auditLog = createAuditLog(action, entityType, entityId, entityName,
                    null, null, "ERROR", null, "ERROR", errorMessage);
            auditLogRepository.save(auditLog);
            log.debug("Audit error log created: {} - {}", action, errorMessage);
        } catch (Exception e) {
            log.error("Failed to create audit error log: {}", e.getMessage(), e);
        }
    }

    /**
     * Log a security event
     */
    @Async
    public void logSecurityEvent(String action, String username, String result, String errorMessage) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .timestamp(LocalDateTime.now())
                    .username(username != null ? username : getCurrentUsername())
                    .action(action)
                    .result(result)
                    .errorMessage(errorMessage)
                    .severity(result.equals("SUCCESS") ? "INFO" : "WARNING")
                    .tags("security")
                    .build();

            enrichWithRequestInfo(auditLog);
            auditLogRepository.save(auditLog);
            log.debug("Security audit log created: {} - {}", action, username);
        } catch (Exception e) {
            log.error("Failed to create security audit log: {}", e.getMessage(), e);
        }
    }

    /**
     * Log a login attempt
     */
    @Async
    public void logLogin(String username, boolean success, String errorMessage) {
        String action = success ? "LOGIN_SUCCESS" : "LOGIN_FAILED";
        String result = success ? "SUCCESS" : "FAILURE";
        String severity = success ? "INFO" : "WARNING";

        try {
            AuditLog auditLog = AuditLog.builder()
                    .timestamp(LocalDateTime.now())
                    .username(username)
                    .action(action)
                    .result(result)
                    .errorMessage(errorMessage)
                    .severity(severity)
                    .tags("security,authentication")
                    .build();

            enrichWithRequestInfo(auditLog);
            auditLogRepository.save(auditLog);
            log.debug("Login audit log created: {} - {}", username, result);
        } catch (Exception e) {
            log.error("Failed to create login audit log: {}", e.getMessage(), e);
        }
    }

    /**
     * Log a logout
     */
    @Async
    public void logLogout(String username) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .timestamp(LocalDateTime.now())
                    .username(username)
                    .action("LOGOUT")
                    .result("SUCCESS")
                    .severity("INFO")
                    .tags("security,authentication")
                    .build();

            enrichWithRequestInfo(auditLog);
            auditLogRepository.save(auditLog);
            log.debug("Logout audit log created: {}", username);
        } catch (Exception e) {
            log.error("Failed to create logout audit log: {}", e.getMessage(), e);
        }
    }

    /**
     * Get all audit logs (paginated)
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
    }

    /**
     * Get logs by username
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getLogsByUsername(String username, Pageable pageable) {
        return auditLogRepository.findByUsername(username, pageable);
    }

    /**
     * Get logs by action
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable);
    }

    /**
     * Get logs by entity
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getLogsByEntity(String entityType, Long entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
    }

    /**
     * Get logs by date range
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByTimestampBetween(startDate, endDate, pageable);
    }

    /**
     * Get security logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getSecurityLogs(Pageable pageable) {
        return auditLogRepository.findSecurityLogs(pageable);
    }

    /**
     * Get failed actions
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getFailedActions(Pageable pageable) {
        return auditLogRepository.findFailedActions(pageable);
    }

    /**
     * Search logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> searchLogs(String keyword, Pageable pageable) {
        return auditLogRepository.searchLogs(keyword, pageable);
    }

    /**
     * Get failed login count for a user
     */
    @Transactional(readOnly = true)
    public long getFailedLoginCount(String username, int minutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutes);
        return auditLogRepository.countFailedLoginsSince(username, since);
    }

    /**
     * Get recent logs for a user
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getRecentLogs(String username, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return auditLogRepository.findRecentLogsByUsername(username, since);
    }

    /**
     * Clean up old logs (should be called periodically - e.g., after retention period)
     */
    @Transactional
    public void cleanupOldLogs(int retentionDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        auditLogRepository.deleteOldLogs(cutoffDate);
        log.info("Cleaned up audit logs older than {} days", retentionDays);
    }

    // Helper methods

    private AuditLog createAuditLog(String action, String entityType, Long entityId, String entityName,
                                     Object oldValue, Object newValue, String result,
                                     Map<String, Object> context, String severity, String errorMessage) {
        AuditLog auditLog = AuditLog.builder()
                .timestamp(LocalDateTime.now())
                .username(getCurrentUsername())
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .entityName(entityName)
                .result(result)
                .severity(severity)
                .errorMessage(errorMessage)
                .build();

        // Convert values to JSON
        try {
            if (oldValue != null) {
                auditLog.setOldValue(objectMapper.writeValueAsString(oldValue));
            }
            if (newValue != null) {
                auditLog.setNewValue(objectMapper.writeValueAsString(newValue));
            }
            if (context != null) {
                auditLog.setContext(objectMapper.writeValueAsString(context));
            }
        } catch (Exception e) {
            log.error("Failed to convert audit values to JSON: {}", e.getMessage());
        }

        enrichWithRequestInfo(auditLog);
        return auditLog;
    }

    private void enrichWithRequestInfo(AuditLog auditLog) {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                auditLog.setHttpMethod(request.getMethod());
                auditLog.setRequestUrl(request.getRequestURI());
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                auditLog.setSessionId(request.getSession(false) != null ?
                        request.getSession(false).getId() : null);
            }
        } catch (Exception e) {
            log.debug("Unable to enrich audit log with request info: {}", e.getMessage());
        }
    }

    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            log.debug("Unable to get current username: {}", e.getMessage());
        }
        return "anonymous";
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
