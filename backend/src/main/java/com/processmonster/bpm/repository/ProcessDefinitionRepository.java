package com.processmonster.bpm.repository;

import com.processmonster.bpm.entity.ProcessDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ProcessDefinition entity with versioning support
 */
@Repository
public interface ProcessDefinitionRepository extends JpaRepository<ProcessDefinition, Long> {

    /**
     * Find all process definitions (not deleted) - returns only latest versions
     * @param pageable pagination parameters
     * @return page of latest process versions
     */
    Page<ProcessDefinition> findByDeletedFalseAndIsLatestVersionTrue(Pageable pageable);

    /**
     * Find all process definitions (including all versions)
     * @param pageable pagination parameters
     * @return page of all process versions
     */
    Page<ProcessDefinition> findByDeletedFalse(Pageable pageable);

    /**
     * Find process definition by ID and not deleted
     * @param id process ID
     * @return optional process definition
     */
    @Query("SELECT p FROM ProcessDefinition p WHERE p.id = :id AND p.deleted = false")
    Optional<ProcessDefinition> findByIdAndNotDeleted(@Param("id") Long id);

    /**
     * Find latest version of a process by key
     * @param processKey process key
     * @return optional latest process definition
     */
    @Query("SELECT p FROM ProcessDefinition p WHERE p.processKey = :processKey " +
           "AND p.deleted = false AND p.isLatestVersion = true")
    Optional<ProcessDefinition> findLatestVersionByProcessKey(@Param("processKey") String processKey);

    /**
     * Find specific version of a process
     * @param processKey process key
     * @param version version number
     * @return optional process definition
     */
    @Query("SELECT p FROM ProcessDefinition p WHERE p.processKey = :processKey " +
           "AND p.version = :version AND p.deleted = false")
    Optional<ProcessDefinition> findByProcessKeyAndVersion(
            @Param("processKey") String processKey,
            @Param("version") Integer version);

    /**
     * Find all versions of a process ordered by version descending
     * @param processKey process key
     * @return list of all versions
     */
    @Query("SELECT p FROM ProcessDefinition p WHERE p.processKey = :processKey " +
           "AND p.deleted = false ORDER BY p.version DESC")
    List<ProcessDefinition> findAllVersionsByProcessKey(@Param("processKey") String processKey);

    /**
     * Get the latest version number for a process key
     * @param processKey process key
     * @return latest version number or null if not found
     */
    @Query("SELECT MAX(p.version) FROM ProcessDefinition p WHERE p.processKey = :processKey AND p.deleted = false")
    Integer findLatestVersionNumber(@Param("processKey") String processKey);

    /**
     * Check if a process key exists
     * @param processKey process key
     * @return true if exists
     */
    @Query("SELECT COUNT(p) > 0 FROM ProcessDefinition p WHERE p.processKey = :processKey AND p.deleted = false")
    boolean existsByProcessKey(@Param("processKey") String processKey);

    /**
     * Find all templates (not deleted)
     * @param pageable pagination parameters
     * @return page of templates
     */
    @Query("SELECT p FROM ProcessDefinition p WHERE p.isTemplate = true " +
           "AND p.deleted = false AND p.isLatestVersion = true")
    Page<ProcessDefinition> findAllTemplates(Pageable pageable);

    /**
     * Find all published processes (latest versions only)
     * @param pageable pagination parameters
     * @return page of published processes
     */
    @Query("SELECT p FROM ProcessDefinition p WHERE p.published = true " +
           "AND p.deleted = false AND p.isLatestVersion = true")
    Page<ProcessDefinition> findAllPublished(Pageable pageable);

    /**
     * Find processes by category
     * @param categoryId category ID
     * @param pageable pagination parameters
     * @return page of processes in category (latest versions only)
     */
    @Query("SELECT p FROM ProcessDefinition p WHERE p.category.id = :categoryId " +
           "AND p.deleted = false AND p.isLatestVersion = true")
    Page<ProcessDefinition> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * Search processes by name, key, or tags (latest versions only)
     * @param searchTerm search term
     * @param pageable pagination parameters
     * @return page of matching processes
     */
    @Query("SELECT p FROM ProcessDefinition p WHERE p.deleted = false AND p.isLatestVersion = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.processKey) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.tags) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<ProcessDefinition> searchProcesses(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Mark all versions of a process key as not latest
     * Used before creating a new version
     * @param processKey process key
     */
    @Modifying
    @Query("UPDATE ProcessDefinition p SET p.isLatestVersion = false WHERE p.processKey = :processKey")
    void markAllVersionsAsNotLatest(@Param("processKey") String processKey);

    /**
     * Find deployed processes (with deploymentId not null)
     * @param pageable pagination parameters
     * @return page of deployed processes
     */
    @Query("SELECT p FROM ProcessDefinition p WHERE p.deploymentId IS NOT NULL " +
           "AND p.deleted = false")
    Page<ProcessDefinition> findAllDeployed(Pageable pageable);

    /**
     * Count processes by category
     * @param categoryId category ID
     * @return count of processes
     */
    @Query("SELECT COUNT(p) FROM ProcessDefinition p WHERE p.category.id = :categoryId " +
           "AND p.deleted = false AND p.isLatestVersion = true")
    long countByCategoryId(@Param("categoryId") Long categoryId);
}
