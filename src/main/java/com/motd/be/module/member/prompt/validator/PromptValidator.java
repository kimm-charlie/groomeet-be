package com.motd.be.module.member.prompt.validator;

import static com.motd.be.common.constants.ValidationConstants.*;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.AiProviderException;
import com.motd.be.module.member.prompt.entity.PromptRoom;

@Component
public class PromptValidator {

	public void validatePrompt(String prompt) {
		if (prompt == null || prompt.isBlank()) {
			throw new CustomRuntimeException(AiProviderException.PROMPT_REQUIRED);
		}
	}

	public void validateRoomOwnership(PromptRoom room, Long memberId) {
		if (!room.getMember().getId().equals(memberId)) {
			throw new CustomRuntimeException(AiProviderException.ROOM_ACCESS_DENIED);
		}
	}

	public void validateMaxTurns(PromptRoom room) {
		if (room.getTurnCount() >= MAX_PROMPT_ROOM_TURN_COUNT) {
			throw new CustomRuntimeException(AiProviderException.MAX_TURNS_EXCEEDED);
		}
	}
}
