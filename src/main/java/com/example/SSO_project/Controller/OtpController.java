package com.example.SSO_project.Controller;

import com.example.SSO_project.Repository.UserRepository;
import com.example.SSO_project.Service.OtpService;
import com.example.SSO_project.domain.OTPVERIFY;
import com.example.SSO_project.domain.UserRegister;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

import static com.example.SSO_project.Config.RoleBasedAuthenticationSuccessHandler.PENDING_2FA;

@Controller
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;
    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    @GetMapping("/verify-otp")
    public String showVerifyForm(HttpServletRequest request, Model model) {

        HttpSession session = request.getSession(false);
        if (session == null) {
            return "redirect:/login";
        }

        String pendingUsername = (String) session.getAttribute(PENDING_2FA);
        if (pendingUsername == null) {
            return "redirect:/login";
        }

        Optional<UserRegister> maybeUser = userRepository.findByUsername(pendingUsername);
        if (maybeUser.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }

        model.addAttribute("maskedEmail", maskEmail(maybeUser.get().getEmail()));
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String submitOtp(@RequestParam("otp") String submitted,
                            HttpServletRequest request,
                            HttpServletResponse response,
                            Model model) {

        HttpSession session = request.getSession(false);
        if (session == null) {
            return "redirect:/login";
        }

        String pendingUsername = (String) session.getAttribute(PENDING_2FA);
        if (pendingUsername == null) {
            return "redirect:/login";
        }

        Optional<UserRegister> maybeUser = userRepository.findByUsername(pendingUsername);
        if (maybeUser.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }

        OTPVERIFY result = otpService.verify(pendingUsername, submitted);

        switch (result) {
            case VALID -> {
                boolean isAdmin = completeAuthentication(pendingUsername, request, response);
                session.removeAttribute(PENDING_2FA);
                return isAdmin ? "redirect:/admin/home" : "redirect:/home";
            }
            case INVALID -> {
                model.addAttribute("error", "That code isn't right.");
                model.addAttribute("maskedEmail", maskEmail(maybeUser.get().getEmail()));
                return "verify-otp";
            }
            case EXPIRED -> {
                model.addAttribute("error", "That code expired. Request a new one.");
                model.addAttribute("maskedEmail", maskEmail(maybeUser.get().getEmail()));
                return "verify-otp";
            }
            case TOO_MANY_ATTEMPTS -> {
                session.invalidate();
                return "redirect:/login?otpLocked";
            }
        }

        return "redirect:/login";
    }

    @PostMapping("/verify-otp/resend")
    public String resendOtp(HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session == null) {
            return "redirect:/login";
        }

        String pendingUsername = (String) session.getAttribute(PENDING_2FA);
        if (pendingUsername == null) {
            return "redirect:/login";
        }

        if (!otpService.canResendOtp(pendingUsername)) {
            return "redirect:/verify-otp?cooldown";
        }

        Optional<UserRegister> maybeUser = userRepository.findByUsername(pendingUsername);
        if (maybeUser.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }

        otpService.generateAndSendOtp(maybeUser.get().getUsername(), maybeUser.get().getEmail());
        return "redirect:/verify-otp?resent";
    }
    private boolean completeAuthentication(String username,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        request.changeSessionId();


        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);

        return userDetails.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private String maskEmail(String email) {
        if (email == null) {
            return "";
        }
        int at = email.indexOf('@');
        if (at <= 0) {
            return "•••";
        }
        return email.charAt(0) + "•••" + email.substring(at);
    }
}
