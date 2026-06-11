package com.motd.be.module.member.google_oauth.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.auth.ClientType;
import com.motd.be.module.member.auth.dto.response.OAuthSignInResponse;
import com.motd.be.module.member.auth.service.OAuthResponseHelper;
import com.motd.be.module.member.google_oauth.dto.request.GoogleOauthSignInRequest;
import com.motd.be.module.member.google_oauth.facade.GoogleOauthFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class GoogleOauthController {

	private final GoogleOauthFacade googleOauthFacade;
	private final OAuthResponseHelper oAuthCommonService;

	@PostMapping("/members/signIn/google")
	public ResponseEntity<OAuthSignInResponse> signInWithGoogle(
		@RequestBody @Validated GoogleOauthSignInRequest request, @RequestParam(CLIENT_TYPE) ClientType clientType) {

		OAuthSignInResponse response = googleOauthFacade.signIn(request);
		return oAuthCommonService.createSignInResponse(response, clientType);
	}
}
