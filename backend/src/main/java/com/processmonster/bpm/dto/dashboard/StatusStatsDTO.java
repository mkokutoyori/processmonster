package com.processmonster.bpm.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for statistics grouped by status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Statistics grouped by status")
public class StatusStatsDTO {

    @Schema(description = "Map of status to count", example = "{\"RUNNING\": 42, \"COMPLETED\": 150, \"FAILED\": 3}")
    private Map<String, Long> stats;
}
