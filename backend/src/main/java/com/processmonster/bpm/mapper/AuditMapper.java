package com.processmonster.bpm.mapper;

import com.processmonster.bpm.dto.audit.AuditLogDTO;
import com.processmonster.bpm.entity.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for AuditLog entity
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuditMapper {

    AuditLogDTO toDTO(AuditLog auditLog);
}
