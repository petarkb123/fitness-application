package project.fitnessapplication.stats.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.fitnessapplication.exercise.model.Exercise;
import project.fitnessapplication.exercise.repository.ExerciseRepository;
import project.fitnessapplication.stats.dto.*;
import project.fitnessapplication.template.model.TemplateItem;
import project.fitnessapplication.template.model.WorkoutTemplate;
import project.fitnessapplication.template.repository.TemplateItemRepository;
import project.fitnessapplication.template.repository.WorkoutTemplateRepository;
import project.fitnessapplication.workout.model.WorkoutSession;
import project.fitnessapplication.workout.model.WorkoutSet;
import project.fitnessapplication.workout.repository.WorkoutSessionRepository;
import project.fitnessapplication.workout.repository.WorkoutSetRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdvancedStatsService {

    private final WorkoutSessionRepository sessionRepo;
    private final WorkoutSetRepository setRepo;
    private final ExerciseRepository exerciseRepo;
    private final WorkoutTemplateRepository templateRepo;
    private final TemplateItemRepository templateItemRepo;

    public AdvancedStatsService(
            WorkoutSessionRepository sessionRepo,
            WorkoutSetRepository setRepo,
            ExerciseRepository exerciseRepo,
            WorkoutTemplateRepository templateRepo,
            TemplateItemRepository templateItemRepo) {
        this.sessionRepo = sessionRepo;
        this.setRepo = setRepo;
        this.exerciseRepo = exerciseRepo;
        this.templateRepo = templateRepo;
        this.templateItemRepo = templateItemRepo;
    }

    /**
     * Get comprehensive training frequency analysis
     */
    public TrainingFrequencyDto getTrainingFrequency(UUID userId, LocalDate from, LocalDate to, String workoutFrequency) {
        LocalDateTime fromTs = from.atStartOfDay();
        LocalDateTime toTs = to.plusDays(1).atStartOfDay().minusNanos(1);

        List<WorkoutSession> sessions = sessionRepo
                .findByUserIdAndStartedAtBetweenOrderByStartedAtAsc(userId, fromTs, toTs);

        if (sessions.isEmpty()) {
            return new TrainingFrequencyDto(0, 0.0, Map.of(), List.of(), 0, 0.0, 0.0);
        }

        
        int totalWorkouts = sessions.size();

        
        long daysBetween = ChronoUnit.DAYS.between(from, to) + 1;
        double weeksCount = daysBetween / 7.0;
        // Prevent division by zero - ensure at least 1 week for calculation
        if (weeksCount <= 0) {
            weeksCount = 1.0;
        }
        
        // Calculate average workouts per week
        // For very short periods, we need to be conservative to avoid unrealistic extrapolation
        double avgPerWeek;
        if (daysBetween < 7) {
            // For periods shorter than a week, calculate conservatively
            // Don't extrapolate more than 1 workout per day
            // If they did X workouts in Y days, cap the weekly average at (X/Y) * 7, but max 7 workouts/week
            double workoutsPerDay = totalWorkouts / (double) daysBetween;
            avgPerWeek = workoutsPerDay * 7.0;
            // Cap at 7 workouts per week (can't realistically do more than 1 workout per day consistently)
            avgPerWeek = Math.min(7.0, avgPerWeek);
        } else {
            // For periods of a week or longer, use standard calculation
            avgPerWeek = totalWorkouts / weeksCount;
            // Still cap at 7 workouts per week for realism
            avgPerWeek = Math.min(7.0, avgPerWeek);
        }
        
        // Calculate unique workout days (days with at least one workout)
        Set<LocalDate> uniqueWorkoutDays = sessions.stream()
                .map(s -> s.getStartedAt().toLocalDate())
                .collect(Collectors.toSet());
        int uniqueDaysCount = uniqueWorkoutDays.size();
        
        // Calculate average unique workout days per week
        // This represents how many days per week the user actually trained
        // For very short periods, we need to be careful not to over-extrapolate
        double avgUniqueDaysPerWeek;
        if (daysBetween < 7) {
            // For periods shorter than a week, calculate training rate
            // But don't extrapolate to full 7 days - use a more conservative approach
            // If they trained X days out of Y days, that's their rate
            // We'll use this rate but cap it reasonably
            double trainingRate = uniqueDaysCount / (double) daysBetween;
            // For very short periods, be conservative: don't assume they'll maintain 100% rate
            // Instead, calculate: if they maintain this rate over a week, how many days?
            // But cap it at the number of unique days (can't extrapolate more than what we see)
            avgUniqueDaysPerWeek = Math.min(uniqueDaysCount, trainingRate * 7.0);
            // Also cap at 7.0 (can't train more than 7 days per week)
            avgUniqueDaysPerWeek = Math.min(7.0, avgUniqueDaysPerWeek);
        } else {
            // For periods of a week or longer, use the standard calculation
            avgUniqueDaysPerWeek = uniqueDaysCount / weeksCount;
            // Cap at 7.0 (can't train more than 7 days per week)
            avgUniqueDaysPerWeek = Math.min(7.0, avgUniqueDaysPerWeek);
        }
        

        
        // Calculate day of week for current week only
        LocalDate today = LocalDate.now();
        LocalDate currentWeekStart = today.with(DayOfWeek.MONDAY);
        LocalDate currentWeekEnd = currentWeekStart.plusDays(6);
        
        Map<String, Integer> byDayOfWeek = new LinkedHashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            byDayOfWeek.put(day.name(), 0);
        }
        for (WorkoutSession s : sessions) {
            LocalDate sessionDate = s.getStartedAt().toLocalDate();
            // Only count sessions from the current week
            if (!sessionDate.isBefore(currentWeekStart) && !sessionDate.isAfter(currentWeekEnd)) {
                String dayName = s.getStartedAt().getDayOfWeek().name();
                byDayOfWeek.put(dayName, byDayOfWeek.get(dayName) + 1);
            }
        }

        
        List<TrainingFrequencyDto.WeeklyBreakdown> weeklyBreakdown = new ArrayList<>();
        LocalDate weekStart = from;
        while (!weekStart.isAfter(to)) {
            LocalDate weekEnd = weekStart.plusDays(6);
            if (weekEnd.isAfter(to)) weekEnd = to;

            LocalDateTime weekStartTs = weekStart.atStartOfDay();
            LocalDateTime weekEndTs = weekEnd.plusDays(1).atStartOfDay().minusNanos(1);

            long count = sessions.stream()
                    .filter(s -> !s.getStartedAt().isBefore(weekStartTs) && !s.getStartedAt().isAfter(weekEndTs))
                    .count();

            weeklyBreakdown.add(new TrainingFrequencyDto.WeeklyBreakdown(
                    weekStart, weekEnd, (int) count
            ));

            weekStart = weekStart.plusDays(7);
        }

        
        int longestStreak = calculateLongestStreak(sessions, from, to);

        
        double currentStreak = calculateCurrentStreak(sessions, to);
        
        // Calculate consistency score based on user's target workout frequency
        // Use avgUniqueDaysPerWeek (days trained per week) instead of avgPerWeek (workouts per week)
        double consistencyScore = calculateConsistencyScore(avgUniqueDaysPerWeek, workoutFrequency);
        // Round to 1 decimal place
        double roundedScore = Math.round(consistencyScore * 10.0) / 10.0;

        return new TrainingFrequencyDto(
                totalWorkouts,
                Math.round(avgPerWeek * 10.0) / 10.0,
                byDayOfWeek,
                weeklyBreakdown,
                longestStreak,
                currentStreak,
                roundedScore
        );
    }
    
    /**
     * Calculate consistency score based on user's target workout frequency
     * Formula: (avgUniqueDaysPerWeek / targetDaysPerWeek) × 100
     * This uses the number of unique days trained per week, not total workouts
     * LOW (0-2 days) -> divide by 2, MEDIUM (3-5 days) -> divide by 4, HIGH (6+ days) -> divide by 6
     * Score is calculated as percentage of target days, capped at 100%
     */
    private double calculateConsistencyScore(double avgUniqueDaysPerWeek, String workoutFrequency) {
        // Handle edge case: if avgUniqueDaysPerWeek is 0, return 0
        if (avgUniqueDaysPerWeek <= 0) {
            return 0.0;
        }
        
        // Handle null or empty workout frequency - normalize robustly
        String normalizedFreq = null;
        if (workoutFrequency != null && !workoutFrequency.trim().isEmpty()) {
            // Normalize: trim whitespace, convert to uppercase, remove any extra spaces
            normalizedFreq = workoutFrequency.trim().replaceAll("\\s+", " ").toUpperCase();
        }
        
        double targetDaysPerWeek;
        
        if (normalizedFreq == null || normalizedFreq.isEmpty()) {
            // Default: compare to 3.5 days per week (50% of 7 days)
            targetDaysPerWeek = 3.5;
        } else {
            // Match frequency with case-insensitive comparison and handle variations
            switch (normalizedFreq) {
                case "LOW":
                    // LOW (0-2 days): calculate with 2 as denominator
                    // Example: 2 workouts/week = (2/2) × 100 = 100%
                    targetDaysPerWeek = 2.0;
                    break;
                case "MEDIUM":
                    // MEDIUM (3-5 days): calculate with 4 as denominator
                    // Example: 4 workouts/week = (4/4) × 100 = 100%
                    targetDaysPerWeek = 4.0;
                    break;
                case "HIGH":
                    // HIGH (6 days): calculate with 6 as denominator
                    // Example: 6 workouts/week = (6/6) × 100 = 100%
                    // For 1 workout/week with HIGH: (1/6) × 100 = 16.67%
                    targetDaysPerWeek = 6.0;
                    break;
                case "ATHLETE":
                    // ATHLETE (7+ days): calculate with 7 as denominator
                    // Example: 7 workouts/week = (7/7) × 100 = 100%
                    // For 1 workout/week with ATHLETE: (1/7) × 100 = 14.29%
                    targetDaysPerWeek = 7.0;
                    break;
                default:
                    // If frequency doesn't match expected values, use default
                    // This handles any unexpected values in the database
                    targetDaysPerWeek = 3.5;
                    break;
            }
        }
        
        // Prevent division by zero
        if (targetDaysPerWeek <= 0) {
            targetDaysPerWeek = 3.5;
        }
        
        // Calculate percentage: (actual unique days trained per week / target days per week) × 100
        // Example with 1 day trained/week and HIGH frequency: (1/6) × 100 = 16.67%
        double score = (avgUniqueDaysPerWeek / targetDaysPerWeek) * 100.0;
        
        // Ensure score is between 0 and 100 (cap at 100%, don't allow over 100%)
        double finalScore = Math.max(0.0, Math.min(100.0, score));
        
        return finalScore;
    }

    /**
     * Get exercise-specific volume trends
     */
    public List<ExerciseVolumeTrendDto> getExerciseVolumeTrends(UUID userId, LocalDate from, LocalDate to) {
        LocalDateTime fromTs = from.atStartOfDay();
        LocalDateTime toTs = to.plusDays(1).atStartOfDay().minusNanos(1);

        List<WorkoutSession> sessions = sessionRepo
                .findByUserIdAndStartedAtBetweenOrderByStartedAtAsc(userId, fromTs, toTs);

        if (sessions.isEmpty()) {
            return List.of();
        }

        Map<UUID, WorkoutSession> sessionMap = sessions.stream()
                .collect(Collectors.toMap(WorkoutSession::getId, s -> s));

        List<UUID> sessionIds = sessions.stream().map(WorkoutSession::getId).toList();
        List<WorkoutSet> allSets = setRepo.findAllBySessionIdIn(sessionIds);

        
        Map<UUID, List<WorkoutSet>> setsByExercise = allSets.stream()
                .collect(Collectors.groupingBy(WorkoutSet::getExerciseId));

        
        Set<UUID> exerciseIds = setsByExercise.keySet();
        Map<UUID, Exercise> exerciseMap = exerciseRepo.findAllById(exerciseIds).stream()
                .collect(Collectors.toMap(Exercise::getId, e -> e));

        List<ExerciseVolumeTrendDto> trends = new ArrayList<>();

        for (Map.Entry<UUID, List<WorkoutSet>> entry : setsByExercise.entrySet()) {
            UUID exerciseId = entry.getKey();
            List<WorkoutSet> sets = entry.getValue();
            Exercise exercise = exerciseMap.get(exerciseId);

            if (exercise == null) continue;

            
            Map<LocalDate, BigDecimal> weeklyVolume = new TreeMap<>();
            Map<LocalDate, Integer> weeklySets = new TreeMap<>();

            for (WorkoutSet set : sets) {
                WorkoutSession session = sessionMap.get(set.getSessionId());
                if (session == null) continue;
                
                // Skip warmup sets
                if (set.isWarmup()) continue;
                // Skip drop sets - only track main sets for volume trends
                if (set.getGroupType() != null && set.getGroupType().name().equals("DROP_SET") && 
                    set.getGroupOrder() != null && set.getGroupOrder() > 0) {
                    continue;
                }

                LocalDate weekStart = session.getStartedAt().toLocalDate()
                        .with(DayOfWeek.MONDAY);

                BigDecimal setVolume = BigDecimal.ZERO;
                if (set.getWeight() != null && set.getReps() != null) {
                    setVolume = set.getWeight().multiply(BigDecimal.valueOf(set.getReps()));
                }

                weeklyVolume.merge(weekStart, setVolume, BigDecimal::add);
                weeklySets.merge(weekStart, 1, Integer::sum);
            }

            
            // Calculate total volume excluding warmup and drop sets
            BigDecimal totalVolume = sets.stream()
                    .filter(s -> s.getWeight() != null && s.getReps() != null)
                    .filter(s -> !s.isWarmup())
                    .filter(s -> {
                        if (s.getGroupType() == null) return true;
                        if (s.getGroupType().name().equals("DROP_SET")) {
                            return s.getGroupOrder() == null || s.getGroupOrder() == 0;
                        }
                        return true;
                    })
                    .map(s -> s.getWeight().multiply(BigDecimal.valueOf(s.getReps())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Count total sets excluding warmup and drop sets
            int totalSets = (int) sets.stream()
                    .filter(s -> !s.isWarmup())
                    .filter(s -> {
                        if (s.getGroupType() == null) return true;
                        if (s.getGroupType().name().equals("DROP_SET")) {
                            return s.getGroupOrder() == null || s.getGroupOrder() == 0;
                        }
                        return true;
                    })
                    .count();
            
            // Calculate average volume per session
            // Count unique sessions for this exercise (where this exercise was performed)
            Set<UUID> uniqueSessions = sets.stream()
                    .map(WorkoutSet::getSessionId)
                    .collect(Collectors.toSet());
            int sessionCount = uniqueSessions.size();
            BigDecimal avgVolumePerSession = BigDecimal.ZERO;
            if (sessionCount > 0) {
                avgVolumePerSession = totalVolume.divide(
                    BigDecimal.valueOf(sessionCount),
                    2,
                    RoundingMode.HALF_UP
                );
            }

            
            List<LocalDate> weeks = new ArrayList<>(weeklyVolume.keySet());
            String trend = "stable";
            if (weeks.size() >= 2) {
                int midPoint = weeks.size() / 2;
                int firstHalfSize = midPoint;
                int secondHalfSize = weeks.size() - midPoint;
                
                // Prevent division by zero
                if (firstHalfSize > 0 && secondHalfSize > 0) {
                    BigDecimal firstHalfAvg = weeks.subList(0, midPoint).stream()
                            .map(weeklyVolume::get)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(firstHalfSize), 2, RoundingMode.HALF_UP);

                    BigDecimal secondHalfAvg = weeks.subList(midPoint, weeks.size()).stream()
                            .map(weeklyVolume::get)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(secondHalfSize), 2, RoundingMode.HALF_UP);

                    // Only compare if first half average is not zero to avoid division issues
                    if (firstHalfAvg.compareTo(BigDecimal.ZERO) > 0) {
                        if (secondHalfAvg.compareTo(firstHalfAvg.multiply(BigDecimal.valueOf(1.1))) > 0) {
                            trend = "increasing";
                        } else if (secondHalfAvg.compareTo(firstHalfAvg.multiply(BigDecimal.valueOf(0.9))) < 0) {
                            trend = "decreasing";
                        }
                    }
                }
            }

            
            List<ExerciseVolumeTrendDto.WeeklyData> weeklyData = weeklyVolume.entrySet().stream()
                    .map(e -> new ExerciseVolumeTrendDto.WeeklyData(
                            e.getKey(),
                            e.getValue(),
                            weeklySets.get(e.getKey())
                    ))
                    .toList();

            trends.add(new ExerciseVolumeTrendDto(
                    exerciseId,
                    exercise.getName(),
                    exercise.getPrimaryMuscle().name(),
                    totalVolume,
                    totalSets,
                    avgVolumePerSession,
                    trend,
                    weeklyData
            ));
        }

        
        trends.sort((a, b) -> b.totalVolume().compareTo(a.totalVolume()));

        return trends;
    }

    /**
     * Get progressive overload tracking
     */
    public List<ProgressiveOverloadDto> getProgressiveOverload(UUID userId, LocalDate from, LocalDate to) {
        LocalDateTime fromTs = from.atStartOfDay();
        LocalDateTime toTs = to.plusDays(1).atStartOfDay().minusNanos(1);

        List<WorkoutSession> sessions = sessionRepo
                .findByUserIdAndStartedAtBetweenOrderByStartedAtAsc(userId, fromTs, toTs);

        if (sessions.isEmpty()) {
            return List.of();
        }

        Map<UUID, WorkoutSession> sessionMap = sessions.stream()
                .collect(Collectors.toMap(WorkoutSession::getId, s -> s));

        List<UUID> sessionIds = sessions.stream().map(WorkoutSession::getId).toList();
        List<WorkoutSet> allSets = setRepo.findAllBySessionIdIn(sessionIds);

        
        Map<UUID, List<WorkoutSet>> setsByExercise = allSets.stream()
                .collect(Collectors.groupingBy(WorkoutSet::getExerciseId));

        
        Set<UUID> exerciseIds = setsByExercise.keySet();
        Map<UUID, Exercise> exerciseMap = exerciseRepo.findAllById(exerciseIds).stream()
                .collect(Collectors.toMap(Exercise::getId, e -> e));

        List<ProgressiveOverloadDto> overloads = new ArrayList<>();

        for (Map.Entry<UUID, List<WorkoutSet>> entry : setsByExercise.entrySet()) {
            UUID exerciseId = entry.getKey();
            List<WorkoutSet> sets = entry.getValue();
            Exercise exercise = exerciseMap.get(exerciseId);

            if (exercise == null) continue;

            
            sets.sort((a, b) -> {
                WorkoutSession sa = sessionMap.get(a.getSessionId());
                WorkoutSession sb = sessionMap.get(b.getSessionId());
                if (sa == null || sb == null) return 0;
                return sa.getStartedAt().compareTo(sb.getStartedAt());
            });

            
            List<ProgressiveOverloadDto.ProgressPoint> progressPoints = new ArrayList<>();
            BigDecimal currentMax = BigDecimal.ZERO;

            for (WorkoutSet set : sets) {
                if (set.getWeight() == null) continue;
                // Skip warmup sets
                if (set.isWarmup()) continue;
                // Skip drop sets - only track main sets for progression
                if (set.getGroupType() != null && set.getGroupType().name().equals("DROP_SET") && 
                    set.getGroupOrder() != null && set.getGroupOrder() > 0) {
                    continue;
                }

                WorkoutSession session = sessionMap.get(set.getSessionId());
                if (session == null) continue;

                if (set.getWeight().compareTo(currentMax) > 0) {
                    currentMax = set.getWeight();
                    progressPoints.add(new ProgressiveOverloadDto.ProgressPoint(
                            session.getStartedAt().toLocalDate(),
                            set.getWeight(),
                            set.getReps()
                    ));
                }
            }

            if (progressPoints.isEmpty()) continue;

            
            BigDecimal firstWeight = progressPoints.get(0).weight();
            BigDecimal lastWeight = progressPoints.get(progressPoints.size() - 1).weight();
            double progressPercent = 0.0;
            if (firstWeight.compareTo(BigDecimal.ZERO) > 0) {
                progressPercent = lastWeight.subtract(firstWeight)
                        .divide(firstWeight, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
            }

            
            String status = "maintaining";
            if (progressPoints.size() >= 2) {
                LocalDate lastProgressDate = progressPoints.get(progressPoints.size() - 1).date();
                long daysSinceProgress = ChronoUnit.DAYS.between(lastProgressDate, to);

                if (daysSinceProgress <= 14) {
                    status = "progressing";
                } else if (daysSinceProgress > 30) {
                    status = "plateau";
                }
            }

            overloads.add(new ProgressiveOverloadDto(
                    exerciseId,
                    exercise.getName(),
                    exercise.getPrimaryMuscle().name(),
                    progressPoints.get(0).weight(),
                    lastWeight,
                    Math.round(progressPercent * 10.0) / 10.0,
                    status,
                    progressPoints
            ));
        }

        
        overloads.sort((a, b) -> Double.compare(b.progressPercent(), a.progressPercent()));

        return overloads;
    }

    /**
     * Get personal records and milestones
     */
    public PersonalRecordsDto getPersonalRecords(UUID userId) {
        
        List<WorkoutSession> allSessions = sessionRepo.findByUserIdOrderByStartedAtDesc(userId);

        if (allSessions.isEmpty()) {
            return new PersonalRecordsDto(List.of(), List.of());
        }

        List<UUID> sessionIds = allSessions.stream().map(WorkoutSession::getId).toList();
        List<WorkoutSet> allSets = setRepo.findAllBySessionIdIn(sessionIds);

        Map<UUID, WorkoutSession> sessionMap = allSessions.stream()
                .collect(Collectors.toMap(WorkoutSession::getId, s -> s));

        
        Map<UUID, List<WorkoutSet>> setsByExercise = allSets.stream()
                .collect(Collectors.groupingBy(WorkoutSet::getExerciseId));

        
        Set<UUID> exerciseIds = setsByExercise.keySet();
        Map<UUID, Exercise> exerciseMap = exerciseRepo.findAllById(exerciseIds).stream()
                .collect(Collectors.toMap(Exercise::getId, e -> e));

        List<PersonalRecordsDto.ExercisePR> exercisePRs = new ArrayList<>();

        for (Map.Entry<UUID, List<WorkoutSet>> entry : setsByExercise.entrySet()) {
            UUID exerciseId = entry.getKey();
            List<WorkoutSet> sets = entry.getValue();
            Exercise exercise = exerciseMap.get(exerciseId);

            if (exercise == null) continue;

            
            WorkoutSet maxWeightSet = sets.stream()
                    .filter(s -> s.getWeight() != null)
                    .max(Comparator.comparing(WorkoutSet::getWeight))
                    .orElse(null);

            
            WorkoutSet maxRepsSet = sets.stream()
                    .filter(s -> s.getReps() != null)
                    .max(Comparator.comparing(WorkoutSet::getReps))
                    .orElse(null);

            
            WorkoutSet maxVolumeSet = sets.stream()
                    .filter(s -> s.getWeight() != null && s.getReps() != null)
                    .max(Comparator.comparing(s -> s.getWeight().multiply(BigDecimal.valueOf(s.getReps()))))
                    .orElse(null);

            if (maxWeightSet != null) {
                WorkoutSession session = sessionMap.get(maxWeightSet.getSessionId());
                exercisePRs.add(new PersonalRecordsDto.ExercisePR(
                        exerciseId,
                        exercise.getName(),
                        "Max Weight",
                        maxWeightSet.getWeight(),
                        maxWeightSet.getReps(),
                        session != null ? session.getStartedAt().toLocalDate() : LocalDate.now()
                ));
            }

            if (maxRepsSet != null && maxRepsSet != maxWeightSet) {
                WorkoutSession session = sessionMap.get(maxRepsSet.getSessionId());
                exercisePRs.add(new PersonalRecordsDto.ExercisePR(
                        exerciseId,
                        exercise.getName(),
                        "Max Reps",
                        maxRepsSet.getWeight(),
                        maxRepsSet.getReps(),
                        session != null ? session.getStartedAt().toLocalDate() : LocalDate.now()
                ));
            }

            if (maxVolumeSet != null && maxVolumeSet != maxWeightSet && maxVolumeSet != maxRepsSet) {
                WorkoutSession session = sessionMap.get(maxVolumeSet.getSessionId());
                exercisePRs.add(new PersonalRecordsDto.ExercisePR(
                        exerciseId,
                        exercise.getName(),
                        "Max Volume (Single Set)",
                        maxVolumeSet.getWeight(),
                        maxVolumeSet.getReps(),
                        session != null ? session.getStartedAt().toLocalDate() : LocalDate.now()
                ));
            }
        }

        
        exercisePRs.sort((a, b) -> b.achievedDate().compareTo(a.achievedDate()));

        // Filter to only keep the most recent PR per exercise
        Map<UUID, PersonalRecordsDto.ExercisePR> latestPRByExercise = new LinkedHashMap<>();
        for (PersonalRecordsDto.ExercisePR pr : exercisePRs) {
            latestPRByExercise.putIfAbsent(pr.exerciseId(), pr);
        }
        exercisePRs = new ArrayList<>(latestPRByExercise.values());
        
        // Re-sort by date after filtering
        exercisePRs.sort((a, b) -> b.achievedDate().compareTo(a.achievedDate()));

        
        List<PersonalRecordsDto.Milestone> milestones = new ArrayList<>();

        
        int totalWorkouts = allSessions.size();
        if (totalWorkouts >= 100) milestones.add(new PersonalRecordsDto.Milestone("Century Club", "Completed 100+ workouts", "🏆"));
        else if (totalWorkouts >= 50) milestones.add(new PersonalRecordsDto.Milestone("Half Century", "Completed 50+ workouts", "🎯"));
        else if (totalWorkouts >= 25) milestones.add(new PersonalRecordsDto.Milestone("Quarter Century", "Completed 25+ workouts", "⭐"));
        else if (totalWorkouts >= 10) milestones.add(new PersonalRecordsDto.Milestone("Getting Started", "Completed 10+ workouts", "🌟"));

        
        BigDecimal totalVolume = allSets.stream()
                .filter(s -> s.getWeight() != null && s.getReps() != null)
                .map(s -> s.getWeight().multiply(BigDecimal.valueOf(s.getReps())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalVolume.compareTo(BigDecimal.valueOf(1000000)) >= 0) {
            milestones.add(new PersonalRecordsDto.Milestone("Million Pound Club", "Lifted 1,000,000+ lbs total", "💪"));
        } else if (totalVolume.compareTo(BigDecimal.valueOf(500000)) >= 0) {
            milestones.add(new PersonalRecordsDto.Milestone("Half Million", "Lifted 500,000+ lbs total", "💪"));
        } else if (totalVolume.compareTo(BigDecimal.valueOf(100000)) >= 0) {
            milestones.add(new PersonalRecordsDto.Milestone("100K Club", "Lifted 100,000+ lbs total", "💪"));
        }

        
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        long recentWorkouts = allSessions.stream()
                .filter(s -> s.getStartedAt().toLocalDate().isAfter(thirtyDaysAgo))
                .count();

        if (recentWorkouts >= 20) milestones.add(new PersonalRecordsDto.Milestone("Consistency King", "20+ workouts in 30 days", "👑"));
        else if (recentWorkouts >= 12) milestones.add(new PersonalRecordsDto.Milestone("Dedicated", "12+ workouts in 30 days", "🔥"));

        return new PersonalRecordsDto(exercisePRs, milestones);
    }

    
    private int calculateLongestStreak(List<WorkoutSession> sessions, LocalDate from, LocalDate to) {
        if (sessions.isEmpty()) return 0;

        Set<LocalDate> workoutDates = sessions.stream()
                .map(s -> s.getStartedAt().toLocalDate())
                .collect(Collectors.toSet());

        int longestStreak = 0;
        int currentStreak = 0;

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            if (workoutDates.contains(date)) {
                currentStreak++;
                longestStreak = Math.max(longestStreak, currentStreak);
            } else {
                currentStreak = 0;
            }
        }

        return longestStreak;
    }

    private double calculateCurrentStreak(List<WorkoutSession> sessions, LocalDate to) {
        if (sessions.isEmpty()) return 0.0;

        Set<LocalDate> workoutDates = sessions.stream()
                .map(s -> s.getStartedAt().toLocalDate())
                .collect(Collectors.toSet());

        double streak = 0.0;
        LocalDate date = to;
        LocalDate oldestWorkout = workoutDates.stream().min(LocalDate::compareTo).orElse(to);

        // Count consecutive days backwards from 'to' date
        // Stop if we hit a day without a workout or go past the oldest workout date
        while (workoutDates.contains(date) && !date.isBefore(oldestWorkout)) {
            streak++;
            date = date.minusDays(1);
            // Safety check: don't go back more than 365 days to prevent infinite loops
            if (ChronoUnit.DAYS.between(date, to) > 365) {
                break;
            }
        }

        return streak;
    }

    /**
     * Get template analytics showing performance of exercises within each template
     */
    public List<TemplateAnalyticsDto> getTemplateAnalytics(UUID userId, LocalDate from, LocalDate to) {
        // Get all user's templates
        List<WorkoutTemplate> templates = templateRepo.findAllByOwnerUserIdOrderByCreatedOnDesc(userId);
        
        if (templates.isEmpty()) {
            return List.of();
        }

        LocalDateTime fromTs = from.atStartOfDay();
        LocalDateTime toTs = to.plusDays(1).atStartOfDay().minusNanos(1);

        // Get all user's sessions in date range
        List<WorkoutSession> sessions = sessionRepo
                .findByUserIdAndStartedAtBetweenOrderByStartedAtAsc(userId, fromTs, toTs);
        
        if (sessions.isEmpty()) {
            return List.of();
        }

        Map<UUID, WorkoutSession> sessionMap = sessions.stream()
                .collect(Collectors.toMap(WorkoutSession::getId, s -> s));

        List<UUID> sessionIds = sessions.stream().map(WorkoutSession::getId).toList();
        List<WorkoutSet> allSets = setRepo.findAllBySessionIdIn(sessionIds);

        List<TemplateAnalyticsDto> analytics = new ArrayList<>();

        for (WorkoutTemplate template : templates) {
            // Get exercises in this template
            List<TemplateItem> items = templateItemRepo.findAllByTemplateIdOrderByPositionAsc(template.getId());
            
            if (items.isEmpty()) continue;

            Set<UUID> templateExerciseIds = items.stream()
                    .map(TemplateItem::getExerciseId)
                    .collect(Collectors.toSet());

            // Find sets that match template exercises
            List<WorkoutSet> templateSets = allSets.stream()
                    .filter(set -> templateExerciseIds.contains(set.getExerciseId()))
                    .toList();

            if (templateSets.isEmpty()) continue;

            // Calculate template-level stats
            BigDecimal totalVolume = templateSets.stream()
                    .filter(s -> s.getWeight() != null && s.getReps() != null)
                    .map(s -> s.getWeight().multiply(BigDecimal.valueOf(s.getReps())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Find sessions containing these exercises
            Set<UUID> relevantSessionIds = templateSets.stream()
                    .map(WorkoutSet::getSessionId)
                    .collect(Collectors.toSet());

            int totalSessions = relevantSessionIds.size();
            BigDecimal avgVolumePerSession = totalSessions > 0 ?
                    totalVolume.divide(BigDecimal.valueOf(totalSessions), 2, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;

            // Calculate trend
            List<LocalDate> sessionDates = relevantSessionIds.stream()
                    .map(sid -> sessionMap.get(sid).getStartedAt().toLocalDate())
                    .sorted()
                    .toList();

            String trend = "stable";
            double trendPercent = 0.0;
            
            if (sessionDates.size() >= 4) {
                int midPoint = sessionDates.size() / 2;
                List<LocalDate> firstHalfDates = sessionDates.subList(0, midPoint);
                List<LocalDate> secondHalfDates = sessionDates.subList(midPoint, sessionDates.size());

                BigDecimal firstHalfVolume = templateSets.stream()
                        .filter(s -> s.getWeight() != null && s.getReps() != null)
                        .filter(s -> firstHalfDates.contains(sessionMap.get(s.getSessionId()).getStartedAt().toLocalDate()))
                        .map(s -> s.getWeight().multiply(BigDecimal.valueOf(s.getReps())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal secondHalfVolume = templateSets.stream()
                        .filter(s -> s.getWeight() != null && s.getReps() != null)
                        .filter(s -> secondHalfDates.contains(sessionMap.get(s.getSessionId()).getStartedAt().toLocalDate()))
                        .map(s -> s.getWeight().multiply(BigDecimal.valueOf(s.getReps())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal firstAvg = firstHalfDates.isEmpty() ? BigDecimal.ZERO :
                        firstHalfVolume.divide(BigDecimal.valueOf(firstHalfDates.size()), 2, RoundingMode.HALF_UP);
                BigDecimal secondAvg = secondHalfDates.isEmpty() ? BigDecimal.ZERO :
                        secondHalfVolume.divide(BigDecimal.valueOf(secondHalfDates.size()), 2, RoundingMode.HALF_UP);

                if (firstAvg.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal change = secondAvg.subtract(firstAvg);
                    trendPercent = change.divide(firstAvg, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue();

                    if (trendPercent > 5) {
                        trend = "increasing";
                    } else if (trendPercent < -5) {
                        trend = "decreasing";
                    }
                }
            }

            LocalDate lastUsed = sessionDates.isEmpty() ? null : sessionDates.get(sessionDates.size() - 1);
            LocalDate firstUsed = sessionDates.isEmpty() ? null : sessionDates.get(0);

            // Build exercise stats
            Map<UUID, Exercise> exerciseMap = exerciseRepo.findAllById(templateExerciseIds).stream()
                    .collect(Collectors.toMap(Exercise::getId, e -> e));

            List<TemplateAnalyticsDto.TemplateExerciseStatsDto> exerciseStats = new ArrayList<>();

            for (UUID exerciseId : templateExerciseIds) {
                Exercise exercise = exerciseMap.get(exerciseId);
                if (exercise == null) continue;

                // Filter exercise sets, excluding warmup and drop sets for accurate progression
                List<WorkoutSet> exerciseSets = templateSets.stream()
                        .filter(s -> s.getExerciseId().equals(exerciseId))
                        .filter(s -> !s.isWarmup())
                        .filter(s -> {
                            // Include main sets (no group type, or groupOrder 0, or not a drop set)
                            if (s.getGroupType() == null) return true;
                            if (s.getGroupType().name().equals("DROP_SET")) {
                                return s.getGroupOrder() == null || s.getGroupOrder() == 0;
                            }
                            // Supersets are fine - each exercise is tracked separately
                            return true;
                        })
                        .toList();

                if (exerciseSets.isEmpty()) continue;

                int exerciseSessions = exerciseSets.stream()
                        .map(WorkoutSet::getSessionId)
                        .collect(Collectors.toSet())
                        .size();

                int totalExerciseSets = exerciseSets.size();

                // Sort by session date
                List<WorkoutSet> sortedSets = exerciseSets.stream()
                        .sorted((a, b) -> {
                            WorkoutSession sa = sessionMap.get(a.getSessionId());
                            WorkoutSession sb = sessionMap.get(b.getSessionId());
                            return sa.getStartedAt().compareTo(sb.getStartedAt());
                        })
                        .toList();

                // Filter out warmup and drop sets for weight tracking
                List<WorkoutSet> validSets = sortedSets.stream()
                        .filter(s -> s.getWeight() != null)
                        .filter(s -> !s.isWarmup())
                        .filter(s -> {
                            // Include main sets (no group type, or groupOrder 0, or not a drop set)
                            if (s.getGroupType() == null) return true;
                            if (s.getGroupType().name().equals("DROP_SET")) {
                                return s.getGroupOrder() == null || s.getGroupOrder() == 0;
                            }
                            // Supersets are fine - each exercise is tracked separately
                            return true;
                        })
                        .toList();

                BigDecimal startingWeight = validSets.stream()
                        .findFirst()
                        .map(WorkoutSet::getWeight)
                        .orElse(BigDecimal.ZERO);

                BigDecimal currentWeight = validSets.stream()
                        .reduce((first, second) -> second)
                        .map(WorkoutSet::getWeight)
                        .orElse(BigDecimal.ZERO);

                double progressPercent = 0.0;
                if (startingWeight.compareTo(BigDecimal.ZERO) > 0) {
                    progressPercent = currentWeight.subtract(startingWeight)
                            .divide(startingWeight, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();
                }

                BigDecimal exerciseVolume = exerciseSets.stream()
                        .filter(s -> s.getWeight() != null && s.getReps() != null)
                        .map(s -> s.getWeight().multiply(BigDecimal.valueOf(s.getReps())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Build progress points using valid sets (already filtered)
                Map<LocalDate, BigDecimal> maxWeightByDate = new TreeMap<>();
                for (WorkoutSet set : validSets) {
                    LocalDate date = sessionMap.get(set.getSessionId()).getStartedAt().toLocalDate();
                    maxWeightByDate.merge(date, set.getWeight(), (a, b) -> a.compareTo(b) > 0 ? a : b);
                }

                List<TemplateAnalyticsDto.TemplateExerciseStatsDto.ProgressPoint> progressPoints =
                        maxWeightByDate.entrySet().stream()
                                .map(e -> {
                                    // Find set with this weight on this date from valid sets
                                    WorkoutSet set = validSets.stream()
                                            .filter(s -> s.getWeight() != null &&
                                                    s.getWeight().equals(e.getValue()) &&
                                                    sessionMap.get(s.getSessionId()).getStartedAt().toLocalDate().equals(e.getKey()))
                                            .findFirst()
                                            .orElse(null);
                                    
                                    Integer reps = set != null ? set.getReps() : null;
                                    BigDecimal volume = set != null && set.getWeight() != null && set.getReps() != null ?
                                            set.getWeight().multiply(BigDecimal.valueOf(set.getReps())) : BigDecimal.ZERO;
                                    
                                    return new TemplateAnalyticsDto.TemplateExerciseStatsDto.ProgressPoint(
                                            e.getKey(), e.getValue(), reps, volume
                                    );
                                })
                                .toList();

                exerciseStats.add(new TemplateAnalyticsDto.TemplateExerciseStatsDto(
                        exerciseId,
                        exercise.getName(),
                        exercise.getPrimaryMuscle().name(),
                        exerciseSessions,
                        totalExerciseSets,
                        startingWeight,
                        currentWeight,
                        Math.round(progressPercent * 10.0) / 10.0,
                        exerciseVolume,
                        progressPoints
                ));
            }

            analytics.add(new TemplateAnalyticsDto(
                    template.getId(),
                    template.getName(),
                    totalVolume,
                    totalSessions,
                    avgVolumePerSession,
                    trend,
                    Math.round(trendPercent * 10.0) / 10.0,
                    lastUsed,
                    firstUsed,
                    exerciseStats
            ));
        }

        return analytics;
    }

    /**
     * Compare two workout sessions side-by-side
     */
    public TemplateComparisonDto compareWorkouts(UUID userId, UUID session1Id, UUID session2Id) {
        WorkoutSession session1 = sessionRepo.findById(session1Id)
                .orElseThrow(() -> new IllegalArgumentException("Session 1 not found"));
        WorkoutSession session2 = sessionRepo.findById(session2Id)
                .orElseThrow(() -> new IllegalArgumentException("Session 2 not found"));

        if (!session1.getUserId().equals(userId) || !session2.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Sessions do not belong to user");
        }

        List<WorkoutSet> sets1 = setRepo.findAllBySessionId(session1Id);
        List<WorkoutSet> sets2 = setRepo.findAllBySessionId(session2Id);

        // Calculate session-level stats
        BigDecimal volume1 = sets1.stream()
                .filter(s -> s.getWeight() != null && s.getReps() != null)
                .map(s -> s.getWeight().multiply(BigDecimal.valueOf(s.getReps())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal volume2 = sets2.stream()
                .filter(s -> s.getWeight() != null && s.getReps() != null)
                .map(s -> s.getWeight().multiply(BigDecimal.valueOf(s.getReps())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int duration1 = calculateDuration(session1);
        int duration2 = calculateDuration(session2);

        TemplateComparisonDto.WorkoutComparison workout1 = new TemplateComparisonDto.WorkoutComparison(
                session1.getId(),
                session1.getStartedAt(),
                volume1,
                sets1.size(),
                duration1
        );

        TemplateComparisonDto.WorkoutComparison workout2 = new TemplateComparisonDto.WorkoutComparison(
                session2.getId(),
                session2.getStartedAt(),
                volume2,
                sets2.size(),
                duration2
        );

        // Calculate comparison summary
        BigDecimal volumeDiff = volume2.subtract(volume1);
        double volumeChangePercent = volume1.compareTo(BigDecimal.ZERO) > 0 ?
                volumeDiff.divide(volume1, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue() : 0;
        int setsDiff = sets2.size() - sets1.size();
        String overallTrend = volumeChangePercent > 5 ? "improving" : (volumeChangePercent < -5 ? "declining" : "stable");

        TemplateComparisonDto.ComparisonSummary summary = new TemplateComparisonDto.ComparisonSummary(
                volumeDiff,
                Math.round(volumeChangePercent * 10.0) / 10.0,
                setsDiff,
                overallTrend
        );

        // Get exercises from both sessions
        Set<UUID> allExerciseIds = new HashSet<>();
        sets1.forEach(s -> allExerciseIds.add(s.getExerciseId()));
        sets2.forEach(s -> allExerciseIds.add(s.getExerciseId()));

        Map<UUID, Exercise> exerciseMap = exerciseRepo.findAllById(allExerciseIds).stream()
                .collect(Collectors.toMap(Exercise::getId, e -> e));

        // Build exercise comparisons
        List<TemplateComparisonDto.ExerciseComparison> exerciseComparisons = new ArrayList<>();

        for (UUID exerciseId : allExerciseIds) {
            Exercise exercise = exerciseMap.get(exerciseId);
            if (exercise == null) continue;

            List<WorkoutSet> exSets1 = sets1.stream()
                    .filter(s -> s.getExerciseId().equals(exerciseId))
                    .toList();
            List<WorkoutSet> exSets2 = sets2.stream()
                    .filter(s -> s.getExerciseId().equals(exerciseId))
                    .toList();

            // Session 1 data
            TemplateComparisonDto.ExerciseComparison.ExerciseWorkoutData data1 = buildExerciseWorkoutData(exSets1);
            
            // Session 2 data
            TemplateComparisonDto.ExerciseComparison.ExerciseWorkoutData data2 = buildExerciseWorkoutData(exSets2);

            // Calculate differences
            BigDecimal weightChange = data2.avgWeight().subtract(data1.avgWeight());
            double weightChangePercent = data1.avgWeight().compareTo(BigDecimal.ZERO) > 0 ?
                    weightChange.divide(data1.avgWeight(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue() : 0;
            int setsChange = data2.sets() - data1.sets();
            BigDecimal volumeChange = data2.volume().subtract(data1.volume());
            double exVolumeChangePercent = data1.volume().compareTo(BigDecimal.ZERO) > 0 ?
                    volumeChange.divide(data1.volume(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue() : 0;
            String exTrend = exVolumeChangePercent > 5 ? "improved" : (exVolumeChangePercent < -5 ? "declined" : "maintained");

            TemplateComparisonDto.ExerciseComparison.ExerciseDifference difference =
                    new TemplateComparisonDto.ExerciseComparison.ExerciseDifference(
                            weightChange,
                            Math.round(weightChangePercent * 10.0) / 10.0,
                            setsChange,
                            volumeChange,
                            Math.round(exVolumeChangePercent * 10.0) / 10.0,
                            exTrend
                    );

            exerciseComparisons.add(new TemplateComparisonDto.ExerciseComparison(
                    exerciseId,
                    exercise.getName(),
                    exercise.getPrimaryMuscle().name(),
                    data1,
                    data2,
                    difference
            ));
        }

        return new TemplateComparisonDto(
                null, // No template ID
                "Workout Comparison",
                workout1,
                workout2,
                summary,
                exerciseComparisons
        );
    }

    private TemplateComparisonDto.ExerciseComparison.ExerciseWorkoutData buildExerciseWorkoutData(List<WorkoutSet> sets) {
        if (sets.isEmpty()) {
            return new TemplateComparisonDto.ExerciseComparison.ExerciseWorkoutData(
                    0, BigDecimal.ZERO, BigDecimal.ZERO, 0, BigDecimal.ZERO
            );
        }

        int numSets = sets.size();
        
        List<WorkoutSet> setsWithWeight = sets.stream()
                .filter(s -> s.getWeight() != null)
                .toList();
        
        BigDecimal avgWeight = setsWithWeight.isEmpty() ? BigDecimal.ZERO :
                setsWithWeight.stream()
                        .map(WorkoutSet::getWeight)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(setsWithWeight.size()), 2, RoundingMode.HALF_UP);

        BigDecimal maxWeight = sets.stream()
                .filter(s -> s.getWeight() != null)
                .map(WorkoutSet::getWeight)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        int totalReps = sets.stream()
                .filter(s -> s.getReps() != null)
                .mapToInt(WorkoutSet::getReps)
                .sum();

        BigDecimal volume = sets.stream()
                .filter(s -> s.getWeight() != null && s.getReps() != null)
                .map(s -> s.getWeight().multiply(BigDecimal.valueOf(s.getReps())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new TemplateComparisonDto.ExerciseComparison.ExerciseWorkoutData(
                numSets, avgWeight, maxWeight, totalReps, volume
        );
    }

    private int calculateDuration(WorkoutSession session) {
        if (session.getStartedAt() == null || session.getFinishedAt() == null) {
            return 0;
        }
        return (int) java.time.Duration.between(session.getStartedAt(), session.getFinishedAt()).toMinutes();
    }
}

