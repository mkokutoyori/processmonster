package com.processmonster.bpm.repository;

import com.processmonster.bpm.entity.ProcessCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ProcessCategory entity
 */
@Repository
public interface ProcessCategoryRepository extends JpaRepository<ProcessCategory, Long> {

    /**
     * Find all active categories (not deleted)
     * @param pageable pagination parameters
     * @return page of categories
     */
    Page<ProcessCategory> findByDeletedFalse(Pageable pageable);

    /**
     * Find all active categories as list (for dropdowns)
     * @return list of active categories ordered by displayOrder
     */
    @Query("SELECT c FROM ProcessCategory c WHERE c.deleted = false ORDER BY c.displayOrder ASC, c.name ASC")
    List<ProcessCategory> findAllActiveOrderedByDisplayOrder();

    /**
     * Find category by code (case-insensitive)
     * @param code category code
     * @return optional category
     */
    @Query("SELECT c FROM ProcessCategory c WHERE LOWER(c.code) = LOWER(:code) AND c.deleted = false")
    Optional<ProcessCategory> findByCodeIgnoreCase(@Param("code") String code);

    /**
     * Find category by ID and not deleted
     * @param id category ID
     * @return optional category
     */
    @Query("SELECT c FROM ProcessCategory c WHERE c.id = :id AND c.deleted = false")
    Optional<ProcessCategory> findByIdAndNotDeleted(@Param("id") Long id);

    /**
     * Check if a category code already exists (case-insensitive)
     * @param code category code
     * @return true if exists
     */
    @Query("SELECT COUNT(c) > 0 FROM ProcessCategory c WHERE LOWER(c.code) = LOWER(:code) AND c.deleted = false")
    boolean existsByCodeIgnoreCase(@Param("code") String code);

    /**
     * Check if a category code exists excluding a specific ID (for updates)
     * @param code category code
     * @param id category ID to exclude
     * @return true if exists
     */
    @Query("SELECT COUNT(c) > 0 FROM ProcessCategory c WHERE LOWER(c.code) = LOWER(:code) AND c.id != :id AND c.deleted = false")
    boolean existsByCodeIgnoreCaseAndIdNot(@Param("code") String code, @Param("id") Long id);

    /**
     * Find active categories
     * @param pageable pagination parameters
     * @return page of active categories
     */
    Page<ProcessCategory> findByActiveTrueAndDeletedFalse(Pageable pageable);

    /**
     * Search categories by name or code
     * @param searchTerm search term
     * @param pageable pagination parameters
     * @return page of matching categories
     */
    @Query("SELECT c FROM ProcessCategory c WHERE c.deleted = false AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<ProcessCategory> searchCategories(@Param("searchTerm") String searchTerm, Pageable pageable);
}
