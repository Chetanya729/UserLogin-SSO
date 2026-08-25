package com.example.SSO_project.Controller;

import com.example.SSO_project.Exception.InvalidCredentialsException;
import com.example.SSO_project.Exception.UserAlreadyExists;
import com.example.SSO_project.Repository.UserRepository;
import com.example.SSO_project.Service.UserProfileUpdateService;
import com.example.SSO_project.domain.UpdateProfileRequest;
import com.example.SSO_project.domain.UserRegister;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final UserProfileUpdateService userProfileUpdateService;

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        model.addAttribute("user", loadCurrentUser(authentication));
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute UpdateProfileRequest updateProfileRequest,
                                Authentication authentication,
                                HttpServletRequest httpRequest,
                                Model model) {

        UserRegister user = loadCurrentUser(authentication);
        String oldUsername = user.getUsername();

        try {
            userProfileUpdateService.updateUserProfile(oldUsername, updateProfileRequest);
        } catch (InvalidCredentialsException | UserAlreadyExists e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            return "profile";
        }

        String newUsername = updateProfileRequest.getNewUsername();
        if (newUsername != null && !newUsername.equals(oldUsername)) {
            httpRequest.getSession().invalidate();
            return "redirect:/login?usernameChanged";
        }

        return "redirect:/profile?updated";
    }
    @PostMapping("/profile/two-factor")
    public String toggleTwoFactor(@RequestParam boolean enabled,
                                  @RequestParam String currentPassword,
                                  Authentication authentication,
                                  Model model) {

        UserRegister user = loadCurrentUser(authentication);

        try {
            userProfileUpdateService.setTwoFactorEnabled(user.getUsername(), enabled, currentPassword);
        } catch (InvalidCredentialsException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            return "profile";
        }

        return "redirect:/profile?" + (enabled ? "twoFactorOn" : "twoFactorOff");
    }

    private UserRegister loadCurrentUser(Authentication auth) {
        Object principal = auth.getPrincipal();

        if (principal instanceof OidcUser oidc) {
            return findByEmailOrThrow(oidc.getEmail());
        }

        if (principal instanceof OAuth2User oauth2) {
            String email = oauth2.getAttribute("email");
            if (email == null) {
                email = oauth2.getAttribute("id") + "@users.noreply.github.com";
            }
            return findByEmailOrThrow(email);
        }

        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found in DB: " + auth.getName()));
    }

    private UserRegister findByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found in DB for email: " + email));
    }
}
