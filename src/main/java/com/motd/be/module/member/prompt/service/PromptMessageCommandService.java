package com.motd.be.module.member.prompt.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.prompt.entity.PromptMessage;
import com.motd.be.module.member.prompt.entity.PromptRoom;
import com.motd.be.module.member.prompt.repository.PromptMessageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PromptMessageCommandService {

	private final PromptMessageRepository promptMessageRepository;

	public PromptMessage save(PromptMessage promptMessage) {
		return promptMessageRepository.save(promptMessage);
	}

	public List<PromptMessage> findAllByRoom(PromptRoom promptRoom) {
		return promptMessageRepository.findAllByPromptRoomOrderByIdAsc(promptRoom);
	}
}
