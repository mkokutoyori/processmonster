package com.processmonster.bpm.repository;

import com.processmonster.bpm.entity.FormSubmission;
import com.processmonster.bpm.entity.SubmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for FormSubmission entity
 */
@Repository
public interface FormSubmissionRepository extends JpaRepository<FormSubmission, Long> {

    /**
     * Find submission by ID (non-deleted)
     */
    Optional<FormSubmission> findByIdAndDeletedFalse(Long id);

    /**
     * Find all non-deleted submissions
     */
    Page<FormSubmission> findByDeletedFalse(Pageable pageable);

    /**
     * Find submissions by form definition
     */
    Page<FormSubmission> findByFormDefinitionIdAndDeletedFalse(Long formDefinitionId, Pageable pageable);

    /**
     * Find submissions by status
     */
    Page<FormSubmission> findByStatusAndDeletedFalse(SubmissionStatus status, Pageable pageable);

    /**
     * Find submissions by user
     */
    Page<FormSubmission> findBySubmittedByAndDeletedFalse(String username, Pageable pageable);

    /**
     * Find draft submissions by user
     */
    Page<FormSubmission> findBySubmittedByAndStatusAndDeletedFalse(
        String username,
        SubmissionStatus status,
        Pageable pageable
    );

    /**
     * Find submissions by task
     */
    Page<FormSubmission> findByTaskIdAndDeletedFalse(Long taskId, Pageable pageable);

    /**
     * Find submissions by process instance
     */
    Page<FormSubmission> findByProcessInstanceIdAndDeletedFalse(Long processInstanceId, Pageable pageable);

    /**
     * Find submission by business key
     */
    Optional<FormSubmission> findByBusinessKeyAndDeletedFalse(String businessKey);

    /**
     * Find submissions by form key
     */
    @Query("SELECT s FROM FormSubmission s JOIN s.formDefinition f WHERE " +
           "f.formKey = :formKey AND s.deleted = false")
    Page<FormSubmission> findByFormKey(@Param("formKey") String formKey, Pageable pageable);

    /**
     * Count submissions by form definition
     */
    Long countByFormDefinitionIdAndDeletedFalse(Long formDefinitionId);

    /**
     * Count submissions by status
     */
    Long countByStatusAndDeletedFalse(SubmissionStatus status);

    /**
     * Count submissions by user
     */
    Long countBySubmittedByAndDeletedFalse(String username);

    /**
     * Find pending submissions (submitted but not approved/rejected)
     */
    @Query("SELECT s FROM FormSubmission s WHERE s.status = 'SUBMITTED' AND s.deleted = false")
    Page<FormSubmission> findPendingSubmissions(Pageable pageable);

    /**
     * Find submissions that need review
     */
    @Query("SELECT s FROM FormSubmission s WHERE s.status IN ('SUBMITTED') AND s.deleted = false " +
           "ORDER BY s.createdAt ASC")
    Page<FormSubmission> findSubmissionsForReview(Pageable pageable);

    /**
     * Search submissions by business key or notes
     */
    @Query("SELECT s FROM FormSubmission s WHERE s.deleted = false AND " +
           "(LOWER(s.businessKey) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.notes) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<FormSubmission> searchSubmissions(@Param("keyword") String keyword, Pageable pageable);
}
