package com.processmonster.bpm.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a system parameter
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSystemParameterDTO {

    @NotBlank(message = "{systemparameter.key.required}")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "{systemparameter.key.format}")
    private String key;

    private String value;

    private String description;

    private String category;

    @Pattern(regexp = "^(STRING|INTEGER|LONG|DOUBLE|BOOLEAN)$", message = "{systemparameter.datatype.invalid}")
    private String dataType;

    private String defaultValue;

    private Boolean encrypted;

    private Boolean editable;

    private String validationPattern;

    private String allowedValues;

    private Integer displayOrder;
}
