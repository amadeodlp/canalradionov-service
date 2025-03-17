package com.amadeodlp.canalradionov.core.model;

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
