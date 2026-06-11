package com.motd.be.module.member.google_oauth.facade;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.auth.dto.response.OAuthSignInResponse;
import com.motd.be.module.member.auth.service.AuthOauthService;
import com.motd.be.module.member.google_oauth.dto.request.GoogleOauthSignInRequest;
import com.motd.be.module.member.google_oauth.dto.response.GoogleSignInContext;
import com.motd.be.module.member.google_oauth.service.GoogleOauthService;
import com.motd.be.module.member.member.entity.SignInPlatform;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoogleOauthFacade {

	private final GoogleOauthService googleOauthService;
	private final AuthOauthService authOauthService;

	/**
	 * Google 사용자 인증 정보 처리
	 * 1. ID 토큰 검증
	 * 2. 유효 시 이메일, 식별자(identifier) 추출
	 * 3. 컨텍스트 객체로 반환
	 */
	@Transactional
	public OAuthSignInResponse signIn(GoogleOauthSignInRequest request) {
		// 1. Google ID 토큰 검증 및 사용자 정보 추출
		GoogleSignInContext context = googleOauthService.processIdentity(request);

		// 2. 공통 OAuth 처리 (회원가입 또는 로그인)
		Map<String, String> oauthResult = authOauthService.handleOauthProcess(
			SignInPlatform.GOOGLE,
			context.getIdentifier(),
			context.getEmail(),
			Map.of()
		);

		// 3. 응답 객체 생성 및 반환
		return OAuthSignInResponse.of(oauthResult);
	}

}
