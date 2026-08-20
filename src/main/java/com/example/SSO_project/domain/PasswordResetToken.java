package com.example.SSO_project.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_token")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true,nullable = false,length = 36)
    private String token = UUID.randomUUID().toString();

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiry_At;

    @Column(nullable = false)
    private boolean used = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserRegister user;

    public boolean isValid(){
            return !used && Instant.now().isBefore(expiry_At);
    }
}
