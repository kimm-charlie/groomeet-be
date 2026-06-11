package com.motd.be.module.member.member_block.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberBlockCheckResponse {

	private boolean blocked;

	public static MemberBlockCheckResponse from(boolean blocked) {
		return MemberBlockCheckResponse.builder()
			.blocked(blocked)
			.build();
	}
}
