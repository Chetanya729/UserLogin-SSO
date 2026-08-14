package com.example.SSO_project.Service;

import com.example.SSO_project.domain.User;

public interface JwtTokenService {
    String generateToken(User user);
    String getSecretKey();
}
