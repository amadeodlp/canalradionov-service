package com.vikingsasquatch.cognito_service_prototype.app.web.controller;

import com.vikingsasquatch.cognito_service_prototype.core.exceptions.UnauthorizedException;
import com.vikingsasquatch.cognito_service_prototype.core.model.LoginRequest;
import com.vikingsasquatch.cognito_service_prototype.core.model.LoginResponse;
import com.vikingsasquatch.cognito_service_prototype.core.model.SessionResponse;
import com.vikingsasquatch.cognito_service_prototype.core.services.CognitoService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
	public static final Logger LOG = LoggerFactory.getLogger(SessionController.class);
	private final CognitoService cognitoService;
	
	public AuthController(CognitoService cognitoService) {this.cognitoService = cognitoService;}
	
	@PostMapping("/login")
	public ResponseEntity<SessionResponse> login(@RequestBody LoginRequest req, HttpServletResponse response) {
		LOG.info("Logging in user");
		try {
			LoginResponse loginResponse = cognitoService.login(req.orgId(), req.username(), req.password());
			Cookie jwtCookie = new Cookie("jwt-token", loginResponse.accessToken());
			jwtCookie.setPath("/");
			jwtCookie.setMaxAge(60 * 60);
			response.addCookie(jwtCookie);
			return ResponseEntity.ok(loginResponse.session());
		} catch (UnauthorizedException e) {
			return ResponseEntity.status(401).body(null);
		}
	}
	
	@PostMapping("/logout")
	public ResponseEntity<String> logout(@RequestHeader("Authorization") String token, HttpServletResponse response) {
		LOG.info("Logging out user");
		Cookie jwtCookie = new Cookie("jwt-token", "");
		jwtCookie.setPath("/");
		jwtCookie.setMaxAge(0);
		response.addCookie(jwtCookie);
		cognitoService.logout(token);
		return ResponseEntity.ok().body("Logged out");
	}
	
	@PostMapping("/register")
	public ResponseEntity<Void> register(@RequestBody String orgId, @RequestBody String username, @RequestBody String email, @RequestBody String password) {
		LOG.info("Registering user");
		cognitoService.adminCreateUser(orgId, username, email, password);
		return ResponseEntity.ok().build();
	}
	
	@PostMapping("/forgotPassword")
	public ResponseEntity<Void> forgotPassword(@RequestBody String orgId, @RequestBody String username) {
		LOG.info("Forgot password for user");
		cognitoService.forgotPassword(orgId, username);
		return ResponseEntity.ok().build();
	}
	
	@PostMapping("/resetPassword")
	public ResponseEntity<Void> confirmForgotPassword(
		@RequestBody String orgId,
		@RequestBody String username,
		@RequestBody String newPassword,
		@RequestBody String confirmationCode
	) {
		LOG.info("Resetting password for user");
		cognitoService.confirmForgotPassword(orgId, username, newPassword, confirmationCode);
		return ResponseEntity.ok().build();
	}
}