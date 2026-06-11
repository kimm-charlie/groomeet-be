package com.motd.be.module.member.auth.dto.response;

import com.motd.be.redis.domain.sign_In_bridge_code.entity.SignInBridgeCode;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthGenerateBridgeCodeResponse {

	private String bridgeCode;

	public static AuthGenerateBridgeCodeResponse from(SignInBridgeCode signInBridgeCode) {
		return AuthGenerateBridgeCodeResponse.builder()
			.bridgeCode(signInBridgeCode.getUuid())
			.build();
	}
}
