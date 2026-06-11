package com.motd.be.module.member.apple_refresh_token.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.apple_refresh_token.entity.AppleRefreshToken;
import com.motd.be.module.member.apple_refresh_token.repository.AppleRefreshTokenRepository;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppleRefreshTokenQueryService {

	private final AppleRefreshTokenRepository appleTokenRepository;

	public Optional<AppleRefreshToken> findByIdentifier(String identifier) {
		return appleTokenRepository.findByIdentifier(identifier);
	}

	public Optional<AppleRefreshToken> findByMember(Member member) {
		return appleTokenRepository.findByMember(member);
	}
}
