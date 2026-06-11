package com.motd.be.module.member.apple_refresh_token.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.apple_refresh_token.entity.AppleRefreshToken;
import com.motd.be.module.member.apple_refresh_token.repository.AppleRefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppleRefreshTokenCommandService {

	private final AppleRefreshTokenRepository appleTokenRepository;

	public AppleRefreshToken save(AppleRefreshToken appleToken) {
		return appleTokenRepository.save(appleToken);
	}

	public void deleteById(Long id) {
		appleTokenRepository.deleteById(id);
	}
}
