package com.amadeodlp.canalradionov.app.web.controller;

import com.amadeodlp.canalradionov.core.exceptions.UnauthorizedException;
import com.amadeodlp.canalradionov.core.model.LoginRequest;
import com.amadeodlp.canalradionov.core.model.LoginResponse;
import com.amadeodlp.canalradionov.core.model.SessionResponse;
import com.amadeodlp.canalradionov.core.services.AuthService;
import com.amadeodlp.canalradionov.core.services.SessionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {
    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private final SessionService sessionService;
    
    public AuthController(AuthService authService, SessionService sessionService) {
        this.authService = authService;
        this.sessionService = sessionService;
    }
    
    @PostMapping("/login")
    public ResponseEntity<SessionResponse> login(@RequestBody LoginRequest req, HttpServletResponse response) {
        LOG.info("Login attempt for user: {}", req.username());
        try {
            LoginResponse loginResponse = authService.login(req);
            
            // Set JWT as a cookie
            Cookie jwtCookie = new Cookie("jwt-token", loginResponse.token());
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(60 * 60); // 1 hour
            jwtCookie.setHttpOnly(true);  // For security - not accessible by JavaScript
            response.addCookie(jwtCookie);
            
            return ResponseEntity.ok(loginResponse.session());
        } catch (UnauthorizedException e) {
            LOG.warn("Login failed for user: {}", req.username());
            return ResponseEntity.status(401).body(null);
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token, HttpServletResponse response) {
        LOG.info("Logging out user");
        
        // Clear the JWT cookie
        Cookie jwtCookie = new Cookie("jwt-token", "");
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        jwtCookie.setHttpOnly(true);
        response.addCookie(jwtCookie);
        
        // Invalidate token on the server
        String cleanToken = token.replace("Bearer ", "");
        authService.logout(cleanToken);
        
        return ResponseEntity.ok("Logged out successfully");
    }
    
    @PostMapping("/session")
    public ResponseEntity<SessionResponse> validateSession(
            @RequestHeader("Authorization") String authHeader) {
        LOG.info("Validating session");
        
        try {
            String token = authHeader.replace("Bearer ", "");
            SessionResponse session = sessionService.validateSession(token);
            return ResponseEntity.ok(session);
        } catch (UnauthorizedException e) {
            LOG.warn("Session validation failed");
            return ResponseEntity.status(401).body(null);
        }
    }
}
