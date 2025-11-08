package com.processmonster.bpm.dto.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for TaskAttachment entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAttachmentDTO {

    private Long id;
    private Long taskId;
    private String fileName;
    private String storedFileName;
    private String filePath;
    private String mimeType;
    private Long fileSize;
    private String formattedSize;
    private String description;
    private LocalDateTime createdAt;
    private String createdBy;
}
