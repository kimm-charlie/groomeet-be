package com.motd.be.module.director.chat_message.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.chat_message.repository.ChatMessageRepositoryForDirector;
import com.motd.be.module.member.chat_message.entity.ChatMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageCommandServiceForDirector {

	private final ChatMessageRepositoryForDirector chatMessageRepositoryForDirector;

	public ChatMessage save(ChatMessage entity) {
		return chatMessageRepositoryForDirector.save(entity);
	}
}
