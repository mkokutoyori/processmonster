package com.processmonster.bpm.controller;

import com.processmonster.bpm.dto.task.*;
import com.processmonster.bpm.entity.Task;
import com.processmonster.bpm.entity.Task.TaskStatus;
import com.processmonster.bpm.entity.TaskAttachment;
import com.processmonster.bpm.entity.TaskComment;
import com.processmonster.bpm.mapper.TaskMapper;
import com.processmonster.bpm.service.FileStorageService;
import com.processmonster.bpm.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for task management
 */
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task Management API")
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;
    private final FileStorageService fileStorageService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('TASK_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get all tasks", description = "Retrieve all tasks with pagination")
    public ResponseEntity<Page<TaskDTO>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dueDate,asc") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<TaskDTO> tasks = taskService.getAllTasks(pageable)
                .map(taskMapper::toDTO);

        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/inbox")
    @PreAuthorize("hasAnyAuthority('TASK_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get my tasks inbox", description = "Retrieve tasks assigned to current user")
    public ResponseEntity<Page<TaskDTO>> getMyTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dueDate,asc") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<TaskDTO> tasks = taskService.getMyTasks(pageable)
                .map(taskMapper::toDTO);

        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/inbox/active")
    @PreAuthorize("hasAnyAuthority('TASK_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get my active tasks", description = "Retrieve active tasks (assigned or in progress) for current user")
    public ResponseEntity<Page<TaskDTO>> getMyActiveTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("priority").descending().and(Sort.by("dueDate")));
        Page<TaskDTO> tasks = taskService.getMyActiveTasks(pageable)
                .map(taskMapper::toDTO);

        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/queue/{candidateGroup}")
    @PreAuthorize("hasAnyAuthority('TASK_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get task queue", description = "Retrieve unassigned tasks for a candidate group")
    public ResponseEntity<Page<TaskDTO>> getQueueTasks(
            @PathVariable String candidateGroup,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("priority").descending().and(Sort.by("createdAt")));
        Page<TaskDTO> tasks = taskService.getQueueTasks(candidateGroup, pageable)
                .map(taskMapper::toDTO);

        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('TASK_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get tasks by status", description = "Retrieve tasks filtered by status")
    public ResponseEntity<Page<TaskDTO>> getTasksByStatus(
            @PathVariable TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("dueDate").ascending());
        Page<TaskDTO> tasks = taskService.getTasksByStatus(status, pageable)
                .map(taskMapper::toDTO);

        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyAuthority('TASK_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get overdue tasks", description = "Retrieve all overdue tasks")
    public ResponseEntity<Page<TaskDTO>> getOverdueTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("dueDate").ascending());
        Page<TaskDTO> tasks = taskService.getOverdueTasks(pageable)
                .map(taskMapper::toDTO);

        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/due-soon")
    @PreAuthorize("hasAnyAuthority('TASK_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get tasks due soon", description = "Retrieve tasks due in the next 24 hours")
    public ResponseEntity<Page<TaskDTO>> getTasksDueSoon(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("dueDate").ascending());
        Page<TaskDTO> tasks = taskService.getTasksDueSoon(pageable)
                .map(taskMapper::toDTO);

        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('TASK_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Search tasks", description = "Search tasks by name or description")
    public ResponseEntity<Page<TaskDTO>> searchTasks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("dueDate").ascending());
        Page<TaskDTO> tasks = taskService.searchTasks(keyword, pageable)
                .map(taskMapper::toDTO);

        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TASK_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get task by ID", description = "Retrieve a specific task by its ID")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);
        return ResponseEntity.ok(taskMapper.toDTO(task));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('TASK_CREATE', 'ROLE_ADMIN')")
    @Operation(summary = "Create task", description = "Create a new task")
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody CreateTaskDTO createDTO) {
        Task task = taskMapper.toEntity(createDTO);
        Task created = taskService.createTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskMapper.toDTO(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TASK_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Update task", description = "Update task metadata")
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskDTO updateDTO) {

        Task task = taskService.getTaskById(id);
        taskMapper.updateEntityFromDTO(updateDTO, task);
        Task updated = taskService.updateTask(id, task);
        return ResponseEntity.ok(taskMapper.toDTO(updated));
    }

    @PutMapping("/{id}/claim")
    @PreAuthorize("hasAnyAuthority('TASK_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Claim task", description = "Claim task for current user")
    public ResponseEntity<TaskDTO> claimTask(@PathVariable Long id) {
        Task claimed = taskService.claimTask(id);
        return ResponseEntity.ok(taskMapper.toDTO(claimed));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyAuthority('TASK_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Assign task", description = "Assign task to a specific user")
    public ResponseEntity<TaskDTO> assignTask(
            @PathVariable Long id,
            @RequestParam String assignee) {

        Task assigned = taskService.assignTask(id, assignee);
        return ResponseEntity.ok(taskMapper.toDTO(assigned));
    }

    @PutMapping("/{id}/start")
    @PreAuthorize("hasAnyAuthority('TASK_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Start task", description = "Mark task as in progress")
    public ResponseEntity<TaskDTO> startTask(@PathVariable Long id) {
        Task started = taskService.startTask(id);
        return ResponseEntity.ok(taskMapper.toDTO(started));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyAuthority('TASK_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Complete task", description = "Complete task with optional form data")
    public ResponseEntity<TaskDTO> completeTask(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> formData) {

        Task completed = taskService.completeTask(id, formData);
        return ResponseEntity.ok(taskMapper.toDTO(completed));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('TASK_DELETE', 'ROLE_ADMIN')")
    @Operation(summary = "Cancel task", description = "Cancel a task")
    public ResponseEntity<TaskDTO> cancelTask(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {

        Task cancelled = taskService.cancelTask(id, reason);
        return ResponseEntity.ok(taskMapper.toDTO(cancelled));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TASK_DELETE', 'ROLE_ADMIN')")
    @Operation(summary = "Delete task", description = "Delete a task (soft delete)")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    // Comments

    @GetMapping("/{taskId}/comments")
    @PreAuthorize("hasAnyAuthority('TASK_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get task comments", description = "Retrieve all comments for a task")
    public ResponseEntity<List<TaskCommentDTO>> getTaskComments(@PathVariable Long taskId) {
        List<TaskCommentDTO> comments = taskService.getTaskComments(taskId)
                .stream()
                .map(taskMapper::toCommentDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(comments);
    }

    @PostMapping("/{taskId}/comments")
    @PreAuthorize("hasAnyAuthority('TASK_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Add comment", description = "Add a comment to a task")
    public ResponseEntity<TaskCommentDTO> addComment(
            @PathVariable Long taskId,
            @Valid @RequestBody CreateCommentDTO commentDTO) {

        TaskComment comment = taskService.addComment(taskId, commentDTO.getContent(), commentDTO.getType());
        return ResponseEntity.status(HttpStatus.CREATED).body(taskMapper.toCommentDTO(comment));
    }

    // Attachments

    @GetMapping("/{taskId}/attachments")
    @PreAuthorize("hasAnyAuthority('TASK_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get task attachments", description = "Retrieve all attachments for a task")
    public ResponseEntity<List<TaskAttachmentDTO>> getTaskAttachments(@PathVariable Long taskId) {
        List<TaskAttachmentDTO> attachments = taskService.getTaskAttachments(taskId)
                .stream()
                .map(taskMapper::toAttachmentDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(attachments);
    }

    @PostMapping(value = "/{taskId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('TASK_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Add attachment", description = "Upload a file attachment to a task")
    public ResponseEntity<TaskAttachmentDTO> addAttachment(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String description) {

        TaskAttachment attachment = taskService.addAttachment(taskId, file, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskMapper.toAttachmentDTO(attachment));
    }

    @GetMapping("/attachments/{id}/download")
    @PreAuthorize("hasAnyAuthority('TASK_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Download attachment", description = "Download a task attachment file")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long id) {
        // Implementation would retrieve attachment metadata and return file
        // TODO: Implement file download
        throw new UnsupportedOperationException("Download endpoint not yet implemented");
    }

    @DeleteMapping("/attachments/{id}")
    @PreAuthorize("hasAnyAuthority('TASK_DELETE', 'ROLE_ADMIN')")
    @Operation(summary = "Delete attachment", description = "Delete a task attachment")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long id) {
        taskService.deleteAttachment(id);
        return ResponseEntity.noContent().build();
    }

    // Statistics

    @GetMapping("/stats/my-active-count")
    @PreAuthorize("hasAnyAuthority('TASK_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Count my active tasks", description = "Get count of active tasks for current user")
    public ResponseEntity<Long> countMyActiveTasks() {
        return ResponseEntity.ok(taskService.countMyActiveTasks());
    }

    @GetMapping("/stats/overdue-count")
    @PreAuthorize("hasAnyAuthority('TASK_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Count overdue tasks", description = "Get count of all overdue tasks")
    public ResponseEntity<Long> countOverdueTasks() {
        return ResponseEntity.ok(taskService.countOverdueTasks());
    }

    // Helper methods

    private Pageable createPageable(int page, int size, String sort) {
        String[] sortParams = sort.split(",");
        String property = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return PageRequest.of(page, size, Sort.by(direction, property));
    }
}
