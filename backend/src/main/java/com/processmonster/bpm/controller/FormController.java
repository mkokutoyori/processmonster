package com.processmonster.bpm.controller;

import com.processmonster.bpm.dto.form.*;
import com.processmonster.bpm.entity.FormDefinition;
import com.processmonster.bpm.entity.FormSubmission;
import com.processmonster.bpm.entity.SubmissionStatus;
import com.processmonster.bpm.mapper.FormMapper;
import com.processmonster.bpm.service.FormService;
import com.processmonster.bpm.service.FormValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for dynamic form management
 */
@RestController
@RequestMapping("/api/v1/forms")
@RequiredArgsConstructor
@Tag(name = "Forms", description = "Dynamic Form Management API")
public class FormController {

    private final FormService formService;
    private final FormMapper formMapper;
    private final FormValidationService validationService;

    // ========== FormDefinition Endpoints ==========

    @GetMapping("/definitions")
    @PreAuthorize("hasAnyAuthority('FORM_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get all form definitions", description = "Retrieve all form definitions with pagination")
    public ResponseEntity<Page<FormDefinitionDTO>> getAllForms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<FormDefinitionDTO> forms = formService.getAllForms(pageable)
                .map(formMapper::toDTO);

        return ResponseEntity.ok(forms);
    }

    @GetMapping("/definitions/{id}")
    @PreAuthorize("hasAnyAuthority('FORM_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get form definition by ID", description = "Retrieve a specific form definition")
    public ResponseEntity<FormDefinitionDTO> getFormById(@PathVariable Long id) {
        FormDefinition form = formService.getFormById(id);
        return ResponseEntity.ok(formMapper.toDTO(form));
    }

    @GetMapping("/definitions/key/{formKey}")
    @PreAuthorize("hasAnyAuthority('FORM_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get form by key", description = "Retrieve latest version of form by key")
    public ResponseEntity<FormDefinitionDTO> getFormByKey(@PathVariable String formKey) {
        FormDefinition form = formService.getFormByKey(formKey);
        return ResponseEntity.ok(formMapper.toDTO(form));
    }

    @GetMapping("/definitions/key/{formKey}/versions")
    @PreAuthorize("hasAnyAuthority('FORM_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get form versions", description = "Retrieve all versions of a form")
    public ResponseEntity<List<FormDefinitionDTO>> getFormVersions(@PathVariable String formKey) {
        List<FormDefinitionDTO> versions = formService.getFormVersions(formKey).stream()
                .map(formMapper::toDTO)
                .toList();
        return ResponseEntity.ok(versions);
    }

    @GetMapping("/definitions/published")
    @PreAuthorize("hasAnyAuthority('FORM_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get published forms", description = "Retrieve all published forms")
    public ResponseEntity<Page<FormDefinitionDTO>> getPublishedForms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<FormDefinitionDTO> forms = formService.getPublishedForms(pageable)
                .map(formMapper::toDTO);

        return ResponseEntity.ok(forms);
    }

    @GetMapping("/definitions/category/{category}")
    @PreAuthorize("hasAnyAuthority('FORM_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get forms by category", description = "Retrieve forms in a specific category")
    public ResponseEntity<Page<FormDefinitionDTO>> getFormsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<FormDefinitionDTO> forms = formService.getFormsByCategory(category, pageable)
                .map(formMapper::toDTO);

        return ResponseEntity.ok(forms);
    }

    @GetMapping("/definitions/search")
    @PreAuthorize("hasAnyAuthority('FORM_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Search forms", description = "Search forms by keyword")
    public ResponseEntity<Page<FormDefinitionDTO>> searchForms(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<FormDefinitionDTO> forms = formService.searchForms(keyword, pageable)
                .map(formMapper::toDTO);

        return ResponseEntity.ok(forms);
    }

    @PostMapping("/definitions")
    @PreAuthorize("hasAnyAuthority('FORM_CREATE', 'ROLE_ADMIN')")
    @Operation(summary = "Create form definition", description = "Create a new form definition")
    public ResponseEntity<FormDefinitionDTO> createForm(@Valid @RequestBody CreateFormDefinitionDTO createDTO) {
        FormDefinition form = formMapper.toEntity(createDTO);
        FormDefinition created = formService.createForm(form);
        return ResponseEntity.status(HttpStatus.CREATED).body(formMapper.toDTO(created));
    }

    @PutMapping("/definitions/{id}")
    @PreAuthorize("hasAnyAuthority('FORM_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Update form definition", description = "Update form (creates new version if schema changed)")
    public ResponseEntity<FormDefinitionDTO> updateForm(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFormDefinitionDTO updateDTO) {

        FormDefinition form = new FormDefinition();
        formMapper.updateEntityFromDTO(updateDTO, form);
        FormDefinition updated = formService.updateForm(id, form);

        return ResponseEntity.ok(formMapper.toDTO(updated));
    }

    @PutMapping("/definitions/{id}/publish")
    @PreAuthorize("hasAnyAuthority('FORM_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Publish form", description = "Publish a form definition")
    public ResponseEntity<FormDefinitionDTO> publishForm(@PathVariable Long id) {
        FormDefinition published = formService.publishForm(id);
        return ResponseEntity.ok(formMapper.toDTO(published));
    }

    @PutMapping("/definitions/{id}/unpublish")
    @PreAuthorize("hasAnyAuthority('FORM_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Unpublish form", description = "Unpublish a form definition")
    public ResponseEntity<FormDefinitionDTO> unpublishForm(@PathVariable Long id) {
        FormDefinition unpublished = formService.unpublishForm(id);
        return ResponseEntity.ok(formMapper.toDTO(unpublished));
    }

    @DeleteMapping("/definitions/{id}")
    @PreAuthorize("hasAnyAuthority('FORM_DELETE', 'ROLE_ADMIN')")
    @Operation(summary = "Delete form definition", description = "Soft delete a form definition")
    public ResponseEntity<Void> deleteForm(@PathVariable Long id) {
        formService.deleteForm(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/definitions/{id}/validate-schema")
    @PreAuthorize("hasAnyAuthority('FORM_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Validate JSON Schema", description = "Validate if JSON Schema is valid")
    public ResponseEntity<Map<String, Object>> validateSchema(@RequestBody String schemaJson) {
        boolean valid = validationService.isValidJsonSchema(schemaJson);

        Map<String, Object> response = new HashMap<>();
        response.put("valid", valid);
        response.put("message", valid ? "Schema is valid" : "Schema is invalid");

        return ResponseEntity.ok(response);
    }

    // ========== FormSubmission Endpoints ==========

    @GetMapping("/submissions")
    @PreAuthorize("hasAnyAuthority('FORM_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get all submissions", description = "Retrieve all form submissions with pagination")
    public ResponseEntity<Page<FormSubmissionDTO>> getAllSubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<FormSubmissionDTO> submissions = formService.getAllSubmissions(pageable)
                .map(formMapper::toSubmissionDTO);

        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/submissions/{id}")
    @PreAuthorize("hasAnyAuthority('FORM_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get submission by ID", description = "Retrieve a specific form submission")
    public ResponseEntity<FormSubmissionDTO> getSubmissionById(@PathVariable Long id) {
        FormSubmission submission = formService.getSubmissionById(id);
        return ResponseEntity.ok(formMapper.toSubmissionDTO(submission));
    }

    @GetMapping("/submissions/my")
    @PreAuthorize("hasAnyAuthority('FORM_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get my submissions", description = "Retrieve submissions by current user")
    public ResponseEntity<Page<FormSubmissionDTO>> getMySubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<FormSubmissionDTO> submissions = formService.getMySubmissions(pageable)
                .map(formMapper::toSubmissionDTO);

        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/submissions/my/drafts")
    @PreAuthorize("hasAnyAuthority('FORM_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get my drafts", description = "Retrieve draft submissions by current user")
    public ResponseEntity<Page<FormSubmissionDTO>> getMyDrafts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<FormSubmissionDTO> drafts = formService.getMyDrafts(pageable)
                .map(formMapper::toSubmissionDTO);

        return ResponseEntity.ok(drafts);
    }

    @GetMapping("/submissions/definition/{formDefinitionId}")
    @PreAuthorize("hasAnyAuthority('FORM_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get submissions by form", description = "Retrieve submissions for a specific form")
    public ResponseEntity<Page<FormSubmissionDTO>> getSubmissionsByForm(
            @PathVariable Long formDefinitionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<FormSubmissionDTO> submissions = formService.getSubmissionsByForm(formDefinitionId, pageable)
                .map(formMapper::toSubmissionDTO);

        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/submissions/pending")
    @PreAuthorize("hasAnyAuthority('FORM_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Get pending submissions", description = "Retrieve submissions pending review")
    public ResponseEntity<Page<FormSubmissionDTO>> getPendingSubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<FormSubmissionDTO> submissions = formService.getPendingSubmissions(pageable)
                .map(formMapper::toSubmissionDTO);

        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/submissions/status/{status}")
    @PreAuthorize("hasAnyAuthority('FORM_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Get submissions by status", description = "Retrieve submissions by status")
    public ResponseEntity<Page<FormSubmissionDTO>> getSubmissionsByStatus(
            @PathVariable SubmissionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<FormSubmissionDTO> submissions = formService.getSubmissionsByStatus(status, pageable)
                .map(formMapper::toSubmissionDTO);

        return ResponseEntity.ok(submissions);
    }

    @PostMapping("/submissions/save-draft")
    @PreAuthorize("hasAnyAuthority('FORM_CREATE', 'ROLE_ADMIN')")
    @Operation(summary = "Save draft", description = "Save or update a form draft (no validation)")
    public ResponseEntity<FormSubmissionDTO> saveDraft(@Valid @RequestBody SaveDraftDTO draftDTO) {
        FormSubmission draft = formService.saveDraft(
                draftDTO.getFormDefinitionId(),
                draftDTO.getDataJson(),
                draftDTO.getBusinessKey()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(formMapper.toSubmissionDTO(draft));
    }

    @PostMapping("/submissions/submit")
    @PreAuthorize("hasAnyAuthority('FORM_CREATE', 'ROLE_ADMIN')")
    @Operation(summary = "Submit form", description = "Submit a form (validates against schema)")
    public ResponseEntity<FormSubmissionDTO> submitForm(@Valid @RequestBody SubmitFormDTO submitDTO) {
        FormSubmission submission = formService.submitForm(
                submitDTO.getFormDefinitionId(),
                submitDTO.getDataJson(),
                submitDTO.getBusinessKey()
        );
        submission.setNotes(submitDTO.getNotes());
        return ResponseEntity.status(HttpStatus.CREATED).body(formMapper.toSubmissionDTO(submission));
    }

    @PostMapping("/submissions/{id}/validate")
    @PreAuthorize("hasAnyAuthority('FORM_READ', 'ROLE_ADMIN')")
    @Operation(summary = "Validate submission data", description = "Validate form data against schema")
    public ResponseEntity<Map<String, Object>> validateSubmission(
            @PathVariable Long id,
            @RequestBody String dataJson) {

        FormSubmission submission = formService.getSubmissionById(id);
        List<String> errors = validationService.validate(
                submission.getFormDefinition().getSchemaJson(),
                dataJson
        );

        Map<String, Object> response = new HashMap<>();
        response.put("valid", errors.isEmpty());
        response.put("errors", errors);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/submissions/{id}/approve")
    @PreAuthorize("hasAnyAuthority('FORM_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Approve submission", description = "Approve a submitted form")
    public ResponseEntity<FormSubmissionDTO> approveSubmission(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {

        String notes = body != null ? body.get("notes") : null;
        FormSubmission approved = formService.approveSubmission(id, notes);
        return ResponseEntity.ok(formMapper.toSubmissionDTO(approved));
    }

    @PutMapping("/submissions/{id}/reject")
    @PreAuthorize("hasAnyAuthority('FORM_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Reject submission", description = "Reject a submitted form")
    public ResponseEntity<FormSubmissionDTO> rejectSubmission(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {

        String notes = body != null ? body.get("notes") : null;
        FormSubmission rejected = formService.rejectSubmission(id, notes);
        return ResponseEntity.ok(formMapper.toSubmissionDTO(rejected));
    }

    @PutMapping("/submissions/{id}/update-draft")
    @PreAuthorize("hasAnyAuthority('FORM_UPDATE', 'ROLE_ADMIN')")
    @Operation(summary = "Update draft", description = "Update an existing draft")
    public ResponseEntity<FormSubmissionDTO> updateDraft(
            @PathVariable Long id,
            @RequestBody String dataJson) {

        FormSubmission updated = formService.updateDraft(id, dataJson);
        return ResponseEntity.ok(formMapper.toSubmissionDTO(updated));
    }

    @DeleteMapping("/submissions/{id}")
    @PreAuthorize("hasAnyAuthority('FORM_DELETE', 'ROLE_ADMIN')")
    @Operation(summary = "Delete submission", description = "Soft delete a form submission")
    public ResponseEntity<Void> deleteSubmission(@PathVariable Long id) {
        formService.deleteSubmission(id);
        return ResponseEntity.noContent().build();
    }

    // ========== Helper Methods ==========

    private Pageable createPageable(int page, int size, String sort) {
        String[] sortParams = sort.split(",");
        String property = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return PageRequest.of(page, size, Sort.by(direction, property));
    }
}
