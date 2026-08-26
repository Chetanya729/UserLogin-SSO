package com.example.SSO_project.ServiceImpl;

import com.example.SSO_project.Exception.InvalidCredentialsException;
import com.example.SSO_project.Exception.UserAlreadyExists;
import com.example.SSO_project.Repository.UserRepository;
import com.example.SSO_project.Service.UserCacheService;
import com.example.SSO_project.domain.PROVIDER;
import com.example.SSO_project.domain.ROLE;
import com.example.SSO_project.domain.UpdateProfileRequest;
import com.example.SSO_project.domain.UserRegister;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileUpdateServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserCacheService userCacheService;

    @InjectMocks private UserProfileUpdateServiceImpl service;

    private static final String USERNAME = "chetanya";
    private static final String EMAIL = "chetanya@example.com";
    private static final String HASH = "$2a$10$storedhash";
    private static final String RAW_PASSWORD = "correct-password";

    private UserRegister existingUser() {
        return UserRegister.builder()
                .id(1L)
                .username(USERNAME)
                .email(EMAIL)
                .password(HASH)
                .role(ROLE.USER)
                .provider(PROVIDER.LOCAL)
                .build();
    }

    private UpdateProfileRequest request(String newUsername, String newEmail, String currentPassword) {
        return UpdateProfileRequest.builder()
                .newUsername(newUsername)
                .newEmail(newEmail)
                .currentPassword(currentPassword)
                .build();
    }

    @Test
    @DisplayName("changing only the email saves the user and evicts the cache")
    void updatesEmail() {
        // Arrange — describe the world the method will find.
        UserRegister user = existingUser();
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, HASH)).thenReturn(true);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        // Act
        service.updateUserProfile(USERNAME, request(USERNAME, "new@example.com", RAW_PASSWORD));

        // Assert — state that changed, plus the collaborators that were called.
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getUsername()).isEqualTo(USERNAME);
        verify(userRepository).save(user);
        verify(userCacheService).evictUser(USERNAME);
    }

    @Test
    @DisplayName("a wrong current password is rejected before anything is written")
    void rejectsWrongPassword() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(existingUser()));
        when(passwordEncoder.matches("wrong", HASH)).thenReturn(false);

        assertThatThrownBy(() ->
                service.updateUserProfile(USERNAME, request(USERNAME, "new@example.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("incorrect");

        // The important half: failing loudly is not enough, it must also change nothing.
        verify(userRepository, never()).save(any());
        verifyNoInteractions(userCacheService);
    }

    @Test
    @DisplayName("a username already taken by someone else is rejected")
    void rejectsTakenUsername() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(existingUser()));
        when(passwordEncoder.matches(RAW_PASSWORD, HASH)).thenReturn(true);
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() ->
                service.updateUserProfile(USERNAME, request("taken", EMAIL, RAW_PASSWORD)))
                .isInstanceOf(UserAlreadyExists.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("resubmitting the form unchanged does not trip the uniqueness checks")
    void allowsUnchangedResubmit() {
        UserRegister user = existingUser();
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, HASH)).thenReturn(true);

        service.updateUserProfile(USERNAME, request(USERNAME, EMAIL, RAW_PASSWORD));

        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository).save(user);
    }
}
