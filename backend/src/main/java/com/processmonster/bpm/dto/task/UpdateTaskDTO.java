package com.processmonster.bpm.dto.task;

import com.processmonster.bpm.entity.Task.TaskPriority;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for updating a task
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskDTO {

    @Size(max = 200, message = "{task.name.size}")
    private String name;

    @Size(max = 5000, message = "{task.description.size}")
    private String description;

    private TaskPriority priority;

    private LocalDateTime dueDate;

    private LocalDateTime followUpDate;
}
