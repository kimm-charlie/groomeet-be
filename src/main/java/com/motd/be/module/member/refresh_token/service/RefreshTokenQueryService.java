package com.motd.be.module.member.refresh_token.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.refresh_token.entity.RefreshToken;
import com.motd.be.module.member.refresh_token.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenQueryService {

	private final RefreshTokenRepository refreshTokenRepository;

	public Optional<RefreshToken> findByMemberIdAndTokenWithLockOptional(Long memberId, String refreshToken) {
		return refreshTokenRepository.findByMemberIdAndTokenWithLock(memberId, refreshToken);
	}
}
