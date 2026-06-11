package com.motd.be.redis.domain.sign_In_bridge_code.entity;

import static com.motd.be.common.utils.Utils.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash("SignInBridgeCode")
public class SignInBridgeCode {

	@Id
	private String uuid;
	private String accessToken;
	@TimeToLive
	private long timeToLive;

	@Builder
	public SignInBridgeCode(String uuid, String accessToken) {
		this.uuid = uuid;
		this.accessToken = accessToken;
		this.timeToLive = 60; // 1 minutes
	}

	public static SignInBridgeCode of(String accessToken) {
		return SignInBridgeCode.builder()
			.uuid(generateBridgeCode())
			.accessToken(accessToken)
			.build();
	}
}
