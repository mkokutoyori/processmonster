package com.processmonster.bpm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processmonster.bpm.dto.instance.StartProcessInstanceDTO;
import com.processmonster.bpm.entity.ProcessDefinition;
import com.processmonster.bpm.entity.ProcessInstance;
import com.processmonster.bpm.repository.ProcessDefinitionRepository;
import com.processmonster.bpm.repository.ProcessInstanceRepository;
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
import java.util.Map;

import static com.processmonster.bpm.entity.ProcessInstance.ProcessInstanceStatus.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProcessInstanceController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Process Instance Controller Integration Tests")
class ProcessInstanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcessDefinitionRepository definitionRepository;

    @Autowired
    private ProcessInstanceRepository instanceRepository;

    private ProcessDefinition testDefinition;
    private ProcessInstance testInstance;

    private static final String VALID_BPMN_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                         targetNamespace="http://processmonster.com/bpmn">
              <process id="test-process" name="Test Process" isExecutable="true">
                <startEvent id="start"/>
                <endEvent id="end"/>
              </process>
            </definitions>
            """;

    @BeforeEach
    void setUp() {
        // Create test process definition
        testDefinition = new ProcessDefinition();
        testDefinition.setProcessKey("test-process");
        testDefinition.setName("Test Process");
        testDefinition.setVersion(1);
        testDefinition.setIsLatestVersion(true);
        testDefinition.setPublished(true);
        testDefinition.setActive(true);
        testDefinition.setBpmnXml(VALID_BPMN_XML);
        testDefinition = definitionRepository.save(testDefinition);

        // Create test instance
        testInstance = new ProcessInstance();
        testInstance.setProcessDefinition(testDefinition);
        testInstance.setStatus(RUNNING);
        testInstance.setStartTime(LocalDateTime.now());
        testInstance.setStartedBy("testuser");
        testInstance.setBusinessKey("TEST-001");
        testInstance = instanceRepository.save(testInstance);
    }

    @Test
    @WithMockUser(authorities = {"INSTANCE_CREATE"})
    @DisplayName("Should start process instance successfully")
    void startProcess_ShouldReturnCreated_WhenValidInput() throws Exception {
        // Given
        StartProcessInstanceDTO startDTO = StartProcessInstanceDTO.builder()
                .processDefinitionId(testDefinition.getId())
                .businessKey("LOAN-12345")
                .variables(Map.of("amount", 10000, "applicant", "John Doe"))
                .build();

        // When / Then
        mockMvc.perform(post("/api/v1/instances/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("RUNNING"))
                .andExpect(jsonPath("$.businessKey").value("LOAN-12345"))
                .andExpect(jsonPath("$.processDefinitionName").value("Test Process"));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void startProcess_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        StartProcessInstanceDTO startDTO = StartProcessInstanceDTO.builder()
                .processDefinitionId(testDefinition.getId())
                .build();

        mockMvc.perform(post("/api/v1/instances/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {"INSTANCE_CREATE"})
    @DisplayName("Should return 400 when process definition ID is null")
    void startProcess_ShouldReturn400_WhenDefinitionIdNull() throws Exception {
        StartProcessInstanceDTO startDTO = StartProcessInstanceDTO.builder()
                .processDefinitionId(null)
                .build();

        mockMvc.perform(post("/api/v1/instances/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"INSTANCE_READ"})
    @DisplayName("Should get all instances with pagination")
    void getAllInstances_ShouldReturnPagedResults() throws Exception {
        mockMvc.perform(get("/api/v1/instances")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.pageable").exists())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    @Test
    @WithMockUser(authorities = {"INSTANCE_READ"})
    @DisplayName("Should get instance by ID")
    void getInstanceById_ShouldReturnInstance_WhenExists() throws Exception {
        mockMvc.perform(get("/api/v1/instances/{id}", testInstance.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testInstance.getId()))
                .andExpect(jsonPath("$.status").value("RUNNING"))
                .andExpect(jsonPath("$.businessKey").value("TEST-001"))
                .andExpect(jsonPath("$.processKey").value("test-process"));
    }

    @Test
    @WithMockUser(authorities = {"INSTANCE_READ"})
    @DisplayName("Should return 404 when instance not found")
    void getInstanceById_ShouldReturn404_WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/instances/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"INSTANCE_READ"})
    @DisplayName("Should get instances by status")
    void getInstancesByStatus_ShouldReturnFilteredResults() throws Exception {
        mockMvc.perform(get("/api/v1/instances/status/{status}", "RUNNING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status").value("RUNNING"));
    }

    @Test
    @WithMockUser(authorities = {"INSTANCE_READ"})
    @DisplayName("Should get active instances only")
    void getActiveInstances_ShouldReturnOnlyActiveInstances() throws Exception {
        mockMvc.perform(get("/api/v1/instances/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(authorities = {"INSTANCE_UPDATE"})
    @DisplayName("Should suspend running instance")
    void suspendInstance_ShouldReturnSuspendedInstance() throws Exception {
        mockMvc.perform(put("/api/v1/instances/{id}/suspend", testInstance.getId())
                        .param("reason", "Pending review"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testInstance.getId()))
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }

    @Test
    @WithMockUser(authorities = {"INSTANCE_UPDATE"})
    @DisplayName("Should resume suspended instance")
    void resumeInstance_ShouldReturnRunningInstance() throws Exception {
        // First suspend the instance
        testInstance.setStatus(SUSPENDED);
        instanceRepository.save(testInstance);

        // Then resume it
        mockMvc.perform(put("/api/v1/instances/{id}/resume", testInstance.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testInstance.getId()))
                .andExpect(jsonPath("$.status").value("RUNNING"));
    }

    @Test
    @WithMockUser(authorities = {"INSTANCE_DELETE"})
    @DisplayName("Should terminate instance")
    void terminateInstance_ShouldReturnTerminatedInstance() throws Exception {
        mockMvc.perform(put("/api/v1/instances/{id}/terminate", testInstance.getId())
                        .param("reason", "Business requirements changed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testInstance.getId()))
                .andExpect(jsonPath("$.status").value("TERMINATED"));
    }

    @Test
    @WithMockUser(authorities = {"INSTANCE_READ"})
    @DisplayName("Should get instance variables")
    void getVariables_ShouldReturnVariablesMap() throws Exception {
        mockMvc.perform(get("/api/v1/instances/{id}/variables", testInstance.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap());
    }

    @Test
    @WithMockUser(authorities = {"INSTANCE_UPDATE"})
    @DisplayName("Should set instance variables")
    void setVariables_ShouldReturn200() throws Exception {
        Map<String, Object> variables = Map.of(
                "amount", 15000,
                "status", "approved"
        );

        mockMvc.perform(put("/api/v1/instances/{id}/variables", testInstance.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(variables)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"INSTANCE_READ"})
    @DisplayName("Should get instance execution history")
    void getInstanceHistory_ShouldReturnHistory() throws Exception {
        mockMvc.perform(get("/api/v1/instances/{id}/history", testInstance.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(authorities = {"INSTANCE_DELETE"})
    @DisplayName("Should delete completed instance")
    void deleteInstance_ShouldReturn204_WhenInstanceCompleted() throws Exception {
        // Set instance to completed
        testInstance.setStatus(COMPLETED);
        testInstance.setEndTime(LocalDateTime.now());
        instanceRepository.save(testInstance);

        mockMvc.perform(delete("/api/v1/instances/{id}", testInstance.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = {"INSTANCE_DELETE"})
    @DisplayName("Should not delete active instance")
    void deleteInstance_ShouldReturn400_WhenInstanceActive() throws Exception {
        // Instance is RUNNING (active)
        mockMvc.perform(delete("/api/v1/instances/{id}", testInstance.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"INSTANCE_READ"})
    @DisplayName("Should respect pagination parameters")
    void getAllInstances_ShouldRespectPaginationParams() throws Exception {
        mockMvc.perform(get("/api/v1/instances")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "startTime,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @WithMockUser(authorities = {})
    @DisplayName("Should return 403 when insufficient permissions")
    void startProcess_ShouldReturn403_WhenInsufficientPermissions() throws Exception {
        StartProcessInstanceDTO startDTO = StartProcessInstanceDTO.builder()
                .processDefinitionId(testDefinition.getId())
                .build();

        mockMvc.perform(post("/api/v1/instances/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"INSTANCE_UPDATE"})
    @DisplayName("Should return 400 when suspending non-running instance")
    void suspendInstance_ShouldReturn400_WhenNotRunning() throws Exception {
        // Set instance to completed
        testInstance.setStatus(COMPLETED);
        instanceRepository.save(testInstance);

        mockMvc.perform(put("/api/v1/instances/{id}/suspend", testInstance.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"INSTANCE_UPDATE"})
    @DisplayName("Should return 400 when resuming non-suspended instance")
    void resumeInstance_ShouldReturn400_WhenNotSuspended() throws Exception {
        // Instance is RUNNING (not suspended)
        mockMvc.perform(put("/api/v1/instances/{id}/resume", testInstance.getId()))
                .andExpect(status().isBadRequest());
    }
}
