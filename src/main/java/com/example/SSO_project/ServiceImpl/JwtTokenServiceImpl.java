package com.example.SSO_project.ServiceImpl;

import com.example.SSO_project.Service.JwtTokenService;
import com.example.SSO_project.domain.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtTokenServiceImpl implements JwtTokenService {

    @Value("${spring.security.jwt.secret-key}")
    private String SECRET_KEY;

    @Value("${spring.security.jwt.expiration}")
    private String expiration;

    @Override
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + Long.parseLong(expiration));
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    @Override
    public String getSecretKey() {
        return SECRET_KEY;
    }
}
