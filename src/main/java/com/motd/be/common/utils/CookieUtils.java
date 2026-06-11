package com.motd.be.common.utils;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.constants.TimePolicy.*;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CookieUtils {

	private final ActiveProfileUtils activeProfileUtils;

	/**
	 * Controller 단: ResponseEntity 에 삭제 쿠키 헤더 추가
	 */
	public HttpHeaders createDeleteAuthCookiesHeaders() {
		HttpHeaders headers = new HttpHeaders();
		deleteTokenCookies().forEach(c -> headers.add(HttpHeaders.SET_COOKIE, c.toString()));
		return headers;
	}

	/**
	 * Controller 단: ResponseEntity 에 발급 쿠키 헤더 추가
	 */
	public HttpHeaders createAuthCookiesHeaders(String accessToken, String refreshToken) {
		HttpHeaders headers = new HttpHeaders();
		createAuthCookies(accessToken, refreshToken).forEach(c -> headers.add(HttpHeaders.SET_COOKIE, c.toString()));
		return headers;
	}

	/**
	 * Filter 단: HttpServletResponse 에 삭제 쿠키 직접 세팅
	 */
	public void clearAuthCookies(HttpServletResponse response) {
		deleteTokenCookies().forEach(c -> response.addHeader(HttpHeaders.SET_COOKIE, c.toString()));
	}

	/**
	 * Filter 단: HttpServletResponse 에 발급 쿠키 직접 세팅
	 */
	public void addAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
		createAuthCookies(accessToken, refreshToken).forEach(
			c -> response.addHeader(HttpHeaders.SET_COOKIE, c.toString()));
	}

	/**
	 * Filter 단: HttpServletResponse 에 발급 쿠키 직접 세팅 (Servlet Cookie 버전)
	 */
	public Cookie[] toServletCookies(String accessToken, String refreshToken) {
		return new Cookie[] {
			toServletCookie(createCookie(ACCESS_TOKEN, accessToken, COOKIE_JWT_TOKEN_EXPIRE_SECOND, "/")),
			toServletCookie(createCookie(REFRESH_TOKEN, refreshToken, COOKIE_JWT_TOKEN_EXPIRE_SECOND, "/"))
		};
	}

	public HttpHeaders createAccessTokenCookieHeadersForAdmin(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(
			HttpHeaders.SET_COOKIE,
			createCookie(ACCESS_TOKEN, accessToken, COOKIE_JWT_TOKEN_EXPIRE_SECOND_FOR_ADMIN, "/api/admin").toString()
		);
		return headers;
	}

	/**
	 * Admin 전용: /api/admin path로 쿠키 삭제
	 */
	public void clearAdminAuthCookies(HttpServletResponse response) {
		deleteAdminTokenCookies().forEach(c -> response.addHeader(HttpHeaders.SET_COOKIE, c.toString()));
	}

	/**
	 * Admin 전용: /api/admin path로 쿠키 삭제 헤더 생성
	 */
	public HttpHeaders createDeleteAdminAuthCookiesHeaders() {
		HttpHeaders headers = new HttpHeaders();
		deleteAdminTokenCookies().forEach(c -> headers.add(HttpHeaders.SET_COOKIE, c.toString()));
		return headers;
	}

	// 내부 로직 --------------------------------------------

	private Cookie toServletCookie(ResponseCookie responseCookie) {
		Cookie cookie = new Cookie(responseCookie.getName(), responseCookie.getValue());
		cookie.setPath(responseCookie.getPath());
		cookie.setHttpOnly(responseCookie.isHttpOnly());
		cookie.setSecure(responseCookie.isSecure());
		return cookie;
	}

	private List<ResponseCookie> deleteTokenCookies() {
		return Arrays.asList(
			createCookie(ACCESS_TOKEN, "", 0, "/"),
			createCookie(REFRESH_TOKEN, "", 0, "/")
		);
	}

	private List<ResponseCookie> deleteAdminTokenCookies() {
		return Arrays.asList(
			createCookie(ACCESS_TOKEN, "", 0, "/api/admin")
		);
	}

	private List<ResponseCookie> createAuthCookies(String accessToken, String refreshToken) {
		return Arrays.asList(
			createCookie(ACCESS_TOKEN, accessToken, COOKIE_JWT_TOKEN_EXPIRE_SECOND, "/"),
			createCookie(REFRESH_TOKEN, refreshToken, COOKIE_JWT_TOKEN_EXPIRE_SECOND, "/")
		);
	}

	private ResponseCookie createCookie(String name, String value, long maxAge, String path) {
		boolean isDev = activeProfileUtils.isDevProfileActive();

		ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
			.httpOnly(!isDev)
			.secure(true)
			.path(path)
			.maxAge(maxAge)
			.sameSite(isDev ? "None" : "Lax");

		return builder.build();
	}
}
