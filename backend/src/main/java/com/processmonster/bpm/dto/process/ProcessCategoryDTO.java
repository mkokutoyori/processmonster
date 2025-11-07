package com.processmonster.bpm.dto.process;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for ProcessCategory entity (response)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessCategoryDTO {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String icon;
    private String color;
    private Integer displayOrder;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    /**
     * Count of processes in this category (optional, populated on demand)
     */
    private Long processCount;
}
