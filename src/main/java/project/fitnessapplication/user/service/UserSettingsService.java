package project.fitnessapplication.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.fitnessapplication.user.model.User;
import project.fitnessapplication.user.repository.UserRepository;

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

    public void updateTimezone(UUID userId, String region) {
        User u = users.findById(userId).orElseThrow();
        u.setRegion(region);
        users.save(u);
    }
}
