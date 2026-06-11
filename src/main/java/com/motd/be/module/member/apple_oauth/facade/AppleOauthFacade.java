package com.motd.be.module.member.apple_oauth.facade;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.apple_oauth.dto.request.AppleOauthSignInRequest;
import com.motd.be.module.member.apple_oauth.dto.response.AppleSignInContext;
import com.motd.be.module.member.apple_oauth.service.AppleOauthService;
import com.motd.be.module.member.auth.ClientType;
import com.motd.be.module.member.auth.dto.response.OAuthSignInResponse;
import com.motd.be.module.member.auth.service.AuthOauthService;
import com.motd.be.module.member.member.entity.SignInPlatform;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppleOauthFacade {

	private final AuthOauthService authOauthService;
	private final AppleOauthService appleOauthService;

	@Transactional
	public OAuthSignInResponse signIn(AppleOauthSignInRequest request, ClientType clientType) {
		// 1. Apple 인증 정보 파싱 및 속성 처리
		AppleSignInContext context = appleOauthService.processIdentity(request, clientType);

		// 2. 공통 OAuth 처리 로직 호출 (회원가입 or 로그인)
		Map<String, String> oauthResult = authOauthService.handleOauthProcess(
			SignInPlatform.APPLE,
			context.getIdentifier(),
			context.getEmail(),
			context.getAttributes()
		);

		// 3. 최종 응답 변환
		return OAuthSignInResponse.of(oauthResult);
	}
}
