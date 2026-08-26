package com.example.SSO_project.Config;

import com.example.SSO_project.Repository.UserRepository;
import com.example.SSO_project.Service.OtpService;
import com.example.SSO_project.domain.PROVIDER;
import com.example.SSO_project.domain.UserRegister;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RoleBasedAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final OtpService otpService;

    public static final String PENDING_2FA = "PENDING_2FA";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        UserRegister user = userRepository.findByUsername(authentication.getName()).orElse(null);

        if (requireOtp(user)) {
            directToChallenge(request, response, user);
            return;
        }
        redirectByRole(request, response, authentication);
    }

    private boolean requireOtp(UserRegister user) {
        return user != null && user.getProvider() == PROVIDER.LOCAL && user.isTwoFactorEnabled();
    }

    private void directToChallenge(HttpServletRequest request, HttpServletResponse response, UserRegister user) throws IOException {

        String username = user.getUsername();
        String email = user.getEmail();

        request.getSession().invalidate();
        request.getSession(true).setAttribute(RoleBasedAuthenticationSuccessHandler.PENDING_2FA, username);

        SecurityContextHolder.clearContext();
        otpService.generateAndSendOtp(username, email);

        getRedirectStrategy().sendRedirect(request, response, "/verify-otp");

    }

    private void redirectByRole(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        String target = isAdmin ? "/admin/home" : "/home";
        getRedirectStrategy().sendRedirect(request, response, target);
    }
}
