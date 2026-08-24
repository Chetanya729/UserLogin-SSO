package com.example.SSO_project.ServiceImpl;

import com.example.SSO_project.Exception.InvalidCredentialsException;
import com.example.SSO_project.Exception.UserAlreadyExists;
import com.example.SSO_project.Repository.UserRepository;
import com.example.SSO_project.Service.UserCacheService;
import com.example.SSO_project.Service.UserProfileUpdateService;
import com.example.SSO_project.domain.UpdateProfileRequest;
import com.example.SSO_project.domain.UserRegister;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileUpdateServiceImpl implements UserProfileUpdateService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserCacheService userCacheService;

    @Override
    @Transactional
    public void updateUserProfile(String currUsername, UpdateProfileRequest request) {

        UserRegister user = userRepository.findByUsername(currUsername)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found in DB: " + currUsername));

        if (user.getPassword() == null) {
            throw new InvalidCredentialsException(
                    "This account signs in with an external provider and has no password to verify.");
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect.");
        }

        String newUsername = request.getNewUsername();
        if (newUsername != null && !newUsername.equals(user.getUsername())) {
            if (userRepository.existsByUsername(newUsername)) {
                throw new UserAlreadyExists("That username is already taken.");
            }
            user.setUsername(newUsername);
        }

        String newEmail = request.getNewEmail();
        if (newEmail != null && !newEmail.equals(user.getEmail())) {
            if (userRepository.existsByEmail(newEmail)) {
                throw new UserAlreadyExists("That email is already registered.");
            }
            user.setEmail(newEmail);
        }

        userRepository.save(user);

        userCacheService.evictUser(currUsername);
    }
}
