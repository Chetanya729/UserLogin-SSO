package com.example.SSO_project.Controller;

import com.example.SSO_project.Service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @GetMapping("/forgot-password")
    public String passwordResetPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String submit(@RequestParam("email") String email, Model model, HttpServletRequest httpServletRequest) {
        String baseUrl = httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName()
                +(httpServletRequest.getServerPort() == 80 || httpServletRequest.getServerPort() == 443 ? "" : ":" + httpServletRequest.getServerPort() );


        passwordResetService.createTokenAndSendEmail(email , baseUrl);

        model.addAttribute("message",
                "If an account exists for that email, we've sent a reset link. Check your inbox.");
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
                return "reset-password";

    }
    @PostMapping("/reset-password")
    public String submitResetPassword(@RequestParam String token, @RequestParam String password,@RequestParam String confirmPassword, Model model) {
        try {
            passwordResetService.resetPassword(token,password,confirmPassword);
            return "redirect:/login?reset" ;
        }catch (RuntimeException e){
            model.addAttribute("error",e.getMessage());
            model.addAttribute("token",token);
            return "reset-password";
        }
    }
}
