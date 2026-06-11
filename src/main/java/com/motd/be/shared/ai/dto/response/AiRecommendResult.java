package com.motd.be.shared.ai.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRecommendResult {

	private boolean matched;
	private String message;
	private List<Recommendation> recommendations;

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Recommendation {
		private Long serviceId;
		private String serviceName;
	}
}
