package com.processmonster.bpm.repository;

import com.processmonster.bpm.entity.ProcessVariable;
import com.processmonster.bpm.entity.ProcessVariable.VariableScope;
import com.processmonster.bpm.entity.ProcessVariable.VariableType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ProcessVariable entity
 */
@Repository
public interface ProcessVariableRepository extends JpaRepository<ProcessVariable, Long> {

    /**
     * Find all variables for a process instance
     */
    @Query("SELECT pv FROM ProcessVariable pv WHERE pv.processInstance.id = :instanceId")
    List<ProcessVariable> findByProcessInstanceId(@Param("instanceId") Long instanceId);

    /**
     * Find variable by instance ID and name
     */
    @Query("SELECT pv FROM ProcessVariable pv WHERE pv.processInstance.id = :instanceId " +
           "AND pv.variableName = :variableName")
    Optional<ProcessVariable> findByInstanceIdAndName(
            @Param("instanceId") Long instanceId,
            @Param("variableName") String variableName);

    /**
     * Find variables by scope
     */
    @Query("SELECT pv FROM ProcessVariable pv WHERE pv.processInstance.id = :instanceId " +
           "AND pv.scope = :scope")
    List<ProcessVariable> findByInstanceIdAndScope(
            @Param("instanceId") Long instanceId,
            @Param("scope") VariableScope scope);

    /**
     * Find non-transient variables (for persistence)
     */
    @Query("SELECT pv FROM ProcessVariable pv WHERE pv.processInstance.id = :instanceId " +
           "AND pv.isTransient = false")
    List<ProcessVariable> findNonTransientVariables(@Param("instanceId") Long instanceId);

    /**
     * Find variables by type
     */
    @Query("SELECT pv FROM ProcessVariable pv WHERE pv.processInstance.id = :instanceId " +
           "AND pv.variableType = :type")
    List<ProcessVariable> findByInstanceIdAndType(
            @Param("instanceId") Long instanceId,
            @Param("type") VariableType type);

    /**
     * Check if variable exists
     */
    @Query("SELECT COUNT(pv) > 0 FROM ProcessVariable pv WHERE pv.processInstance.id = :instanceId " +
           "AND pv.variableName = :variableName")
    boolean existsByInstanceIdAndName(
            @Param("instanceId") Long instanceId,
            @Param("variableName") String variableName);

    /**
     * Delete variable by instance ID and name
     */
    @Modifying
    @Query("DELETE FROM ProcessVariable pv WHERE pv.processInstance.id = :instanceId " +
           "AND pv.variableName = :variableName")
    void deleteByInstanceIdAndName(
            @Param("instanceId") Long instanceId,
            @Param("variableName") String variableName);

    /**
     * Delete all variables for an instance
     */
    @Modifying
    @Query("DELETE FROM ProcessVariable pv WHERE pv.processInstance.id = :instanceId")
    void deleteByInstanceId(@Param("instanceId") Long instanceId);

    /**
     * Delete transient variables for an instance
     */
    @Modifying
    @Query("DELETE FROM ProcessVariable pv WHERE pv.processInstance.id = :instanceId " +
           "AND pv.isTransient = true")
    void deleteTransientVariables(@Param("instanceId") Long instanceId);

    /**
     * Count variables for an instance
     */
    @Query("SELECT COUNT(pv) FROM ProcessVariable pv WHERE pv.processInstance.id = :instanceId")
    long countByInstanceId(@Param("instanceId") Long instanceId);

    /**
     * Search variables by name pattern
     */
    @Query("SELECT pv FROM ProcessVariable pv WHERE pv.processInstance.id = :instanceId " +
           "AND LOWER(pv.variableName) LIKE LOWER(CONCAT('%', :pattern, '%'))")
    List<ProcessVariable> searchByNamePattern(
            @Param("instanceId") Long instanceId,
            @Param("pattern") String pattern);
}
