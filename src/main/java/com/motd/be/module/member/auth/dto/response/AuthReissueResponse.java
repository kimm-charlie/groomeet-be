package com.motd.be.module.member.auth.dto.response;

import com.motd.be.module.member.jwt.Jwt;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthReissueResponse {

	private String accessToken;
	private String refreshToken;

	public static AuthReissueResponse from(Jwt reissuedToken) {
		return AuthReissueResponse.builder()
			.accessToken(reissuedToken.getAccessToken())
			.refreshToken(reissuedToken.getRefreshToken())
			.build();
	}

	public void deleteRefreshToken() {
		this.refreshToken = null;
	}
}
