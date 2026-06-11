package com.motd.be.module.admin.member.dto.response;

import com.motd.be.module.member.member.entity.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberWithProfileImageResponseForAdmin {

	private Long id;
	private String nickname;
	private String profileImageUrl;

	public static MemberWithProfileImageResponseForAdmin from(Member member) {
		return MemberWithProfileImageResponseForAdmin.builder()
			.id(member.getId())
			.nickname(member.getNickname())
			.profileImageUrl(member.getCdnProfileImageUrl())
			.build();
	}
}
