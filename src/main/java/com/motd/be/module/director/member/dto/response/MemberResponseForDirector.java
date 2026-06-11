package com.motd.be.module.director.member.dto.response;

import com.motd.be.module.member.member.entity.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberResponseForDirector {

	private Long id;
	private String nickname;
	private String profileImageUrl;
	private Boolean isWithdrawal;

	public static MemberResponseForDirector from(Member member) {
		return MemberResponseForDirector.builder()
			.id(member.getId())
			.nickname(member.getNickname())
			.profileImageUrl(member.getCdnProfileImageUrl())
			.isWithdrawal(member.isWithdrawn())
			.build();
	}
}
