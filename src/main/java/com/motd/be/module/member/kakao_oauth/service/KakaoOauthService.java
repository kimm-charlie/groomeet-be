package com.motd.be.module.member.kakao_oauth.service;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.KakaoException;
import com.motd.be.module.member.kakao_oauth.controller.KakaoOauthClient;
import com.motd.be.module.member.kakao_oauth.controller.KakaoTokenClient;
import com.motd.be.module.member.kakao_oauth.dto.response.KakaoOauthMemberInfoResponse;
import com.motd.be.module.member.kakao_oauth.dto.response.KakaoSignInContext;
import com.motd.be.module.member.kakao_oauth.dto.response.KakaoTokenResponse;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoOauthService {

	private final KakaoOauthClient kakaoClient;
	private final KakaoTokenClient kakaoTokenClient;
	@Value("${kakao.rest-api-key}")
	private String clientId;
	@Value("${kakao.client-secret}")
	private String clientSecret;
	@Value("${kakao.redirect-uri}")
	private String redirectUri;

	/**
	 * Kakao 사용자 인증 정보 처리
	 * 1. 액세스 토큰을 Bearer 형식으로 세팅
	 * 2. Kakao API 호출을 통해 사용자 정보 조회
	 * 3. identifier, email 추출
	 * 4. 컨텍스트 객체로 반환
	 */
	public KakaoSignInContext processIdentity(String accessToken) {
		String accessTokenWithPrefix = TOKEN_PREFIX + accessToken;

		try {
			// Kakao API를 통해 사용자 정보 조회
			KakaoOauthMemberInfoResponse kakaoUserInfoResponse = kakaoClient.getKakaoMemberInfo(accessTokenWithPrefix);

			String email = kakaoUserInfoResponse.getKakaoAccount().getEmail();
			String identifier = kakaoUserInfoResponse.getId();

			// email, identifier, attributes를 컨텍스트로 묶어서 반환
			return KakaoSignInContext.builder()
				.identifier(identifier)
				.email(email)
				.build();

		} catch (FeignException.FeignClientException e) {
			throw new CustomRuntimeException(KakaoException.KAKAO_AUTH_FAIL);
		}
	}

	/**
	 * 웹은 authorization code 를 통해서 accessToken 을 서버측에서 발급 받는다.
	 *
	 * @param authorizationCode
	 * @return
	 */
	public KakaoTokenResponse getAccessToken(String authorizationCode) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(GRANT_TYPE, AUTHORIZATION_CODE);
		params.add(CLIENT_ID, clientId);
		params.add(REDIRECT_URI, redirectUri);
		params.add(CODE, authorizationCode);
		params.add(CLIENT_SECRET, clientSecret);

		try {
			return kakaoTokenClient.getAccessToken(params);
		} catch (FeignException.FeignClientException e) {
			log.error("Kakao getAccessToken failed: {}", e.getMessage());
			throw new CustomRuntimeException(KakaoException.KAKAO_AUTH_FAIL);
		}
	}
}
