package com.processmonster.bpm.repository;

import com.processmonster.bpm.entity.TaskAttachment;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for TaskAttachment entity
 */
@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {

    /**
     * Find attachment by ID (non-deleted)
     */
    Optional<TaskAttachment> findByIdAndDeletedFalse(Long id);

    /**
     * Find all attachments for a task
     */
    List<TaskAttachment> findByTaskIdAndDeletedFalse(Long taskId, Sort sort);

    /**
     * Find attachments by task ID ordered by creation date
     */
    default List<TaskAttachment> findByTaskIdOrderByCreatedAtAsc(Long taskId) {
        return findByTaskIdAndDeletedFalse(taskId, Sort.by(Sort.Direction.ASC, "createdAt"));
    }

    /**
     * Count attachments for a task
     */
    Long countByTaskIdAndDeletedFalse(Long taskId);

    /**
     * Find attachment by stored file name
     */
    Optional<TaskAttachment> findByStoredFileNameAndDeletedFalse(String storedFileName);
}
