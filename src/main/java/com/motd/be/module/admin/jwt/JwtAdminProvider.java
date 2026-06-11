package com.motd.be.module.admin.jwt;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.constants.TimePolicy.*;

import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.jwt.Jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtAdminProvider {

	private static byte[] secret;
	private static Key adminTokenKey;
	@Value("${jwt.accessToken.secret-key}")
	private String adminTokenSignature;

	public static Jwt createAdminTokens(Map<String, Object> claims) {
		String accessToken = createAdminAccessToken(claims);
		return new Jwt(accessToken, null);
	}

	private static String createAdminAccessToken(Map<String, Object> claims) {
		long currentTimeMillis = System.currentTimeMillis();

		Map<String, Object> modifiableClaims = new HashMap<>(claims);
		modifiableClaims.put(CREATED_MILLIS, currentTimeMillis);

		Map<String, Object> immutableClaims = Collections.unmodifiableMap(modifiableClaims);

		return Jwts.builder()
			.setClaims(immutableClaims)
			.setExpiration(getExpireDateAdminAccessToken())
			.signWith(adminTokenKey)
			.compact();
	}

	public static Claims getClaimsFromAdminToken(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(adminTokenKey)
			.build()
			.parseClaimsJws(token)
			.getBody();
	}

	private static Date getExpireDateAdminAccessToken() {
		return Date.from(Instant.now().plus(Duration.ofSeconds(ACCESS_TOKEN_EXPIRE_SECOND)));
	}

	@PostConstruct
	public void setSecretKey() {
		secret = adminTokenSignature.getBytes();
		adminTokenKey = Keys.hmacShaKeyFor(secret);
	}
}
