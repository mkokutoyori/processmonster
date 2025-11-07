package com.processmonster.bpm.dto.task;

import com.processmonster.bpm.entity.Task.TaskPriority;
import com.processmonster.bpm.entity.Task.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Task entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {

    private Long id;
    private String name;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private String assignee;
    private String candidateGroup;
    private LocalDateTime dueDate;
    private LocalDateTime followUpDate;

    // Process instance info
    private Long processInstanceId;
    private String processInstanceBusinessKey;
    private String processDefinitionName;

    // Activity info
    private String activityId;
    private String formKey;

    // Completion info
    private LocalDateTime completedDate;
    private String completedBy;

    // Claim info
    private LocalDateTime claimedDate;
    private String claimedBy;

    // Counts
    private Long commentCount;
    private Long attachmentCount;

    // Computed fields
    private Boolean isOverdue;
    private Boolean isActive;

    // Audit
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
