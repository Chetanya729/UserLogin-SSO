package com.example.SSO_project.Service;

import com.example.SSO_project.domain.UpdateProfileRequest;

public interface UserProfileUpdateService {

    void updateUserProfile(String currUsername, UpdateProfileRequest request);

    /**
     * Turns email-OTP two-factor authentication on or off for this account.
     * Requires the current password: enabling adds a login step, disabling
     * removes one, and both are security-relevant enough to re-prove identity.
     */
    void setTwoFactorEnabled(String currUsername, boolean enabled, String currentPassword);
}
