package com.motd.be.module.admin.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberToggleAuthenticationResponse {

	private Boolean isAuthenticated;

	public static MemberToggleAuthenticationResponse from(Boolean isAuthenticated) {
		return MemberToggleAuthenticationResponse.builder()
			.isAuthenticated(isAuthenticated)
			.build();
	}
}
