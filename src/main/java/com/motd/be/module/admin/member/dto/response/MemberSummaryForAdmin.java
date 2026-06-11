package com.motd.be.module.admin.member.dto.response;

import com.motd.be.module.member.member.entity.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberSummaryForAdmin {

	private Long id;
	private String nickname;

	public static MemberSummaryForAdmin from(Member member) {
		if (member == null) {
			return null;
		}
		return MemberSummaryForAdmin.builder()
			.id(member.getId())
			.nickname(member.getNickname())
			.build();
	}
}
