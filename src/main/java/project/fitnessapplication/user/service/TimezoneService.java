package project.fitnessapplication.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.fitnessapplication.user.model.User;
import project.fitnessapplication.user.repository.UserRepository;

import java.time.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimezoneService {

    private final UserRepository users;

    /**
     * Get user's timezone or default to UTC
     */
    public ZoneId getUserZoneId(UUID userId) {
        User user = users.findById(userId).orElse(null);
        if (user == null || user.getTimezone() == null || user.getTimezone().trim().isEmpty()) {
            return ZoneId.of("UTC");
        }
        try {
            return ZoneId.of(user.getTimezone());
        } catch (Exception e) {
            return ZoneId.of("UTC");
        }
    }

    /**
     * Get user's timezone or default to UTC (by username)
     */
    public ZoneId getUserZoneId(String username) {
        User user = users.findByUsername(username).orElse(null);
        if (user == null || user.getTimezone() == null || user.getTimezone().trim().isEmpty()) {
            return ZoneId.of("UTC");
        }
        try {
            return ZoneId.of(user.getTimezone());
        } catch (Exception e) {
            return ZoneId.of("UTC");
        }
    }

    /**
     * Convert UTC LocalDateTime to user's local time
     */
    public LocalDateTime toUserLocalTime(LocalDateTime utcTime, UUID userId) {
        if (utcTime == null) return null;
        ZoneId userZone = getUserZoneId(userId);
        return utcTime.atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(userZone)
                .toLocalDateTime();
    }

    /**
     * Convert UTC LocalDateTime to user's local time (by username)
     */
    public LocalDateTime toUserLocalTime(LocalDateTime utcTime, String username) {
        if (utcTime == null) return null;
        ZoneId userZone = getUserZoneId(username);
        return utcTime.atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(userZone)
                .toLocalDateTime();
    }

    /**
     * Convert user's local time to UTC for storage
     */
    public LocalDateTime toUtc(LocalDateTime userLocalTime, UUID userId) {
        if (userLocalTime == null) return null;
        ZoneId userZone = getUserZoneId(userId);
        return userLocalTime.atZone(userZone)
                .withZoneSameInstant(ZoneId.of("UTC"))
                .toLocalDateTime();
    }

    /**
     * Get current time in user's timezone
     */
    public LocalDateTime nowInUserTimezone(UUID userId) {
        ZoneId userZone = getUserZoneId(userId);
        return ZonedDateTime.now(userZone).toLocalDateTime();
    }

    /**
     * Get current UTC time
     */
    public LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneId.of("UTC"));
    }

    /**
     * Validate timezone string
     */
    public boolean isValidTimezone(String timezone) {
        if (timezone == null || timezone.trim().isEmpty()) {
            return false;
        }
        try {
            ZoneId.of(timezone);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

