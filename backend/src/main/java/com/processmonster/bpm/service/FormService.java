package com.processmonster.bpm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processmonster.bpm.entity.FormDefinition;
import com.processmonster.bpm.entity.FormSubmission;
import com.processmonster.bpm.entity.SubmissionStatus;
import com.processmonster.bpm.exception.BusinessException;
import com.processmonster.bpm.exception.ResourceNotFoundException;
import com.processmonster.bpm.repository.FormDefinitionRepository;
import com.processmonster.bpm.repository.FormSubmissionRepository;
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

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for form definition and submission management
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FormService {

    private final FormDefinitionRepository formDefinitionRepository;
    private final FormSubmissionRepository formSubmissionRepository;
    private final FormValidationService validationService;
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;

    // ========== FormDefinition Operations ==========

    /**
     * Get all form definitions
     */
    public Page<FormDefinition> getAllForms(Pageable pageable) {
        return formDefinitionRepository.findByDeletedFalse(pageable);
    }

    /**
     * Get form definition by ID
     */
    public FormDefinition getFormById(Long id) {
        return formDefinitionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        getMessage("form.definition.not-found", id)));
    }

    /**
     * Get form definition by key (latest version)
     */
    public FormDefinition getFormByKey(String formKey) {
        return formDefinitionRepository.findByFormKeyAndIsLatestVersionTrueAndDeletedFalse(formKey)
                .orElseThrow(() -> new ResourceNotFoundException(
                        getMessage("form.definition.not-found-by-key", formKey)));
    }

    /**
     * Get form definition by key and version
     */
    public FormDefinition getFormByKeyAndVersion(String formKey, Integer version) {
        return formDefinitionRepository.findByFormKeyAndVersionAndDeletedFalse(formKey, version)
                .orElseThrow(() -> new ResourceNotFoundException(
                        getMessage("form.definition.not-found-version", formKey, version)));
    }

    /**
     * Get all versions of a form
     */
    public List<FormDefinition> getFormVersions(String formKey) {
        return formDefinitionRepository.findByFormKeyAndDeletedFalseOrderByVersionDesc(formKey);
    }

    /**
     * Get published forms
     */
    public Page<FormDefinition> getPublishedForms(Pageable pageable) {
        return formDefinitionRepository.findByPublishedTrueAndDeletedFalse(pageable);
    }

    /**
     * Get forms by category
     */
    public Page<FormDefinition> getFormsByCategory(String category, Pageable pageable) {
        return formDefinitionRepository.findByCategoryAndDeletedFalse(category, pageable);
    }

    /**
     * Search forms
     */
    public Page<FormDefinition> searchForms(String keyword, Pageable pageable) {
        return formDefinitionRepository.searchForms(keyword, pageable);
    }

    /**
     * Create new form definition
     */
    @Transactional
    public FormDefinition createForm(FormDefinition form) {
        log.info("Creating new form definition: {}", form.getFormKey());

        // Validate JSON Schema
        if (!validationService.isValidJsonSchema(form.getSchemaJson())) {
            throw new BusinessException(getMessage("form.schema.invalid"));
        }

        // Check if form key already exists
        if (formDefinitionRepository.existsByFormKeyAndDeletedFalse(form.getFormKey())) {
            throw new BusinessException(getMessage("form.key.already-exists", form.getFormKey()));
        }

        // Set initial version
        form.setVersion(1);
        form.setIsLatestVersion(true);

        FormDefinition saved = formDefinitionRepository.save(form);
        log.info("Form definition created with ID: {}", saved.getId());

        return saved;
    }

    /**
     * Update form definition (creates new version if schema changed)
     */
    @Transactional
    public FormDefinition updateForm(Long id, FormDefinition updatedForm) {
        log.info("Updating form definition ID: {}", id);

        FormDefinition existing = getFormById(id);

        // Validate new JSON Schema
        if (!validationService.isValidJsonSchema(updatedForm.getSchemaJson())) {
            throw new BusinessException(getMessage("form.schema.invalid"));
        }

        // Check if schema changed
        boolean schemaChanged = !existing.getSchemaJson().equals(updatedForm.getSchemaJson());

        if (schemaChanged) {
            // Create new version
            log.info("Schema changed, creating new version for form: {}", existing.getFormKey());

            // Mark existing as not latest
            existing.setIsLatestVersion(false);
            formDefinitionRepository.save(existing);

            // Create new version
            FormDefinition newVersion = FormDefinition.builder()
                    .formKey(existing.getFormKey())
                    .name(updatedForm.getName())
                    .description(updatedForm.getDescription())
                    .category(updatedForm.getCategory())
                    .version(existing.getVersion() + 1)
                    .schemaJson(updatedForm.getSchemaJson())
                    .uiSchemaJson(updatedForm.getUiSchemaJson())
                    .published(false) // New versions start unpublished
                    .isLatestVersion(true)
                    .processDefinitionKey(updatedForm.getProcessDefinitionKey())
                    .taskDefinitionKey(updatedForm.getTaskDefinitionKey())
                    .build();

            return formDefinitionRepository.save(newVersion);
        } else {
            // Update metadata only (no new version needed)
            existing.setName(updatedForm.getName());
            existing.setDescription(updatedForm.getDescription());
            existing.setCategory(updatedForm.getCategory());
            existing.setUiSchemaJson(updatedForm.getUiSchemaJson());
            existing.setProcessDefinitionKey(updatedForm.getProcessDefinitionKey());
            existing.setTaskDefinitionKey(updatedForm.getTaskDefinitionKey());

            return formDefinitionRepository.save(existing);
        }
    }

    /**
     * Publish form definition
     */
    @Transactional
    public FormDefinition publishForm(Long id) {
        log.info("Publishing form definition ID: {}", id);

        FormDefinition form = getFormById(id);
        form.setPublished(true);

        return formDefinitionRepository.save(form);
    }

    /**
     * Unpublish form definition
     */
    @Transactional
    public FormDefinition unpublishForm(Long id) {
        log.info("Unpublishing form definition ID: {}", id);

        FormDefinition form = getFormById(id);
        form.setPublished(false);

        return formDefinitionRepository.save(form);
    }

    /**
     * Delete form definition (soft delete)
     */
    @Transactional
    public void deleteForm(Long id) {
        log.info("Deleting form definition ID: {}", id);

        FormDefinition form = getFormById(id);

        // Check if form has submissions
        Long submissionCount = formSubmissionRepository.countByFormDefinitionIdAndDeletedFalse(id);
        if (submissionCount > 0) {
            throw new BusinessException(getMessage("form.has-submissions", submissionCount));
        }

        form.markAsDeleted();
        formDefinitionRepository.save(form);

        log.info("Form definition deleted: {}", id);
    }

    // ========== FormSubmission Operations ==========

    /**
     * Get all submissions
     */
    public Page<FormSubmission> getAllSubmissions(Pageable pageable) {
        return formSubmissionRepository.findByDeletedFalse(pageable);
    }

    /**
     * Get submission by ID
     */
    public FormSubmission getSubmissionById(Long id) {
        return formSubmissionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        getMessage("form.submission.not-found", id)));
    }

    /**
     * Get submissions by form definition
     */
    public Page<FormSubmission> getSubmissionsByForm(Long formDefinitionId, Pageable pageable) {
        return formSubmissionRepository.findByFormDefinitionIdAndDeletedFalse(formDefinitionId, pageable);
    }

    /**
     * Get submissions by current user
     */
    public Page<FormSubmission> getMySubmissions(Pageable pageable) {
        String username = getCurrentUsername();
        return formSubmissionRepository.findBySubmittedByAndDeletedFalse(username, pageable);
    }

    /**
     * Get draft submissions for current user
     */
    public Page<FormSubmission> getMyDrafts(Pageable pageable) {
        String username = getCurrentUsername();
        return formSubmissionRepository.findBySubmittedByAndStatusAndDeletedFalse(
                username, SubmissionStatus.DRAFT, pageable);
    }

    /**
     * Get submissions by status
     */
    public Page<FormSubmission> getSubmissionsByStatus(SubmissionStatus status, Pageable pageable) {
        return formSubmissionRepository.findByStatusAndDeletedFalse(status, pageable);
    }

    /**
     * Get pending submissions for review
     */
    public Page<FormSubmission> getPendingSubmissions(Pageable pageable) {
        return formSubmissionRepository.findSubmissionsForReview(pageable);
    }

    /**
     * Create or save draft submission
     */
    @Transactional
    public FormSubmission saveDraft(Long formDefinitionId, String dataJson, String businessKey) {
        log.info("Saving draft for form definition ID: {}", formDefinitionId);

        FormDefinition formDefinition = getFormById(formDefinitionId);
        String username = getCurrentUsername();

        // Validate JSON format (not schema, since draft may be incomplete)
        if (!validationService.isValidJson(dataJson)) {
            throw new BusinessException(getMessage("form.data.invalid-json"));
        }

        FormSubmission draft = FormSubmission.builder()
                .formDefinition(formDefinition)
                .dataJson(dataJson)
                .businessKey(businessKey)
                .status(SubmissionStatus.DRAFT)
                .submittedBy(username)
                .build();

        return formSubmissionRepository.save(draft);
    }

    /**
     * Submit form (validates and marks as submitted)
     */
    @Transactional
    public FormSubmission submitForm(Long formDefinitionId, String dataJson, String businessKey) {
        log.info("Submitting form for definition ID: {}", formDefinitionId);

        FormDefinition formDefinition = getFormById(formDefinitionId);
        String username = getCurrentUsername();

        // Validate data against schema
        List<String> errors = validationService.validate(formDefinition.getSchemaJson(), dataJson);
        if (!errors.isEmpty()) {
            String errorMsg = String.join(", ", errors);
            throw new BusinessException(getMessage("form.validation.failed", errorMsg));
        }

        FormSubmission submission = FormSubmission.builder()
                .formDefinition(formDefinition)
                .dataJson(dataJson)
                .businessKey(businessKey)
                .status(SubmissionStatus.SUBMITTED)
                .submittedBy(username)
                .submittedAt(LocalDateTime.now())
                .build();

        FormSubmission saved = formSubmissionRepository.save(submission);
        log.info("Form submitted with ID: {}", saved.getId());

        return saved;
    }

    /**
     * Update draft submission
     */
    @Transactional
    public FormSubmission updateDraft(Long submissionId, String dataJson) {
        log.info("Updating draft submission ID: {}", submissionId);

        FormSubmission submission = getSubmissionById(submissionId);

        // Check ownership
        String username = getCurrentUsername();
        if (!submission.getSubmittedBy().equals(username)) {
            throw new BusinessException(getMessage("form.submission.not-owner"));
        }

        // Can only update drafts
        if (!submission.isDraft()) {
            throw new BusinessException(getMessage("form.submission.not-draft"));
        }

        // Validate JSON format
        if (!validationService.isValidJson(dataJson)) {
            throw new BusinessException(getMessage("form.data.invalid-json"));
        }

        submission.setDataJson(dataJson);
        return formSubmissionRepository.save(submission);
    }

    /**
     * Approve submission
     */
    @Transactional
    public FormSubmission approveSubmission(Long submissionId, String notes) {
        log.info("Approving submission ID: {}", submissionId);

        FormSubmission submission = getSubmissionById(submissionId);

        if (submission.getStatus() != SubmissionStatus.SUBMITTED) {
            throw new BusinessException(getMessage("form.submission.invalid-status"));
        }

        submission.setStatus(SubmissionStatus.APPROVED);
        submission.setNotes(notes);

        return formSubmissionRepository.save(submission);
    }

    /**
     * Reject submission
     */
    @Transactional
    public FormSubmission rejectSubmission(Long submissionId, String notes) {
        log.info("Rejecting submission ID: {}", submissionId);

        FormSubmission submission = getSubmissionById(submissionId);

        if (submission.getStatus() != SubmissionStatus.SUBMITTED) {
            throw new BusinessException(getMessage("form.submission.invalid-status"));
        }

        submission.setStatus(SubmissionStatus.REJECTED);
        submission.setNotes(notes);

        return formSubmissionRepository.save(submission);
    }

    /**
     * Delete submission (soft delete)
     */
    @Transactional
    public void deleteSubmission(Long id) {
        log.info("Deleting submission ID: {}", id);

        FormSubmission submission = getSubmissionById(id);

        // Check ownership or admin permission
        String username = getCurrentUsername();
        if (!submission.getSubmittedBy().equals(username)) {
            // TODO: Check if user has ADMIN role
            throw new BusinessException(getMessage("form.submission.not-owner"));
        }

        submission.markAsDeleted();
        formSubmissionRepository.save(submission);

        log.info("Submission deleted: {}", id);
    }

    // ========== Helper Methods ==========

    /**
     * Get current authenticated username
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }

    /**
     * Get internationalized message
     */
    private String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
