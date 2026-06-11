package com.motd.be.module.director.chat_room.dto.response;

import java.util.List;

import com.motd.be.module.director.director_service.dto.response.DirectorServiceWithFullNameResponseForDirector;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomFindChatRoomServicesResponseForDirector {

	private List<DirectorServiceWithFullNameResponseForDirector> services;

	public static ChatRoomFindChatRoomServicesResponseForDirector from(List<ChatRoomMember> chatRoomMembers) {
		return ChatRoomFindChatRoomServicesResponseForDirector.builder()
			.services(chatRoomMembers.stream()
				.flatMap(chatRoomMember -> chatRoomMember.getChatRoom()
					.getChatRoomServiceEstimateMappings().stream()
					.map(mapping -> mapping.getServiceEstimate().getServiceRequest().getDirectorService()))
				.map(DirectorServiceWithFullNameResponseForDirector::from)
				.toList())
			.build();
	}
}
