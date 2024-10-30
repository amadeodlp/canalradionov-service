package com.amadeodlp.canalradionov.core.services;

import com.amadeodlp.canalradionov.core.exceptions.UnauthorizedException;
import com.amadeodlp.canalradionov.core.model.CachedScopes;
import com.amadeodlp.canalradionov.core.model.SessionResponse;
import com.amadeodlp.canalradionov.core.utils.JwkUtil;
import com.vikingsasquatch.common.auth.scope.ScopeOperation;
import com.vikingsasquatch.common.auth.scope.UserScope;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.Base64;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.UUID;

@Service
public class SessionService {
	
	private final ConcurrentHashMap<String, CachedScopes> scopeCache = new ConcurrentHashMap<>();
	public static final long SCOPE_CACHE_EXPIRATION_TIME = 30 * 60 * 1000;
	
	public SessionResponse validateSession(String token, String orgId) throws UnauthorizedException {
		String header = new String(Base64.getUrlDecoder().decode(token.split("\\.")[0]));
		String kid = extractKid(header);
		PublicKey publicKey;
		try {
			publicKey = JwkUtil.getPublicKey(kid);
		} catch (Exception e) {
			throw new UnauthorizedException("Unable to get Public Key from token");
		}
		Claims claims = Jwts.parserBuilder()
			.setSigningKey(publicKey)
			.build()
			.parseClaimsJws(token)
			.getBody();
		Set<UserScope> scopes = getUserScopes(claims);
		return new SessionResponse(
			UUID.randomUUID(),
			claims.getSubject(),
			orgId,
			scopes
		);
	}
	
	public Set<UserScope> getUserScopes(Claims claims) {
		String userId = claims.getSubject();
		CachedScopes cachedScopes = scopeCache.get(userId);
		
		if (cachedScopes != null && !isCacheExpired(cachedScopes)) {
			return cachedScopes.scopes();
		}
		
		Set<String> groups = new HashSet<>();
		
		Object cognitoGroupsClaim = claims.get("cognito:groups");
		
		if (cognitoGroupsClaim instanceof List<?> cognitoGroups) {
			for (Object group : cognitoGroups) {
				if (group instanceof String) {
					groups.add((String) group);
				}
			}
		}
		Set<UserScope> userScopes = createUserScopeSet(groups.contains("admins"));
		scopeCache.put(userId, new CachedScopes(userScopes, System.currentTimeMillis()));
		return userScopes;
	}
	
	private Set<UserScope> createUserScopeSet(boolean isAdmin) {
		Set<UserScope> userScopes = EnumSet.allOf(UserScope.class);
		if (!isAdmin) {
			userScopes.removeIf(scope -> scope.getOperation() == ScopeOperation.ADMIN);
		}
		return userScopes;
	}
	
	private boolean isCacheExpired(CachedScopes scopes) {
		return System.currentTimeMillis() - scopes.timestamp() > SCOPE_CACHE_EXPIRATION_TIME;
	}
	
	private String extractKid(String header) {
		String kid = header.split("\"kid\":\"")[1].split("\"")[0];
		return kid.replace("\\/", "/");
	}
}