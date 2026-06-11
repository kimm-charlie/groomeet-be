package com.motd.be.module.admin.auth.dto.response;

import com.motd.be.module.member.jwt.Jwt;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthAdminSignInResponse {

	private String accessToken;

	public static AuthAdminSignInResponse from(Jwt jwt) {
		return AuthAdminSignInResponse.builder()
			.accessToken(jwt.getAccessToken())
			.build();
	}
}
