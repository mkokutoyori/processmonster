package com.processmonster.bpm.service;

import com.processmonster.bpm.entity.Task;
import com.processmonster.bpm.entity.Task.TaskPriority;
import com.processmonster.bpm.entity.Task.TaskStatus;
import com.processmonster.bpm.entity.TaskComment;
import com.processmonster.bpm.exception.BusinessException;
import com.processmonster.bpm.exception.ResourceNotFoundException;
import com.processmonster.bpm.repository.TaskAttachmentRepository;
import com.processmonster.bpm.repository.TaskCommentRepository;
import com.processmonster.bpm.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Task Service Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskCommentRepository commentRepository;

    @Mock
    private TaskAttachmentRepository attachmentRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(
                taskRepository,
                commentRepository,
                attachmentRepository,
                notificationService,
                fileStorageService,
                messageSource
        );

        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);

        when(messageSource.getMessage(anyString(), any(), any(), any()))
                .thenReturn("Test message");
    }

    @Test
    @DisplayName("Should create task successfully")
    void shouldCreateTask() {
        // Given
        Task task = Task.builder()
                .name("Review Loan Application")
                .description("Review loan application for John Doe")
                .build();

        Task savedTask = Task.builder()
                .id(1L)
                .name("Review Loan Application")
                .status(TaskStatus.CREATED)
                .priority(TaskPriority.NORMAL)
                .build();

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        // When
        Task result = taskService.createTask(task);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TaskStatus.CREATED);
        assertThat(result.getPriority()).isEqualTo(TaskPriority.NORMAL);

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("Should claim unassigned task")
    void shouldClaimTask() {
        // Given
        Task task = Task.builder()
                .id(1L)
                .name("Test Task")
                .status(TaskStatus.CREATED)
                .build();

        when(taskRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // When
        Task result = taskService.claimTask(1L);

        // Then
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());

        Task savedTask = taskCaptor.getValue();
        assertThat(savedTask.getAssignee()).isEqualTo("testuser");
        assertThat(savedTask.getStatus()).isEqualTo(TaskStatus.ASSIGNED);
        assertThat(savedTask.getClaimedBy()).isEqualTo("testuser");
        assertThat(savedTask.getClaimedDate()).isNotNull();

        verify(notificationService).sendTaskAssigned(any(Task.class), eq("testuser"));
    }

    @Test
    @DisplayName("Should throw exception when claiming already claimed task")
    void shouldThrowExceptionWhenClaimingAlreadyClaimedTask() {
        // Given
        Task task = Task.builder()
                .id(1L)
                .name("Test Task")
                .status(TaskStatus.ASSIGNED)
                .assignee("anotheruser")
                .build();

        when(taskRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(task));

        // When / Then
        assertThatThrownBy(() -> taskService.claimTask(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Should assign task to specific user")
    void shouldAssignTask() {
        // Given
        Task task = Task.builder()
                .id(1L)
                .name("Test Task")
                .status(TaskStatus.CREATED)
                .build();

        when(taskRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // When
        Task result = taskService.assignTask(1L, "john.doe");

        // Then
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());

        Task savedTask = taskCaptor.getValue();
        assertThat(savedTask.getAssignee()).isEqualTo("john.doe");
        assertThat(savedTask.getStatus()).isEqualTo(TaskStatus.ASSIGNED);

        verify(notificationService).sendTaskAssigned(any(Task.class), eq("john.doe"));
    }

    @Test
    @DisplayName("Should start assigned task")
    void shouldStartTask() {
        // Given
        Task task = Task.builder()
                .id(1L)
                .name("Test Task")
                .status(TaskStatus.ASSIGNED)
                .assignee("testuser")
                .build();

        when(taskRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // When
        Task result = taskService.startTask(1L);

        // Then
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());

        Task savedTask = taskCaptor.getValue();
        assertThat(savedTask.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Should complete task")
    void shouldCompleteTask() {
        // Given
        Task task = Task.builder()
                .id(1L)
                .name("Test Task")
                .status(TaskStatus.IN_PROGRESS)
                .assignee("testuser")
                .build();

        when(taskRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // When
        Task result = taskService.completeTask(1L, null);

        // Then
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());

        Task savedTask = taskCaptor.getValue();
        assertThat(savedTask.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(savedTask.getCompletedBy()).isEqualTo("testuser");
        assertThat(savedTask.getCompletedDate()).isNotNull();

        verify(notificationService).sendTaskCompleted(any(Task.class));
    }

    @Test
    @DisplayName("Should throw exception when completing task not assigned to user")
    void shouldThrowExceptionWhenCompletingTaskNotAssignedToUser() {
        // Given
        Task task = Task.builder()
                .id(1L)
                .name("Test Task")
                .status(TaskStatus.IN_PROGRESS)
                .assignee("anotheruser")
                .build();

        when(taskRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(task));

        // When / Then
        assertThatThrownBy(() -> taskService.completeTask(1L, null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Should add comment to task")
    void shouldAddComment() {
        // Given
        Task task = Task.builder()
                .id(1L)
                .name("Test Task")
                .build();

        TaskComment savedComment = TaskComment.builder()
                .id(1L)
                .task(task)
                .content("This is a comment")
                .type(TaskComment.CommentType.GENERAL)
                .build();

        when(taskRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(task));
        when(commentRepository.save(any(TaskComment.class))).thenReturn(savedComment);

        // When
        TaskComment result = taskService.addComment(1L, "This is a comment", TaskComment.CommentType.GENERAL);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("This is a comment");

        verify(commentRepository).save(any(TaskComment.class));
        verify(notificationService).sendCommentAdded(any(Task.class), eq("testuser"), anyString());
    }

    @Test
    @DisplayName("Should get my tasks")
    void shouldGetMyTasks() {
        // Given
        Task task1 = Task.builder().id(1L).name("Task 1").assignee("testuser").build();
        Task task2 = Task.builder().id(2L).name("Task 2").assignee("testuser").build();

        Page<Task> taskPage = new PageImpl<>(List.of(task1, task2));
        Pageable pageable = PageRequest.of(0, 10);

        when(taskRepository.findByAssigneeAndDeletedFalse("testuser", pageable)).thenReturn(taskPage);

        // When
        Page<Task> result = taskService.getMyTasks(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Should get overdue tasks")
    void shouldGetOverdueTasks() {
        // Given
        Task overdueTask = Task.builder()
                .id(1L)
                .name("Overdue Task")
                .dueDate(LocalDateTime.now().minusDays(1))
                .status(TaskStatus.IN_PROGRESS)
                .build();

        Page<Task> taskPage = new PageImpl<>(List.of(overdueTask));
        Pageable pageable = PageRequest.of(0, 10);

        when(taskRepository.findOverdueTasks(any(LocalDateTime.class), eq(pageable))).thenReturn(taskPage);

        // When
        Page<Task> result = taskService.getOverdueTasks(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should throw exception when task not found")
    void shouldThrowExceptionWhenTaskNotFound() {
        // Given
        when(taskRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> taskService.getTaskById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
