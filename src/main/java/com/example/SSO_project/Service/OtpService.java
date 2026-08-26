package com.example.SSO_project.Service;

import com.example.SSO_project.domain.OTPVERIFY;

public interface OtpService {

    void generateAndSendOtp(String username, String email);

    OTPVERIFY verify(String username, String otp);

    void invalidateOtp(String username);

    boolean canResendOtp(String username);
}
