package com.processmonster.bpm.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for system parameter information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemParameterDTO {

    private Long id;
    private String key;
    private String value;
    private String description;
    private String category;
    private String dataType;
    private String defaultValue;
    private Boolean encrypted;
    private Boolean editable;
    private String validationPattern;
    private String allowedValues;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
