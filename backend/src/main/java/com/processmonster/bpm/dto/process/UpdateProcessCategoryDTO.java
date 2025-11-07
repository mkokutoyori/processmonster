package com.processmonster.bpm.dto.process;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing ProcessCategory
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProcessCategoryDTO {

    @Size(min = 2, max = 50, message = "{validation.size}")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "{process.category.code.pattern}")
    private String code;

    @Size(min = 2, max = 100, message = "{validation.size}")
    private String name;

    @Size(max = 500, message = "{validation.size.max}")
    private String description;

    @Size(max = 50, message = "{validation.size.max}")
    private String icon;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "{process.category.color.pattern}")
    private String color;

    private Integer displayOrder;

    private Boolean active;
}
