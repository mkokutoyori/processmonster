package com.processmonster.bpm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processmonster.bpm.dto.form.CreateFormDefinitionDTO;
import com.processmonster.bpm.dto.form.SaveDraftDTO;
import com.processmonster.bpm.dto.form.SubmitFormDTO;
import com.processmonster.bpm.dto.form.UpdateFormDefinitionDTO;
import com.processmonster.bpm.entity.FormDefinition;
import com.processmonster.bpm.entity.FormSubmission;
import com.processmonster.bpm.entity.SubmissionStatus;
import com.processmonster.bpm.repository.FormDefinitionRepository;
import com.processmonster.bpm.repository.FormSubmissionRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for FormController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Form Controller Integration Tests")
class FormControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FormDefinitionRepository formDefinitionRepository;

    @Autowired
    private FormSubmissionRepository formSubmissionRepository;

    private FormDefinition testFormDefinition;
    private FormSubmission testSubmission;

    private static final String VALID_SCHEMA = """
            {
                "type": "object",
                "properties": {
                    "firstName": {"type": "string", "minLength": 1},
                    "lastName": {"type": "string", "minLength": 1},
                    "email": {"type": "string", "format": "email"},
                    "amount": {"type": "number", "minimum": 0}
                },
                "required": ["firstName", "lastName", "email"]
            }
            """;

    private static final String VALID_DATA = """
            {
                "firstName": "John",
                "lastName": "Doe",
                "email": "john.doe@example.com",
                "amount": 1000
            }
            """;

    @BeforeEach
    void setUp() {
        // Create test form definition
        testFormDefinition = FormDefinition.builder()
                .formKey("test-loan-application")
                .name("Test Loan Application Form")
                .description("Test form for loan applications")
                .category("LOAN")
                .version(1)
                .schemaJson(VALID_SCHEMA)
                .published(true)
                .isLatestVersion(true)
                .build();
        testFormDefinition = formDefinitionRepository.save(testFormDefinition);

        // Create test submission
        testSubmission = FormSubmission.builder()
                .formDefinition(testFormDefinition)
                .dataJson(VALID_DATA)
                .status(SubmissionStatus.DRAFT)
                .submittedBy("testuser")
                .businessKey("LOAN-TEST-001")
                .build();
        testSubmission = formSubmissionRepository.save(testSubmission);
    }

    // ========== FormDefinition Tests ==========

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/forms/definitions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {"FORM_READ"})
    @DisplayName("Should get all form definitions")
    void shouldGetAllFormDefinitions() throws Exception {
        mockMvc.perform(get("/api/v1/forms/definitions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))));
    }

    @Test
    @WithMockUser(authorities = {"FORM_READ"})
    @DisplayName("Should get form definition by ID")
    void shouldGetFormDefinitionById() throws Exception {
        mockMvc.perform(get("/api/v1/forms/definitions/" + testFormDefinition.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testFormDefinition.getId()))
                .andExpect(jsonPath("$.formKey").value("test-loan-application"))
                .andExpect(jsonPath("$.name").value("Test Loan Application Form"));
    }

    @Test
    @WithMockUser(authorities = {"FORM_READ"})
    @DisplayName("Should get form definition by key")
    void shouldGetFormDefinitionByKey() throws Exception {
        mockMvc.perform(get("/api/v1/forms/definitions/key/test-loan-application"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formKey").value("test-loan-application"))
                .andExpect(jsonPath("$.isLatestVersion").value(true));
    }

    @Test
    @WithMockUser(authorities = {"FORM_READ"})
    @DisplayName("Should get published forms")
    void shouldGetPublishedForms() throws Exception {
        mockMvc.perform(get("/api/v1/forms/definitions/published"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].published").value(true));
    }

    @Test
    @WithMockUser(authorities = {"FORM_CREATE"})
    @DisplayName("Should create form definition successfully")
    void shouldCreateFormDefinition() throws Exception {
        CreateFormDefinitionDTO createDTO = CreateFormDefinitionDTO.builder()
                .formKey("account-opening")
                .name("Account Opening Form")
                .description("Form for opening new accounts")
                .category("ACCOUNT")
                .schemaJson(VALID_SCHEMA)
                .build();

        mockMvc.perform(post("/api/v1/forms/definitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.formKey").value("account-opening"))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.isLatestVersion").value(true));
    }

    @Test
    @WithMockUser(authorities = {"FORM_CREATE"})
    @DisplayName("Should return 400 when creating form with invalid schema")
    void shouldReturn400WhenCreatingFormWithInvalidSchema() throws Exception {
        CreateFormDefinitionDTO createDTO = CreateFormDefinitionDTO.builder()
                .formKey("invalid-form")
                .name("Invalid Form")
                .schemaJson("{invalid json}")
                .build();

        mockMvc.perform(post("/api/v1/forms/definitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"FORM_UPDATE"})
    @DisplayName("Should update form definition successfully")
    void shouldUpdateFormDefinition() throws Exception {
        UpdateFormDefinitionDTO updateDTO = UpdateFormDefinitionDTO.builder()
                .name("Updated Loan Application Form")
                .description("Updated description")
                .category("LOAN")
                .schemaJson(VALID_SCHEMA)
                .build();

        mockMvc.perform(put("/api/v1/forms/definitions/" + testFormDefinition.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Loan Application Form"));
    }

    @Test
    @WithMockUser(authorities = {"FORM_UPDATE"})
    @DisplayName("Should publish form definition")
    void shouldPublishFormDefinition() throws Exception {
        // First create an unpublished form
        FormDefinition unpublishedForm = FormDefinition.builder()
                .formKey("unpublished-form")
                .name("Unpublished Form")
                .schemaJson(VALID_SCHEMA)
                .version(1)
                .published(false)
                .isLatestVersion(true)
                .build();
        unpublishedForm = formDefinitionRepository.save(unpublishedForm);

        mockMvc.perform(put("/api/v1/forms/definitions/" + unpublishedForm.getId() + "/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.published").value(true));
    }

    @Test
    @WithMockUser(authorities = {"FORM_UPDATE"})
    @DisplayName("Should unpublish form definition")
    void shouldUnpublishFormDefinition() throws Exception {
        mockMvc.perform(put("/api/v1/forms/definitions/" + testFormDefinition.getId() + "/unpublish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.published").value(false));
    }

    @Test
    @WithMockUser(authorities = {"FORM_DELETE"})
    @DisplayName("Should delete form definition successfully")
    void shouldDeleteFormDefinition() throws Exception {
        // Create a form with no submissions
        FormDefinition formToDelete = FormDefinition.builder()
                .formKey("delete-me")
                .name("Delete Me")
                .schemaJson(VALID_SCHEMA)
                .version(1)
                .published(false)
                .isLatestVersion(true)
                .build();
        formToDelete = formDefinitionRepository.save(formToDelete);

        mockMvc.perform(delete("/api/v1/forms/definitions/" + formToDelete.getId()))
                .andExpect(status().isNoContent());
    }

    // ========== FormSubmission Tests ==========

    @Test
    @WithMockUser(authorities = {"FORM_READ"})
    @DisplayName("Should get all submissions")
    void shouldGetAllSubmissions() throws Exception {
        mockMvc.perform(get("/api/v1/forms/submissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))));
    }

    @Test
    @WithMockUser(authorities = {"FORM_READ"})
    @DisplayName("Should get submission by ID")
    void shouldGetSubmissionById() throws Exception {
        mockMvc.perform(get("/api/v1/forms/submissions/" + testSubmission.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testSubmission.getId()))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.businessKey").value("LOAN-TEST-001"));
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"FORM_READ"})
    @DisplayName("Should get current user's submissions")
    void shouldGetMySubmissions() throws Exception {
        mockMvc.perform(get("/api/v1/forms/submissions/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"FORM_READ"})
    @DisplayName("Should get current user's drafts")
    void shouldGetMyDrafts() throws Exception {
        mockMvc.perform(get("/api/v1/forms/submissions/my/drafts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status").value("DRAFT"));
    }

    @Test
    @WithMockUser(authorities = {"FORM_READ"})
    @DisplayName("Should get submissions by form definition")
    void shouldGetSubmissionsByForm() throws Exception {
        mockMvc.perform(get("/api/v1/forms/submissions/definition/" + testFormDefinition.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(authorities = {"FORM_UPDATE"})
    @DisplayName("Should get pending submissions")
    void shouldGetPendingSubmissions() throws Exception {
        // Create a submitted (pending) submission
        FormSubmission pendingSubmission = FormSubmission.builder()
                .formDefinition(testFormDefinition)
                .dataJson(VALID_DATA)
                .status(SubmissionStatus.SUBMITTED)
                .submittedBy("user2")
                .businessKey("LOAN-PENDING-001")
                .build();
        formSubmissionRepository.save(pendingSubmission);

        mockMvc.perform(get("/api/v1/forms/submissions/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(authorities = {"FORM_CREATE"})
    @DisplayName("Should save draft successfully")
    void shouldSaveDraft() throws Exception {
        SaveDraftDTO draftDTO = SaveDraftDTO.builder()
                .formDefinitionId(testFormDefinition.getId())
                .dataJson(VALID_DATA)
                .businessKey("LOAN-DRAFT-002")
                .build();

        mockMvc.perform(post("/api/v1/forms/submissions/save-draft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(draftDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.businessKey").value("LOAN-DRAFT-002"));
    }

    @Test
    @WithMockUser(authorities = {"FORM_CREATE"})
    @DisplayName("Should submit form successfully")
    void shouldSubmitForm() throws Exception {
        SubmitFormDTO submitDTO = SubmitFormDTO.builder()
                .formDefinitionId(testFormDefinition.getId())
                .dataJson(VALID_DATA)
                .businessKey("LOAN-SUBMIT-001")
                .notes("Please review")
                .build();

        mockMvc.perform(post("/api/v1/forms/submissions/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.businessKey").value("LOAN-SUBMIT-001"));
    }

    @Test
    @WithMockUser(authorities = {"FORM_CREATE"})
    @DisplayName("Should return 400 when submitting form with invalid data")
    void shouldReturn400WhenSubmittingFormWithInvalidData() throws Exception {
        String invalidData = "{\"firstName\": \"John\"}"; // Missing required fields

        SubmitFormDTO submitDTO = SubmitFormDTO.builder()
                .formDefinitionId(testFormDefinition.getId())
                .dataJson(invalidData)
                .businessKey("LOAN-INVALID-001")
                .build();

        mockMvc.perform(post("/api/v1/forms/submissions/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"FORM_UPDATE"})
    @DisplayName("Should approve submission successfully")
    void shouldApproveSubmission() throws Exception {
        // Create a submitted submission
        FormSubmission submittedSubmission = FormSubmission.builder()
                .formDefinition(testFormDefinition)
                .dataJson(VALID_DATA)
                .status(SubmissionStatus.SUBMITTED)
                .submittedBy("user3")
                .businessKey("LOAN-APPROVE-001")
                .build();
        submittedSubmission = formSubmissionRepository.save(submittedSubmission);

        mockMvc.perform(put("/api/v1/forms/submissions/" + submittedSubmission.getId() + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notes\": \"Approved\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(authorities = {"FORM_UPDATE"})
    @DisplayName("Should reject submission successfully")
    void shouldRejectSubmission() throws Exception {
        // Create a submitted submission
        FormSubmission submittedSubmission = FormSubmission.builder()
                .formDefinition(testFormDefinition)
                .dataJson(VALID_DATA)
                .status(SubmissionStatus.SUBMITTED)
                .submittedBy("user4")
                .businessKey("LOAN-REJECT-001")
                .build();
        submittedSubmission = formSubmissionRepository.save(submittedSubmission);

        mockMvc.perform(put("/api/v1/forms/submissions/" + submittedSubmission.getId() + "/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notes\": \"Rejected - incomplete\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"FORM_UPDATE"})
    @DisplayName("Should update draft successfully")
    void shouldUpdateDraft() throws Exception {
        String updatedData = """
                {
                    "firstName": "Jane",
                    "lastName": "Smith",
                    "email": "jane.smith@example.com",
                    "amount": 2000
                }
                """;

        mockMvc.perform(put("/api/v1/forms/submissions/" + testSubmission.getId() + "/update-draft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedData))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testSubmission.getId()));
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"FORM_DELETE"})
    @DisplayName("Should delete submission successfully")
    void shouldDeleteSubmission() throws Exception {
        mockMvc.perform(delete("/api/v1/forms/submissions/" + testSubmission.getId()))
                .andExpect(status().isNoContent());
    }
}
