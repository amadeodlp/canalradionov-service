package com.amadeodlp.canalradionov.core.model;

public record CognitoResponse(
	String accessToken,
	String idToken,
	String refreshToken,
	long expiresIn
) {
}
