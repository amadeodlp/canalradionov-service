package com.vikingsasquatch.cognito_service_prototype.core.model;

public record SignupRequest(
	String email,
	String password,
	String firstName,
	String lastName,
	String phoneNumber,
	String zoneinfo,
	String accountType,
	String userType,
	String orgId
) {
}
