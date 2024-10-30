package com.vikingsasquatch.cognito_service_prototype.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import java.security.spec.RSAPublicKeySpec;
import org.springframework.stereotype.Component;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
public class JwkUtil {
	
	private static final String JWK_URL = System.getenv("JWK_URL");
	
	public static PublicKey getPublicKey(String kid) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> jwks = objectMapper.readValue(new URL(JWK_URL), Map.class);
		List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");
		
		for (Map<String, Object> key : keys) {
			if (kid.equals(key.get("kid"))) {
				String n = (String) key.get("n");
				String e = (String) key.get("e");
				return generatePublicKey(n, e);
			}
		}
		throw new IllegalArgumentException("Public key not found for kid: " + kid);
	}
	
	private static PublicKey generatePublicKey(String n, String e) throws Exception {
		byte[] modulusBytes = Base64.getUrlDecoder().decode(n);
		byte[] exponentBytes = Base64.getUrlDecoder().decode(e);
		BigInteger modulus = new BigInteger(1, modulusBytes);
		BigInteger exponent = new BigInteger(1, exponentBytes);
		RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, exponent);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePublic(publicKeySpec);
	}
}
