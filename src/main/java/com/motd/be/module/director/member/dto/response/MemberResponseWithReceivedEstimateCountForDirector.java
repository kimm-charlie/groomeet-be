package com.motd.be.module.director.member.dto.response;

import com.motd.be.module.member.member.entity.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberResponseWithReceivedEstimateCountForDirector {

	private Long id;
	private String nickname;
	private String profileImageUrl;
	private Integer receivedEstimateCount;

	public static MemberResponseWithReceivedEstimateCountForDirector of(Member member, Integer receivedEstimateCount) {
		return MemberResponseWithReceivedEstimateCountForDirector.builder()
			.id(member.getId())
			.nickname(member.getNickname())
			.profileImageUrl(member.getCdnProfileImageUrl())
			.receivedEstimateCount(receivedEstimateCount)
			.build();
	}
}
