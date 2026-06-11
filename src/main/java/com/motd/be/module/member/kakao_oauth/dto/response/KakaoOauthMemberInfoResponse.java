package com.motd.be.module.member.kakao_oauth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoOauthMemberInfoResponse {

	private String id;
	@JsonProperty("kakao_account")
	private KakaoAccount kakaoAccount;

	@Getter
	@Setter
	public static class KakaoAccount {
		private String email;
	}

}
