package com.motd.be.module.member.auth.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.common.argument_resolver.AccessToken;
import com.motd.be.common.utils.CookieUtils;
import com.motd.be.module.member.auth.ClientType;
import com.motd.be.module.member.auth.dto.request.AuthReissueTokenRequest;
import com.motd.be.module.member.auth.dto.request.AuthSignOutRequest;
import com.motd.be.module.member.auth.dto.request.AuthSignUpRequest;
import com.motd.be.module.member.auth.dto.request.AuthWithdrawalRequest;
import com.motd.be.module.member.auth.dto.response.AuthExchangeCodeForTokenResponse;
import com.motd.be.module.member.auth.dto.response.AuthGenerateBridgeCodeResponse;
import com.motd.be.module.member.auth.dto.response.AuthReissueResponse;
import com.motd.be.module.member.auth.dto.response.AuthSignUpResponse;
import com.motd.be.module.member.auth.dto.response.MemberIdentityResponse;
import com.motd.be.module.member.auth.facade.AuthFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

	private final CookieUtils cookieUtils;
	private final AuthFacade authFacade;

	@PostMapping("/members/signUp")
	public ResponseEntity<AuthSignUpResponse> signUp(@RequestBody @Validated AuthSignUpRequest authSignUpRequest,
		@RequestParam(CLIENT_TYPE) ClientType clientType) {
		AuthSignUpResponse response = authFacade.signUp(authSignUpRequest, clientType);

		// Refresh Token을 HTTPOnly 쿠키로 설정
		if (clientType.equals(ClientType.WEB)) {
			// 응답에 쿠키 포함
			return ResponseEntity.status(HttpStatus.CREATED)
				.headers(cookieUtils.createAuthCookiesHeaders(response.getAccessToken(), response.getRefreshToken()))
				.body(AuthSignUpResponse.builder().memberId(response.getMemberId()).build());
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PreAuthorize("hasAnyRole('MEMBER', 'DIRECTOR')")
	@PostMapping("/members/signOut")
	public ResponseEntity<Void> signOut(@AuthenticationPrincipal Long memberId,
		@AccessToken String accessToken,
		@CookieValue(value = REFRESH_TOKEN, required = false) String refreshToken,
		@RequestParam(CLIENT_TYPE) ClientType clientType,
		@RequestBody @Validated AuthSignOutRequest authSignOutRequest) {

		authFacade.signOut(memberId, accessToken, refreshToken, clientType, authSignOutRequest);

		return ResponseEntity.status(HttpStatus.NO_CONTENT)
			.headers(cookieUtils.createDeleteAuthCookiesHeaders())
			.build();
	}

	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@PostMapping("/members/withdrawal")
	public ResponseEntity<Void> withdrawal(@AuthenticationPrincipal Long memberId,
		@RequestBody @Validated AuthWithdrawalRequest request, @AccessToken String accessToken) {

		authFacade.withdrawal(memberId, request, accessToken);

		return ResponseEntity.status(HttpStatus.NO_CONTENT)
			.headers(cookieUtils.createDeleteAuthCookiesHeaders())
			.build();
	}

	@PostMapping("/members/reissue")
	public ResponseEntity<AuthReissueResponse> reissueToken(@RequestBody AuthReissueTokenRequest request) {

		AuthReissueResponse response = authFacade.reissueToken(null, ClientType.APP, request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@PostMapping("/members/bridge/code")
	public ResponseEntity<AuthGenerateBridgeCodeResponse> generateBridgeCode(@AccessToken String accessToken) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authFacade.generateBridgeCode(accessToken));
	}

	@PostMapping("/members/bridge/token")
	public ResponseEntity<Void> exchangeCodeForToken(@RequestParam(BRIDGE_CODE) String code) {
		AuthExchangeCodeForTokenResponse response = authFacade.exchangeCodeForToken(code);

		// 응답에 쿠키 포함
		return ResponseEntity.status(HttpStatus.CREATED)
			.headers(cookieUtils.createAuthCookiesHeaders(response.getAccessToken(), response.getRefreshToken()))
			.build();
	}

	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@GetMapping("/members/auth/identity")
	public ResponseEntity<MemberIdentityResponse> getAuthInfo(@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.ok(MemberIdentityResponse.from(memberId));
	}

}
