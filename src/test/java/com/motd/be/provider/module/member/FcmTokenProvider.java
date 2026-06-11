package com.motd.be.provider.module.member;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.fcm_token.entity.FcmToken;
import com.motd.be.module.member.fcm_token.repository.FcmTokenRepository;
import com.motd.be.module.member.member.entity.Member;

@Component
public class FcmTokenProvider {

	@Autowired
	private FcmTokenRepository fcmTokenRepository;

	public FcmToken save(String token, Member member) {
		return fcmTokenRepository.save(FcmToken.builder()
			.token(token)
			.member(member)
			.build());
	}

	public FcmToken findById(Long id) {
		return fcmTokenRepository.findById(id).orElse(null);
	}

	public List<FcmToken> findAll() {
		return fcmTokenRepository.findAll();
	}
}
