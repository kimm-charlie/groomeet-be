package com.motd.be.module.member.chat_file.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.chat_file.entity.ChatFile;
import com.motd.be.module.member.chat_file.repository.ChatFileRepository;
import com.motd.be.module.member.chat_message.entity.ChatMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatFileCommandService {

	private final ChatFileRepository chatFileRepository;

	public ChatFile save(ChatFile chatFile) {
		return chatFileRepository.save(chatFile);
	}
	
	public void mapChatMessageByIds(ChatMessage chatMessage, List<ChatFile> chatFiles) {
		chatFileRepository.mapChatMessageByIds(chatMessage, chatFiles);
	}
}
