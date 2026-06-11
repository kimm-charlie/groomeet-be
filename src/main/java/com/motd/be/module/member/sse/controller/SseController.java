package com.motd.be.module.member.sse.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.motd.be.module.member.member.entity.Role;
import com.motd.be.module.member.sse.facade.SseFacade;

import lombok.RequiredArgsConstructor;

/**
 * SSE 연결 시 같은 회원이라도 여러개의 에미터를 사용하는 경우를 고려하여 UUID 를 통해 에미터를 구분하여 사용하고,
 * 서버는 (memberId, role, uuid(클라이언트로 부터 받은)) 기준으로 emitter를 관리한다.
 * <p>
 * 서버 간 이벤트 전파는 Redis Pub/Sub을 통해 동기화한다.
 */

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class SseController {

	private final SseFacade sseFacade;

	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@GetMapping(value = "/sse/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public ResponseEntity<SseEmitter> connect(@AuthenticationPrincipal Long memberId,
		@RequestParam(ROLE) Role role,
		@RequestHeader(value = LAST_EVENT_ID, required = false) String lastEventId) {
		return ResponseEntity.status(HttpStatus.OK).body(sseFacade.connect(memberId, role, lastEventId));
	}
}
