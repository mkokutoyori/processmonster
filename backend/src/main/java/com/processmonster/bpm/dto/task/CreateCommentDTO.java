package com.processmonster.bpm.dto.task;

import com.processmonster.bpm.entity.TaskComment.CommentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a task comment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentDTO {

    @NotBlank(message = "{task.comment.content.required}")
    @Size(max = 5000, message = "{task.comment.content.size}")
    private String content;

    private CommentType type;
}
