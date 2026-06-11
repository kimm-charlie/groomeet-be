package com.motd.be.provider.module.member;

import static com.motd.be.Constants.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_message.entity.ChatMessageType;
import com.motd.be.module.member.chat_message.repository.ChatMessageRepository;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

@Component
public class ChatMessageProvider {

	@Autowired
	private ChatMessageRepository chatMessageRepository;

	public List<ChatMessage> findAll() {
		return chatMessageRepository.findAll();
	}

	public void deleteAll() {
		chatMessageRepository.deleteAll();
	}

	public ChatMessage saveTextType(ChatRoom chatRoom, ChatRoomMember chatRoomMember, LocalDateTime sendAt) {
		return chatMessageRepository.save(ChatMessage.builder()
			.chatRoom(chatRoom)
			.chatRoomMember(chatRoomMember)
			.content(CONTENT_STR)
			.messageType(ChatMessageType.TEXT)
			.sendAt(sendAt)
			.build());
	}

	public ChatMessage saveImageType(ChatRoom chatRoom, ChatRoomMember chatRoomMember, LocalDateTime sendAt) {
		return chatMessageRepository.save(ChatMessage.builder()
			.chatRoom(chatRoom)
			.chatRoomMember(chatRoomMember)
			.content(null)
			.messageType(ChatMessageType.IMAGE)
			.sendAt(sendAt)
			.build());
	}

	public ChatMessage saveEstimateType(ChatRoom chatRoom, ChatRoomMember chatRoomMember, ServiceEstimate estimate,
		LocalDateTime sendAt) {
		return chatMessageRepository.save(ChatMessage.builder()
			.chatRoom(chatRoom)
			.chatRoomMember(chatRoomMember)
			.content(null)
			.messageType(ChatMessageType.ESTIMATE)
			.serviceEstimate(estimate)
			.sendAt(sendAt)
			.build());
	}

	public ChatMessage saveWithIsDeletedTrue(ChatRoom chatRoom, ChatRoomMember chatRoomMemberForDirector,
		LocalDateTime localDateTime) {
		return chatMessageRepository.save(ChatMessage.builder()
			.chatRoom(chatRoom)
			.chatRoomMember(chatRoomMemberForDirector)
			.content(CONTENT_STR)
			.messageType(ChatMessageType.TEXT)
			.isDeleted(true)
			.sendAt(localDateTime)
			.build());
	}

	public ChatMessage findById(Long id) {
		return chatMessageRepository.findById(id).orElse(null);
	}

	public ChatMessage saveWithIsVisibleToOpponentFalse(ChatRoom chatRoom, ChatRoomMember chatRoomMember) {
		return chatMessageRepository.save(ChatMessage.builder()
			.chatRoom(chatRoom)
			.chatRoomMember(chatRoomMember)
			.content(CONTENT_STR)
			.messageType(ChatMessageType.TEXT)
			.sendAt(LocalDateTime.now())
			.isVisibleToOpponent(false)
			.build());
	}

}
