package project.fitnessapplication.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.fitnessapplication.exercise.repository.ExerciseRepository;
import project.fitnessapplication.restday.repository.RestDayRepository;
import project.fitnessapplication.schedule.repository.ScheduledWorkoutRepository;
import project.fitnessapplication.template.repository.TemplateItemRepository;
import project.fitnessapplication.template.repository.WorkoutTemplateRepository;
import project.fitnessapplication.user.model.User;
import project.fitnessapplication.user.repository.PasswordResetTokenRepository;
import project.fitnessapplication.user.repository.UserRepository;
import project.fitnessapplication.workout.repository.WorkoutSessionRepository;
import project.fitnessapplication.workout.repository.WorkoutSetRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountMaintenanceService {

    private final UserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final WorkoutSessionRepository sessions;
    private final WorkoutSetRepository sets;
    private final WorkoutTemplateRepository templates;
    private final TemplateItemRepository templateItems;
    private final ScheduledWorkoutRepository scheduled;
    private final RestDayRepository restDays;
    private final ExerciseRepository exercises;

    @Transactional
    public void resetAccount(UUID userId) {
        User user = users.findById(userId).orElseThrow();

        // Invalidate tokens
        tokens.markAllAsUsedForUser(user);

        // Delete scheduled workouts
        scheduled.deleteAll(scheduled.findByUserId(userId));

        // Delete rest days
        restDays.deleteAll(restDays.findByUserIdAndActiveTrueOrderByDateDesc(userId));

        // Delete workout sessions and sets
        var userSessions = sessions.findByUserIdOrderByStartedAtDesc(userId);
        for (var s : userSessions) {
            sets.deleteBySessionId(s.getId());
        }
        sessions.deleteAll(userSessions);

        // Delete templates and items
        var userTemplates = templates.findAllByOwnerUserIdOrderByCreatedOnDesc(userId);
        for (var t : userTemplates) {
            templateItems.deleteByTemplateId(t.getId());
        }
        templates.deleteAll(userTemplates);

        // Delete user-created exercises
        exercises.deleteAll(exercises.findAllByOwnerUserId(userId));
    }

    @Transactional
    public void deleteAccount(UUID userId) {
        resetAccount(userId);
        users.deleteById(userId);
    }
}


