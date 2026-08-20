package com.example.SSO_project.ServiceImpl;

import com.example.SSO_project.Exception.InvalidTokenException;
import com.example.SSO_project.Exception.PasswordMismatchException;
import com.example.SSO_project.Repository.PasswordResetRepository;
import com.example.SSO_project.Repository.UserRepository;
import com.example.SSO_project.Service.PasswordResetService;
import com.example.SSO_project.domain.PasswordResetToken;
import com.example.SSO_project.domain.UserRegister;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {
    private final JavaMailSender MailSender;
    private final PasswordResetRepository passwordResetRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Value("${app.password-reset.token-validity-minutes:15}")
    private long tokenValidityMinutes;

    @Value("${app.mail.from}")
    private String fromAddress;


    @Override
    @Transactional
    public void createTokenAndSendEmail(String email, String baseUrl) {
        Optional<UserRegister> MaybeUser = userRepository.findByEmail(email);
        if (MaybeUser.isEmpty() || MaybeUser.get().getPassword() == null) {
            log.info("No user found with email {}", email);
            return;
        }
        UserRegister user = MaybeUser.get();
        passwordResetRepository.deleteByUser(user);
        PasswordResetToken token = PasswordResetToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .createdAt(Instant.now())
                .expiry_At(Instant.now().plusSeconds(tokenValidityMinutes*60))
                .used(false)
                .build();
        passwordResetRepository.save(token);
        sendEmail(user.getEmail(),token.getToken());
    }

    @Override
    public void resetPassword(String tokenValue, String newPassword, String confirmPassword) {
        if (!confirmPassword.equals(newPassword)) {
            throw new PasswordMismatchException("Passwords do not match");
        }
        if(newPassword.length() < 6 ||  newPassword.length() > 20) {
            throw new PasswordMismatchException("Password must be 6-20 characters of length");
        }
        PasswordResetToken token = passwordResetRepository.findByToken(tokenValue)
                .orElseThrow(() -> new InvalidTokenException("Invalid token"));

        if(!token.isValid()) {
            throw new InvalidTokenException("The reset link has been expired or already user");
        }

        UserRegister user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsed(true);
        passwordResetRepository.save(token);
    }

    private void sendEmail(String toAddress, String tokenValue) {
        String resetUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/reset-password")
                .queryParam("token",tokenValue)
                .toUriString();
       try{
           SimpleMailMessage mailMessage = new SimpleMailMessage();
           mailMessage.setFrom(fromAddress);
           mailMessage.setTo(toAddress);
           mailMessage.setSubject("Reset Password");
           mailMessage.setText(
                   "Someone requested a password reset for your account.\n\n" +
                           "Click the link below to set a new password:\n" +
                           resetUrl + "/reset-password?token=" + tokenValue + "\n\n" +
                           "This link expires in " + tokenValidityMinutes + " minutes.\n" +
                           "If you didn't request this, you can safely ignore this email."
           );
           MailSender.send(mailMessage);
       }
        catch(Exception e){
           log.info("failed to send reset password mail to {}",toAddress,e);
        }
    }
}
