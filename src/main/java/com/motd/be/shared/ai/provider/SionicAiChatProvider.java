package com.motd.be.shared.ai.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.AiProviderException;
import com.motd.be.common.config.SionicAiProperties;
import com.motd.be.shared.ai.dto.request.AiChatCompletionRequest;
import com.motd.be.shared.ai.dto.response.AiChatCompletionResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.provider", havingValue = "sionic")
public class SionicAiChatProvider implements AiChatProvider {

	private static final Logger log = LoggerFactory.getLogger(SionicAiChatProvider.class);
	private static final String CHAT_COMPLETIONS_PATH = "/v1/chat/completions";

	private final RestTemplate restTemplate;
	private final SionicAiProperties sionicAiProperties;

	@Override
	public AiChatCompletionResponse chat(AiChatCompletionRequest request) {
		String url = sionicAiProperties.getBaseUrl() + CHAT_COMPLETIONS_PATH;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(sionicAiProperties.getApiKey());

		AiChatCompletionRequest requestWithModel = AiChatCompletionRequest.builder()
			.model(sionicAiProperties.getModel())
			.messages(request.getMessages())
			.build();

		HttpEntity<AiChatCompletionRequest> entity = new HttpEntity<>(requestWithModel, headers);

		try {
			AiChatCompletionResponse response = restTemplate.postForObject(url, entity, AiChatCompletionResponse.class);

			if (response == null) {
				log.error("Sionic AI 응답 null - model: {}, messageCount: {}", sionicAiProperties.getModel(),
					request.getMessages().size());
				throw new CustomRuntimeException(AiProviderException.API_CALL_FAILED, "AI Provider 응답이 null입니다.");
			}

			return response;
		} catch (RestClientException e) {
			log.error("Sionic AI API 호출 실패 - model: {}, messageCount: {}",
				sionicAiProperties.getModel(), request.getMessages().size(), e);
			throw new CustomRuntimeException(AiProviderException.API_CALL_FAILED, "AI Provider 호출 실패: " + e.getMessage());
		}
	}
}
