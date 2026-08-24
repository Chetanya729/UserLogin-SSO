package com.example.SSO_project.Service;

import com.example.SSO_project.domain.UpdateProfileRequest;

public interface UserProfileUpdateService {

    void updateUserProfile(String currUsername, UpdateProfileRequest request);
}
