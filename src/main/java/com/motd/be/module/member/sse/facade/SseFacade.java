package com.motd.be.module.member.sse.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.motd.be.module.member.member.entity.Role;
import com.motd.be.module.member.sse.service.SseService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SseFacade {

	private final SseService sseService;

	public SseEmitter connect(Long memberId, Role role, String lastEventId) {
		return sseService.connect(memberId, role, lastEventId);
	}
}
