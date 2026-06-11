package com.motd.be.module.member.member.dto.response;

import java.util.List;

import com.motd.be.module.member.member.entity.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberResponse {

	private Long id;
	private String nickname;
	private String profileImageUrl;
	private Boolean isWithdrawal;

	public static MemberResponse from(Member member) {
		return MemberResponse.builder()
			.id(member.getId())
			.nickname(member.getNickname())
			.profileImageUrl(member.getCdnProfileImageUrl())
			.isWithdrawal(member.isWithdrawn())
			.build();
	}

	public static List<MemberResponse> fromList(List<Member> members) {
		return members.stream()
			.map(MemberResponse::from)
			.toList();
	}
}
