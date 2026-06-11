package com.motd.be.module.member.fcm_token.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.FcmTokenException;
import com.motd.be.module.member.fcm_token.entity.FcmToken;
import com.motd.be.module.member.fcm_token.repository.FcmTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FcmTokenQueryService {

	private final FcmTokenRepository fcmTokenRepository;

	public FcmToken findById(Long id) {
		return fcmTokenRepository.findById(id)
			.orElseThrow(() -> new CustomRuntimeException(FcmTokenException.NOT_FOUND));
	}

	public List<FcmToken> findAllByMemberIdsWithIsActivityPushAgreed(List<Long> memberIds) {
		return fcmTokenRepository.findAllByMemberIdsWithIsActivityPushAgreed(memberIds);
	}
}
