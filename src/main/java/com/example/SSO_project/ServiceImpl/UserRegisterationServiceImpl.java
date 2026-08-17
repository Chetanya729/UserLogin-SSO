package com.example.SSO_project.ServiceImpl;

import com.example.SSO_project.Exception.UserAlreadyExists;
import com.example.SSO_project.Repository.UserRepository;
import com.example.SSO_project.Service.UserRegisterationService;
import com.example.SSO_project.domain.PROVIDER;
import com.example.SSO_project.domain.ROLE;
import com.example.SSO_project.domain.RegisterRequest;
import com.example.SSO_project.domain.UserRegister;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRegisterationServiceImpl implements UserRegisterationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public UserRegister registerUser(RegisterRequest request) {
        if(userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExists("Username already exists");
        }
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExists("Email already exists");
        }
        UserRegister entity = UserRegister.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() == null ? ROLE.USER : request.getRole())
                .provider(PROVIDER.LOCAL)
                .build();
        return userRepository.save(entity);
    }
}
