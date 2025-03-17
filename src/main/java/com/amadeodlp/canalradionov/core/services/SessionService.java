package com.amadeodlp.canalradionov.core.services;

import com.amadeodlp.canalradionov.core.auth.Role;
import com.amadeodlp.canalradionov.core.exceptions.UnauthorizedException;
import com.amadeodlp.canalradionov.core.model.SessionResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Service to manage user sessions.
 * Simplified version that works with JWT tokens.
 */
@Service
public class SessionService {
    private static final Logger LOG = LoggerFactory.getLogger(SessionService.class);
    
    @Value("${jwt.secret:defaultSecretThatShouldBeChanged}")
    private String jwtSecret;
    
    /**
     * Validates a session token and returns session information.
     * 
     * @param token The JWT token to validate
     * @return SessionResponse with user information
     * @throws UnauthorizedException if the token is invalid
     */
    public SessionResponse validateSession(String token) throws UnauthorizedException {
        LOG.info("Validating session token");
        try {
            Claims claims = parseToken(token);
            String userId = claims.getSubject();
            String username = claims.get("username", String.class);
            String roleStr = claims.get("role", String.class);
            Role role = Role.valueOf(roleStr);
            
            return new SessionResponse(
                UUID.randomUUID(),
                userId,
                username,
                role
            );
        } catch (Exception e) {
            LOG.error("Error validating token", e);
            throw new UnauthorizedException("Invalid session token");
        }
    }
    
    /**
     * Parse and validate JWT token
     */
    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
    
    /**
     * Get signing key for JWT
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
