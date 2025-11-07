package com.processmonster.bpm.repository;

import com.processmonster.bpm.entity.FormDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for FormDefinition entity
 */
@Repository
public interface FormDefinitionRepository extends JpaRepository<FormDefinition, Long> {

    /**
     * Find form by ID (non-deleted)
     */
    Optional<FormDefinition> findByIdAndDeletedFalse(Long id);

    /**
     * Find all non-deleted forms
     */
    Page<FormDefinition> findByDeletedFalse(Pageable pageable);

    /**
     * Find form by key (latest version)
     */
    Optional<FormDefinition> findByFormKeyAndIsLatestVersionTrueAndDeletedFalse(String formKey);

    /**
     * Find all versions of a form by key
     */
    List<FormDefinition> findByFormKeyAndDeletedFalseOrderByVersionDesc(String formKey);

    /**
     * Find forms by category
     */
    Page<FormDefinition> findByCategoryAndDeletedFalse(String category, Pageable pageable);

    /**
     * Find published forms
     */
    Page<FormDefinition> findByPublishedTrueAndDeletedFalse(Pageable pageable);

    /**
     * Find published forms by category
     */
    Page<FormDefinition> findByPublishedTrueAndCategoryAndDeletedFalse(String category, Pageable pageable);

    /**
     * Find forms by process definition key
     */
    Page<FormDefinition> findByProcessDefinitionKeyAndDeletedFalse(String processDefinitionKey, Pageable pageable);

    /**
     * Find forms by task definition key
     */
    Page<FormDefinition> findByTaskDefinitionKeyAndDeletedFalse(String taskDefinitionKey, Pageable pageable);

    /**
     * Search forms by name or description
     */
    @Query("SELECT f FROM FormDefinition f WHERE f.deleted = false AND " +
           "(LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.formKey) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<FormDefinition> searchForms(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Count forms by form key
     */
    Long countByFormKeyAndDeletedFalse(String formKey);

    /**
     * Check if form key exists
     */
    boolean existsByFormKeyAndDeletedFalse(String formKey);

    /**
     * Get latest version number for a form key
     */
    @Query("SELECT MAX(f.version) FROM FormDefinition f WHERE f.formKey = :formKey AND f.deleted = false")
    Integer findLatestVersionByFormKey(@Param("formKey") String formKey);

    /**
     * Find form by key and version
     */
    Optional<FormDefinition> findByFormKeyAndVersionAndDeletedFalse(String formKey, Integer version);
}
