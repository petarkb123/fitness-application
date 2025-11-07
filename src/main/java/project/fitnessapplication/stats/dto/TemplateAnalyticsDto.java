package project.fitnessapplication.stats.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TemplateAnalyticsDto(
        UUID templateId,
        String templateName,
        BigDecimal totalVolume,
        int totalSessions,
        BigDecimal avgVolumePerSession,
        String trend,
        double trendPercent,
        LocalDate lastUsedDate,
        LocalDate firstUsedDate,
        List<TemplateExerciseStatsDto> exercises
) {
    public record TemplateExerciseStatsDto(
            UUID exerciseId,
            String exerciseName,
            String muscleGroup,
            int totalSessions,
            int totalSets,
            BigDecimal startingWeight,
            BigDecimal currentWeight,
            double progressPercent,
            BigDecimal volumeContribution,
            List<ProgressPoint> progressPoints
    ) {
        public record ProgressPoint(
                LocalDate date,
                BigDecimal weight,
                Integer reps,
                BigDecimal volume
        ) {}
    }
}

