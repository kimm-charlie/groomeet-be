package com.motd.be.module.member.chat_message.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_message.repository.ChatMessageRepository;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageCommandService {

	private final ChatMessageRepository chatMessageRepository;

	public ChatMessage save(ChatMessage entity) {
		return chatMessageRepository.save(entity);
	}

	public void deleteChatFilesByChatMessageAndMember(ChatMessage chatMessage, Member member) {
		chatMessageRepository.deleteChatImagesByChatMessageAndMember(chatMessage, member);
	}
}
