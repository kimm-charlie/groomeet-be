package com.motd.be.module.member.auth.dto.response;

import com.motd.be.module.member.jwt.Jwt;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthExchangeCodeForTokenResponse {

	private String accessToken;
	private String refreshToken;

	public static AuthExchangeCodeForTokenResponse from(Jwt jwt) {
		return AuthExchangeCodeForTokenResponse.builder()
			.accessToken(jwt.getAccessToken())
			.refreshToken(jwt.getRefreshToken())
			.build();
	}

	public void deleteRefreshToken() {
		this.refreshToken = null;
	}
}
