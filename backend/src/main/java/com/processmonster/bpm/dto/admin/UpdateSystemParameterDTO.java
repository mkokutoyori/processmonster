package com.processmonster.bpm.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating a system parameter
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSystemParameterDTO {

    private String description;

    private String category;

    private String validationPattern;

    private String allowedValues;

    private Integer displayOrder;

    private Boolean editable;
}
