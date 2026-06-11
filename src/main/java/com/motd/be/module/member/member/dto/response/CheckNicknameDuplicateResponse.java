package com.motd.be.module.member.member.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CheckNicknameDuplicateResponse {

	private Boolean isDuplicated;

	public static CheckNicknameDuplicateResponse from(Boolean isDuplicated) {
		return CheckNicknameDuplicateResponse.builder()
			.isDuplicated(isDuplicated)
			.build();
	}
}
