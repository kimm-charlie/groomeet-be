package com.motd.be.module.member.refresh_token.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.refresh_token.entity.RefreshToken;
import com.motd.be.module.member.refresh_token.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenCommandService {

	private final RefreshTokenRepository tokenRepository;

	public void save(RefreshToken token) {
		tokenRepository.save(token);
	}

	public void deleteByMemberIdAndRefreshToken(Long memberId, String refreshToken) {
		tokenRepository.deleteByMemberIdAndRefreshToken(memberId, refreshToken);
	}

	public void deleteByToken(String refreshToken) {
		tokenRepository.deleteByToken(refreshToken);
	}

	public void deleteByMemberId(Long memberId) {
		tokenRepository.deleteByMemberId(memberId);
	}
}
