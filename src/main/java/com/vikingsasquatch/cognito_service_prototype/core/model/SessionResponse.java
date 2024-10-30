package com.vikingsasquatch.cognito_service_prototype.core.model;

import com.vikingsasquatch.common.auth.scope.UserScope;
import java.util.Set;
import java.util.UUID;

public record SessionResponse(
	UUID sessionId,
	String userId,
	String orgId,
	Set<UserScope> scopes
) {}
