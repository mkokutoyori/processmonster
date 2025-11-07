package com.processmonster.bpm.dto.process;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for ProcessDefinition entity (response)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDefinitionDTO {

    private Long id;
    private String processKey;
    private String name;
    private Integer version;
    private Boolean isLatestVersion;
    private String description;
    private Boolean isTemplate;
    private Boolean published;
    private Boolean active;
    private String tags;
    private LocalDateTime deployedAt;
    private String deployedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    /**
     * Category information (optional)
     */
    private ProcessCategoryDTO category;

    /**
     * List of tags (parsed from tags string)
     */
    private List<String> tagList;

    /**
     * Full identifier (processKey:vX)
     */
    private String fullIdentifier;

    /**
     * Is deployed flag
     */
    private Boolean deployed;
}
