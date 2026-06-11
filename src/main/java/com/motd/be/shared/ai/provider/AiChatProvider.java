package com.motd.be.shared.ai.provider;

import com.motd.be.shared.ai.dto.request.AiChatCompletionRequest;
import com.motd.be.shared.ai.dto.response.AiChatCompletionResponse;

public interface AiChatProvider {

	AiChatCompletionResponse chat(AiChatCompletionRequest request);
}
