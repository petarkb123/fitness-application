package project.fitnessapplication.workout.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.Map;

@Value
@Builder
public class LastPerformanceData {
    // Map of setNumber -> {weight, reps}
    Map<Integer, SetData> sets;
    
    @Value
    @Builder
    public static class SetData {
        BigDecimal weight;
        Integer reps;
    }
    
    public static LastPerformanceData empty() {
        return LastPerformanceData.builder()
            .sets(Map.of())
            .build();
    }
}

