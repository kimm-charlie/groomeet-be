package com.motd.be.module.member.prompt.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.AiProviderException;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.prompt.dto.response.PromptRecommendResult;
import com.motd.be.shared.ai.builder.AiPromptBuilder;
import com.motd.be.shared.ai.dto.request.AiChatCompletionRequest;
import com.motd.be.shared.ai.dto.request.AiChatCompletionRequest.Message;
import com.motd.be.shared.ai.dto.response.AiChatCompletionResponse;
import com.motd.be.shared.ai.dto.response.AiRecommendResult;
import com.motd.be.shared.ai.provider.AiChatProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromptService {

	private final AiChatProvider aiChatProvider;
	private final ObjectMapper objectMapper;

	public String generateRequest(String prompt, DirectorService service, List<String> cdnUrls,
		List<Message> conversationHistory) {
		AiChatCompletionRequest request = AiPromptBuilder.buildServiceRequest(prompt, service, cdnUrls,
			conversationHistory);
		AiChatCompletionResponse response = aiChatProvider.chat(request);

		return response.getChoices().get(0).getMessage().getContent();
	}

	public PromptRecommendResult recommendServices(String prompt, List<DirectorService> allServices,
		List<String> cdnUrls, List<Message> conversationHistory) {
		AiChatCompletionRequest request = AiPromptBuilder.buildServiceRecommend(prompt, allServices, cdnUrls,
			conversationHistory);
		AiChatCompletionResponse response = aiChatProvider.chat(request);

		String content = response.getChoices().get(0).getMessage().getContent();
		AiRecommendResult result = parseRecommendResponse(content);

		return new PromptRecommendResult(result, content);
	}

	private AiRecommendResult parseRecommendResponse(String content) {
		try {
			String json = content.strip();
			if (json.startsWith("```")) {
				json = json.replaceAll("```json?\\s*", "").replaceAll("```\\s*$", "").strip();
			}
			return objectMapper.readValue(json, new TypeReference<>() {
			});
		} catch (JsonProcessingException e) {
			throw new CustomRuntimeException(AiProviderException.RECOMMEND_PARSE_FAILED);
		}
	}
}
