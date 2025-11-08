package com.processmonster.bpm.repository;

import com.processmonster.bpm.entity.Task;
import com.processmonster.bpm.entity.Task.TaskPriority;
import com.processmonster.bpm.entity.Task.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Task entity
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Find task by ID (non-deleted)
     */
    Optional<Task> findByIdAndDeletedFalse(Long id);

    /**
     * Find all non-deleted tasks
     */
    Page<Task> findByDeletedFalse(Pageable pageable);

    /**
     * Find tasks assigned to a user
     */
    Page<Task> findByAssigneeAndDeletedFalse(String assignee, Pageable pageable);

    /**
     * Find tasks in candidate group (unassigned)
     */
    Page<Task> findByCandidateGroupAndAssigneeIsNullAndDeletedFalse(String candidateGroup, Pageable pageable);

    /**
     * Find tasks by status
     */
    Page<Task> findByStatusAndDeletedFalse(TaskStatus status, Pageable pageable);

    /**
     * Find active tasks for a user
     */
    @Query("SELECT t FROM Task t WHERE t.assignee = :assignee " +
           "AND t.status IN ('ASSIGNED', 'IN_PROGRESS') " +
           "AND t.deleted = false")
    Page<Task> findActiveTasksByAssignee(@Param("assignee") String assignee, Pageable pageable);

    /**
     * Find tasks by process instance
     */
    Page<Task> findByProcessInstanceIdAndDeletedFalse(Long processInstanceId, Pageable pageable);

    /**
     * Find overdue tasks
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate < :now " +
           "AND t.status IN ('CREATED', 'ASSIGNED', 'IN_PROGRESS') " +
           "AND t.deleted = false")
    Page<Task> findOverdueTasks(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * Find tasks due soon (within next hours)
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :now AND :future " +
           "AND t.status IN ('CREATED', 'ASSIGNED', 'IN_PROGRESS') " +
           "AND t.deleted = false")
    Page<Task> findTasksDueSoon(@Param("now") LocalDateTime now,
                                 @Param("future") LocalDateTime future,
                                 Pageable pageable);

    /**
     * Find tasks by priority
     */
    Page<Task> findByPriorityAndDeletedFalse(TaskPriority priority, Pageable pageable);

    /**
     * Count active tasks for a user
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignee = :assignee " +
           "AND t.status IN ('ASSIGNED', 'IN_PROGRESS') " +
           "AND t.deleted = false")
    Long countActiveTasksByAssignee(@Param("assignee") String assignee);

    /**
     * Count overdue tasks
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.dueDate < :now " +
           "AND t.status IN ('CREATED', 'ASSIGNED', 'IN_PROGRESS') " +
           "AND t.deleted = false")
    Long countOverdueTasks(@Param("now") LocalDateTime now);

    /**
     * Search tasks by name or description
     */
    @Query("SELECT t FROM Task t WHERE " +
           "(LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND t.deleted = false")
    Page<Task> searchTasks(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find tasks by assignee and status
     */
    Page<Task> findByAssigneeAndStatusAndDeletedFalse(String assignee, TaskStatus status, Pageable pageable);

    /**
     * Find tasks by candidate group and status
     */
    Page<Task> findByCandidateGroupAndStatusAndDeletedFalse(String candidateGroup, TaskStatus status, Pageable pageable);

    /**
     * Find high priority tasks for a user
     */
    @Query("SELECT t FROM Task t WHERE t.assignee = :assignee " +
           "AND t.priority IN ('HIGH', 'CRITICAL') " +
           "AND t.status IN ('ASSIGNED', 'IN_PROGRESS') " +
           "AND t.deleted = false")
    List<Task> findHighPriorityTasksByAssignee(@Param("assignee") String assignee);

    /**
     * Find all tasks (not deleted) - List version
     */
    List<Task> findByDeletedFalse();

    /**
     * Count all tasks (not deleted)
     */
    long countByDeletedFalse();

    /**
     * Count tasks by status list
     */
    long countByStatusInAndDeletedFalse(List<TaskStatus> statuses);

    /**
     * Count tasks by status and completed after date
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status " +
           "AND t.completedAt > :completedAt AND t.deleted = false")
    long countByStatusAndCompletedAtAfterAndDeletedFalse(
            @Param("status") TaskStatus status,
            @Param("completedAt") LocalDateTime completedAt);

    /**
     * Find tasks by status and completed after date
     */
    @Query("SELECT t FROM Task t WHERE t.status = :status " +
           "AND t.completedAt > :completedAt AND t.deleted = false")
    List<Task> findByStatusAndCompletedAtAfterAndDeletedFalse(
            @Param("status") TaskStatus status,
            @Param("completedAt") LocalDateTime completedAt);

    /**
     * Count tasks by assignee username
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignee.username = :username AND t.deleted = false")
    long countByAssigneeUsernameAndDeletedFalse(@Param("username") String username);

    /**
     * Count tasks by assignee username and status
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignee.username = :username " +
           "AND t.status = :status AND t.deleted = false")
    long countByAssigneeUsernameAndStatusAndDeletedFalse(
            @Param("username") String username,
            @Param("status") TaskStatus status);

    /**
     * Find tasks by assignee username and status
     */
    @Query("SELECT t FROM Task t WHERE t.assignee.username = :username " +
           "AND t.status = :status AND t.deleted = false")
    List<Task> findByAssigneeUsernameAndStatusAndDeletedFalse(
            @Param("username") String username,
            @Param("status") TaskStatus status);

    /**
     * Find overdue tasks - List version
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate < :now " +
           "AND t.status IN ('CREATED', 'ASSIGNED', 'IN_PROGRESS') " +
           "AND t.deleted = false")
    List<Task> findOverdueTasks(@Param("now") LocalDateTime now);

    /**
     * Find tasks by status - List version
     */
    List<Task> findByStatusAndDeletedFalse(TaskStatus status);
}
