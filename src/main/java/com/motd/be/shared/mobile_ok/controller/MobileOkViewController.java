package com.motd.be.shared.mobile_ok.controller;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.constants.TimePolicy.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.motd.be.redis.domain.repository.RedisMobileOkRepository;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class MobileOkViewController {

	private final RedisMobileOkRepository redisMobileOkRepository;
	@Value("${mobile-ok.request-url-app}")
	private String requestUrlForApp;
	@Value("${mobile-ok.script-url}")
	private String scriptUrl;

	@PreAuthorize("hasAnyRole('DIRECTOR','MEMBER')")
	@GetMapping("/mobile-ok/view")
	public String mobileOk(Model model, @AuthenticationPrincipal Long memberId, HttpServletResponse response) {
		// Redis에 토큰 생성 및 memberId 저장
		String tempToken = redisMobileOkRepository.createAuthToken(memberId);

		// ResponseCookie 빌드
		ResponseCookie cookie = ResponseCookie.from(MOBILE_OK_KEY_FOR_APP, tempToken)
			.httpOnly(true)
			.secure(true)
			.path("/api/mobile-ok/authentication")
			.maxAge(MOBILE_OK_COOKIE_MAX_AGE_SECONDS)
			.sameSite("None")
			.build();

		// Set-Cookie 헤더에 추가
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

		model.addAttribute("requestUrl", requestUrlForApp);
		model.addAttribute("scriptUrl", scriptUrl);
		return "mobileOk";
	}

}
