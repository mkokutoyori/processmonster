package com.processmonster.bpm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processmonster.bpm.dto.process.CreateProcessCategoryDTO;
import com.processmonster.bpm.dto.process.UpdateProcessCategoryDTO;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProcessCategoryController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Process Category Controller Integration Tests")
class ProcessCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = {"PROCESS_READ"})
    @DisplayName("Should get all categories when authenticated")
    void getAllCategories_ShouldReturnCategories_WhenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/process-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable").exists());
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void getAllCategories_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/process-categories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {"PROCESS_CREATE", "ROLE_ADMIN"})
    @DisplayName("Should create category successfully")
    void createCategory_ShouldReturnCreated_WhenValidInput() throws Exception {
        // Given
        CreateProcessCategoryDTO createDTO = CreateProcessCategoryDTO.builder()
                .code("TEST-CATEGORY")
                .name("Test Category")
                .description("Test description")
                .icon("category")
                .color("#FF5733")
                .displayOrder(1)
                .active(true)
                .build();

        // When/Then
        mockMvc.perform(post("/api/v1/process-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("TEST-CATEGORY"))
                .andExpect(jsonPath("$.name").value("Test Category"))
                .andExpect(jsonPath("$.description").value("Test description"))
                .andExpect(jsonPath("$.icon").value("category"))
                .andExpect(jsonPath("$.color").value("#FF5733"))
                .andExpect(jsonPath("$.displayOrder").value(1))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @WithMockUser(authorities = {"PROCESS_CREATE"})
    @DisplayName("Should return 400 when creating category with invalid code")
    void createCategory_ShouldReturn400_WhenInvalidCode() throws Exception {
        // Given - code with lowercase letters (should be uppercase only)
        CreateProcessCategoryDTO createDTO = CreateProcessCategoryDTO.builder()
                .code("invalid-code")
                .name("Test Category")
                .build();

        // When/Then
        mockMvc.perform(post("/api/v1/process-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"PROCESS_CREATE"})
    @DisplayName("Should return 400 when creating category with invalid color")
    void createCategory_ShouldReturn400_WhenInvalidColor() throws Exception {
        // Given - invalid hex color format
        CreateProcessCategoryDTO createDTO = CreateProcessCategoryDTO.builder()
                .code("TEST-CAT")
                .name("Test Category")
                .color("invalid-color")
                .build();

        // When/Then
        mockMvc.perform(post("/api/v1/process-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"PROCESS_UPDATE"})
    @DisplayName("Should update category successfully")
    void updateCategory_ShouldReturnOk_WhenValidInput() throws Exception {
        // Given - first create a category
        CreateProcessCategoryDTO createDTO = CreateProcessCategoryDTO.builder()
                .code("UPDATE-TEST")
                .name("Original Name")
                .build();

        String createResponse = mockMvc.perform(post("/api/v1/process-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long categoryId = objectMapper.readTree(createResponse).get("id").asLong();

        // When - update the category
        UpdateProcessCategoryDTO updateDTO = UpdateProcessCategoryDTO.builder()
                .name("Updated Name")
                .description("Updated description")
                .build();

        // Then
        mockMvc.perform(put("/api/v1/process-categories/" + categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.code").value("UPDATE-TEST"))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    @WithMockUser(authorities = {"PROCESS_READ"})
    @DisplayName("Should get category by ID")
    void getCategoryById_ShouldReturnCategory_WhenExists() throws Exception {
        // Given - create a category first
        CreateProcessCategoryDTO createDTO = CreateProcessCategoryDTO.builder()
                .code("GET-TEST")
                .name("Get Test Category")
                .build();

        String createResponse = mockMvc.perform(post("/api/v1/process-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long categoryId = objectMapper.readTree(createResponse).get("id").asLong();

        // When/Then
        mockMvc.perform(get("/api/v1/process-categories/" + categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.code").value("GET-TEST"))
                .andExpect(jsonPath("$.name").value("Get Test Category"));
    }

    @Test
    @WithMockUser(authorities = {"PROCESS_READ"})
    @DisplayName("Should return 404 when category not found")
    void getCategoryById_ShouldReturn404_WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/process-categories/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"PROCESS_READ"})
    @DisplayName("Should get active categories")
    void getAllActiveCategories_ShouldReturnActiveOnly() throws Exception {
        // Given - create both active and inactive categories
        CreateProcessCategoryDTO activeCategory = CreateProcessCategoryDTO.builder()
                .code("ACTIVE-CAT")
                .name("Active Category")
                .active(true)
                .build();

        CreateProcessCategoryDTO inactiveCategory = CreateProcessCategoryDTO.builder()
                .code("INACTIVE-CAT")
                .name("Inactive Category")
                .active(false)
                .build();

        mockMvc.perform(post("/api/v1/process-categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activeCategory)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/process-categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inactiveCategory)))
                .andExpect(status().isCreated());

        // When/Then - get active categories should only return active ones
        mockMvc.perform(get("/api/v1/process-categories/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.code=='ACTIVE-CAT')]").exists())
                .andExpect(jsonPath("$[*].active").value(everyItem(is(true))));
    }

    @Test
    @WithMockUser(authorities = {"PROCESS_READ"})
    @DisplayName("Should search categories by keyword")
    void searchCategories_ShouldReturnMatches() throws Exception {
        // Given - create categories with searchable names
        CreateProcessCategoryDTO category1 = CreateProcessCategoryDTO.builder()
                .code("SEARCH-1")
                .name("Banking Operations")
                .build();

        CreateProcessCategoryDTO category2 = CreateProcessCategoryDTO.builder()
                .code("SEARCH-2")
                .name("Customer Service")
                .build();

        mockMvc.perform(post("/api/v1/process-categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/process-categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category2)))
                .andExpect(status().isCreated());

        // When/Then - search for "Banking"
        mockMvc.perform(get("/api/v1/process-categories/search")
                        .param("keyword", "Banking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.name=='Banking Operations')]").exists());
    }

    @Test
    @WithMockUser(authorities = {"PROCESS_DELETE"})
    @DisplayName("Should delete category successfully")
    void deleteCategory_ShouldReturn204_WhenSuccess() throws Exception {
        // Given - create a category
        CreateProcessCategoryDTO createDTO = CreateProcessCategoryDTO.builder()
                .code("DELETE-TEST")
                .name("Delete Test Category")
                .build();

        String createResponse = mockMvc.perform(post("/api/v1/process-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long categoryId = objectMapper.readTree(createResponse).get("id").asLong();

        // When - delete the category
        mockMvc.perform(delete("/api/v1/process-categories/" + categoryId))
                .andExpect(status().isNoContent());

        // Then - verify it's deleted (soft delete)
        mockMvc.perform(get("/api/v1/process-categories/" + categoryId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"PROCESS_UPDATE"})
    @DisplayName("Should activate category successfully")
    void activateCategory_ShouldReturnOk() throws Exception {
        // Given - create an inactive category
        CreateProcessCategoryDTO createDTO = CreateProcessCategoryDTO.builder()
                .code("ACTIVATE-TEST")
                .name("Activate Test")
                .active(false)
                .build();

        String createResponse = mockMvc.perform(post("/api/v1/process-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long categoryId = objectMapper.readTree(createResponse).get("id").asLong();

        // When/Then - activate the category
        mockMvc.perform(put("/api/v1/process-categories/" + categoryId + "/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser(authorities = {"PROCESS_UPDATE"})
    @DisplayName("Should deactivate category successfully")
    void deactivateCategory_ShouldReturnOk() throws Exception {
        // Given - create an active category
        CreateProcessCategoryDTO createDTO = CreateProcessCategoryDTO.builder()
                .code("DEACTIVATE-TEST")
                .name("Deactivate Test")
                .active(true)
                .build();

        String createResponse = mockMvc.perform(post("/api/v1/process-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long categoryId = objectMapper.readTree(createResponse).get("id").asLong();

        // When/Then - deactivate the category
        mockMvc.perform(put("/api/v1/process-categories/" + categoryId + "/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }
}
