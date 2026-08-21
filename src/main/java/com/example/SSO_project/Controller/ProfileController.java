package com.example.SSO_project.Controller;

import com.example.SSO_project.Repository.UserRepository;
import com.example.SSO_project.domain.UserRegister;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;

    @GetMapping("/profile")
    public String Authentication(Authentication authentication, Model model){
        String username = authentication.getName();
        UserRegister user = userRepository.findByUsername(username)
                .orElseThrow( () -> new IllegalStateException("Authenticated User not found" + username));
        model.addAttribute("user", user);
        return "profile";
    }
}
