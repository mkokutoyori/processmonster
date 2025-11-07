package com.processmonster.bpm.mapper;

import com.processmonster.bpm.dto.form.*;
import com.processmonster.bpm.entity.FormDefinition;
import com.processmonster.bpm.entity.FormSubmission;
import org.mapstruct.*;

/**
 * MapStruct mapper for Form entities and DTOs
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FormMapper {

    // ========== FormDefinition Mappings ==========

    /**
     * Map FormDefinition entity to FormDefinitionDTO
     */
    @Mapping(target = "submissionCount", expression = "java(formDefinition.getSubmissions() != null ? (long) formDefinition.getSubmissions().size() : 0L)")
    FormDefinitionDTO toDTO(FormDefinition formDefinition);

    /**
     * Map CreateFormDefinitionDTO to FormDefinition entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "published", ignore = true)
    @Mapping(target = "isLatestVersion", ignore = true)
    @Mapping(target = "submissions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    FormDefinition toEntity(CreateFormDefinitionDTO createDTO);

    /**
     * Update FormDefinition entity from UpdateFormDefinitionDTO
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "formKey", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "published", ignore = true)
    @Mapping(target = "isLatestVersion", ignore = true)
    @Mapping(target = "submissions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromDTO(UpdateFormDefinitionDTO updateDTO, @MappingTarget FormDefinition formDefinition);

    // ========== FormSubmission Mappings ==========

    /**
     * Map FormSubmission entity to FormSubmissionDTO
     */
    @Mapping(target = "formDefinitionId", source = "formDefinition.id")
    @Mapping(target = "formKey", source = "formDefinition.formKey")
    @Mapping(target = "formName", source = "formDefinition.name")
    @Mapping(target = "formVersion", source = "formDefinition.version")
    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "processInstanceId", source = "processInstance.id")
    FormSubmissionDTO toSubmissionDTO(FormSubmission submission);

    /**
     * Map SaveDraftDTO to FormSubmission entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "formDefinition", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "processInstance", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "submittedBy", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "validationErrors", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    FormSubmission toEntity(SaveDraftDTO draftDTO);

    /**
     * Map SubmitFormDTO to FormSubmission entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "formDefinition", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "processInstance", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "submittedBy", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "validationErrors", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    FormSubmission toEntity(SubmitFormDTO submitDTO);
}
