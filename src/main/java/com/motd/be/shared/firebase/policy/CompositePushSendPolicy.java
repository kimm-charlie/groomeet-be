package com.motd.be.shared.firebase.policy;

import java.util.List;

import lombok.Builder;

@Builder
public class CompositePushSendPolicy implements PushSendPolicy {

	private List<PushSendPolicy> policies;

	@Override
	public boolean canSend(PushContext context) {
		return policies.stream().allMatch(p -> p.canSend(context));
	}

	public static CompositePushSendPolicy of(List<PushSendPolicy> policies) {
		return CompositePushSendPolicy.builder()
			.policies(policies)
			.build();
	}
}

