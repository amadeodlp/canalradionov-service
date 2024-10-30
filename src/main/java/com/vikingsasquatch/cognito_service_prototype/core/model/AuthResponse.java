package com.vikingsasquatch.cognito_service_prototype.core.model;

import io.jsonwebtoken.Jwt;

public record AuthResponse(
	User user,
	String jwt
	) {
}
