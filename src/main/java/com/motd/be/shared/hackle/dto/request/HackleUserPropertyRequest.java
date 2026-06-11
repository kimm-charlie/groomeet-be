package com.motd.be.shared.hackle.dto.request;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HackleUserPropertyRequest {

	private String userId;
	private String deviceId;
	@Builder.Default
	private Operations operations = Operations.builder().build();

	public static HackleUserPropertyRequest of(String memberId, String deviceToken, Map<String, Object> setProperties) {
		return HackleUserPropertyRequest.builder()
			.userId(memberId)
			.deviceId(deviceToken)
			.operations(Operations.builder()
				.set(setProperties)
				.build())
			.build();
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Operations {

		@JsonProperty("$set")
		private Map<String, Object> set;
		@JsonProperty("$setOnce")
		private Map<String, Object> setOnce;
		@JsonProperty("$unset")
		private Map<String, Object> unset;
	}
}

