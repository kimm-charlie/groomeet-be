package com.motd.be.module.member.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberIdentityResponse {

	private Long id;

	public static MemberIdentityResponse from(Long memberId) {
		return MemberIdentityResponse.builder()
			.id(memberId)
			.build();
	}
}
