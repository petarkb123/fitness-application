package project.fitnessapplication.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "ix_users_username", columnList = "username", unique = true),
                @Index(name = "ix_users_email", columnList = "email", unique = true)
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true) @EqualsAndHashCode(of = "id")
public class User {

    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", nullable = false, updatable = false, length = 36, columnDefinition = "char(36)")
    private UUID id;

    @Column(nullable = false, length = 64, unique = true)
    private String username;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "profile_picture", columnDefinition = "text")
    private String profilePicture;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Onboarding data fields
    @Column(length = 20)
    private String gender; // MALE, FEMALE, OTHER

    @Column(name = "workout_frequency", length = 20)
    private String workoutFrequency; // LOW (0-2), MEDIUM (3-5), HIGH (6+)

    @Column
    private Integer heightCm;

    @Column
    private Integer weightKg;

    // Optional original imperial measurements when unitSystem = 'imperial'
    @Column(name = "height_feet")
    private Integer heightFeet;

    @Column(name = "height_inches")
    private Integer heightInches;

    @Column(name = "weight_lbs")
    private Integer weightLbs;

    @Column(name = "birthdate")
    private LocalDate birthdate;

    @Column(length = 20)
    private String goal; // LOSE_WEIGHT, MAINTAIN_WEIGHT, GAIN_WEIGHT

    @Column(name = "desired_weight_kg")
    private Integer desiredWeightKg;

    @Column(name = "weight_change_speed_kg")
    private Double weightChangeSpeedKg; // kg per week

    @Column(name = "unit_system", length = 20)
    private String unitSystem; // metric, imperial

    @Column(name = "timezone", length = 50)
    private String timezone; // IANA timezone ID (e.g., "Europe/Madrid", "America/New_York")

    @PreUpdate void touch() { updatedAt = LocalDateTime.now(); }
}
