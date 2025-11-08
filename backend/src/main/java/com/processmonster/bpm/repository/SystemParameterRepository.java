package com.processmonster.bpm.repository;

import com.processmonster.bpm.entity.SystemParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SystemParameter entity
 */
@Repository
public interface SystemParameterRepository extends JpaRepository<SystemParameter, Long> {

    /**
     * Find parameter by key
     */
    Optional<SystemParameter> findByKeyAndDeletedFalse(String key);

    /**
     * Find parameter by ID (not deleted)
     */
    Optional<SystemParameter> findByIdAndDeletedFalse(Long id);

    /**
     * Find all parameters (not deleted)
     */
    Page<SystemParameter> findByDeletedFalse(Pageable pageable);

    /**
     * Find all parameters (not deleted) - List version
     */
    List<SystemParameter> findByDeletedFalse();

    /**
     * Find parameters by category
     */
    @Query("SELECT sp FROM SystemParameter sp WHERE sp.category = :category " +
           "AND sp.deleted = false ORDER BY sp.displayOrder, sp.key")
    List<SystemParameter> findByCategory(@Param("category") String category);

    /**
     * Find editable parameters
     */
    @Query("SELECT sp FROM SystemParameter sp WHERE sp.editable = true " +
           "AND sp.deleted = false ORDER BY sp.category, sp.displayOrder, sp.key")
    List<SystemParameter> findEditableParameters();

    /**
     * Search parameters by key or description
     */
    @Query("SELECT sp FROM SystemParameter sp WHERE sp.deleted = false AND " +
           "(LOWER(sp.key) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(sp.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<SystemParameter> searchParameters(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Check if key exists
     */
    boolean existsByKeyAndDeletedFalse(String key);

    /**
     * Count parameters by category
     */
    long countByCategoryAndDeletedFalse(String category);

    /**
     * Find all categories
     */
    @Query("SELECT DISTINCT sp.category FROM SystemParameter sp WHERE sp.deleted = false ORDER BY sp.category")
    List<String> findAllCategories();
}
