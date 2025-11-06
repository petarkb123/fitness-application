package project.fitnessapplication.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.fitnessapplication.user.model.User;
import project.fitnessapplication.user.repository.UserRepository;

import java.time.ZoneId;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserSettingsService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User requireByUsername(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    @Transactional
    public void setAvatarUrl(UUID userId, String url) {
        var u = users.findById(userId).orElseThrow();
        String v = (url == null) ? "" : url.trim();
        if (v.isEmpty()) throw new IllegalArgumentException("Avatar URL is required.");
        if (!(v.startsWith("http://") || v.startsWith("https://"))) {
            throw new IllegalArgumentException("Avatar URL must start with http:// or https://");
        }
        u.setProfilePicture(v);
        users.save(u);
    }

    @Transactional
    public void removeAvatar(UUID userId) {
        var u = users.findById(userId).orElseThrow();
        u.setProfilePicture(null);
        users.save(u);
    }

    public void changePassword(UUID userId, String currentRaw, String newRaw, String confirmRaw) {
        if (!Objects.equals(newRaw, confirmRaw)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        if (newRaw == null || newRaw.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters.");
        }
        User u = users.findById(userId).orElseThrow();
        if (!passwordEncoder.matches(currentRaw, u.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        u.setPasswordHash(passwordEncoder.encode(newRaw));
        users.save(u);
    }

    // Update user profile information
    public void updateFirstName(UUID userId, String firstName) {
        User u = users.findById(userId).orElseThrow();
        u.setFirstName(firstName);
        users.save(u);
    }

    public void updateLastName(UUID userId, String lastName) {
        User u = users.findById(userId).orElseThrow();
        u.setLastName(lastName);
        users.save(u);
    }

    public void updateEmail(UUID userId, String email) {
        User u = users.findById(userId).orElseThrow();
        String emailTrimmed = (email == null) ? "" : email.trim().toLowerCase();
        if (emailTrimmed.isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (!emailTrimmed.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        // Check if email is already in use by another user
        if (users.existsByEmail(emailTrimmed) && !u.getEmail().equalsIgnoreCase(emailTrimmed)) {
            throw new IllegalArgumentException("Email is already in use.");
        }
        u.setEmail(emailTrimmed);
        users.save(u);
    }

    public void updateHeight(UUID userId, Integer heightCm) {
        User u = users.findById(userId).orElseThrow();
        u.setHeightCm(heightCm);
        users.save(u);
    }

    public void updateWeight(UUID userId, Integer weightKg) {
        User u = users.findById(userId).orElseThrow();
        u.setWeightKg(weightKg);
        users.save(u);
    }

    public void updateUnitSystem(UUID userId, String unitSystem) {
        User u = users.findById(userId).orElseThrow();
        String newUnits = (unitSystem == null) ? "metric" : unitSystem.trim().toLowerCase();

        // Populate counterpart fields so values persist across unit toggles
        if ("imperial".equals(newUnits)) {
            // Fill feet/inches from cm if missing
            if ((u.getHeightFeet() == null || u.getHeightInches() == null) && u.getHeightCm() != null) {
                double totalInches = u.getHeightCm() / 2.54d;
                int feet = (int) Math.floor(totalInches / 12.0);
                int inches = (int) Math.round(totalInches - feet * 12);
                if (inches == 12) { feet += 1; inches = 0; }
                u.setHeightFeet(feet);
                u.setHeightInches(inches);
            }
            // Fill lbs from kg if missing
            if (u.getWeightLbs() == null && u.getWeightKg() != null) {
                int lbs = (int) Math.round(u.getWeightKg() * 2.20462d);
                u.setWeightLbs(lbs);
            }
        } else { // metric
            // Fill cm from feet/inches if missing
            if (u.getHeightCm() == null && u.getHeightFeet() != null && u.getHeightInches() != null) {
                int cm = (int) Math.round(u.getHeightFeet() * 30.48d + u.getHeightInches() * 2.54d);
                u.setHeightCm(cm);
            }
            // Fill kg from lbs if missing
            if (u.getWeightKg() == null && u.getWeightLbs() != null) {
                int kg = (int) Math.round(u.getWeightLbs() / 2.20462d);
                u.setWeightKg(kg);
            }
        }

        u.setUnitSystem(newUnits);
        users.save(u);
    }

    public void updateHeightImperial(UUID userId, Integer feet, Integer inches) {
        User u = users.findById(userId).orElseThrow();
        u.setHeightFeet(feet);
        u.setHeightInches(inches);
        // Optionally keep cm in sync for compatibility
        if (feet != null && inches != null) {
            int cm = (int) Math.round(feet * 30.48 + inches * 2.54);
            u.setHeightCm(cm);
        }
        users.save(u);
    }

    public void updateWeightLbs(UUID userId, Integer lbs) {
        User u = users.findById(userId).orElseThrow();
        u.setWeightLbs(lbs);
        // Optionally keep kg in sync for compatibility
        if (lbs != null) {
            int kg = (int) Math.round(lbs / 2.20462);
            u.setWeightKg(kg);
        }
        users.save(u);
    }

    public void updateGoal(UUID userId, String goal) {
        User u = users.findById(userId).orElseThrow();
        u.setGoal(goal);
        users.save(u);
    }

    public void updateDesiredWeight(UUID userId, Integer desiredWeightKg) {
        User u = users.findById(userId).orElseThrow();
        u.setDesiredWeightKg(desiredWeightKg);
        users.save(u);
    }

    public void updateWeightChangeSpeed(UUID userId, Double weightChangeSpeedKg) {
        User u = users.findById(userId).orElseThrow();
        u.setWeightChangeSpeedKg(weightChangeSpeedKg);
        users.save(u);
    }

    public void updateWorkoutFrequency(UUID userId, String workoutFrequency) {
        User u = users.findById(userId).orElseThrow();
        u.setWorkoutFrequency(workoutFrequency);
        users.save(u);
    }

    public void updateTimezone(UUID userId, String timezone) {
        User u = users.findById(userId).orElseThrow();
        if (timezone != null && !timezone.trim().isEmpty()) {
            try {
                java.time.ZoneId.of(timezone); // Validate
                u.setTimezone(timezone.trim());
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid timezone: " + timezone);
            }
        } else {
            u.setTimezone("UTC");
        }
        users.save(u);
    }

    public void updateUserTimezone(UUID userId, String timezone) {
        User u = users.findById(userId).orElseThrow();
        if (timezone == null || timezone.trim().isEmpty()) {
            throw new IllegalArgumentException("Timezone is required.");
        }
        try {
            ZoneId.of(timezone); // Validate IANA timezone
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid timezone: " + timezone);
        }
        u.setTimezone(timezone.trim());
        users.save(u);
    }
}
