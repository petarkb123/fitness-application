package project.fitnessapplication.exercise.seed;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.fitnessapplication.config.SystemDefault;
import project.fitnessapplication.exercise.model.Equipment;
import project.fitnessapplication.exercise.model.Exercise;
import project.fitnessapplication.exercise.model.MuscleGroup;
import project.fitnessapplication.exercise.repository.ExerciseRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ExerciseBuiltinSeeder implements ApplicationRunner {

    private final ExerciseRepository repo;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        final var SYS = SystemDefault.SYSTEM_USER_ID;
        
        // Migrate any old LEGS values to QUADS before seeding
        try {
            entityManager.createNativeQuery("UPDATE exercises SET primary_muscle = 'QUADS' WHERE primary_muscle = 'LEGS'")
                    .executeUpdate();
        } catch (Exception e) {
            // Ignore if query fails (table might not exist yet or query syntax issue)
        }

        
        upsert("Barbell Bench Press", MuscleGroup.CHEST, Equipment.BARBELL, SYS);
        upsert("Dumbbell Bench Press", MuscleGroup.CHEST, Equipment.DUMBBELL, SYS);
        upsert("Incline Bench Press", MuscleGroup.CHEST, Equipment.BARBELL, SYS);
        upsert("Decline Bench Press", MuscleGroup.CHEST, Equipment.BARBELL, SYS);
        upsert("Incline Machine Press", MuscleGroup.CHEST, Equipment.MACHINE, SYS);
        upsert("Flat Machine Press", MuscleGroup.CHEST, Equipment.MACHINE, SYS);
        upsert("Machine Bench Press", MuscleGroup.CHEST, Equipment.MACHINE, SYS);
        upsert("Incline Machine Fly", MuscleGroup.CHEST, Equipment.MACHINE, SYS);
        upsert("Incline Dumbbell Press", MuscleGroup.CHEST, Equipment.DUMBBELL, SYS);
        upsert("Decline Dumbbell Press", MuscleGroup.CHEST, Equipment.DUMBBELL, SYS);
        upsert("Dumbbell Fly", MuscleGroup.CHEST, Equipment.DUMBBELL, SYS);
        upsert("Incline Dumbbell Fly", MuscleGroup.CHEST, Equipment.DUMBBELL, SYS);
        upsert("Cable Fly (High to Low)", MuscleGroup.CHEST, Equipment.CABLE, SYS);
        upsert("Cable Fly (Low to High)", MuscleGroup.CHEST, Equipment.CABLE, SYS);
        upsert("Pec Deck Fly", MuscleGroup.CHEST, Equipment.MACHINE, SYS);
        upsert("Smith Machine Bench Press", MuscleGroup.CHEST, Equipment.MACHINE, SYS);
        upsert("Weighted Push-Ups", MuscleGroup.CHEST, Equipment.OTHER, SYS);
        upsert("Push-Ups", MuscleGroup.CHEST, Equipment.BODYWEIGHT, SYS);



        
        upsert("Pull-Ups", MuscleGroup.BACK, Equipment.BODYWEIGHT, SYS);
        upsert("Barbell Rows", MuscleGroup.BACK, Equipment.BARBELL, SYS);
        upsert("Dumbbell Rows", MuscleGroup.BACK, Equipment.DUMBBELL, SYS);
        upsert("Lat Pullovers", MuscleGroup.BACK, Equipment.CABLE, SYS);
        upsert("Lat Pulldowns", MuscleGroup.BACK, Equipment.CABLE, SYS);
        upsert("T-Bar Rows", MuscleGroup.BACK, Equipment.BARBELL, SYS);
        upsert("Deadlifts", MuscleGroup.BACK, Equipment.BARBELL, SYS);
        upsert("Chest Supported Row", MuscleGroup.BACK, Equipment.MACHINE, SYS);
        upsert("Straight-Arm Pulldowns", MuscleGroup.BACK, Equipment.CABLE, SYS);
        upsert("Rack Pull", MuscleGroup.BACK, Equipment.BARBELL, SYS);
        upsert("Single-Arm Dumbbell Row", MuscleGroup.BACK, Equipment.DUMBBELL, SYS);
        upsert("Chest-Supported Dumbbell Row", MuscleGroup.BACK, Equipment.DUMBBELL, SYS);
        upsert("Seated Cable Row", MuscleGroup.BACK, Equipment.CABLE, SYS);
        upsert("One-Arm Cable Row", MuscleGroup.BACK, Equipment.CABLE, SYS);
        upsert("Wide-Grip Lat Pulldown", MuscleGroup.BACK, Equipment.CABLE, SYS);
        upsert("Close-Grip Lat Pulldown", MuscleGroup.BACK, Equipment.CABLE, SYS);
        upsert("Neutral-Grip Lat Pulldown", MuscleGroup.BACK, Equipment.CABLE, SYS);
        upsert("Machine Row", MuscleGroup.BACK, Equipment.MACHINE, SYS);

        
        upsert("Barbell Curl", MuscleGroup.BICEPS, Equipment.BARBELL, SYS);
        upsert("Dumbbell Curl", MuscleGroup.BICEPS, Equipment.DUMBBELL, SYS);
        upsert("Dumbbell Hammer Curl", MuscleGroup.BICEPS, Equipment.DUMBBELL, SYS);
        upsert("Preacher Curl", MuscleGroup.BICEPS, Equipment.MACHINE, SYS);
        upsert("Preacher Hammer Curl", MuscleGroup.BICEPS, Equipment.MACHINE, SYS);
        upsert("Cable Hammer Curl", MuscleGroup.BICEPS, Equipment.CABLE, SYS);
        upsert("Concentration Curl", MuscleGroup.BICEPS, Equipment.DUMBBELL, SYS);
        upsert("Bayesian curl", MuscleGroup.BICEPS, Equipment.CABLE, SYS);
        upsert("Cable Curl", MuscleGroup.BICEPS, Equipment.CABLE, SYS);
        upsert("EZ-Bar Curl", MuscleGroup.BICEPS, Equipment.BARBELL, SYS);
        upsert("Reverse Curl", MuscleGroup.BICEPS, Equipment.BARBELL, SYS);
        upsert("Incline Dumbbell Curl", MuscleGroup.BICEPS, Equipment.DUMBBELL, SYS);
        upsert("Spider Curl", MuscleGroup.BICEPS, Equipment.DUMBBELL, SYS);
        upsert("Concentration Cable Curl", MuscleGroup.BICEPS, Equipment.CABLE, SYS);
        upsert("Machine Biceps Curl", MuscleGroup.BICEPS, Equipment.MACHINE, SYS);

        
        upsert("Close-Grip Bench Press", MuscleGroup.TRICEPS, Equipment.BARBELL, SYS);
        upsert("Tricep Dips", MuscleGroup.TRICEPS, Equipment.BODYWEIGHT, SYS);
        upsert("JM Press", MuscleGroup.TRICEPS, Equipment.MACHINE, SYS);
        upsert("Overhead Tricep Extension", MuscleGroup.TRICEPS, Equipment.CABLE, SYS);
        upsert("Skull Crushers", MuscleGroup.TRICEPS, Equipment.BARBELL, SYS);
        upsert("Tricep Pushdowns", MuscleGroup.TRICEPS, Equipment.CABLE, SYS);
        upsert("Single-Arm Cable Pushdown", MuscleGroup.TRICEPS, Equipment.CABLE, SYS);
        upsert("Machine Dip", MuscleGroup.TRICEPS, Equipment.MACHINE, SYS);



        
        upsert("Overhead Press", MuscleGroup.SHOULDERS, Equipment.BARBELL, SYS);
        upsert("Smith Machine Press", MuscleGroup.SHOULDERS, Equipment.MACHINE, SYS);
        upsert("Arnold Press", MuscleGroup.SHOULDERS, Equipment.DUMBBELL, SYS);
        upsert("Lateral Raises", MuscleGroup.SHOULDERS, Equipment.DUMBBELL, SYS);
        upsert("Cable Lateral Raises", MuscleGroup.SHOULDERS, Equipment.CABLE, SYS);
        upsert("Machine Lateral Raises", MuscleGroup.SHOULDERS, Equipment.MACHINE, SYS);
        upsert("Front Raises", MuscleGroup.SHOULDERS, Equipment.CABLE, SYS);
        upsert("Rear Delt Flyes", MuscleGroup.SHOULDERS, Equipment.MACHINE, SYS);
        upsert("Upright Rows", MuscleGroup.SHOULDERS, Equipment.CABLE, SYS);
        upsert("Seated Dumbbell Shoulder Press", MuscleGroup.SHOULDERS, Equipment.DUMBBELL, SYS);
        upsert("Machine Shoulder Press", MuscleGroup.SHOULDERS, Equipment.MACHINE, SYS);
        upsert("Rear Delt Cable Fly", MuscleGroup.SHOULDERS, Equipment.CABLE, SYS);
        upsert("Cable Face Pull", MuscleGroup.SHOULDERS, Equipment.CABLE, SYS);
        upsert("Cable Y-Raise", MuscleGroup.SHOULDERS, Equipment.CABLE, SYS);
        upsert("Barbell Upright Row", MuscleGroup.SHOULDERS, Equipment.BARBELL, SYS);

        
        upsert("Wrist Curls", MuscleGroup.FOREARMS, Equipment.BARBELL, SYS);
        upsert("Cable Forearm Curl", MuscleGroup.FOREARMS, Equipment.CABLE, SYS);
        upsert("Behind-the-Back Wrist Curl", MuscleGroup.FOREARMS, Equipment.BARBELL, SYS);

        
        upsert("Plank", MuscleGroup.CORE, Equipment.BODYWEIGHT, SYS);
        upsert("Hanging Leg Raises", MuscleGroup.CORE, Equipment.BODYWEIGHT, SYS);
        upsert("Cable Crunches", MuscleGroup.CORE, Equipment.CABLE, SYS);
        upsert("Russian Twists", MuscleGroup.CORE, Equipment.BODYWEIGHT, SYS);
        upsert("Bicycle Crunches", MuscleGroup.CORE, Equipment.BODYWEIGHT, SYS);
        upsert("Ab Rollouts", MuscleGroup.CORE, Equipment.OTHER, SYS);
        upsert("Sit-Ups", MuscleGroup.CORE, Equipment.BODYWEIGHT, SYS);
 

        
        upsert("Back Squat", MuscleGroup.QUADS, Equipment.BARBELL, SYS);
        upsert("Hack Squat", MuscleGroup.QUADS, Equipment.MACHINE, SYS);
        upsert("Front Squat", MuscleGroup.QUADS, Equipment.BARBELL, SYS);
        upsert("Leg Extension", MuscleGroup.QUADS, Equipment.MACHINE, SYS);
        upsert("Bulgarian Split Squat", MuscleGroup.QUADS, Equipment.DUMBBELL, SYS);
        upsert("Walking Lunges", MuscleGroup.QUADS, Equipment.DUMBBELL, SYS);
        upsert("Leg Press", MuscleGroup.QUADS, Equipment.MACHINE, SYS);
        upsert("Smith Machine Squat", MuscleGroup.QUADS, Equipment.MACHINE, SYS);
        upsert("Goblet Squat", MuscleGroup.QUADS, Equipment.DUMBBELL, SYS);
        upsert("Belt Squat", MuscleGroup.QUADS, Equipment.MACHINE, SYS);

        
        upsert("Romanian Deadlift", MuscleGroup.HAMSTRINGS, Equipment.BARBELL, SYS);
        upsert("Stiff-Leg Deadlift", MuscleGroup.HAMSTRINGS, Equipment.BARBELL, SYS);
        upsert("Good Mornings", MuscleGroup.HAMSTRINGS, Equipment.BARBELL, SYS);
        upsert("Seated Leg Curl", MuscleGroup.HAMSTRINGS, Equipment.MACHINE, SYS);
        upsert("Lying Leg Curl", MuscleGroup.HAMSTRINGS, Equipment.MACHINE, SYS);
        upsert("Back Extension", MuscleGroup.HAMSTRINGS, Equipment.OTHER, SYS);


        
        upsert("Standing Calf Raises", MuscleGroup.CALVES, Equipment.MACHINE, SYS);
        upsert("Seated Calf Raises", MuscleGroup.CALVES, Equipment.MACHINE, SYS);
        upsert("Single-Leg Calf Raises", MuscleGroup.CALVES, Equipment.MACHINE, SYS);
        upsert("Leg Press Calf Press", MuscleGroup.CALVES, Equipment.MACHINE, SYS);

    }

    private void upsert(String name, MuscleGroup mg, Equipment eq, java.util.UUID owner) {
        repo.findByNameIgnoreCaseAndOwnerUserId(name, owner).ifPresentOrElse(
                e -> {}, 
                () -> {
                    var ex = new Exercise();
                    ex.setOwnerUserId(owner);
                    ex.setName(name);
                    ex.setPrimaryMuscle(mg);
                    ex.setEquipment(eq);
                    ex.setCreatedOn(LocalDateTime.now());
                    repo.save(ex);
                }
        );
    }
}
