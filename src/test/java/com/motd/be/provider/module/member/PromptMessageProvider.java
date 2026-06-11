package com.motd.be.provider.module.member;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.prompt.entity.PromptMessage;
import com.motd.be.module.member.prompt.entity.PromptMessageRole;
import com.motd.be.module.member.prompt.entity.PromptRoom;
import com.motd.be.module.member.prompt.repository.PromptMessageRepository;

@Component
public class PromptMessageProvider {

	@Autowired
	private PromptMessageRepository promptMessageRepository;

	public PromptMessage save(PromptRoom room, PromptMessageRole role, String content) {
		return promptMessageRepository.save(PromptMessage.builder()
			.promptRoom(room)
			.role(role)
			.content(content)
			.build());
	}

	public List<PromptMessage> findAllByRoom(PromptRoom room) {
		return promptMessageRepository.findAllByPromptRoomOrderByIdAsc(room);
	}

	public List<PromptMessage> findAll() {
		return promptMessageRepository.findAll();
	}
}
