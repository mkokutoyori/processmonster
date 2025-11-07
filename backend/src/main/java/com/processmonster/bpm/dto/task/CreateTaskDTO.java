package com.processmonster.bpm.dto.task;

import com.processmonster.bpm.entity.Task.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for creating a new task
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskDTO {

    @NotBlank(message = "{task.name.required}")
    @Size(max = 200, message = "{task.name.size}")
    private String name;

    @Size(max = 5000, message = "{task.description.size}")
    private String description;

    @Builder.Default
    private TaskPriority priority = TaskPriority.NORMAL;

    @Size(max = 100, message = "{task.assignee.size}")
    private String assignee;

    @Size(max = 100, message = "{task.candidateGroup.size}")
    private String candidateGroup;

    private LocalDateTime dueDate;

    private LocalDateTime followUpDate;

    private Long processInstanceId;

    @Size(max = 100, message = "{task.activityId.size}")
    private String activityId;

    @Size(max = 200, message = "{task.formKey.size}")
    private String formKey;
}
