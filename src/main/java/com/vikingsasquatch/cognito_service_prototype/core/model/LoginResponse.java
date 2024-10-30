package com.vikingsasquatch.cognito_service_prototype.core.model;

public record LoginResponse(
	String accessToken,
	SessionResponse session
) {
}
