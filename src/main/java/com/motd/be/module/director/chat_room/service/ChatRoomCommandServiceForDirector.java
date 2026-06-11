package com.motd.be.module.director.chat_room.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room.repository.ChatRoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomCommandServiceForDirector {

	private final ChatRoomRepository chatRoomRepository;

	public ChatRoom save(ChatRoom entity) {
		return chatRoomRepository.save(entity);
	}
}
