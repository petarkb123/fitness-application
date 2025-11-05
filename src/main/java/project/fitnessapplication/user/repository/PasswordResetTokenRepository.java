package project.fitnessapplication.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.fitnessapplication.user.model.PasswordResetToken;
import project.fitnessapplication.user.model.User;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByToken(String token);
    
    @Modifying
    @Query("update PasswordResetToken t set t.used = true where t.user = :user")
    void markAllAsUsedForUser(@Param("user") User user);
    
    @Modifying
    @Query("delete from PasswordResetToken t where t.expiresAt < current_timestamp or t.used = true")
    void deleteExpiredAndUsedTokens();
}

