package com.processmonster.bpm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processmonster.bpm.entity.FormDefinition;
import com.processmonster.bpm.entity.FormSubmission;
import com.processmonster.bpm.entity.SubmissionStatus;
import com.processmonster.bpm.exception.BusinessException;
import com.processmonster.bpm.exception.ResourceNotFoundException;
import com.processmonster.bpm.repository.FormDefinitionRepository;
import com.processmonster.bpm.repository.FormSubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FormService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Form Service Tests")
class FormServiceTest {

    @Mock
    private FormDefinitionRepository formDefinitionRepository;

    @Mock
    private FormSubmissionRepository formSubmissionRepository;

    @Mock
    private FormValidationService validationService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private FormService formService;
    private ObjectMapper objectMapper;

    private static final String VALID_SCHEMA = "{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"}}}";
    private static final String VALID_DATA = "{\"name\":\"John Doe\"}";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        formService = new FormService(
                formDefinitionRepository,
                formSubmissionRepository,
                validationService,
                messageSource,
                objectMapper
        );

        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);

        when(messageSource.getMessage(anyString(), any(), any()))
                .thenReturn("Test message");
    }

    // ========== FormDefinition Tests ==========

    @Test
    @DisplayName("Should get all forms successfully")
    void shouldGetAllForms() {
        // Given
        List<FormDefinition> forms = List.of(
                createFormDefinition(1L, "loan-application"),
                createFormDefinition(2L, "account-opening")
        );
        Page<FormDefinition> page = new PageImpl<>(forms);
        Pageable pageable = PageRequest.of(0, 20);

        when(formDefinitionRepository.findByDeletedFalse(pageable)).thenReturn(page);

        // When
        Page<FormDefinition> result = formService.getAllForms(pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        verify(formDefinitionRepository).findByDeletedFalse(pageable);
    }

    @Test
    @DisplayName("Should get form by ID successfully")
    void shouldGetFormById() {
        // Given
        FormDefinition form = createFormDefinition(1L, "loan-application");
        when(formDefinitionRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(form));

        // When
        FormDefinition result = formService.getFormById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFormKey()).isEqualTo("loan-application");
        verify(formDefinitionRepository).findByIdAndDeletedFalse(1L);
    }

    @Test
    @DisplayName("Should throw exception when form not found by ID")
    void shouldThrowExceptionWhenFormNotFoundById() {
        // Given
        when(formDefinitionRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> formService.getFormById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(formDefinitionRepository).findByIdAndDeletedFalse(1L);
    }

    @Test
    @DisplayName("Should get form by key successfully")
    void shouldGetFormByKey() {
        // Given
        FormDefinition form = createFormDefinition(1L, "loan-application");
        when(formDefinitionRepository.findByFormKeyAndIsLatestVersionTrueAndDeletedFalse("loan-application"))
                .thenReturn(Optional.of(form));

        // When
        FormDefinition result = formService.getFormByKey("loan-application");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFormKey()).isEqualTo("loan-application");
        verify(formDefinitionRepository).findByFormKeyAndIsLatestVersionTrueAndDeletedFalse("loan-application");
    }

    @Test
    @DisplayName("Should create form successfully")
    void shouldCreateForm() {
        // Given
        FormDefinition form = createFormDefinition(null, "loan-application");
        FormDefinition savedForm = createFormDefinition(1L, "loan-application");

        when(validationService.isValidJsonSchema(VALID_SCHEMA)).thenReturn(true);
        when(formDefinitionRepository.existsByFormKeyAndDeletedFalse("loan-application")).thenReturn(false);
        when(formDefinitionRepository.save(any(FormDefinition.class))).thenReturn(savedForm);

        // When
        FormDefinition result = formService.createForm(form);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getVersion()).isEqualTo(1);
        assertThat(result.getIsLatestVersion()).isTrue();
        verify(formDefinitionRepository).save(any(FormDefinition.class));
    }

    @Test
    @DisplayName("Should throw exception when creating form with invalid schema")
    void shouldThrowExceptionWhenCreatingFormWithInvalidSchema() {
        // Given
        FormDefinition form = createFormDefinition(null, "loan-application");

        when(validationService.isValidJsonSchema(VALID_SCHEMA)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> formService.createForm(form))
                .isInstanceOf(BusinessException.class);
        verify(validationService).isValidJsonSchema(VALID_SCHEMA);
        verify(formDefinitionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when creating form with duplicate key")
    void shouldThrowExceptionWhenCreatingFormWithDuplicateKey() {
        // Given
        FormDefinition form = createFormDefinition(null, "loan-application");

        when(validationService.isValidJsonSchema(VALID_SCHEMA)).thenReturn(true);
        when(formDefinitionRepository.existsByFormKeyAndDeletedFalse("loan-application")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> formService.createForm(form))
                .isInstanceOf(BusinessException.class);
        verify(formDefinitionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create new version when updating form with schema change")
    void shouldCreateNewVersionWhenUpdatingFormWithSchemaChange() {
        // Given
        FormDefinition existingForm = createFormDefinition(1L, "loan-application");
        existingForm.setSchemaJson("{\"type\":\"object\"}");
        existingForm.setVersion(1);

        FormDefinition updatedForm = createFormDefinition(null, "loan-application");
        updatedForm.setSchemaJson(VALID_SCHEMA); // Different schema

        FormDefinition newVersion = createFormDefinition(2L, "loan-application");
        newVersion.setVersion(2);

        when(formDefinitionRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existingForm));
        when(validationService.isValidJsonSchema(VALID_SCHEMA)).thenReturn(true);
        when(formDefinitionRepository.save(any(FormDefinition.class))).thenReturn(existingForm, newVersion);

        // When
        FormDefinition result = formService.updateForm(1L, updatedForm);

        // Then
        assertThat(result.getVersion()).isEqualTo(2);
        verify(formDefinitionRepository, times(2)).save(any(FormDefinition.class));
    }

    @Test
    @DisplayName("Should publish form successfully")
    void shouldPublishForm() {
        // Given
        FormDefinition form = createFormDefinition(1L, "loan-application");
        form.setPublished(false);

        when(formDefinitionRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(form));
        when(formDefinitionRepository.save(any(FormDefinition.class))).thenReturn(form);

        // When
        FormDefinition result = formService.publishForm(1L);

        // Then
        assertThat(result.getPublished()).isTrue();
        verify(formDefinitionRepository).save(any(FormDefinition.class));
    }

    @Test
    @DisplayName("Should delete form successfully")
    void shouldDeleteForm() {
        // Given
        FormDefinition form = createFormDefinition(1L, "loan-application");

        when(formDefinitionRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(form));
        when(formSubmissionRepository.countByFormDefinitionIdAndDeletedFalse(1L)).thenReturn(0L);
        when(formDefinitionRepository.save(any(FormDefinition.class))).thenReturn(form);

        // When
        formService.deleteForm(1L);

        // Then
        verify(formDefinitionRepository).save(any(FormDefinition.class));
    }

    @Test
    @DisplayName("Should throw exception when deleting form with submissions")
    void shouldThrowExceptionWhenDeletingFormWithSubmissions() {
        // Given
        FormDefinition form = createFormDefinition(1L, "loan-application");

        when(formDefinitionRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(form));
        when(formSubmissionRepository.countByFormDefinitionIdAndDeletedFalse(1L)).thenReturn(5L);

        // When & Then
        assertThatThrownBy(() -> formService.deleteForm(1L))
                .isInstanceOf(BusinessException.class);
        verify(formDefinitionRepository, never()).save(any());
    }

    // ========== FormSubmission Tests ==========

    @Test
    @DisplayName("Should save draft successfully")
    void shouldSaveDraft() {
        // Given
        FormDefinition formDef = createFormDefinition(1L, "loan-application");
        FormSubmission draft = createFormSubmission(1L, formDef);

        when(formDefinitionRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(formDef));
        when(validationService.isValidJson(VALID_DATA)).thenReturn(true);
        when(formSubmissionRepository.save(any(FormSubmission.class))).thenReturn(draft);

        // When
        FormSubmission result = formService.saveDraft(1L, VALID_DATA, "LOAN-001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(SubmissionStatus.DRAFT);
        verify(formSubmissionRepository).save(any(FormSubmission.class));
    }

    @Test
    @DisplayName("Should submit form successfully")
    void shouldSubmitForm() {
        // Given
        FormDefinition formDef = createFormDefinition(1L, "loan-application");
        FormSubmission submission = createFormSubmission(1L, formDef);
        submission.setStatus(SubmissionStatus.SUBMITTED);

        when(formDefinitionRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(formDef));
        when(validationService.validate(VALID_SCHEMA, VALID_DATA)).thenReturn(new ArrayList<>());
        when(formSubmissionRepository.save(any(FormSubmission.class))).thenReturn(submission);

        // When
        FormSubmission result = formService.submitForm(1L, VALID_DATA, "LOAN-001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
        verify(validationService).validate(VALID_SCHEMA, VALID_DATA);
        verify(formSubmissionRepository).save(any(FormSubmission.class));
    }

    @Test
    @DisplayName("Should throw exception when submitting form with validation errors")
    void shouldThrowExceptionWhenSubmittingFormWithValidationErrors() {
        // Given
        FormDefinition formDef = createFormDefinition(1L, "loan-application");
        List<String> errors = List.of("Field 'name' is required");

        when(formDefinitionRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(formDef));
        when(validationService.validate(VALID_SCHEMA, VALID_DATA)).thenReturn(errors);

        // When & Then
        assertThatThrownBy(() -> formService.submitForm(1L, VALID_DATA, "LOAN-001"))
                .isInstanceOf(BusinessException.class);
        verify(formSubmissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should approve submission successfully")
    void shouldApproveSubmission() {
        // Given
        FormDefinition formDef = createFormDefinition(1L, "loan-application");
        FormSubmission submission = createFormSubmission(1L, formDef);
        submission.setStatus(SubmissionStatus.SUBMITTED);

        when(formSubmissionRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(submission));
        when(formSubmissionRepository.save(any(FormSubmission.class))).thenReturn(submission);

        // When
        FormSubmission result = formService.approveSubmission(1L, "Approved");

        // Then
        assertThat(result.getStatus()).isEqualTo(SubmissionStatus.APPROVED);
        verify(formSubmissionRepository).save(any(FormSubmission.class));
    }

    @Test
    @DisplayName("Should reject submission successfully")
    void shouldRejectSubmission() {
        // Given
        FormDefinition formDef = createFormDefinition(1L, "loan-application");
        FormSubmission submission = createFormSubmission(1L, formDef);
        submission.setStatus(SubmissionStatus.SUBMITTED);

        when(formSubmissionRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(submission));
        when(formSubmissionRepository.save(any(FormSubmission.class))).thenReturn(submission);

        // When
        FormSubmission result = formService.rejectSubmission(1L, "Rejected");

        // Then
        assertThat(result.getStatus()).isEqualTo(SubmissionStatus.REJECTED);
        verify(formSubmissionRepository).save(any(FormSubmission.class));
    }

    @Test
    @DisplayName("Should update draft successfully")
    void shouldUpdateDraft() {
        // Given
        FormDefinition formDef = createFormDefinition(1L, "loan-application");
        FormSubmission draft = createFormSubmission(1L, formDef);
        draft.setStatus(SubmissionStatus.DRAFT);
        draft.setSubmittedBy("testuser");

        when(formSubmissionRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(draft));
        when(validationService.isValidJson(VALID_DATA)).thenReturn(true);
        when(formSubmissionRepository.save(any(FormSubmission.class))).thenReturn(draft);

        // When
        FormSubmission result = formService.updateDraft(1L, VALID_DATA);

        // Then
        assertThat(result).isNotNull();
        verify(formSubmissionRepository).save(any(FormSubmission.class));
    }

    // ========== Helper Methods ==========

    private FormDefinition createFormDefinition(Long id, String formKey) {
        return FormDefinition.builder()
                .id(id)
                .formKey(formKey)
                .name("Test Form")
                .description("Test Description")
                .category("TEST")
                .version(1)
                .schemaJson(VALID_SCHEMA)
                .published(false)
                .isLatestVersion(true)
                .submissions(new ArrayList<>())
                .build();
    }

    private FormSubmission createFormSubmission(Long id, FormDefinition formDefinition) {
        return FormSubmission.builder()
                .id(id)
                .formDefinition(formDefinition)
                .dataJson(VALID_DATA)
                .status(SubmissionStatus.DRAFT)
                .submittedBy("testuser")
                .businessKey("TEST-001")
                .build();
    }
}
