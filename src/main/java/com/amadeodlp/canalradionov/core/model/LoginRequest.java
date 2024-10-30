package com.amadeodlp.canalradionov.core.model;

public record LoginRequest(
	String orgId,
	String username,
	String password
) {
}
