package com.motd.be.module.member.fcm_token.dto.request;

import com.motd.be.module.member.fcm_token.entity.FcmToken;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class FcmTokenRequest {

	private String token;

	public FcmToken toEntity() {
		return FcmToken.builder()
			.token(token)
			.build();
	}

}
