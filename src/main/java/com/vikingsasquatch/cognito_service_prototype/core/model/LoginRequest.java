package com.vikingsasquatch.cognito_service_prototype.core.model;

public record LoginRequest(
	String orgId,
	String username,
	String password
) {
}
