package com.motd.be.common.filter.endpoints;

import java.util.regex.Pattern;

import org.springframework.http.HttpMethod;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * reissued 를 하더라도 response header 에 set-cookie 를 추가해주지 않아도 되는 URL 들 이다.
 */
@RequiredArgsConstructor
@Getter
public enum ReissueWithoutCookieUrl {

	SING_OUT("^/api/members/signOut$", HttpMethod.POST),
	WITHDRAWAL("^/api/members/withdrawal$", HttpMethod.POST);

	private final String pattern;
	private final HttpMethod method;

	public boolean isReissueWithoutCookieUrl(String url, String method) {
		return Pattern.matches(this.pattern, url) && this.method.matches(method);
	}
}
