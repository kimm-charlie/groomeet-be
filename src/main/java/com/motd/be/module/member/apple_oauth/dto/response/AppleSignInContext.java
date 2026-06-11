package com.motd.be.module.member.apple_oauth.dto.response;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AppleSignInContext {

	private String identifier;
	private String email;
	private Map<String, String> attributes;

}
