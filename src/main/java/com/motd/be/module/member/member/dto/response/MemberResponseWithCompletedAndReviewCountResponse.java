package com.motd.be.module.member.member.dto.response;

import com.motd.be.module.member.member.entity.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberResponseWithCompletedAndReviewCountResponse {

	private Long id;
	private String nickname;
	private String profileImageUrl;
	private Integer completedEstimateCount;
	private Integer reviewCount;
	private Boolean isWithdrawal;

	public static MemberResponseWithCompletedAndReviewCountResponse from(Member member) {
		return MemberResponseWithCompletedAndReviewCountResponse.builder()
			.id(member.getId())
			.nickname(member.getNickname())
			.profileImageUrl(member.getCdnProfileImageUrl())
			.completedEstimateCount(member.getDirectorInfo().getCompletedEstimateCount())
			.reviewCount(member.getDirectorInfo().getReviewCount())
			.isWithdrawal(member.getIsWithdrawal())
			.build();
	}
}
