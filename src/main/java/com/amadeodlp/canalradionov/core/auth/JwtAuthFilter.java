package com.amadeodlp.canalradionov.core.auth;

import com.amadeodlp.canalradionov.core.exceptions.UnauthorizedException;
import com.amadeodlp.canalradionov.core.model.SessionResponse;
import com.amadeodlp.canalradionov.core.services.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(JwtAuthFilter.class);
    
    private final SessionService sessionService;
    
    public JwtAuthFilter(SessionService sessionService) {
        this.sessionService = sessionService;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            Optional<String> token = getTokenFromRequest(request);
            
            if (token.isPresent()) {
                try {
                    SessionResponse session = sessionService.validateSession(token.get());
                    
                    // Create authentication token with appropriate role
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            session.username(),
                            null,
                            Collections.singleton(new SimpleGrantedAuthority("ROLE_" + session.role().name()))
                    );
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (UnauthorizedException e) {
                    LOG.warn("Invalid JWT token", e);
                    // Let the request continue without authentication
                }
            }
        } catch (Exception e) {
            LOG.error("Error processing JWT authentication", e);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private Optional<String> getTokenFromRequest(HttpServletRequest request) {
        // First try to get from Authorization header
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return Optional.of(bearerToken.substring(7));
        }
        
        // Then try to get from cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt-token".equals(cookie.getName())) {
                    return Optional.of(cookie.getValue());
                }
            }
        }
        
        return Optional.empty();
    }
}
