package com.motd.be.module.member.member.dto.response;

import com.motd.be.module.member.member.entity.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberIsAuthenticatedResponse {

	private Boolean isAuthenticated;

	public static MemberIsAuthenticatedResponse from(Member member) {
		return MemberIsAuthenticatedResponse.builder()
			.isAuthenticated(member.getIsAuthenticated())
			.build();
	}
}
