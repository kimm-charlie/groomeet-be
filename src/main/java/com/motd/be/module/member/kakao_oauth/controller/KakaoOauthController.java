package com.motd.be.module.member.kakao_oauth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.auth.ClientType;
import com.motd.be.module.member.auth.dto.response.OAuthSignInResponse;
import com.motd.be.module.member.auth.service.OAuthResponseHelper;
import com.motd.be.module.member.kakao_oauth.dto.request.KakaoOauthSignInRequestInApp;
import com.motd.be.module.member.kakao_oauth.dto.request.KakaoOauthSignInRequestInWeb;
import com.motd.be.module.member.kakao_oauth.facade.KakaoOauthFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class KakaoOauthController {

	private final KakaoOauthFacade kakaoOauthFacade;
	private final OAuthResponseHelper oAuthCommonService;

	@PostMapping(value = "/members/signIn/kakao", params = "Client-Type=APP")
	public ResponseEntity<OAuthSignInResponse> signInWithKakaoInApp(
		@RequestBody @Validated KakaoOauthSignInRequestInApp authKakaoSignInRequest) {

		OAuthSignInResponse response = kakaoOauthFacade.signInApp(authKakaoSignInRequest);
		return oAuthCommonService.createSignInResponse(response, ClientType.APP);
	}

	@PostMapping(value = "/members/signIn/kakao", params = "Client-Type=WEB")
	public ResponseEntity<OAuthSignInResponse> signInWithKakao(
		@RequestBody @Validated KakaoOauthSignInRequestInWeb authKakaoSignInRequest) {

		OAuthSignInResponse response = kakaoOauthFacade.signInWeb(authKakaoSignInRequest);
		return oAuthCommonService.createSignInResponse(response, ClientType.WEB);
	}
}
