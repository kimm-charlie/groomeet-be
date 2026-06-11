package com.motd.be.module.member.apple_oauth.dto.request;

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
public class AppleOauthSignInRequest {

	@NotBlank(message = IDENTITY_TOKEN_REQUIRED)
	private String identityToken;
	@NotBlank(message = AUTHORIZATION_CODE_REQUIRED)
	private String authorizationCode;

}
