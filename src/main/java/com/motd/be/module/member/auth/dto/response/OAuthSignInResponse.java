package com.motd.be.module.member.auth.dto.response;

import static com.motd.be.common.constants.Constants.*;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;

/**
 * 모든 OAuth 로그인 응답에 사용되는 공통 Response DTO
 * Apple, Google, Kakao OAuth 로그인 시 동일한 응답 구조를 사용합니다.
 */
@Getter
@Builder(toBuilder = true)
public class OAuthSignInResponse {

	private Boolean isExistingMember;
	private String accessToken;
	private String refreshToken;
	private String uuid;
	private String role;
	private Boolean isBanned;
	private String unBannedAt;
	private Long memberId;

	public static OAuthSignInResponse of(Map<String, String> oauthResult) {
		Boolean isExistingMember = Boolean.parseBoolean(oauthResult.get(IS_EXISTING_MEMBER));
		Boolean isBanned = Boolean.parseBoolean(oauthResult.get(IS_BANNED));
		return OAuthSignInResponse.builder()
			.isExistingMember(isExistingMember)
			.accessToken(isExistingMember ? oauthResult.get(ACCESS_TOKEN) : null)
			.refreshToken(isExistingMember ? oauthResult.get(REFRESH_TOKEN) : null)
			.uuid(isExistingMember ? null : oauthResult.get(UUID_KEY))
			.role(isExistingMember ? oauthResult.get(ROLE) : null)
			.isBanned(isBanned)
			.unBannedAt(isBanned ? oauthResult.get(UNBANNED_AT) : null)
			.memberId(oauthResult.get(MEMBER_ID) != null ? Long.valueOf(oauthResult.get(MEMBER_ID)) : null)
			.build();
	}

	public OAuthSignInResponse toSignInResponseForWeb() {
		return this.toBuilder()
			.accessToken(null)
			.refreshToken(null)
			.uuid(null)
			.build();
	}
}
