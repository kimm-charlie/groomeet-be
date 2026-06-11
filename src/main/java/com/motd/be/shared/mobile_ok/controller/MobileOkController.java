package com.motd.be.shared.mobile_ok.controller;

import static com.motd.be.common.constants.Constants.*;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.motd.be.shared.mobile_ok.dto.response.MobileOkCreateTokenResponse;
import com.motd.be.shared.mobile_ok.facade.MobileOkFacade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class MobileOkController {

	@Value("${domain}")
	private String domain;
	private final MobileOkFacade mobileOkFacade;

	@PreAuthorize("hasAnyRole('DIRECTOR','MEMBER')")
	@PostMapping("/mobile-ok/token")
	public ResponseEntity<MobileOkCreateTokenResponse> createMobileOkToken(@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.status(HttpStatus.CREATED).body(mobileOkFacade.createMobileOkToken(memberId));
	}

	@PreAuthorize("hasAnyRole('DIRECTOR','MEMBER')")
	@PostMapping("/mobile-ok/web/token")
	public ResponseEntity<MobileOkCreateTokenResponse> createMobileOkTokenForWeb(
		@AuthenticationPrincipal Long memberId) {
		return ResponseEntity.status(HttpStatus.CREATED).body(mobileOkFacade.createMobileOkTokenForWeb(memberId));
	}

	@PreAuthorize("hasAnyRole('DIRECTOR','MEMBER')")
	@PostMapping(value = "/mobile-ok/web/authentication")
	public ResponseEntity<Map<String, Object>> getMobileOKResultForWeb(@AuthenticationPrincipal Long memberId,
		@RequestParam(DATA) String data) {
		try {
			mobileOkFacade.processResultForWeb(memberId, data);

			// SDK가 기대하는 형식으로 응답
			Map<String, Object> response = new LinkedHashMap<>();
			response.put("resultCode", "2000");
			response.put("resultMsg", "성공");

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			Map<String, Object> response = new LinkedHashMap<>();
			response.put("resultCode", "5000");
			response.put("resultMsg", e.getMessage());

			return ResponseEntity.ok(response);  // 200으로 주고 바디에 에러 정보
		}
	}

	@PostMapping(value = "/mobile-ok/authentication")
	public ResponseEntity<Void> getMobileOKResultForApp(@RequestParam(DATA) String data,
		@CookieValue(value = MOBILE_OK_KEY_FOR_APP, required = false) String mobileOkAuthToken) {

		try {
			mobileOkFacade.processResultForApp(mobileOkAuthToken, data);

			// 성공 시 리다이렉트
			return ResponseEntity
				.status(HttpStatus.FOUND)
				.location(UriComponentsBuilder
					.fromUriString(domain + "/account/MyAccountSetting")
					.queryParam("result", "success")
					.build()
					.encode(StandardCharsets.UTF_8)
					.toUri())
				.build();

		} catch (RuntimeException e) {
			log.error("Mobile OK 인증 실패: {}", e.getMessage());
			return ResponseEntity
				.status(HttpStatus.FOUND)
				.location(
					UriComponentsBuilder
						.fromUriString(domain + "/account/MyAccountSetting")
						.queryParam("result", e.getMessage())
						.build()
						.encode(StandardCharsets.UTF_8)
						.toUri()
				)
				.build();
		}
	}

}
