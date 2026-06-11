package com.motd.be.module.member.prompt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.AiProviderException;
import com.motd.be.module.member.prompt.entity.PromptRoom;
import com.motd.be.module.member.prompt.repository.PromptRoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PromptRoomCommandService {

	private final PromptRoomRepository promptRoomRepository;

	public PromptRoom save(PromptRoom promptRoom) {
		return promptRoomRepository.save(promptRoom);
	}

	public PromptRoom findById(Long id) {
		return promptRoomRepository.findById(id)
			.orElseThrow(() -> new CustomRuntimeException(AiProviderException.ROOM_NOT_FOUND));
	}
}
