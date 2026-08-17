package com.example.SSO_project.Repository;

import com.example.SSO_project.domain.UserRegister;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserRegister, Long> {
    Optional<UserRegister> findByUsername(String username);
    Optional<UserRegister> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
