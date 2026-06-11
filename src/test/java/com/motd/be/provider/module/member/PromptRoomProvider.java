package com.motd.be.provider.module.member;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.prompt.entity.PromptRoom;
import com.motd.be.module.member.prompt.repository.PromptRoomRepository;

@Component
public class PromptRoomProvider {

	@Autowired
	private PromptRoomRepository promptRoomRepository;

	public PromptRoom save(Member member) {
		return promptRoomRepository.save(PromptRoom.builder()
			.member(member)
			.build());
	}

	public List<PromptRoom> findAll() {
		return promptRoomRepository.findAll();
	}
}
