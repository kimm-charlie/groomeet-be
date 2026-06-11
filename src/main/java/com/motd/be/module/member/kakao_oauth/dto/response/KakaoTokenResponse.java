package com.motd.be.module.member.kakao_oauth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoTokenResponse {

	@JsonProperty("access_token")
	private String accessToken;
	@JsonProperty("refresh_token")
	private String refreshToken;
	@JsonProperty("token_type")
	private String tokenType;
	@JsonProperty("expires_in")
	private Long expiresIn;
	@JsonProperty("refresh_token_expires_in")
	private Long refreshTokenExpiresIn;
}
