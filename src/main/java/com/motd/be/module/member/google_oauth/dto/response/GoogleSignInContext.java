package com.motd.be.module.member.google_oauth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GoogleSignInContext {

	private String email;
	private String identifier;
	
}
