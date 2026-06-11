package com.motd.be.module.member.auth.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.motd.be.common.utils.CookieUtils;
import com.motd.be.module.member.auth.ClientType;
import com.motd.be.module.member.auth.dto.response.OAuthSignInResponse;

import lombok.RequiredArgsConstructor;

/**
 * OAuth 로그인 이후, HTTP 응답(ResponseEntity)과 인증 쿠키를 조립하는 프레젠테이션 헬퍼.
 * - 컨트롤러에서만 사용되며, 도메인 트랜잭션을 수행하지 않습니다.
 * - 기존 회원(Web)인 경우에만 쿠키 헤더를 추가하고, 응답 바디는 민감 정보 없이 내려줍니다.
 */
@Component
@RequiredArgsConstructor
public class OAuthResponseHelper {

	private final CookieUtils cookieUtils;

	public ResponseEntity<OAuthSignInResponse> createSignInResponse(OAuthSignInResponse response,
		ClientType clientType) {

		if (response.getIsBanned()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}

		// 이미 회원가입을 했는데, Web 로그인인 경우 또는 refreshToken을 cookie에 설정
		if (response.getIsExistingMember()) {
			if (clientType.equals(ClientType.WEB)) {
				// 응답에 쿠키 포함
				return ResponseEntity.status(HttpStatus.OK)
					.headers(
						cookieUtils.createAuthCookiesHeaders(response.getAccessToken(), response.getRefreshToken()))
					.body(response.toSignInResponseForWeb());
			}
			// 앱 로그인 회원은 refreshToken을 응답에 포함
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}

		// 신규 회원 은 clientType에 상관없이 쿠키 없이 응답
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
