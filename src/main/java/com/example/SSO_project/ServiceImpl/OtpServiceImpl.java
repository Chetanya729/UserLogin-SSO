package com.example.SSO_project.ServiceImpl;

import com.example.SSO_project.Service.OtpService;
import com.example.SSO_project.domain.OTPVERIFY;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redis;

    @Value("${app.otp.validity-minutes:5}")
    private long validityMinutes;

    @Value("${app.otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.otp.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Override
    public void generateAndSendOtp(String username, String email) {
        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        redis.opsForValue().set(otpKey(username), otp, Duration.ofMinutes(validityMinutes));
        redis.delete(attemptsKey(username));

        sendEmail(email, otp);

        redis.opsForValue().set(resendKey(username), "1", Duration.ofSeconds(resendCooldownSeconds));
    }

    @Override
    public OTPVERIFY verify(String username, String otp) {

        String attempts = redis.opsForValue().get(attemptsKey(username));
        if (attempts != null && Integer.parseInt(attempts) >= maxAttempts) {
            invalidateOtp(username);
            return OTPVERIFY.TOO_MANY_ATTEMPTS;
        }

        String stored = redis.opsForValue().get(otpKey(username));
        if (stored == null) {

            return OTPVERIFY.EXPIRED;
        }

        if (!constantTimeEquals(stored, otp)) {
            recordFailedAttempt(username);
            return OTPVERIFY.INVALID;
        }
        invalidateOtp(username);
        return OTPVERIFY.VALID;
    }

    @Override
    public void invalidateOtp(String username) {
        redis.delete(otpKey(username));
        redis.delete(attemptsKey(username));
    }

    @Override
    public boolean canResendOtp(String username) {
        return Boolean.FALSE.equals(redis.hasKey(resendKey(username)));
    }

    private void recordFailedAttempt(String username) {

        Long count = redis.opsForValue().increment(attemptsKey(username));
        if (count != null && count == 1L) {
            redis.expire(attemptsKey(username), Duration.ofMinutes(validityMinutes));
        }
    }

    private void sendEmail(String toAddress, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toAddress);
        message.setSubject("Your sign-in code");
        message.setText(
                "Your sign-in code is: " + otp + "\n\n" +
                "It expires in " + validityMinutes + " minutes.\n" +
                "If you didn't try to sign in, someone may know your password — change it."
        );

        mailSender.send(message);
    }
    private boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b == null ? new byte[0] : b.getBytes(StandardCharsets.UTF_8));
    }

    private String otpKey(String username)      { return "otp:" + username; }
    private String attemptsKey(String username) { return "otp:attempts:" + username; }
    private String resendKey(String username)   { return "otp:resend:" + username; }
}
