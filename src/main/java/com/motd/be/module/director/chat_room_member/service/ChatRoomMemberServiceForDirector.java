package com.motd.be.module.director.chat_room_member.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomMemberServiceForDirector {

	private final ChatRoomMemberCommandServiceForDirector chatRoomMemberCommandService;
	private final ChatRoomMemberQueryServiceForDirector chatRoomMemberQueryService;

	public List<ChatRoomMember> saveOrFind(ChatRoom chatRoom, Member director, Member receiver) {
		List<ChatRoomMember> existingMembers = chatRoomMemberQueryService.findAllByChatRoomId(chatRoom);

		if (!existingMembers.isEmpty()) {
			ChatRoomMember directorMember = existingMembers.stream()
				.filter(ChatRoomMember::getIsDirector)
				.findFirst()
				.orElseThrow();

			ChatRoomMember receiverMember = existingMembers.stream()
				.filter(m -> !m.getIsDirector())
				.findFirst()
				.orElseThrow();

			return List.of(receiverMember, directorMember);
		}

		ChatRoomMember receiverMember = chatRoomMemberCommandService.save(
			ChatRoomMember.builder().chatRoom(chatRoom).member(receiver).isChatRoomDeleted(false).build());

		ChatRoomMember directorMember = chatRoomMemberCommandService.save(ChatRoomMember.builder()
			.chatRoom(chatRoom)
			.member(director)
			.isDirector(true)
			.isChatRoomDeleted(false)
			.build());

		return List.of(receiverMember, directorMember);
	}
}
