package com.example.SSO_project.Service;

import com.example.SSO_project.domain.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserDetailService extends UserDetailsService {
    User authenticate(String username, String password);
}
