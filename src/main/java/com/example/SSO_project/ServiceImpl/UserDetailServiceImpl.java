package com.example.SSO_project.ServiceImpl;

import com.example.SSO_project.Service.UserDetailService;
import com.example.SSO_project.domain.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserDetailServiceImpl implements UserDetailService {

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private static final Map<String, String> users = new HashMap<>();
    static {
        users.put("[EnterUsername]", ENCODER.encode("[EnterPassword]"));
    }

    @Override
    public User authenticate(String username, String password) {
        String storedPassword = users.get(username);
        if (storedPassword != null && ENCODER.matches(password, storedPassword)) {
            return new User(username, null);
        }
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String storedPassword = users.get(username);
        if (storedPassword != null) {
            return org.springframework.security.core.userdetails.User.builder()
                    .username(username)
                    .password(storedPassword)
                    .roles("USER")
                    .build();
        } else {
            throw new UsernameNotFoundException("username not found");
        }
    }
}
