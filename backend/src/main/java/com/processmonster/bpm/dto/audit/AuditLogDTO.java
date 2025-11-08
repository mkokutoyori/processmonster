package com.processmonster.bpm.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for audit log information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {

    private Long id;
    private LocalDateTime timestamp;
    private String username;
    private String action;
    private String entityType;
    private Long entityId;
    private String entityName;
    private String oldValue;
    private String newValue;
    private String httpMethod;
    private String requestUrl;
    private String ipAddress;
    private String userAgent;
    private String sessionId;
    private String result;
    private String errorMessage;
    private String context;
    private String severity;
    private String tags;
}
