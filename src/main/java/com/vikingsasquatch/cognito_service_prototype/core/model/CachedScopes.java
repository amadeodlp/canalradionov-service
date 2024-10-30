package com.vikingsasquatch.cognito_service_prototype.core.model;

import com.vikingsasquatch.common.auth.scope.UserScope;
import java.util.Set;

public record CachedScopes(Set<UserScope> scopes, long timestamp) {
}
