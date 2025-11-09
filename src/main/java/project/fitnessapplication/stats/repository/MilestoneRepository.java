package project.fitnessapplication.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.fitnessapplication.stats.model.Milestone;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MilestoneRepository extends JpaRepository<Milestone, UUID> {

    List<Milestone> findByUserIdOrderByAchievedDateDesc(UUID userId);

    Optional<Milestone> findByUserIdAndTitle(UUID userId, String title);
}
