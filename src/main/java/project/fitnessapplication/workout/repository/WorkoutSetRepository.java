package project.fitnessapplication.workout.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.fitnessapplication.workout.model.WorkoutSet;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface WorkoutSetRepository extends JpaRepository<WorkoutSet, UUID> {

    List<WorkoutSet> findAllBySessionIdIn(Collection<UUID> sessionIds);

    void deleteBySessionId(UUID sessionId);

    List<WorkoutSet> findAllBySessionIdOrderByExerciseOrderAscIdAsc(UUID sessionId);
    
    List<WorkoutSet> findAllBySessionIdOrderByExerciseOrderAscSetNumberAsc(UUID sessionId);
    
    List<WorkoutSet> findAllBySessionId(UUID sessionId);
    
    @Query("SELECT ws FROM WorkoutSet ws " +
           "JOIN WorkoutSession s ON ws.sessionId = s.id " +
           "WHERE ws.exerciseId = :exerciseId AND s.userId = :userId " +
           "AND ws.weight IS NOT NULL AND ws.reps IS NOT NULL " +
           "AND (ws.groupType IS NULL OR ws.groupOrder = 0) " +
           "ORDER BY s.startedAt DESC")
    List<WorkoutSet> findLastSetsByExerciseAndUser(@Param("exerciseId") UUID exerciseId, 
                                                     @Param("userId") UUID userId);
}
