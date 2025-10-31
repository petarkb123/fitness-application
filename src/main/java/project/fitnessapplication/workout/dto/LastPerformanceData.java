package project.fitnessapplication.workout.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Value
@Builder
public class LastPerformanceData {
    // Map of setNumber -> {weight, reps, dropSets}
    Map<Integer, SetData> sets;
    
    @Value
    @Builder
    public static class SetData {
        BigDecimal weight;
        Integer reps;
        // List of drop sets for this main set (ordered by groupOrder)
        List<DropSetData> dropSets;
    }
    
    @Value
    @Builder
    public static class DropSetData {
        Integer groupOrder;
        BigDecimal weight;
        Integer reps;
    }
    
    public static LastPerformanceData empty() {
        return LastPerformanceData.builder()
            .sets(Map.of())
            .build();
    }
}

