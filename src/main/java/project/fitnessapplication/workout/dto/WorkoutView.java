package project.fitnessapplication.workout.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkoutView {
    private final LocalDateTime startedAt;
    private final LocalDateTime finishedAt;
    private final List<ExerciseBlock> exercises;
    
    public WorkoutView(LocalDateTime startedAt, LocalDateTime finishedAt, List<ExerciseBlock> exercises) {
        // Times should already be converted to user's local timezone before passing to this constructor
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.exercises = exercises;
    }
}

