package com.amadeodlp.canalradionov.core.services;

import com.amadeodlp.canalradionov.core.model.CognitoPoolConfig;
import com.amadeodlp.canalradionov.core.model.LoginResponse;
import com.amadeodlp.canalradionov.core.model.SessionResponse;
import com.amadeodlp.canalradionov.core.exceptions.UnauthorizedException;
import java.util.Objects;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.Map;

@Service
public class CognitoService {
	
	private final CognitoIdentityProviderClient cognitoClient;
	private final SessionService sessionService;
	private final Environment env;
	
	public CognitoService(Environment env, SessionService sessionService) {
		String region = Objects.requireNonNull(env.getProperty("AWS_REGION"), "AWS_REGION is not set");
		String accessKey = Objects.requireNonNull(env.getProperty("AWS_ACCESS_KEY_ID"), "AWS_ACCESS_KEY_ID is not set");
		String secretKey = Objects.requireNonNull(env.getProperty("AWS_SECRET_ACCESS_KEY"), "AWS_SECRET_ACCESS_KEY is not set");
		String sessionToken = Objects.requireNonNull(env.getProperty("AWS_SESSION_TOKEN"), "AWS_SESSION_TOKEN is not set");
		
		// Use AwsSessionCredentials if you need to include a session token
		AwsSessionCredentials sessionCredentials = AwsSessionCredentials.create(
			accessKey,
			secretKey,
			sessionToken
		);
		
		this.cognitoClient = CognitoIdentityProviderClient.builder()
			.region(Region.of(region))
			.credentialsProvider(StaticCredentialsProvider.create(sessionCredentials))
			.build();
		
		this.sessionService = sessionService;
		this.env = env;
	}
	
	private CognitoPoolConfig getCognitoConfigForOrg(String orgId) {
		try {
			return new CognitoPoolConfig(
				env.getProperty(orgId + "_USER_POOL_ID"),
				env.getProperty(orgId + "_CLIENT_ID")
			);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid orgId");
		}
	}
	
	public AdminCreateUserResponse adminCreateUser(String orgId, String username, String email, String temporaryPassword) {
		CognitoPoolConfig config = getCognitoConfigForOrg(orgId);
		
		AdminCreateUserRequest createUserRequest = AdminCreateUserRequest.builder()
			.userPoolId(config.userPoolId())
			.username(username)
			.userAttributes(
				AttributeType.builder().name("email").value(email).build()
			)
			.temporaryPassword(temporaryPassword)
			.messageAction(MessageActionType.SUPPRESS)
			.build();
		
		return cognitoClient.adminCreateUser(createUserRequest);
	}
	
	public LoginResponse login(String orgId, String email, String password) throws UnauthorizedException {
		CognitoPoolConfig config = getCognitoConfigForOrg(orgId);
		
		AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
			.userPoolId(config.userPoolId())
			.clientId(config.clientId())
			.authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
			.authParameters(Map.of(
				"USERNAME", email,
				"PASSWORD", password
			))
			.build();
		AdminInitiateAuthResponse authResponse;
		try {
			authResponse = cognitoClient.adminInitiateAuth(authRequest);
		} catch (Exception e) {
			throw new UnauthorizedException("Invalid credentials");
		}
		
		String accessToken = authResponse.authenticationResult().accessToken();
		SessionResponse session = sessionService.validateSession(accessToken, orgId);
		return new LoginResponse(accessToken, session);
	}
	
	public void logout(String accessToken) {
		GlobalSignOutRequest signOutRequest = GlobalSignOutRequest.builder()
			.accessToken(accessToken)
			.build();
		
		cognitoClient.globalSignOut(signOutRequest);
	}
	
	public void forgotPassword(String orgId, String username) {
		CognitoPoolConfig config = getCognitoConfigForOrg(orgId);
		
		ForgotPasswordRequest request = ForgotPasswordRequest.builder()
			.clientId(config.clientId())
			.username(username)
			.build();
		
		cognitoClient.forgotPassword(request);
	}
	
	public void confirmForgotPassword(String orgId, String username, String confirmationCode, String newPassword) {
		CognitoPoolConfig config = getCognitoConfigForOrg(orgId);
		
		ConfirmForgotPasswordRequest confirmRequest = ConfirmForgotPasswordRequest.builder()
			.clientId(config.clientId())
			.username(username)
			.confirmationCode(confirmationCode)
			.password(newPassword)
			.build();
		
		cognitoClient.confirmForgotPassword(confirmRequest);
	}
}