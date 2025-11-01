package project.fitnessapplication.workout.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class FinishWorkoutRequest {
    @NotNull
    private UUID sessionId;
    private List<ExercisePayload> exercises;
    // Optional: allow editing start/finish times when updating an existing workout
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}

