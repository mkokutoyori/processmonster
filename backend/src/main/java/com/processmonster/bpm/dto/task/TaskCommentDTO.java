package com.processmonster.bpm.dto.task;

import com.processmonster.bpm.entity.TaskComment.CommentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for TaskComment entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCommentDTO {

    private Long id;
    private Long taskId;
    private String content;
    private CommentType type;
    private LocalDateTime createdAt;
    private String createdBy;
}
