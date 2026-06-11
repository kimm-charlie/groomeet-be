package com.motd.be.module.member.kakao_oauth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoSignInContext {

	private String identifier;
	private String email;

}
