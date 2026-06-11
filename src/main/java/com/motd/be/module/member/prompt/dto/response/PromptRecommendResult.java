package com.motd.be.module.member.prompt.dto.response;

import com.motd.be.shared.ai.dto.response.AiRecommendResult;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PromptRecommendResult {

	private final AiRecommendResult parsedResult;
	private final String rawAiContent;
}
