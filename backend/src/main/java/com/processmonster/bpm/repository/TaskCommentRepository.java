package com.processmonster.bpm.repository;

import com.processmonster.bpm.entity.TaskComment;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for TaskComment entity
 */
@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {

    /**
     * Find comment by ID (non-deleted)
     */
    Optional<TaskComment> findByIdAndDeletedFalse(Long id);

    /**
     * Find all comments for a task
     */
    List<TaskComment> findByTaskIdAndDeletedFalse(Long taskId, Sort sort);

    /**
     * Find comments by task ID ordered by creation date
     */
    default List<TaskComment> findByTaskIdOrderByCreatedAtAsc(Long taskId) {
        return findByTaskIdAndDeletedFalse(taskId, Sort.by(Sort.Direction.ASC, "createdAt"));
    }

    /**
     * Count comments for a task
     */
    Long countByTaskIdAndDeletedFalse(Long taskId);
}
