package com.motd.be.shared.ai.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatCompletionResponse {

	private String id;
	private String model;
	private List<Choice> choices;
	private Usage usage;

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Choice {
		private int index;
		private Message message;
		@JsonProperty("finish_reason")
		private String finishReason;
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Message {
		private String role;
		private String content;
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Usage {
		@JsonProperty("prompt_tokens")
		private int promptTokens;
		@JsonProperty("completion_tokens")
		private int completionTokens;
		@JsonProperty("total_tokens")
		private int totalTokens;
	}
}
