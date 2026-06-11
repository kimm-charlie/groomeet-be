package com.motd.be.module.member.member.dto.response;

import com.motd.be.common.utils.DateFormatUtils;
import com.motd.be.module.member.member.entity.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberFindInfoResponse {

	private Long id;
	private String email;
	private String signInPlatform;
	private String nickname;
	private String profileImage;
	private String role;
	private String createdAt;

	public static MemberFindInfoResponse from(Member member) {
		return MemberFindInfoResponse.builder()
			.id(member.getId())
			.email(member.getEmail())
			.signInPlatform(member.getSignInPlatform().name())
			.nickname(member.getNickname())
			.profileImage(member.getCdnProfileImageUrl())
			.role(member.getRole().getRoleType())
			.createdAt(DateFormatUtils.formatToDateString(member.getCreatedAt()))
			.build();
	}
}
