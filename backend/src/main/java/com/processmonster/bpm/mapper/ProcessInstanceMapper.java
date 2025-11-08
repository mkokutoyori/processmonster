package com.processmonster.bpm.mapper;

import com.processmonster.bpm.dto.instance.ExecutionHistoryDTO;
import com.processmonster.bpm.dto.instance.ProcessInstanceDTO;
import com.processmonster.bpm.entity.ExecutionHistory;
import com.processmonster.bpm.entity.ProcessInstance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for ProcessInstance and ExecutionHistory
 */
@Mapper(componentModel = "spring")
public interface ProcessInstanceMapper {

    /**
     * Convert ProcessInstance entity to DTO
     */
    @Mapping(target = "processDefinitionId", source = "processDefinition.id")
    @Mapping(target = "processDefinitionName", source = "processDefinition.name")
    @Mapping(target = "processKey", source = "processDefinition.processKey")
    @Mapping(target = "processVersion", source = "processDefinition.version")
    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    ProcessInstanceDTO toDTO(ProcessInstance entity);

    /**
     * Convert ExecutionHistory entity to DTO
     */
    @Mapping(target = "eventType", expression = "java(entity.getEventType().name())")
    ExecutionHistoryDTO toDTO(ExecutionHistory entity);
}
