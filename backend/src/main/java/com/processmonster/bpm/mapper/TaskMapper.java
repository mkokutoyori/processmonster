package com.processmonster.bpm.mapper;

import com.processmonster.bpm.dto.task.*;
import com.processmonster.bpm.entity.ProcessInstance;
import com.processmonster.bpm.entity.Task;
import com.processmonster.bpm.entity.TaskAttachment;
import com.processmonster.bpm.entity.TaskComment;
import org.mapstruct.*;

/**
 * MapStruct mapper for Task entities and DTOs
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TaskMapper {

    /**
     * Map Task entity to TaskDTO
     */
    @Mapping(target = "processInstanceId", source = "processInstance.id")
    @Mapping(target = "processInstanceBusinessKey", source = "processInstance.businessKey")
    @Mapping(target = "processDefinitionName", source = "processInstance.processDefinition.name")
    @Mapping(target = "commentCount", expression = "java(task.getComments() != null ? (long) task.getComments().size() : 0L)")
    @Mapping(target = "attachmentCount", expression = "java(task.getAttachments() != null ? (long) task.getAttachments().size() : 0L)")
    @Mapping(target = "isOverdue", expression = "java(task.isOverdue())")
    @Mapping(target = "isActive", expression = "java(task.isActive())")
    TaskDTO toDTO(Task task);

    /**
     * Map CreateTaskDTO to Task entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "completedDate", ignore = true)
    @Mapping(target = "completedBy", ignore = true)
    @Mapping(target = "claimedDate", ignore = true)
    @Mapping(target = "claimedBy", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Task toEntity(CreateTaskDTO createDTO);

    /**
     * Update Task entity from UpdateTaskDTO
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "candidateGroup", ignore = true)
    @Mapping(target = "processInstance", ignore = true)
    @Mapping(target = "activityId", ignore = true)
    @Mapping(target = "formKey", ignore = true)
    @Mapping(target = "completedDate", ignore = true)
    @Mapping(target = "completedBy", ignore = true)
    @Mapping(target = "claimedDate", ignore = true)
    @Mapping(target = "claimedBy", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromDTO(UpdateTaskDTO updateDTO, @MappingTarget Task task);

    /**
     * Map TaskComment to TaskCommentDTO
     */
    @Mapping(target = "taskId", source = "task.id")
    TaskCommentDTO toCommentDTO(TaskComment comment);

    /**
     * Map TaskAttachment to TaskAttachmentDTO
     */
    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "formattedSize", expression = "java(attachment.getFormattedSize())")
    TaskAttachmentDTO toAttachmentDTO(TaskAttachment attachment);
}
