package com.processmonster.bpm.mapper;

import com.processmonster.bpm.dto.process.CreateProcessDefinitionDTO;
import com.processmonster.bpm.dto.process.ProcessDefinitionDTO;
import com.processmonster.bpm.dto.process.ProcessDefinitionDetailDTO;
import com.processmonster.bpm.dto.process.ProcessVersionInfoDTO;
import com.processmonster.bpm.dto.process.UpdateProcessDefinitionDTO;
import com.processmonster.bpm.entity.ProcessDefinition;
import org.mapstruct.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for ProcessDefinition entity and DTOs
 */
@Mapper(componentModel = "spring",
        uses = {ProcessCategoryMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProcessDefinitionMapper {

    /**
     * Convert entity to DTO (without BPMN XML)
     */
    @Mapping(target = "tagList", expression = "java(parseTags(entity.getTags()))")
    @Mapping(target = "fullIdentifier", expression = "java(entity.getFullIdentifier())")
    @Mapping(target = "deployed", expression = "java(entity.isDeployed())")
    ProcessDefinitionDTO toDTO(ProcessDefinition entity);

    /**
     * Convert entity to detail DTO (with BPMN XML)
     */
    @Mapping(target = "tagList", expression = "java(parseTags(entity.getTags()))")
    @Mapping(target = "fullIdentifier", expression = "java(entity.getFullIdentifier())")
    @Mapping(target = "deployed", expression = "java(entity.isDeployed())")
    ProcessDefinitionDetailDTO toDetailDTO(ProcessDefinition entity);

    /**
     * Convert entity to version info DTO
     */
    @Mapping(target = "fullIdentifier", expression = "java(entity.getFullIdentifier())")
    @Mapping(target = "deployed", expression = "java(entity.isDeployed())")
    ProcessVersionInfoDTO toVersionInfoDTO(ProcessDefinition entity);

    /**
     * Convert create DTO to entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "processKey", ignore = true) // Will be extracted from BPMN XML
    @Mapping(target = "version", ignore = true) // Will be set by service
    @Mapping(target = "isLatestVersion", ignore = true) // Will be set by service
    @Mapping(target = "category", ignore = true) // Will be set by service from categoryId
    @Mapping(target = "deploymentId", ignore = true)
    @Mapping(target = "deployedAt", ignore = true)
    @Mapping(target = "deployedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    ProcessDefinition toEntity(CreateProcessDefinitionDTO dto);

    /**
     * Update existing entity from DTO (partial update)
     * Note: BPMN XML changes trigger versioning, handled by service
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "processKey", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isLatestVersion", ignore = true)
    @Mapping(target = "category", ignore = true) // Will be set by service from categoryId
    @Mapping(target = "bpmnXml", ignore = true) // Handled separately by service
    @Mapping(target = "deploymentId", ignore = true)
    @Mapping(target = "deployedAt", ignore = true)
    @Mapping(target = "deployedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(UpdateProcessDefinitionDTO dto, @MappingTarget ProcessDefinition entity);

    /**
     * Parse comma-separated tags into list
     */
    default List<String> parseTags(String tags) {
        if (tags == null || tags.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toList());
    }
}
