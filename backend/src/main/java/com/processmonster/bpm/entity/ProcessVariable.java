package com.processmonster.bpm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing a process variable (data associated with a process instance)
 */
@Entity
@Table(name = "process_variables",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"process_instance_id", "variableName"})
        },
        indexes = {
            @Index(name = "idx_variable_instance", columnList = "process_instance_id"),
            @Index(name = "idx_variable_name", columnList = "variableName"),
            @Index(name = "idx_variable_type", columnList = "variableType")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ProcessVariable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the process instance
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_instance_id", nullable = false)
    private ProcessInstance processInstance;

    /**
     * Variable name (must be unique within a process instance)
     */
    @Column(nullable = false, length = 255)
    private String variableName;

    /**
     * Variable type (STRING, INTEGER, DOUBLE, BOOLEAN, DATE, JSON, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VariableType variableType;

    /**
     * String value (for STRING type)
     */
    @Column(columnDefinition = "TEXT")
    private String stringValue;

    /**
     * Integer value (for INTEGER type)
     */
    private Long integerValue;

    /**
     * Double value (for DOUBLE type)
     */
    private Double doubleValue;

    /**
     * Boolean value (for BOOLEAN type)
     */
    private Boolean booleanValue;

    /**
     * Date value (for DATE type)
     */
    private LocalDateTime dateValue;

    /**
     * JSON value (for JSON/OBJECT type)
     */
    @Column(columnDefinition = "TEXT")
    private String jsonValue;

    /**
     * Binary value (for BYTES type) - stored as base64
     */
    @Column(columnDefinition = "TEXT")
    private String bytesValue;

    /**
     * Scope of the variable (GLOBAL, LOCAL, TRANSIENT)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VariableScope scope = VariableScope.GLOBAL;

    /**
     * Whether this is a transient variable (not persisted to history)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isTransient = false;

    /**
     * Audit fields
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * Variable type enum
     */
    public enum VariableType {
        STRING,     // Text value
        INTEGER,    // Long integer
        DOUBLE,     // Floating point
        BOOLEAN,    // True/False
        DATE,       // Date/Time
        JSON,       // JSON object/array
        BYTES       // Binary data
    }

    /**
     * Variable scope enum
     */
    public enum VariableScope {
        GLOBAL,     // Available to entire process instance
        LOCAL,      // Available only to current execution
        TRANSIENT   // Not persisted, only in memory
    }

    /**
     * Get the value as Object based on type
     */
    public Object getValue() {
        return switch (variableType) {
            case STRING -> stringValue;
            case INTEGER -> integerValue;
            case DOUBLE -> doubleValue;
            case BOOLEAN -> booleanValue;
            case DATE -> dateValue;
            case JSON -> jsonValue;
            case BYTES -> bytesValue;
        };
    }

    /**
     * Set the value based on type
     */
    public void setValue(Object value) {
        if (value == null) {
            clearValues();
            return;
        }

        clearValues();
        switch (variableType) {
            case STRING -> stringValue = value.toString();
            case INTEGER -> integerValue = value instanceof Number ? ((Number) value).longValue() : Long.parseLong(value.toString());
            case DOUBLE -> doubleValue = value instanceof Number ? ((Number) value).doubleValue() : Double.parseDouble(value.toString());
            case BOOLEAN -> booleanValue = value instanceof Boolean ? (Boolean) value : Boolean.parseBoolean(value.toString());
            case DATE -> dateValue = value instanceof LocalDateTime ? (LocalDateTime) value : LocalDateTime.parse(value.toString());
            case JSON -> jsonValue = value.toString();
            case BYTES -> bytesValue = value.toString();
        }
    }

    /**
     * Clear all value fields
     */
    private void clearValues() {
        stringValue = null;
        integerValue = null;
        doubleValue = null;
        booleanValue = null;
        dateValue = null;
        jsonValue = null;
        bytesValue = null;
    }
}
