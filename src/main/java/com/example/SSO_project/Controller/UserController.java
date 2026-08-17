package com.example.SSO_project.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
    public class UserController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
    @GetMapping("/error")
    public String error() {
        return "error";
    }
    @GetMapping("/home")
    public String home() {
        return "home";
    }
}
