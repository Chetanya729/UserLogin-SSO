package com.example.SSO_project.Service;

import com.example.SSO_project.domain.OTPVERIFY;

public interface OtpService {

    /**
     * Generates a fresh code, stores it in Redis under a short TTL, and emails it.
     * Deliberately returns void: the plaintext code must not travel back up into
     * controllers, logs, or responses.
     */
    void generateAndSendOtp(String username, String email);

    /** Checks a submitted code, consuming it on success and counting failures. */
    OTPVERIFY verify(String username, String otp);

    /** Drops any outstanding code and its failure counter for this user. */
    void invalidateOtp(String username);

    /** False while the resend cooldown for this user is still active. */
    boolean canResendOtp(String username);
}
