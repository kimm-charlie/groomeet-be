package com.motd.be.provider.module.member;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.apple_refresh_token.entity.AppleRefreshToken;
import com.motd.be.module.member.apple_refresh_token.repository.AppleRefreshTokenRepository;

@Component
public class AppleRefreshTokenProvider {

	@Autowired
	private AppleRefreshTokenRepository appleTokenRepository;

	public AppleRefreshToken save(AppleRefreshToken appleToken) {
		return appleTokenRepository.save(appleToken);
	}

	public AppleRefreshToken findById(Long id) {
		return appleTokenRepository.findById(id).orElseThrow();
	}

	public List<AppleRefreshToken> findAll() {
		return appleTokenRepository.findAll();
	}
}
