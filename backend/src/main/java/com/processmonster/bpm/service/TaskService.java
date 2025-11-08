package com.processmonster.bpm.service;

import com.processmonster.bpm.entity.Task;
import com.processmonster.bpm.entity.TaskAttachment;
import com.processmonster.bpm.entity.TaskComment;
import com.processmonster.bpm.entity.Task.TaskPriority;
import com.processmonster.bpm.entity.Task.TaskStatus;
import com.processmonster.bpm.exception.BusinessException;
import com.processmonster.bpm.exception.ResourceNotFoundException;
import com.processmonster.bpm.repository.TaskAttachmentRepository;
import com.processmonster.bpm.repository.TaskCommentRepository;
import com.processmonster.bpm.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for task management operations
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskCommentRepository commentRepository;
    private final TaskAttachmentRepository attachmentRepository;
    private final NotificationService notificationService;
    private final FileStorageService fileStorageService;
    private final MessageSource messageSource;

    /**
     * Get all tasks with pagination
     */
    public Page<Task> getAllTasks(Pageable pageable) {
        return taskRepository.findByDeletedFalse(pageable);
    }

    /**
     * Get task by ID
     */
    public Task getTaskById(Long id) {
        return taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        getMessage("task.not-found", id)));
    }

    /**
     * Get tasks assigned to current user
     */
    public Page<Task> getMyTasks(Pageable pageable) {
        String username = getCurrentUsername();
        return taskRepository.findByAssigneeAndDeletedFalse(username, pageable);
    }

    /**
     * Get active tasks for current user
     */
    public Page<Task> getMyActiveTasks(Pageable pageable) {
        String username = getCurrentUsername();
        return taskRepository.findActiveTasksByAssignee(username, pageable);
    }

    /**
     * Get tasks in queue (candidate group, unassigned)
     */
    public Page<Task> getQueueTasks(String candidateGroup, Pageable pageable) {
        return taskRepository.findByCandidateGroupAndAssigneeIsNullAndDeletedFalse(
                candidateGroup, pageable);
    }

    /**
     * Get tasks by status
     */
    public Page<Task> getTasksByStatus(TaskStatus status, Pageable pageable) {
        return taskRepository.findByStatusAndDeletedFalse(status, pageable);
    }

    /**
     * Get overdue tasks
     */
    public Page<Task> getOverdueTasks(Pageable pageable) {
        return taskRepository.findOverdueTasks(LocalDateTime.now(), pageable);
    }

    /**
     * Get tasks due soon (within next 24 hours)
     */
    public Page<Task> getTasksDueSoon(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusHours(24);
        return taskRepository.findTasksDueSoon(now, tomorrow, pageable);
    }

    /**
     * Search tasks
     */
    public Page<Task> searchTasks(String keyword, Pageable pageable) {
        return taskRepository.searchTasks(keyword, pageable);
    }

    /**
     * Create new task
     */
    @Transactional
    public Task createTask(Task task) {
        // Set initial status
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.CREATED);
        }

        // Set default priority
        if (task.getPriority() == null) {
            task.setPriority(TaskPriority.NORMAL);
        }

        Task saved = taskRepository.save(task);
        log.info("Task created: {} (ID: {})", saved.getName(), saved.getId());

        // Send notification if assigned
        if (saved.getAssignee() != null) {
            notificationService.sendTaskAssigned(saved, saved.getAssignee());
        }

        return saved;
    }

    /**
     * Claim task (assign to current user)
     */
    @Transactional
    public Task claimTask(Long taskId) {
        Task task = getTaskById(taskId);
        String username = getCurrentUsername();

        // Validate task can be claimed
        if (task.getAssignee() != null) {
            throw new BusinessException(getMessage("task.already-claimed", task.getName()));
        }

        if (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.CANCELLED) {
            throw new BusinessException(getMessage("task.cannot-claim-completed", task.getName()));
        }

        // Claim task
        task.setAssignee(username);
        task.setClaimedBy(username);
        task.setClaimedDate(LocalDateTime.now());
        task.setStatus(TaskStatus.ASSIGNED);

        Task saved = taskRepository.save(task);
        log.info("Task claimed: {} by {}", saved.getName(), username);

        notificationService.sendTaskAssigned(saved, username);
        return saved;
    }

    /**
     * Assign task to a specific user
     */
    @Transactional
    public Task assignTask(Long taskId, String assignee) {
        Task task = getTaskById(taskId);
        String currentUsername = getCurrentUsername();

        // Check permissions (only assignee or admin can reassign)
        if (task.getAssignee() != null &&
            !task.getAssignee().equals(currentUsername) &&
            !isAdmin()) {
            throw new BusinessException(getMessage("task.cannot-reassign"));
        }

        String previousAssignee = task.getAssignee();

        // Assign task
        task.setAssignee(assignee);
        task.setStatus(TaskStatus.ASSIGNED);

        if (previousAssignee == null) {
            task.setClaimedBy(assignee);
            task.setClaimedDate(LocalDateTime.now());
        }

        Task saved = taskRepository.save(task);
        log.info("Task assigned: {} to {} (previous: {})",
                saved.getName(), assignee, previousAssignee);

        if (previousAssignee != null) {
            notificationService.sendTaskReassigned(saved, previousAssignee, assignee);
        } else {
            notificationService.sendTaskAssigned(saved, assignee);
        }

        return saved;
    }

    /**
     * Start working on task
     */
    @Transactional
    public Task startTask(Long taskId) {
        Task task = getTaskById(taskId);
        String username = getCurrentUsername();

        // Validate
        if (task.getAssignee() == null || !task.getAssignee().equals(username)) {
            throw new BusinessException(getMessage("task.not-assigned-to-you"));
        }

        if (task.getStatus() != TaskStatus.ASSIGNED) {
            throw new BusinessException(getMessage("task.already-in-progress"));
        }

        task.setStatus(TaskStatus.IN_PROGRESS);

        Task saved = taskRepository.save(task);
        log.info("Task started: {} by {}", saved.getName(), username);

        return saved;
    }

    /**
     * Complete task
     */
    @Transactional
    public Task completeTask(Long taskId, Map<String, Object> formData) {
        Task task = getTaskById(taskId);
        String username = getCurrentUsername();

        // Validate
        if (task.getAssignee() == null || !task.getAssignee().equals(username)) {
            throw new BusinessException(getMessage("task.not-assigned-to-you"));
        }

        if (!task.isActive()) {
            throw new BusinessException(getMessage("task.already-completed"));
        }

        // Complete task
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedBy(username);
        task.setCompletedDate(LocalDateTime.now());

        // TODO: Save form data as process variables

        Task saved = taskRepository.save(task);
        log.info("Task completed: {} by {}", saved.getName(), username);

        notificationService.sendTaskCompleted(saved);
        return saved;
    }

    /**
     * Cancel task
     */
    @Transactional
    public Task cancelTask(Long taskId, String reason) {
        Task task = getTaskById(taskId);

        if (!task.isActive()) {
            throw new BusinessException(getMessage("task.already-completed"));
        }

        task.setStatus(TaskStatus.CANCELLED);
        task.setCompletedBy(getCurrentUsername());
        task.setCompletedDate(LocalDateTime.now());

        // Could add a cancellation reason field if needed

        Task saved = taskRepository.save(task);
        log.info("Task cancelled: {} by {} (reason: {})",
                saved.getName(), getCurrentUsername(), reason);

        return saved;
    }

    /**
     * Add comment to task
     */
    @Transactional
    public TaskComment addComment(Long taskId, String content, TaskComment.CommentType type) {
        Task task = getTaskById(taskId);
        String username = getCurrentUsername();

        TaskComment comment = TaskComment.builder()
                .task(task)
                .content(content)
                .type(type != null ? type : TaskComment.CommentType.GENERAL)
                .build();

        TaskComment saved = commentRepository.save(comment);
        log.info("Comment added to task {}: {} characters by {}",
                taskId, content.length(), username);

        notificationService.sendCommentAdded(task, username, content);
        return saved;
    }

    /**
     * Get task comments
     */
    public List<TaskComment> getTaskComments(Long taskId) {
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
    }

    /**
     * Add attachment to task
     */
    @Transactional
    public TaskAttachment addAttachment(Long taskId, MultipartFile file, String description) {
        Task task = getTaskById(taskId);

        // Store file
        String storedFileName = fileStorageService.storeFile(file, "tasks/" + taskId);

        TaskAttachment attachment = TaskAttachment.builder()
                .task(task)
                .fileName(file.getOriginalFilename())
                .storedFileName(storedFileName)
                .filePath("tasks/" + taskId + "/" + storedFileName)
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .description(description)
                .build();

        TaskAttachment saved = attachmentRepository.save(attachment);
        log.info("Attachment added to task {}: {} ({} bytes)",
                taskId, file.getOriginalFilename(), file.getSize());

        return saved;
    }

    /**
     * Get task attachments
     */
    public List<TaskAttachment> getTaskAttachments(Long taskId) {
        return attachmentRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
    }

    /**
     * Delete attachment
     */
    @Transactional
    public void deleteAttachment(Long attachmentId) {
        TaskAttachment attachment = attachmentRepository.findByIdAndDeletedFalse(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        getMessage("task.attachment.not-found", attachmentId)));

        // Delete file from storage
        fileStorageService.deleteFile(attachment.getStoredFileName());

        // Soft delete attachment
        attachment.setDeleted(true);
        attachment.setDeletedAt(LocalDateTime.now());
        attachmentRepository.save(attachment);

        log.info("Attachment deleted: {} (ID: {})", attachment.getFileName(), attachmentId);
    }

    /**
     * Update task
     */
    @Transactional
    public Task updateTask(Long taskId, Task updates) {
        Task task = getTaskById(taskId);

        // Update allowed fields
        if (updates.getName() != null) {
            task.setName(updates.getName());
        }
        if (updates.getDescription() != null) {
            task.setDescription(updates.getDescription());
        }
        if (updates.getPriority() != null) {
            task.setPriority(updates.getPriority());
        }
        if (updates.getDueDate() != null) {
            task.setDueDate(updates.getDueDate());
        }
        if (updates.getFollowUpDate() != null) {
            task.setFollowUpDate(updates.getFollowUpDate());
        }

        Task saved = taskRepository.save(task);
        log.info("Task updated: {}", saved.getName());

        return saved;
    }

    /**
     * Delete task (soft delete)
     */
    @Transactional
    public void deleteTask(Long taskId) {
        Task task = getTaskById(taskId);

        // Only allow deleting cancelled tasks
        if (task.isActive()) {
            throw new BusinessException(getMessage("task.delete-active-task"));
        }

        task.setDeleted(true);
        task.setDeletedAt(LocalDateTime.now());
        taskRepository.save(task);

        log.info("Task deleted: {} (ID: {})", task.getName(), taskId);
    }

    /**
     * Count active tasks for current user
     */
    public Long countMyActiveTasks() {
        String username = getCurrentUsername();
        return taskRepository.countActiveTasksByAssignee(username);
    }

    /**
     * Count overdue tasks
     */
    public Long countOverdueTasks() {
        return taskRepository.countOverdueTasks(LocalDateTime.now());
    }

    /**
     * Create task from Camunda - used by Camunda listeners
     */
    @Transactional
    public Task createTaskFromCamunda(com.processmonster.bpm.dto.TaskCreateDTO taskCreateDTO) {
        Task task = Task.builder()
                .name(taskCreateDTO.getTitle())
                .description(taskCreateDTO.getDescription())
                .priority(mapStringToPriority(taskCreateDTO.getPriority()))
                .status(taskCreateDTO.getStatus() != null ? taskCreateDTO.getStatus() : TaskStatus.CREATED)
                .dueDate(taskCreateDTO.getDueDate())
                .processInstanceId(taskCreateDTO.getProcessInstanceId())
                .camundaTaskId(taskCreateDTO.getCamundaTaskId())
                .formKey(taskCreateDTO.getFormKey())
                .build();

        // Set assignee if present
        if (taskCreateDTO.getAssigneeId() != null) {
            // TODO: Fetch user by ID and set username
            // For now, just set the ID as string
            task.setAssignee(taskCreateDTO.getAssigneeId().toString());
        }

        Task saved = taskRepository.save(task);
        log.info("Task created from Camunda - ID: {}, Camunda Task ID: {}",
                saved.getId(), saved.getCamundaTaskId());

        return saved;
    }

    /**
     * Update task status from Camunda - used by Camunda listeners
     */
    @Transactional
    public void updateTaskStatusFromCamunda(Long taskId, TaskStatus status) {
        Task task = getTaskById(taskId);
        task.setStatus(status);

        if (status == TaskStatus.COMPLETED) {
            task.setCompletedDate(LocalDateTime.now());
            // Completed by will be set by Camunda assignee
        }

        taskRepository.save(task);
        log.info("Task status updated from Camunda - Task ID: {}, Status: {}",
                taskId, status);
    }

    /**
     * Assign task from Camunda - used by Camunda listeners
     */
    @Transactional
    public void assignTaskFromCamunda(Long taskId, Long assigneeId) {
        Task task = getTaskById(taskId);

        // TODO: Fetch user by ID and get username
        // For now, just set the ID as string
        String assignee = assigneeId.toString();

        task.setAssignee(assignee);
        task.setStatus(TaskStatus.ASSIGNED);

        if (task.getClaimedBy() == null) {
            task.setClaimedBy(assignee);
            task.setClaimedDate(LocalDateTime.now());
        }

        taskRepository.save(task);
        log.info("Task assigned from Camunda - Task ID: {}, Assignee ID: {}",
                taskId, assigneeId);
    }

    /**
     * Map string priority to TaskPriority enum
     */
    private TaskPriority mapStringToPriority(String priority) {
        if (priority == null) {
            return TaskPriority.NORMAL;
        }

        return switch (priority.toUpperCase()) {
            case "URGENT", "CRITICAL" -> TaskPriority.CRITICAL;
            case "HIGH" -> TaskPriority.HIGH;
            case "LOW" -> TaskPriority.LOW;
            default -> TaskPriority.NORMAL;
        };
    }

    /**
     * Get current authenticated username
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }

    /**
     * Check if current user is admin
     */
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;

        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Get localized message
     */
    private String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, code, LocaleContextHolder.getLocale());
    }
}
