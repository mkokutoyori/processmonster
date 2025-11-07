package com.processmonster.bpm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing a file attachment on a task
 */
@Entity
@Table(name = "task_attachments", indexes = {
        @Index(name = "idx_task_attachment_task", columnList = "task_id"),
        @Index(name = "idx_task_attachment_deleted", columnList = "deleted")
})
@EntityListeners(AuditingEntityListener.class)
@Where(clause = "deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Parent task
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    /**
     * Original file name
     */
    @Column(nullable = false, length = 255)
    private String fileName;

    /**
     * Stored file name (may be different from original)
     */
    @Column(nullable = false, length = 255)
    private String storedFileName;

    /**
     * File path in storage system
     */
    @Column(nullable = false, length = 500)
    private String filePath;

    /**
     * File MIME type
     */
    @Column(length = 100)
    private String mimeType;

    /**
     * File size in bytes
     */
    private Long fileSize;

    /**
     * Optional description of the attachment
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    // Audit fields
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(nullable = false, updatable = false, length = 100)
    private String createdBy;

    // Soft delete
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    private LocalDateTime deletedAt;

    /**
     * Get file size in human-readable format
     */
    public String getFormattedSize() {
        if (fileSize == null) return "0 B";

        long bytes = fileSize;
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
