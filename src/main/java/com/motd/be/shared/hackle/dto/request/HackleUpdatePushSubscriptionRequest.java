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
public class HackleUpdatePushSubscriptionRequest {

	private String userId;
	private HackleSubscriptionStatus marketingSubscriptionStatus;
	private HackleSubscriptionStatus informationSubscriptionStatus;

	public static HackleUpdatePushSubscriptionRequest from(Member member) {
		return HackleUpdatePushSubscriptionRequest.builder()
			.userId(String.valueOf(member.getId()))
			.marketingSubscriptionStatus(member.getIsMarketingPushAgreed() ? HackleSubscriptionStatus.SUBSCRIBED :
				HackleSubscriptionStatus.UNSUBSCRIBED)
			.informationSubscriptionStatus(member.getIsActivityPushAgreed() ? HackleSubscriptionStatus.SUBSCRIBED :
				HackleSubscriptionStatus.UNSUBSCRIBED)
			.build();
	}
}
