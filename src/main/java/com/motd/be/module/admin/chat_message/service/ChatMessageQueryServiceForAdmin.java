package com.motd.be.module.admin.chat_message.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.module.admin.chat_message.repository.ChatMessageQueryDslRepositoryForAdmin;
import com.motd.be.module.member.chat_message.entity.ChatMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageQueryServiceForAdmin {

	private final ChatMessageQueryDslRepositoryForAdmin chatMessageQueryDslRepositoryForAdmin;

	public Slice<ChatMessage> findAllByChatRoomId(Long chatRoomId, Long lastMessageId, Pageable pageable) {
		return chatMessageQueryDslRepositoryForAdmin.findAllByChatRoomId(chatRoomId, lastMessageId, pageable);
	}
}
