package com.motd.be.module.member.kakao_oauth.facade;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.auth.dto.response.OAuthSignInResponse;
import com.motd.be.module.member.auth.service.AuthOauthService;
import com.motd.be.module.member.kakao_oauth.dto.request.KakaoOauthSignInRequestInApp;
import com.motd.be.module.member.kakao_oauth.dto.request.KakaoOauthSignInRequestInWeb;
import com.motd.be.module.member.kakao_oauth.dto.response.KakaoSignInContext;
import com.motd.be.module.member.kakao_oauth.dto.response.KakaoTokenResponse;
import com.motd.be.module.member.kakao_oauth.service.KakaoOauthService;
import com.motd.be.module.member.member.entity.SignInPlatform;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KakaoOauthFacade {

	private final KakaoOauthService kakaoOauthService;
	private final AuthOauthService authOauthService;

	@Transactional
	public OAuthSignInResponse signInApp(KakaoOauthSignInRequestInApp request) {
		// 1. Kakao API를 통해 사용자 정보 조회 및 파싱
		KakaoSignInContext context = kakaoOauthService.processIdentity(request.getAccessToken());

		// 2. 공통 OAuth 처리 로직 호출 (회원가입 또는 로그인)
		Map<String, String> oauthResult = authOauthService.handleOauthProcess(
			SignInPlatform.KAKAO,
			context.getIdentifier(),
			context.getEmail(),
			Map.of()
		);

		// 3. 응답 객체 변환 및 반환
		return OAuthSignInResponse.of(oauthResult);
	}

	@Transactional
	public OAuthSignInResponse signInWeb(KakaoOauthSignInRequestInWeb request) {
		// 1. authorization code 를 사용하여 액세스 토큰 발급
		KakaoTokenResponse tokenResponse = kakaoOauthService.getAccessToken(request.getAuthorizationCode());

		KakaoSignInContext context = kakaoOauthService.processIdentity(tokenResponse.getAccessToken());

		// 2. 공통 OAuth 처리 로직 호출 (회원가입 또는 로그인)
		Map<String, String> oauthResult = authOauthService.handleOauthProcess(
			SignInPlatform.KAKAO,
			context.getIdentifier(),
			context.getEmail(),
			Map.of()
		);

		// 3. 응답 객체 변환 및 반환
		return OAuthSignInResponse.of(oauthResult);
	}
}
