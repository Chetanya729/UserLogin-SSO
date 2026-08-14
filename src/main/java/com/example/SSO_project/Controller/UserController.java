package com.example.SSO_project.Controller;

import com.example.SSO_project.Service.JwtTokenService;
import com.example.SSO_project.Service.UserDetailService;
import com.example.SSO_project.domain.User;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
    public class UserController {
    private final UserDetailService userDetailsService;
    private final JwtTokenService jwtTokenService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }
    @GetMapping("/error")
    public String error() {
        return "error";
    }

    @PostMapping("/login")
    public String login(String username, String password, HttpServletResponse response) {
        User authenticatedUser = userDetailsService.authenticate(username, password);
        if (authenticatedUser != null) {
            String token = jwtTokenService.generateToken(authenticatedUser);
            response.setHeader("Authorization", "Bearer " + token);
            return "redirect:/home";
        } else {
            return "redirect:/login?error";
        }
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/logout")
    public String logout() {
        return "logout";
    }
}
