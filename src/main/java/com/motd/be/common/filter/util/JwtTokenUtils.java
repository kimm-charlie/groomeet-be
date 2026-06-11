package com.motd.be.common.filter.util;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.module.member.jwt.JwtProvider.*;

import com.google.common.net.HttpHeaders;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class JwtTokenUtils {

	public static String extractAccessToken(HttpServletRequest request) {
		// Authorization 헤더
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith(TOKEN_PREFIX)) {
			return header.substring(7);
		}

		// 쿠키
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (ACCESS_TOKEN.equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}

		return null;
	}

	/**
	 * RuntimeException 을 잡는 이유는 만료된 jwt 토큰을 가져왔을때도 log를 남기기 위해서 이다.
	 *
	 * @param cachedRequest
	 * @return
	 */
	public static String extractMemberIdFromAccessTokenWithCatchJwtException(HttpServletRequest cachedRequest) {
		String accessToken = extractAccessToken(cachedRequest);

		if (accessToken == null) {
			return null;
		}

		try {
			Claims claims = getClaimsFromAccessToken(accessToken);
			return String.valueOf(claims.get(ID));
		} catch (RuntimeException e) {
			return accessToken;
		}
	}

	public static boolean isJwtTokenExist(HttpServletRequest request) {
		// 1. Authorization 헤더
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith(TOKEN_PREFIX)) {
			return true;
		}

		// 2. 쿠키
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (ACCESS_TOKEN.equals(cookie.getName())) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean isHeaderToken(HttpServletRequest request) {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		return header != null && header.startsWith(TOKEN_PREFIX);
	}

	public static String extractRefreshTokenFromCookie(HttpServletRequest request) {
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (REFRESH_TOKEN.equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

}
