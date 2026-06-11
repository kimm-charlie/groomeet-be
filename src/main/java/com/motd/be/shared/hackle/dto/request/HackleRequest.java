package com.motd.be.shared.hackle.dto.request;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HackleRequest {

	private final Long campaignKey;
	private final List<HackleUser> users;

	@Getter
	@Builder
	public static class HackleUser {
		private final String userId;
		private final Map<String, Object> apiProperties;
	}

	public static HackleRequest from(Long campaignKey, List<HackleKakaoRequest.HackleKakaoUser> batch) {
		List<HackleUser> hackleUsers = batch.stream()
			.map(u -> HackleUser.builder()
				.userId(String.valueOf(u.getReceiverId()))
				.apiProperties(u.getVariables())
				.build())
			.toList();

		return HackleRequest.builder()
			.campaignKey(campaignKey)
			.users(hackleUsers)
			.build();
	}
}