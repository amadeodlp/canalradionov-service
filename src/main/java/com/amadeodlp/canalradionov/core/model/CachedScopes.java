package com.amadeodlp.canalradionov.core.model;

import com.vikingsasquatch.common.auth.scope.UserScope;
import java.util.Set;

public record CachedScopes(Set<UserScope> scopes, long timestamp) {
}
