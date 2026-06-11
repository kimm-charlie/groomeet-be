package com.motd.be.module.member.member.dto.response;

import com.motd.be.module.member.member.entity.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberPushSettingResponse {

	private Boolean isActivityPushAgreed;
	private Boolean isMarketingPushAgreed;

	public static MemberPushSettingResponse from(Member member) {
		return MemberPushSettingResponse.builder()
			.isMarketingPushAgreed(member.getIsMarketingPushAgreed())
			.isActivityPushAgreed(member.getIsActivityPushAgreed())
			.build();
	}
}
