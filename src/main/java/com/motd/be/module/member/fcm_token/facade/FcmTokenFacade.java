package com.motd.be.module.member.fcm_token.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.fcm_token.dto.request.FcmTokenRequest;
import com.motd.be.module.member.fcm_token.service.FcmTokenCommandService;
import com.motd.be.module.member.fcm_token.service.FcmTokenService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FcmTokenFacade {

	private final FcmTokenCommandService fcmTokenCommandService;
	private final MemberQueryService memberQueryService;
	private final FcmTokenService fcmTokenService;

	@Transactional
	public Long register(FcmTokenRequest request) {
		return fcmTokenCommandService
			.save(request.toEntity())
			.getId();
	}

	@Transactional
	public void mapMember(Long fcmTokenId, Long memberId, FcmTokenRequest request) {
		Member member = memberQueryService.findById(memberId);

		fcmTokenService.mapMember(fcmTokenId, member, request);
	}

	@Transactional
	public void unmapMember(Long fcmTokenId, FcmTokenRequest request, Long memberId) {
		fcmTokenService.unmapMember(fcmTokenId, request, memberId);
	}
}
