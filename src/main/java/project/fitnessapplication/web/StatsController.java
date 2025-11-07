package project.fitnessapplication.web;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import project.fitnessapplication.stats.service.AdvancedStatsService;
import project.fitnessapplication.stats.service.StatsService;
import project.fitnessapplication.user.service.UserService;
import project.fitnessapplication.workout.service.WorkoutService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.UUID;

@Controller
public class StatsController {

    private final StatsService stats;
    private final AdvancedStatsService advancedStats;
    private final UserService users;
    private final WorkoutService workoutService;

    public StatsController(StatsService stats, AdvancedStatsService advancedStats, UserService users, WorkoutService workoutService) {
        this.stats = stats;
        this.advancedStats = advancedStats;
        this.users = users;
        this.workoutService = workoutService;
    }

    @GetMapping("/stats/weekly")
    public String weekly(@AuthenticationPrincipal UserDetails me,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                         Model model) {
        UUID userId = users.findByUsernameOrThrow(me.getUsername()).getId();
        var u = users.findByUsernameOrThrow(me.getUsername());
        LocalDate today = LocalDate.now();
        LocalDate start = (from != null) ? from : today.with(DayOfWeek.MONDAY);
        LocalDate end = (to != null) ? to : start.plusDays(6);
        model.addAttribute("summary", stats.weekly(userId, start, end));
        model.addAttribute("navAvatar", u.getProfilePicture());
        model.addAttribute("username", u.getUsername());
        model.addAttribute("unitSystem", u.getUnitSystem());
        return "stats-weekly";
    }

    @GetMapping("/stats/advanced")
    public String advanced(@AuthenticationPrincipal UserDetails me,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                          Model model) {
        var u = users.findByUsernameOrThrow(me.getUsername());
        UUID userId = u.getId();

        LocalDate end = (to != null) ? to : LocalDate.now();
        
        // Get user's first workout date if available
        var firstWorkout = workoutService.getFirstWorkoutDate(userId);
        LocalDate defaultStart = end.minusDays(90);
        
        // If user has workouts, start from first workout date (but not more than 90 days ago if first workout is older)
        if (firstWorkout != null) {
            LocalDate firstWorkoutDate = firstWorkout.toLocalDate();
            // Use the more recent date: first workout or 90 days ago
            defaultStart = firstWorkoutDate.isBefore(defaultStart) ? defaultStart : firstWorkoutDate;
        }
        
        LocalDate start = (from != null) ? from : defaultStart;

        var trainingFrequency = advancedStats.getTrainingFrequency(userId, start, end, u.getWorkoutFrequency());
        var personalRecords = advancedStats.getPersonalRecords(userId);

        model.addAttribute("navAvatar", u.getProfilePicture());
        model.addAttribute("username", u.getUsername());
        model.addAttribute("from", start);
        model.addAttribute("to", end);
        model.addAttribute("trainingFrequency", trainingFrequency);
        model.addAttribute("personalRecords", personalRecords);
        model.addAttribute("unitSystem", u.getUnitSystem());

        return "advanced-stats";
    }

    @GetMapping("/stats/progressive-overload")
    public String progressiveOverload(@AuthenticationPrincipal UserDetails me,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                     Model model) {
        var u = users.findByUsernameOrThrow(me.getUsername());
        UUID userId = u.getId();

        LocalDate end = (to != null) ? to : LocalDate.now();
        LocalDate start = (from != null) ? from : end.minusDays(90);

        var progressiveOverload = advancedStats.getProgressiveOverload(userId, start, end);

        model.addAttribute("navAvatar", u.getProfilePicture());
        model.addAttribute("username", u.getUsername());
        model.addAttribute("from", start);
        model.addAttribute("to", end);
        model.addAttribute("progressiveOverload", progressiveOverload);
        model.addAttribute("unitSystem", u.getUnitSystem());

        return "progressive-overload";
    }

    @GetMapping("/stats/volume-trends")
    public String volumeTrends(@AuthenticationPrincipal UserDetails me,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                               Model model) {
        var u = users.findByUsernameOrThrow(me.getUsername());
        UUID userId = u.getId();

        LocalDate end = (to != null) ? to : LocalDate.now();
        LocalDate start = (from != null) ? from : end.minusDays(90);

        var volumeTrends = advancedStats.getExerciseVolumeTrends(userId, start, end);

        model.addAttribute("navAvatar", u.getProfilePicture());
        model.addAttribute("username", u.getUsername());
        model.addAttribute("from", start);
        model.addAttribute("to", end);
        model.addAttribute("volumeTrends", volumeTrends);
        model.addAttribute("unitSystem", u.getUnitSystem());

        return "volume-trends";
    }

    @GetMapping("/stats/training-frequency-details")
    public String trainingFrequencyDetails(@AuthenticationPrincipal UserDetails me,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                          Model model) {
        var u = users.findByUsernameOrThrow(me.getUsername());
        UUID userId = u.getId();

        LocalDate end = (to != null) ? to : LocalDate.now();
        
        // Get user's first workout date if available
        var firstWorkout = workoutService.getFirstWorkoutDate(userId);
        LocalDate defaultStart = end.minusDays(90);
        
        // If user has workouts, start from first workout date (but not more than 90 days ago if first workout is older)
        if (firstWorkout != null) {
            LocalDate firstWorkoutDate = firstWorkout.toLocalDate();
            // Use the more recent date: first workout or 90 days ago
            defaultStart = firstWorkoutDate.isBefore(defaultStart) ? defaultStart : firstWorkoutDate;
        }
        
        LocalDate start = (from != null) ? from : defaultStart;

        var trainingFrequency = advancedStats.getTrainingFrequency(userId, start, end, u.getWorkoutFrequency());

        model.addAttribute("navAvatar", u.getProfilePicture());
        model.addAttribute("username", u.getUsername());
        model.addAttribute("from", start);
        model.addAttribute("to", end);
        model.addAttribute("trainingFrequency", trainingFrequency);
        model.addAttribute("unitSystem", u.getUnitSystem());

        return "training-frequency-details";
    }

    @GetMapping("/stats/personal-records-details")
    public String personalRecordsDetails(@AuthenticationPrincipal UserDetails me, Model model) {
        var u = users.findByUsernameOrThrow(me.getUsername());
        UUID userId = u.getId();

        var personalRecords = advancedStats.getPersonalRecords(userId);

        model.addAttribute("navAvatar", u.getProfilePicture());
        model.addAttribute("username", u.getUsername());
        model.addAttribute("personalRecords", personalRecords);
        model.addAttribute("unitSystem", u.getUnitSystem());

        return "personal-records-details";
    }
}
