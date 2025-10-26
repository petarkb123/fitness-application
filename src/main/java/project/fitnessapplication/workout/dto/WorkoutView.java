package project.fitnessapplication.workout.dto;

import lombok.Data;
import project.fitnessapplication.config.TimezoneConfig;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkoutView {
    private final LocalDateTime startedAt;
    private final LocalDateTime finishedAt;
    private final List<ExerciseBlock> exercises;
    private final String region;
    
    public WorkoutView(LocalDateTime startedAt, LocalDateTime finishedAt, List<ExerciseBlock> exercises, String region) {
        // Convert times to user's regional timezone if region is provided
        if (region != null && !region.isEmpty()) {
            this.startedAt = TimezoneConfig.convertToRegionalTime(startedAt, region);
            this.finishedAt = TimezoneConfig.convertToRegionalTime(finishedAt, region);
        } else {
            this.startedAt = startedAt;
            this.finishedAt = finishedAt;
        }
        this.exercises = exercises;
        this.region = region;
    }
}

