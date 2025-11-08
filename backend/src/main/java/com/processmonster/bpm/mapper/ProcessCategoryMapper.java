package com.processmonster.bpm.mapper;

import com.processmonster.bpm.dto.process.CreateProcessCategoryDTO;
import com.processmonster.bpm.dto.process.ProcessCategoryDTO;
import com.processmonster.bpm.dto.process.UpdateProcessCategoryDTO;
import com.processmonster.bpm.entity.ProcessCategory;
import org.mapstruct.*;

/**
 * MapStruct mapper for ProcessCategory entity and DTOs
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProcessCategoryMapper {

    /**
     * Convert entity to DTO
     */
    ProcessCategoryDTO toDTO(ProcessCategory entity);

    /**
     * Convert create DTO to entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "processCount", ignore = true)
    ProcessCategory toEntity(CreateProcessCategoryDTO dto);

    /**
     * Update existing entity from DTO (partial update)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "processCount", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(UpdateProcessCategoryDTO dto, @MappingTarget ProcessCategory entity);
}
