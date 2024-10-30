package com.amadeodlp.canalradionov.core.model;

public record LoginResponse(
	String accessToken,
	SessionResponse session
) {
}
