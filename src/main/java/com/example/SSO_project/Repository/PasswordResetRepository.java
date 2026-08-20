package com.example.SSO_project.Repository;

import com.example.SSO_project.domain.PasswordResetToken;
import com.example.SSO_project.domain.UserRegister;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(UserRegister user);
}
