package com.motd.be.module.member.apple_oauth.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.apple_oauth.dto.request.AppleOauthSignInRequest;
import com.motd.be.module.member.apple_oauth.facade.AppleOauthFacade;
import com.motd.be.module.member.auth.ClientType;
import com.motd.be.module.member.auth.dto.response.OAuthSignInResponse;
import com.motd.be.module.member.auth.service.OAuthResponseHelper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AppleOauthController {

	private final AppleOauthFacade appleOauthFacade;
	private final OAuthResponseHelper oAuthCommonService;

	@PostMapping("/members/signIn/apple")
	public ResponseEntity<OAuthSignInResponse> signInWithApple(
		@RequestBody @Validated AppleOauthSignInRequest appleOauthSignInRequest,
		@RequestParam(CLIENT_TYPE) ClientType clientType) {

		OAuthSignInResponse response = appleOauthFacade.signIn(appleOauthSignInRequest, clientType);
		return oAuthCommonService.createSignInResponse(response, clientType);
	}
}
