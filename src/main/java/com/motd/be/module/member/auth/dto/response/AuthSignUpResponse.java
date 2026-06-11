package com.motd.be.module.member.auth.dto.response;

import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthSignUpResponse {

	private String accessToken;
	private String refreshToken;
	private Long memberId;

	public static AuthSignUpResponse of(Jwt jwt, Member member) {
		return AuthSignUpResponse.builder()
			.accessToken(jwt.getAccessToken())
			.refreshToken(jwt.getRefreshToken())
			.memberId(member.getId())
			.build();
	}
}
