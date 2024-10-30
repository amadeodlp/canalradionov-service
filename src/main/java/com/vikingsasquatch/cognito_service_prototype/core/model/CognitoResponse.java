package com.vikingsasquatch.cognito_service_prototype.core.model;

public record CognitoResponse(
	String accessToken,
	String idToken,
	String refreshToken,
	long expiresIn
) {
}
