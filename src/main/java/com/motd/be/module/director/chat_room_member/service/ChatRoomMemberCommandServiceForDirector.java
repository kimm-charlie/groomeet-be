package com.motd.be.module.director.chat_room_member.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.chat_room_member.repository.ChatRoomMemberRepositoryForDirector;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomMemberCommandServiceForDirector {

	private final ChatRoomMemberRepositoryForDirector chatRoomMemberRepositoryForDirector;

	public ChatRoomMember save(ChatRoomMember entity) {
		return chatRoomMemberRepositoryForDirector.save(entity);
	}
}
