package com.example.SSO_project.ServiceImpl;

import com.example.SSO_project.Repository.UserRepository;
import com.example.SSO_project.Service.UserDetailService;
import com.example.SSO_project.domain.User;
import com.example.SSO_project.domain.UserRegister;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User authenticate(String username, String password) {
        UserRegister entity = userRepository.findByUsername(username).orElse(null);
        if (entity == null || entity.getPassword() == null) {
            return null;
        }
        if (!passwordEncoder.matches(password, entity.getPassword())) {
            return null;
        }
        return new User(entity.getUsername(), null);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserRegister entity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("username not found: " + username));

        if (entity.getPassword() == null) {
            throw new UsernameNotFoundException(
                    "user registered via external provider; please sign in with that provider");
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(entity.getUsername())
                .password(entity.getPassword())
                .roles(entity.getRole().name())
                .build();
    }
}
