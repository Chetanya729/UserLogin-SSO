package com.example.SSO_project.Service;

import org.springframework.stereotype.Service;

@Service
public interface PasswordResetService {

    default void createTokenAndSendEmail(String email) {
    }

    default void resetPassword(String token, String newPassword, String confirmPassword) {
    }
}
