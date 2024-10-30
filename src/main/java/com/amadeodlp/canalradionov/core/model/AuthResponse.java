package com.amadeodlp.canalradionov.core.model;

public record AuthResponse(
	User user,
	String jwt
	) {
}
