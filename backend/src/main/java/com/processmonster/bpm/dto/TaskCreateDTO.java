package com.processmonster.bpm.dto;

import com.processmonster.bpm.entity.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for creating tasks from Camunda
 */
@Data
public class TaskCreateDTO {
    private String title;
    private String description;
    private String priority;
    private TaskStatus status;
    private Long assigneeId;
    private LocalDateTime dueDate;
    private String processInstanceId;
    private String camundaTaskId;
    private String formKey;
}
