package com.motd.be.redis.domain.sign_up_information.entity;

import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import com.motd.be.module.member.member.entity.SignInPlatform;

import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash("SignUpInformation")
public class SignUpInformation {

	@Id
	private String uuid;
	private String email;
	private SignInPlatform platform;
	private String identifier;
	private Map<String, String> attributes;
	@TimeToLive
	private long timeToLive;

	@Builder
	public SignUpInformation(String uuid, String email, SignInPlatform platform, String identifier,
		Map<String, String> attributes) {
		this.uuid = uuid;
		this.email = email;
		this.platform = platform;
		this.identifier = identifier;
		this.timeToLive = 10800;
		this.attributes = attributes;
	}

	public static SignUpInformation of(String uuid, SignInPlatform platform, String identifier, String email,
		Map<String, String> attributes) {
		return SignUpInformation.builder()
			.uuid(uuid)
			.email(email)
			.platform(platform)
			.identifier(identifier)
			.attributes(attributes)
			.build();
	}
}
