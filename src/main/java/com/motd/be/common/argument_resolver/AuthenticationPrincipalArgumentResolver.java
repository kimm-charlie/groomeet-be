package com.motd.be.common.argument_resolver;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthenticationPrincipalArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(AuthenticationPrincipal.class) &&
			parameter.getParameterType().equals(Long.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		// 인증 정보 자체가 없는 경우 → 그냥 null 반환
		if (authentication == null) {
			return null;
		}

		Object principal = authentication.getPrincipal();

		// anonymousUser → 비로그인 취급
		if (principal == null) {
			return null;
		}

		// principal이 String → Long 변환
		if (principal instanceof String) {
			try {
				return Long.valueOf((String)principal);
			} catch (NumberFormatException e) {
				// 이상한 값이면 그냥 null (보안 처리는 PreAuthorize 쪽에 맡김)
				return null;
			}
		}

		return null;
	}

}
