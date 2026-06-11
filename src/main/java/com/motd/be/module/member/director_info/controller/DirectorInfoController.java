package com.motd.be.module.member.director_info.controller;

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

import com.motd.be.common.utils.CookieUtils;
import com.motd.be.module.member.director_info.dto.request.DirectorInfoRegisterRequest;
import com.motd.be.module.member.director_info.dto.response.DirectorRankMainViewResponse;
import com.motd.be.module.member.director_info.dto.response.DirectorRankPageResponse;
import com.motd.be.module.member.director_info.facade.DirectorInfoFacade;
import com.motd.be.module.member.jwt.Jwt;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DirectorInfoController {

	private final DirectorInfoFacade directorInfoFacade;
	private final CookieUtils cookieUtils;

	@PreAuthorize("hasAnyRole('MEMBER')")
	@PostMapping("/members/me/director")
	public ResponseEntity<Void> register(@AuthenticationPrincipal Long memberId,
		@CookieValue(value = ACCESS_TOKEN) String accessToken,
		@CookieValue(value = REFRESH_TOKEN) String refreshToken,
		@RequestBody @Validated DirectorInfoRegisterRequest request) {
		Jwt issuedToken = directorInfoFacade.register(memberId, request, accessToken, refreshToken);

		// 응답에 쿠키 포함
		return ResponseEntity.status(HttpStatus.CREATED)
			.headers(cookieUtils.createAuthCookiesHeaders(issuedToken.getAccessToken(), issuedToken.getRefreshToken()))
			.build();
	}

	@GetMapping(value = "/directors/rank", params = "viewType=mainView")
	public ResponseEntity<DirectorRankMainViewResponse> getDirectorRankForMain() {
		return ResponseEntity.ok(directorInfoFacade.findDirectorRankInMainView());
	}

	@GetMapping(value = "/directors/rank", params = "viewType=rankView")
	public ResponseEntity<DirectorRankPageResponse> getDirectorRankForRankView(
		@RequestParam(name = PAGE, required = false, defaultValue = ZERO) int page) {
		return ResponseEntity.ok(directorInfoFacade.findDirectorRankInRankView(page));
	}

}
