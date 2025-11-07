package project.fitnessapplication.stats.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TemplateComparisonDto(
        UUID templateId,
        String templateName,
        WorkoutComparison workout1,
        WorkoutComparison workout2,
        ComparisonSummary summary,
        List<ExerciseComparison> exerciseComparisons
) {
    public record WorkoutComparison(
            UUID sessionId,
            LocalDateTime date,
            BigDecimal totalVolume,
            int totalSets,
            int duration
    ) {}
    
    public record ComparisonSummary(
            BigDecimal volumeDifference,
            double volumeChangePercent,
            int setsDifference,
            String overallTrend
    ) {}
    
    public record ExerciseComparison(
            UUID exerciseId,
            String exerciseName,
            String muscleGroup,
            ExerciseWorkoutData workout1Data,
            ExerciseWorkoutData workout2Data,
            ExerciseDifference difference
    ) {
        public record ExerciseWorkoutData(
                int sets,
                BigDecimal avgWeight,
                BigDecimal maxWeight,
                int totalReps,
                BigDecimal volume
        ) {}
        
        public record ExerciseDifference(
                BigDecimal weightChange,
                double weightChangePercent,
                int setsChange,
                BigDecimal volumeChange,
                double volumeChangePercent,
                String trend
        ) {}
    }
}

