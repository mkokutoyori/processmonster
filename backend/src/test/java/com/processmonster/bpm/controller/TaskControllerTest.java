package com.processmonster.bpm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processmonster.bpm.dto.task.CreateCommentDTO;
import com.processmonster.bpm.dto.task.CreateTaskDTO;
import com.processmonster.bpm.dto.task.UpdateTaskDTO;
import com.processmonster.bpm.entity.Task;
import com.processmonster.bpm.entity.Task.TaskPriority;
import com.processmonster.bpm.entity.Task.TaskStatus;
import com.processmonster.bpm.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TaskController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Task Controller Integration Tests")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    private Task testTask;

    @BeforeEach
    void setUp() {
        // Create test task
        testTask = Task.builder()
                .name("Test Task")
                .description("Test Description")
                .status(TaskStatus.CREATED)
                .priority(TaskPriority.NORMAL)
                .dueDate(LocalDateTime.now().plusDays(7))
                .build();
        testTask = taskRepository.save(testTask);
    }

    @Test
    @WithMockUser(authorities = {"TASK_CREATE"})
    @DisplayName("Should create task successfully")
    void shouldCreateTask() throws Exception {
        CreateTaskDTO createDTO = CreateTaskDTO.builder()
                .name("New Task")
                .description("Task description")
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDateTime.now().plusDays(3))
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("New Task"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {"TASK_READ"})
    @DisplayName("Should get all tasks")
    void shouldGetAllTasks() throws Exception {
        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))));
    }

    @Test
    @WithMockUser(authorities = {"TASK_READ"})
    @DisplayName("Should get task by ID")
    void shouldGetTaskById() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/{id}", testTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTask.getId()))
                .andExpect(jsonPath("$.name").value("Test Task"));
    }

    @Test
    @WithMockUser(authorities = {"TASK_READ"})
    @DisplayName("Should return 404 when task not found")
    void shouldReturn404WhenTaskNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"TASK_UPDATE"})
    @DisplayName("Should claim task")
    void shouldClaimTask() throws Exception {
        mockMvc.perform(put("/api/v1/tasks/{id}/claim", testTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTask.getId()))
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.assignee").exists());
    }

    @Test
    @WithMockUser(authorities = {"TASK_UPDATE"})
    @DisplayName("Should assign task to specific user")
    void shouldAssignTask() throws Exception {
        mockMvc.perform(put("/api/v1/tasks/{id}/assign", testTask.getId())
                        .param("assignee", "john.doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignee").value("john.doe"))
                .andExpect(jsonPath("$.status").value("ASSIGNED"));
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"TASK_UPDATE"})
    @DisplayName("Should start claimed task")
    void shouldStartTask() throws Exception {
        // First claim the task
        testTask.setAssignee("testuser");
        testTask.setStatus(TaskStatus.ASSIGNED);
        taskRepository.save(testTask);

        mockMvc.perform(put("/api/v1/tasks/{id}/start", testTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"TASK_UPDATE"})
    @DisplayName("Should complete task")
    void shouldCompleteTask() throws Exception {
        // Setup task as in progress for current user
        testTask.setAssignee("testuser");
        testTask.setStatus(TaskStatus.IN_PROGRESS);
        taskRepository.save(testTask);

        mockMvc.perform(put("/api/v1/tasks/{id}/complete", testTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedBy").value("testuser"))
                .andExpect(jsonPath("$.completedDate").exists());
    }

    @Test
    @WithMockUser(authorities = {"TASK_UPDATE"})
    @DisplayName("Should update task")
    void shouldUpdateTask() throws Exception {
        UpdateTaskDTO updateDTO = UpdateTaskDTO.builder()
                .name("Updated Task Name")
                .priority(TaskPriority.CRITICAL)
                .build();

        mockMvc.perform(put("/api/v1/tasks/{id}", testTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Task Name"))
                .andExpect(jsonPath("$.priority").value("CRITICAL"));
    }

    @Test
    @WithMockUser(authorities = {"TASK_UPDATE"})
    @DisplayName("Should add comment to task")
    void shouldAddComment() throws Exception {
        CreateCommentDTO commentDTO = CreateCommentDTO.builder()
                .content("This is a test comment")
                .build();

        mockMvc.perform(post("/api/v1/tasks/{id}/comments", testTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("This is a test comment"))
                .andExpect(jsonPath("$.taskId").value(testTask.getId()));
    }

    @Test
    @WithMockUser(authorities = {"TASK_READ"})
    @DisplayName("Should get task comments")
    void shouldGetTaskComments() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/{id}/comments", testTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(authorities = {"TASK_DELETE"})
    @DisplayName("Should cancel task")
    void shouldCancelTask() throws Exception {
        mockMvc.perform(put("/api/v1/tasks/{id}/cancel", testTask.getId())
                        .param("reason", "Not needed anymore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser(authorities = {"TASK_DELETE"})
    @DisplayName("Should delete completed task")
    void shouldDeleteCompletedTask() throws Exception {
        // Set task as completed
        testTask.setStatus(TaskStatus.COMPLETED);
        testTask.setCompletedDate(LocalDateTime.now());
        taskRepository.save(testTask);

        mockMvc.perform(delete("/api/v1/tasks/{id}", testTask.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = {"TASK_READ"})
    @DisplayName("Should get my tasks inbox")
    void shouldGetMyTasksInbox() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/inbox"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(authorities = {"TASK_READ"})
    @DisplayName("Should search tasks")
    void shouldSearchTasks() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/search")
                        .param("keyword", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(authorities = {"TASK_READ"})
    @DisplayName("Should get overdue tasks")
    void shouldGetOverdueTasks() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(authorities = {})
    @DisplayName("Should return 403 when insufficient permissions")
    void shouldReturn403WhenInsufficientPermissions() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
