package com.motd.be.module.member.member.dto.response;

import static com.motd.be.common.utils.Utils.*;

import com.motd.be.common.utils.DateFormatUtils;
import com.motd.be.module.member.member.entity.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberFindAccountInfoResponse {

	private Long id;
	private String email;
	private String signInPlatform;
	private String phoneNumber;
	private String role;
	private String createdAt;

	public static MemberFindAccountInfoResponse from(Member member) {
		return MemberFindAccountInfoResponse.builder()
			.id(member.getId())
			.email(member.getEmail())
			.signInPlatform(member.getSignInPlatform().name())
			.phoneNumber(member.getIsAuthenticated() ? formatPhoneNumber(member.getPhoneNumber()) : null)
			.role(member.getRole().getRoleType())
			.createdAt(DateFormatUtils.formatToDateString(member.getCreatedAt()))
			.build();
	}
}
