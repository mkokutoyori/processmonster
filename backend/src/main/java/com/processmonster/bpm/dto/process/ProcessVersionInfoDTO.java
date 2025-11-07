package com.processmonster.bpm.dto.process;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for simplified process version information (for version listing)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessVersionInfoDTO {

    private Long id;
    private String processKey;
    private String name;
    private Integer version;
    private Boolean isLatestVersion;
    private Boolean published;
    private Boolean active;
    private Boolean deployed;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime deployedAt;
    private String deployedBy;

    /**
     * Full identifier (processKey:vX)
     */
    private String fullIdentifier;
}
