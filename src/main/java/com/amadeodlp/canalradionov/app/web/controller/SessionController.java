package com.amadeodlp.canalradionov.app.web.controller;

import com.amadeodlp.canalradionov.core.model.SessionResponse;
import com.amadeodlp.canalradionov.core.exceptions.UnauthorizedException;
import com.amadeodlp.canalradionov.core.services.SessionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SessionController {
	public static final Logger LOG = LoggerFactory.getLogger(SessionController.class);
	private final SessionService sessionService;
	
	public SessionController(SessionService sessionService) {this.sessionService = sessionService;}
	
	@PostMapping("/session")
	public ResponseEntity<SessionResponse> validateSession(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @RequestBody String orgId, HttpServletResponse response) {
		LOG.info("Validating session token");
		String token = authorizationHeader.replace("Bearer ", "");
		try {
			SessionResponse sessionResponse = sessionService.validateSession(token, orgId);
			Cookie jwtCookie = new Cookie("jwt-token", token);
			jwtCookie.setPath("/");
			jwtCookie.setMaxAge(60 * 60);
			response.addCookie(jwtCookie);
			return ResponseEntity.ok(sessionResponse);
		} catch (UnauthorizedException e) {
			return ResponseEntity.status(401).body(null);
		}
	}
}