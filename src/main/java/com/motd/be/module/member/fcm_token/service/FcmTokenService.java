package com.motd.be.module.member.fcm_token.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.fcm_token.dto.request.FcmTokenRequest;
import com.motd.be.module.member.fcm_token.entity.FcmToken;
import com.motd.be.module.member.fcm_token.validator.FcmTokenValidator;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FcmTokenService {

	private final FcmTokenQueryService fcmTokenQueryService;
	private final FcmTokenValidator fcmTokenValidator;

	public void mapMember(Long fcmTokenId, Member member, FcmTokenRequest fcmTokenRequest) {
		FcmToken fcmToken = fcmTokenQueryService.findById(fcmTokenId);

		fcmTokenValidator.validateToken(fcmToken, fcmTokenRequest);

		fcmToken.updateMember(member);
		fcmToken.updateUsedAt();
	}

	public void unmapMember(Long fcmTokenId, FcmTokenRequest fcmTokenRequest, Long memberId) {
		FcmToken fcmToken = fcmTokenQueryService.findById(fcmTokenId);

		if (fcmToken.getMember() == null) {
			// 이미 언매핑됨 → idempotent success
			return;
		}

		fcmTokenValidator.validateToken(fcmToken, fcmTokenRequest);
		fcmTokenValidator.validateOwnership(fcmToken, memberId);

		fcmToken.deleteMember();
		fcmToken.updateUsedAt();
	}
}
