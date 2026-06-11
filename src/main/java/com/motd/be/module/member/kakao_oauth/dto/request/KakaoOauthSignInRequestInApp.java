package com.motd.be.module.member.kakao_oauth.dto.request;

import static com.motd.be.common.constants.ValidationMessages.*;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class KakaoOauthSignInRequestInApp {

	@NotBlank(message = KAKAO_ACCESS_TOKEN_REQUIRED)
	private String accessToken;
}
