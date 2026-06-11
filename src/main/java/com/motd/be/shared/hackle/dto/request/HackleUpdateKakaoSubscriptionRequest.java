package com.motd.be.shared.hackle.dto.request;

import com.motd.be.module.member.member.entity.Member;

import io.hackle.sdk.common.subscription.HackleSubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HackleUpdateKakaoSubscriptionRequest {

	private String userId;
	private HackleSubscriptionStatus marketingSubscriptionStatus;
	private HackleSubscriptionStatus informationSubscriptionStatus;

	public static HackleUpdateKakaoSubscriptionRequest from(Member member) {
		return HackleUpdateKakaoSubscriptionRequest.builder()
			.userId(String.valueOf(member.getId()))
			.informationSubscriptionStatus(HackleSubscriptionStatus.SUBSCRIBED)
			.build();
	}
}
