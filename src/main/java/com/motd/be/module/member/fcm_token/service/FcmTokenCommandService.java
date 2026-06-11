package com.motd.be.module.member.fcm_token.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.fcm_token.entity.FcmToken;
import com.motd.be.module.member.fcm_token.repository.FcmTokenRepository;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class FcmTokenCommandService {

	private final FcmTokenRepository fcmTokenRepository;

	public FcmToken save(FcmToken fcmToken) {
		return fcmTokenRepository.save(fcmToken);
	}

	public void incrementFailedCountOrMarkAsDeletedForSingleFcmTokenId(Long id) {
		fcmTokenRepository.incrementFailedCountOrMarkAsDeletedForSingleFcmTokenId(id);
	}

	public void incrementFailedCountOrMarkAsDeletedForMultipleFcmTokenIds(List<Long> fcmTokenIds) {
		fcmTokenRepository.incrementFailedCountOrMarkAsDeletedForMultipleFcmTokenIds(fcmTokenIds);
	}

	public void unmapAllFcmTokensFromMember(Member member) {
		fcmTokenRepository.unmapAllFcmTokensFromMember(member);
	}
}
