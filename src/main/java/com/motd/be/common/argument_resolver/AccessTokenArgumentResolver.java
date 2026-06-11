package com.motd.be.common.argument_resolver;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AccessTokenArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(AccessToken.class) && parameter.getParameterType().equals(String.class);
	}

	@Override
	public String resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

		HttpServletRequest request = (HttpServletRequest)webRequest.getNativeRequest();

		// 1. Authorization 헤더 우선
		String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);
		if (authorizationHeader != null && !authorizationHeader.isBlank()) {
			return extractAccessTokenWithoutPrefix(authorizationHeader);
		}

		// 2. 쿠키에서 accessToken 추출
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (ACCESS_TOKEN.equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}

		return null; // 토큰이 없는 경우
	}

	private String extractAccessTokenWithoutPrefix(String authorizationHeader) {
		return authorizationHeader.substring(7);
	}

}
