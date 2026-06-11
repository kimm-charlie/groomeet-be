package com.motd.be.redis.domain.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SseConnectionEventData {
	
	private Long memberId;

	public static SseConnectionEventData from(Long memberId) {
		return SseConnectionEventData.builder()
			.memberId(memberId)
			.build();
	}
}
