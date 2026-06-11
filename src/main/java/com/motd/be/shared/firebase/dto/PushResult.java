package com.motd.be.shared.firebase.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PushResult {

	private int successCount;
	private int failureCount;

	public static PushResult of(int successCount, int failureCount) {
		return PushResult.builder()
			.successCount(successCount)
			.failureCount(failureCount)
			.build();
	}
}
