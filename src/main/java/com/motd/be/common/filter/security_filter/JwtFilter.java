package com.motd.be.common.filter.security_filter;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.filter.util.JwtTokenUtils.*;
import static com.motd.be.module.member.jwt.JwtProvider.*;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.motd.be.common.filter.endpoints.ReissueWithoutCookieUrl;
import com.motd.be.common.utils.CookieUtils;
import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.JwtException;
import com.motd.be.module.member.auth.ClientType;
import com.motd.be.module.member.auth.dto.response.AuthReissueResponse;
import com.motd.be.module.member.auth.facade.AuthFacade;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private final AuthFacade authFacade;
	private final CookieUtils cookieUtils;

	/**
	 * 이 메서드에서 결국 Jwt 토큰의 유효성을 검사하고, 유효한 경우 SecurityContext에 인증 정보를 설정합니다.
	 * 들어올수있는 토큰의 case 는 2가지입니다.
	 * 1. header 에 accessToken 이 있는경우
	 * -> 이때는 refreshToken 을 통해 reissue 를 해야함으로, 401 에러를 반환합니다.
	 * <p>
	 * 2. cookcie 에 accessToken 이 있는경우
	 * -> 이때는 refreshToken 도 같이 쿠키에 담겨있으므로, accessToken 이 만료된경우 refreshToken 으로 재발급을 진행합니다.
	 *
	 * @param request
	 * @param response
	 * @param filterChain
	 * @throws IOException
	 * @throws ServletException
	 */
	@Override
	public void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain) throws IOException, ServletException {
		// accessToken 추출
		String token = extractAccessToken(request);

		HttpServletRequest toUse = request;
		if (token != null) {
			toUse = processJwtAuthentication(token, request, response);
		}
		filterChain.doFilter(toUse, response);
	}

	private HttpServletRequest processJwtAuthentication(String token, HttpServletRequest request,
		HttpServletResponse response) {
		try {
			Claims claims = getClaimsFromAccessToken(token);
			setAuthentication(claims);

		} catch (RuntimeException e) {
			CustomRuntimeException exception = new CustomRuntimeException(JwtException.from(e));

			if (exception.getCustomException().equals(JwtException.EXPIRED_JWT)) {
				// 헤더에서 온 토큰이라면 → refresh 불가 → 401 리턴
				if (isHeaderToken(request)) {
					throw exception; // 그대로 401
				}

				// 쿠키에서 온 토큰이라면 → refresh 시도
				try {
					return handleTokenInCookie(request, response);
				} catch (CustomRuntimeException ex) {
					// refresh 실패 → 쿠키 전부 삭제
					clearAuthCookiesByPath(request, response);
					throw ex;
				}

			} else {
				// 만료 외 다른 JWT 예외 → 쿠키 전부 삭제
				clearAuthCookiesByPath(request, response);
				throw exception;
			}
		}
		return request;
	}

	private void clearAuthCookiesByPath(HttpServletRequest request, HttpServletResponse response) {
		if (isAdminPath(request)) {
			cookieUtils.clearAdminAuthCookies(response);
		} else {
			cookieUtils.clearAuthCookies(response);
		}
	}

	private boolean isAdminPath(HttpServletRequest request) {
		String uri = request.getRequestURI();
		return uri != null && uri.startsWith("/api/admin");
	}

	private HttpServletRequestWrapper handleTokenInCookie(HttpServletRequest request,
		HttpServletResponse response) {
		String refreshToken = extractRefreshTokenFromCookie(request);

		if (refreshToken == null) {
			throw new CustomRuntimeException(JwtException.REFRESH_TOKEN_NOT_FOUND_OR_NOT_VALID);
		}

		AuthReissueResponse reissued = authFacade.reissueToken(refreshToken, ClientType.WEB, null);

		// reissued 를 하더라도 response header 에 set-cookie 를 추가해주지 않아도 되는 경우를 필터링 해야한다.
		if (!isReissueWithoutCookieUrl(request)) {
			cookieUtils.addAuthCookies(response, reissued.getAccessToken(), reissued.getRefreshToken());
		}

		setAuthentication(getClaimsFromAccessToken(reissued.getAccessToken()));

		// 새로 발급된 accessToken 을 담은 requestWrapper 반환
		return new HttpServletRequestWrapper(request) {
			@Override
			public Cookie[] getCookies() {
				return cookieUtils.toServletCookies(
					reissued.getAccessToken(),
					reissued.getRefreshToken()
				);
			}
		};
	}

	public boolean isReissueWithoutCookieUrl(HttpServletRequest httpRequest) {
		String url = httpRequest.getRequestURI();
		String method = httpRequest.getMethod();

		for (ReissueWithoutCookieUrl uri : ReissueWithoutCookieUrl.values()) {
			if (uri.isReissueWithoutCookieUrl(url, method)) {
				return true;
			}
		}
		return false;
	}

	private void setAuthentication(Claims claims) {
		Object id = claims.get(ID);
		Object role = claims.get(ROLE);

		if (id == null || role == null) {
			throw new CustomRuntimeException(JwtException.MALFORMED_JWT);
		}

		Authentication auth = new CustomAuthentication(String.valueOf(id), role.toString());
		auth.setAuthenticated(true);
		SecurityContextHolder.getContext().setAuthentication(auth);
	}
}
