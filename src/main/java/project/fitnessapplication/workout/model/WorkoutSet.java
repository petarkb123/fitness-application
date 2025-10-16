package project.fitnessapplication.workout.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name="workout_sets",
        indexes={
                @Index(name="ix_wset_session", columnList="session_id"),
                @Index(name="ix_wset_ex", columnList="exercise_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true) @EqualsAndHashCode(of="id")
public class WorkoutSet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", columnDefinition = "char(36)")
    private UUID id;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "session_id", nullable = false, columnDefinition = "char(36)")
    private UUID sessionId;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "exercise_id", nullable = false, columnDefinition = "char(36)")
    private UUID exerciseId;

    private Integer reps;
    private BigDecimal weight;
    private boolean warmup;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "group_id", columnDefinition = "char(36)")
    private UUID groupId;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_type", length = 20)
    private SetGroupType groupType;

    @Column(name = "group_order")
    private Integer groupOrder;
    
    @Column(name = "set_number")
    private Integer setNumber;
    
    @Column(name = "exercise_order")
    private Integer exerciseOrder;
}
