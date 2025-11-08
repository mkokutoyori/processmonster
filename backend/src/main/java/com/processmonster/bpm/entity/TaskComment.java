package com.processmonster.bpm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing a comment on a task
 */
@Entity
@Table(name = "task_comments", indexes = {
        @Index(name = "idx_task_comment_task", columnList = "task_id"),
        @Index(name = "idx_task_comment_deleted", columnList = "deleted")
})
@EntityListeners(AuditingEntityListener.class)
@Where(clause = "deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskComment {

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
     * Comment content
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Comment type (optional for categorization)
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private CommentType type;

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
     * Comment type enum
     */
    public enum CommentType {
        GENERAL,      // General comment
        QUESTION,     // Question
        DECISION,     // Decision made
        ESCALATION,   // Escalation note
        RESOLUTION    // Resolution/solution
    }
}
