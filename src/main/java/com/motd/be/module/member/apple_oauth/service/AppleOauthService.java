package com.motd.be.module.member.apple_oauth.service;

import static com.motd.be.common.constants.Constants.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.AppleOauthException;
import com.motd.be.module.member.apple_oauth.dto.request.AppleOauthSignInRequest;
import com.motd.be.module.member.apple_oauth.dto.response.AppleSignInContext;
import com.motd.be.module.member.apple_refresh_token.service.AppleRefreshTokenQueryService;
import com.motd.be.module.member.apple_refresh_token.service.AppleRefreshTokenService;
import com.motd.be.module.member.auth.ClientType;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppleOauthService {

	private final AppleRefreshTokenService appleOauthRefreshTokenService;
	private final AppleRefreshTokenQueryService appleTokenQueryService;
	private final AppleJwtDecoder appleJwtDecoder;

	/**
	 * Apple identity token 디코딩 및 refresh token 처리 로직
	 */
	public AppleSignInContext processIdentity(AppleOauthSignInRequest request, ClientType clientType) {
		// 1. Apple에서 전달받은 identity token 디코딩
		Claims claims = appleJwtDecoder.decodeIdentityToken(request.getIdentityToken());

		String email = claims.get(EMAIL, String.class);
		String identifier = claims.get(SUB, String.class);

		if (email == null) {
			throw new CustomRuntimeException(AppleOauthException.FAIL_TO_PARSING_IDENTITY_TOKEN);
		}

		// 2. 추가 속성(attribute) 초기화
		Map<String, String> attributes = new HashMap<>();

		// 3. identifier 기준 refresh token 확인 및 최초 로그인 처리
		if (appleTokenQueryService.findByIdentifier(identifier).isEmpty()) {
			String appleRefreshToken = appleOauthRefreshTokenService.getRefreshToken(request.getAuthorizationCode(),
				clientType);
			attributes.put(APPLE_REFRESH_TOKEN, appleRefreshToken);
		}

		// 4. 객체 반환 (email, identifier, attributes)
		return AppleSignInContext.builder()
			.identifier(identifier)
			.email(email)
			.attributes(attributes)
			.build();
	}

}
