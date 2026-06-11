package com.motd.be.module.member.chat_message.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ChatMessageException;
import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_message.repository.ChatMessageQueryDslRepository;
import com.motd.be.module.member.chat_message.repository.ChatMessageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageQueryService {

	private final ChatMessageRepository chatMessageRepository;
	private final ChatMessageQueryDslRepository chatMessageQueryDslRepository;

	public Slice<ChatMessage> findAllByChatRoomId(Long chatRoomId, Long lastMessageId, Pageable pageable,
		Long viewerId) {
		return chatMessageQueryDslRepository.findAllByChatRoomId(chatRoomId, lastMessageId, pageable, viewerId);
	}

	public boolean validateChatMessageBelongsToChatRoom(Long lastMessageId, Long chatRoomId) {
		return chatMessageRepository.validateChatMessageBelongsToChatRoom(lastMessageId, chatRoomId);

	}

	public ChatMessage findByIdWithChatRoomMember(Long chatMessageId) {
		return chatMessageRepository.findByIdWithChatRoomMember(chatMessageId)
			.orElseThrow(() -> new CustomRuntimeException(ChatMessageException.NOT_FOUND));
	}
}
