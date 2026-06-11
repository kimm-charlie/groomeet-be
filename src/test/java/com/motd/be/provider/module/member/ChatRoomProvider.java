package com.motd.be.provider.module.member;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room.repository.ChatRoomRepository;

@Component
public class ChatRoomProvider {

	@Autowired
	private ChatRoomRepository chatRoomRepository;

	public List<ChatRoom> findAll() {
		return chatRoomRepository.findAll();
	}

	public ChatRoom save() {
		return chatRoomRepository.save(ChatRoom.builder()
			.build());
	}

	public ChatRoom saveWithPaidTrue() {
		return chatRoomRepository.save(ChatRoom.builder()
			.isDirectorPaid(Boolean.TRUE)
			.build());
	}

	public ChatRoom saveWithIsDeletedTrue() {
		return chatRoomRepository.save(ChatRoom.builder()
			.isDeleted(true)
			.build());
	}

	public ChatRoom findById(Long id) {
		return chatRoomRepository.findById(id).orElseThrow();
	}
}
