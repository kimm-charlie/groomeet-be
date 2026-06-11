package com.motd.be.module.admin.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.common.argument_resolver.AccessToken;
import com.motd.be.common.utils.CookieUtils;
import com.motd.be.module.admin.auth.dto.request.AuthAdminSignInRequest;
import com.motd.be.module.admin.auth.dto.response.AuthAdminSignInResponse;
import com.motd.be.module.admin.auth.service.AuthAdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AuthAdminController {

	private final AuthAdminService authAdminService;
	private final CookieUtils cookieUtils;

	@PostMapping("/signIn")
	public ResponseEntity<Void> signIn(@RequestBody @Validated AuthAdminSignInRequest authAdminSignInRequest) {
		AuthAdminSignInResponse response = authAdminService.signIn(authAdminSignInRequest);

		return ResponseEntity.status(HttpStatus.OK)
			.headers(cookieUtils.createAccessTokenCookieHeadersForAdmin(response.getAccessToken()))
			.build();
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@PostMapping("/signOut")
	public ResponseEntity<Void> signOut(@AccessToken String accessToken) {
		authAdminService.signOut(accessToken);
		return ResponseEntity.status(HttpStatus.NO_CONTENT)
			.headers(cookieUtils.createDeleteAdminAuthCookiesHeaders())
			.build();
	}
}
