package com.motd.be.module.member.member.dto.response;

import com.motd.be.module.member.member.entity.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberReferralCodeResponse {

	private Long id;
	private String referralCode;

	public static MemberReferralCodeResponse from(Member member) {
		return MemberReferralCodeResponse.builder()
			.id(member.getId())
			.referralCode(member.getReferralCode())
			.build();
	}
}
