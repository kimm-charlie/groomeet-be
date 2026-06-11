package com.motd.be.module.member.jwt;

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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtProvider {

	private static byte[] secret;
	private static Key accessTokenKey;
	private static Key refreshTokenKey;
	@Value("${jwt.accessToken.secret-key}")
	private String accessTokenSignature;
	@Value("${jwt.refreshToken.secret-key}")
	private String refreshTokenSignature;

	private static String createAccessToken(Map<String, Object> claims, Date expireDate) {
		long currentTimeMillis = System.currentTimeMillis();

		Map<String, Object> modifiableClaims = new HashMap<>(claims);
		modifiableClaims.put(CREATED_MILLIS, currentTimeMillis);

		Map<String, Object> immutableClaims = Collections.unmodifiableMap(modifiableClaims);

		return Jwts.builder()
			.setClaims(immutableClaims)
			.setExpiration(expireDate)
			.signWith(accessTokenKey)
			.compact();
	}

	private static String createRefreshToken(Map<String, Object> claims, Date expireDate) {
		long currentTimeMillis = System.currentTimeMillis();

		Map<String, Object> modifiableClaims = new HashMap<>(claims);
		modifiableClaims.put(CREATED_MILLIS, currentTimeMillis);

		Map<String, Object> immutableClaims = Collections.unmodifiableMap(modifiableClaims);

		return Jwts.builder()
			.setClaims(immutableClaims)
			.setExpiration(expireDate)
			.signWith(refreshTokenKey)
			.compact();
	}

	public static Claims getClaimsFromAccessToken(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(accessTokenKey)
			.build()
			.parseClaimsJws(token)
			.getBody();
	}

	public static Claims getClaimsFromExpiredAccessToken(String token) {
		try {
			return Jwts.parser()
				.setSigningKey(accessTokenKey)
				.parseClaimsJws(token)
				.getBody();
		} catch (ExpiredJwtException e) {
			return e.getClaims(); // 만료된 토큰의 클레임을 반환
		}
	}

	public static Claims getClaimsFromRefreshToken(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(refreshTokenKey)
			.build()
			.parseClaimsJws(token)
			.getBody();
	}

	public static Jwt createTokens(Map<String, Object> claims) {
		String accessToken = createAccessToken(claims, getExpireDateAccessToken());
		String refreshToken = createRefreshToken(claims, getExpireDateRefreshToken());
		return new Jwt(accessToken, refreshToken);
	}

	public static Jwt createExpiredTokens(Map<String, Object> claims) {
		String accessToken = createAccessToken(claims, getExpiredDateAccessToken());
		String refreshToken = createRefreshToken(claims, getExpiredDateRefreshToken());
		return new Jwt(accessToken, refreshToken);
	}

	private static Date getExpiredDateAccessToken() {
		return Date.from(Instant.now().minus(Duration.ofSeconds(ACCESS_TOKEN_EXPIRE_SECOND)));
	}

	private static Date getExpiredDateRefreshToken() {
		return Date.from(Instant.now().minus(Duration.ofSeconds(REFRESH_TOKEN_EXPIRE_SECOND)));
	}

	private static Date getExpireDateAccessToken() {
		return Date.from(Instant.now().plus(Duration.ofSeconds(ACCESS_TOKEN_EXPIRE_SECOND)));
	}

	private static Date getExpireDateRefreshToken() {
		return Date.from(Instant.now().plus(Duration.ofSeconds(REFRESH_TOKEN_EXPIRE_SECOND)));
	}

	@PostConstruct
	public void setSecretKey() {
		secret = accessTokenSignature.getBytes();
		accessTokenKey = Keys.hmacShaKeyFor(secret);

		secret = refreshTokenSignature.getBytes();
		refreshTokenKey = Keys.hmacShaKeyFor(secret);
	}

}
