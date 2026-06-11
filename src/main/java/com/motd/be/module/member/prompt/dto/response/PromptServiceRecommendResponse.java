package com.motd.be.module.member.prompt.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PromptServiceRecommendResponse {

	private Long roomId;
	private boolean matched;
	private String message;
	private List<ServiceRecommendation> recommendations;

	public static PromptServiceRecommendResponse ofMatched(Long roomId, List<ServiceRecommendation> recommendations) {
		return PromptServiceRecommendResponse.builder()
			.roomId(roomId)
			.matched(true)
			.recommendations(recommendations)
			.build();
	}

	public static PromptServiceRecommendResponse ofUnmatched(Long roomId, String message) {
		return PromptServiceRecommendResponse.builder()
			.roomId(roomId)
			.matched(false)
			.message(message)
			.recommendations(List.of())
			.build();
	}

	@Getter
	@Builder
	public static class ServiceRecommendation {
		private Long serviceId;
		private String serviceName;

		public static ServiceRecommendation of(Long serviceId, String serviceName) {
			return ServiceRecommendation.builder()
				.serviceId(serviceId)
				.serviceName(serviceName)
				.build();
		}
	}
}
