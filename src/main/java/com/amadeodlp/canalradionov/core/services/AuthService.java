package com.amadeodlp.canalradionov.core.services;

import com.amadeodlp.canalradionov.core.auth.Role;
import com.amadeodlp.canalradionov.core.exceptions.UnauthorizedException;
import com.amadeodlp.canalradionov.core.model.LoginRequest;
import com.amadeodlp.canalradionov.core.model.LoginResponse;
import com.amadeodlp.canalradionov.core.model.SessionResponse;
import com.amadeodlp.canalradionov.core.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);

    @Value("${jwt.secret:defaultSecretThatShouldBeChanged}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400}")
    private int jwtExpiration;

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, String> activeTokens = new ConcurrentHashMap<>(); // Maps tokens to user IDs

    private final SessionService sessionService;

    public AuthService(SessionService sessionService) {
        this.sessionService = sessionService;
        // Add some sample users for testing
        initializeSampleUsers();
    }

    public LoginResponse login(LoginRequest request) throws UnauthorizedException {
        LOG.info("Attempting login for user: {}", request.username());

        // Find user by username
        User user = users.values().stream()
                .filter(u -> u.username().equals(request.username()))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        // Verify password (in a real system, use a proper password hashing library)
        if (!user.passwordHash().equals(request.password())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        // Generate JWT
        String token = generateToken(user);
        
        // Store token for validation
        activeTokens.put(token, user.id());
        
        // Validate token to get session
        SessionResponse session = sessionService.validateSession(token);



        // Update last login time (this is a simplified example)
        User updatedUser = new User(
                user.id(),
                user.username(),
                user.email(),
                user.passwordHash(),
                user.role(),
                user.createdAt(),
                LocalDateTime.now()
        );
        users.put(user.id(), updatedUser);

        return new LoginResponse(token, session);
    }

    public void logout(String token) {
        LOG.info("Logging out user with token");
        activeTokens.remove(token);
    }



    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration * 1000L);
        
        return Jwts.builder()
                .setSubject(user.id())
                .claim("username", user.username())
                .claim("role", user.role().name())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private void initializeSampleUsers() {
        User adminUser = new User(
                "user-1",
                "admin",
                "admin@canalradionov.com",
                "admin123", // In a real system, this would be hashed
                Role.ADMIN,
                LocalDateTime.now().minusDays(30),
                null
        );
        
        User regularUser = new User(
                "user-2",
                "user",
                "user@canalradionov.com",
                "user123", // In a real system, this would be hashed
                Role.USER,
                LocalDateTime.now().minusDays(15),
                null
        );
        
        User creatorUser = new User(
                "user-3",
                "creator",
                "creator@canalradionov.com",
                "creator123", // In a real system, this would be hashed
                Role.CREATOR,
                LocalDateTime.now().minusDays(7),
                null
        );
        
        users.put(adminUser.id(), adminUser);
        users.put(regularUser.id(), regularUser);
        users.put(creatorUser.id(), creatorUser);
    }
}
