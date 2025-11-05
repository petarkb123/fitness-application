package project.fitnessapplication.notification.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import project.fitnessapplication.notification.model.PushSubscription;
import project.fitnessapplication.notification.repository.PushSubscriptionRepository;
import project.fitnessapplication.schedule.model.ScheduledWorkout;
import project.fitnessapplication.schedule.repository.ScheduledWorkoutRepository;
import project.fitnessapplication.workout.model.WorkoutSession;
import project.fitnessapplication.workout.model.SessionStatus;
import project.fitnessapplication.workout.repository.WorkoutSessionRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final PushSubscriptionRepository subscriptions;
    private final ScheduledWorkoutRepository scheduledWorkouts;
    private final WorkoutSessionRepository workoutSessions;
    private final WebPushNotificationService webPush;

    // Run every minute; lightweight selection in-memory
    @Scheduled(cron = "0 * * * * *")
    public void run() {
        List<PushSubscription> active = subscriptions.findAll();
        if (active.isEmpty()) return;

        for (PushSubscription sub : active) {
            if (!sub.isActive()) continue;
            String tz = sub.getTimezone();
            if (tz == null || tz.isBlank()) tz = ZoneId.systemDefault().getId();
            ZoneId zone = ZoneId.of(tz);
            ZonedDateTime now = ZonedDateTime.now(zone);
            LocalDate today = now.toLocalDate();
            int hour = now.getHour();
            int minute = now.getMinute();

            // Morning reminder at 08:00 local (send only if user has a workout today)
            if (hour == 8 && minute == 0) {
                boolean hasWorkoutToday = hasScheduledWorkoutToday(sub);
                if (hasWorkoutToday && (sub.getLastMorningSent() == null || !sub.getLastMorningSent().toLocalDate().isEqual(today))) {
                    webPush.sendToSubscriptions(List.of(sub), "Workout today", "You have a workout scheduled today. Let's go!", "/dashboard");
                    sub.setLastMorningSent(LocalDateTime.now());
                    subscriptions.save(sub);
                }
            }

            // Evening reminder at 17:00 local (only if workout today exists and not finished)
            if (hour == 17 && minute == 0) {
                boolean hasWorkoutToday = hasScheduledWorkoutToday(sub);
                boolean finishedLogged = hasFinishedSessionToday(sub);
                if (hasWorkoutToday && !finishedLogged && (sub.getLastEveningSent() == null || !sub.getLastEveningSent().toLocalDate().isEqual(today))) {
                    webPush.sendToSubscriptions(List.of(sub), "Reminder", "Don’t forget to log your workout.", "/workouts/history");
                    sub.setLastEveningSent(LocalDateTime.now());
                    subscriptions.save(sub);
                }
            }
        }
    }

    private boolean hasScheduledWorkoutToday(PushSubscription sub) {
        try {
            var userId = sub.getUser().getId();
            var zone = ZoneId.of(sub.getTimezone() != null && !sub.getTimezone().isBlank() ? sub.getTimezone() : ZoneId.systemDefault().getId());
            var today = ZonedDateTime.now(zone).toLocalDate();
            var list = scheduledWorkouts.findByUserIdAndDateBetweenOrderByDateAsc(userId, today, today);
            return !list.isEmpty();
        } catch (Exception e) { return false; }
    }

    private boolean hasFinishedSessionToday(PushSubscription sub) {
        try {
            var userId = sub.getUser().getId();
            var zone = ZoneId.of(sub.getTimezone() != null && !sub.getTimezone().isBlank() ? sub.getTimezone() : ZoneId.systemDefault().getId());
            var start = ZonedDateTime.now(zone).toLocalDate().atStartOfDay();
            var end = start.plusDays(1);
            List<WorkoutSession> sessions = workoutSessions.findByUserIdAndStartedAtBetweenOrderByStartedAtAsc(userId, start, end);
            for (WorkoutSession s : sessions) {
                if (s.getStatus() == SessionStatus.FINISHED) return true;
            }
            return false;
        } catch (Exception e) { return false; }
    }
}


