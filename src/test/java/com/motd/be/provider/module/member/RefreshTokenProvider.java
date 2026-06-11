package com.motd.be.provider.module.member;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.refresh_token.entity.RefreshToken;
import com.motd.be.module.member.refresh_token.repository.RefreshTokenRepository;

@Component
public class RefreshTokenProvider {

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	public RefreshToken save(Member member, String refreshToken) {
		return refreshTokenRepository.save(RefreshToken.builder()
			.token(refreshToken)
			.member(member)
			.build());
	}

	public RefreshToken findById(Long id) {
		return refreshTokenRepository.findById(id).orElseThrow();
	}

	public List<RefreshToken> findAllByMemberId(Long memberId) {
		return refreshTokenRepository.findAllByMemberId(memberId);
	}
}
