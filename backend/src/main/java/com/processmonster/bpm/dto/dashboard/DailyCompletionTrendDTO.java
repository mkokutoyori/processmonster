package com.processmonster.bpm.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for daily task completion trend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Daily task completion trend data")
public class DailyCompletionTrendDTO {

    @Schema(description = "Map of date (YYYY-MM-DD) to task count",
            example = "{\"2025-11-01\": 15, \"2025-11-02\": 23, \"2025-11-03\": 18}")
    private Map<String, Long> dailyCompletions;

    @Schema(description = "Number of days included in trend", example = "7")
    private Integer days;
}
