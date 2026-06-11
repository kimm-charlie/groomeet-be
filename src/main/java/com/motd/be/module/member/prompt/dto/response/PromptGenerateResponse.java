package com.motd.be.module.member.prompt.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PromptGenerateResponse {

	private Long roomId;
	private String aiContent;

	public static PromptGenerateResponse from(Long roomId, String aiContent) {
		return PromptGenerateResponse.builder()
			.roomId(roomId)
			.aiContent(aiContent)
			.build();
	}
}