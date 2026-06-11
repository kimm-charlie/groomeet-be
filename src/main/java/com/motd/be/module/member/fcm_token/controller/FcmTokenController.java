package com.motd.be.module.member.fcm_token.controller;

import static com.motd.be.common.constants.Constants.*;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.fcm_token.dto.request.FcmTokenRequest;
import com.motd.be.module.member.fcm_token.facade.FcmTokenFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FcmTokenController {

	private final FcmTokenFacade fcmTokenFacade;

	@PostMapping("/fcm-tokens")
	public ResponseEntity<Map<String, Long>> save(@RequestBody FcmTokenRequest fcmTokenRequest) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(Collections.singletonMap(ID, fcmTokenFacade.register(fcmTokenRequest)));
	}

	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@PostMapping("/fcm-tokens/{fcmTokenId}")
	public ResponseEntity<Void> mapMember(@PathVariable(FCM_TOKEN_ID) Long fcmTokenId,
		@RequestBody FcmTokenRequest fcmTokenRequest,
		@AuthenticationPrincipal Long memberId) {
		fcmTokenFacade.mapMember(fcmTokenId, memberId, fcmTokenRequest);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@DeleteMapping("/fcm-tokens/{fcmTokenId}")
	public ResponseEntity<Void> unmapMember(@PathVariable(FCM_TOKEN_ID) Long fcmTokenId,
		@RequestBody FcmTokenRequest fcmTokenRequest, @AuthenticationPrincipal Long memberId) {
		fcmTokenFacade.unmapMember(fcmTokenId, fcmTokenRequest, memberId);
		return ResponseEntity.noContent().build();
	}
}
