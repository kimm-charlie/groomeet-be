package com.motd.be.module.member.chat_room.dto.response;

import java.util.List;

import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.director_service.dto.response.DirectorServiceWithFullNameResponse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomFindChatRoomServicesResponse {

	private List<DirectorServiceWithFullNameResponse> services;

	public static ChatRoomFindChatRoomServicesResponse from(List<ChatRoomMember> chatRoomMembers) {
		return ChatRoomFindChatRoomServicesResponse.builder()
			.services(chatRoomMembers.stream()
				.flatMap(chatRoomMember -> chatRoomMember.getChatRoom()
					.getChatRoomServiceEstimateMappings().stream()
					.map(mapping -> mapping.getServiceEstimate().getServiceRequest().getDirectorService()))
				.map(DirectorServiceWithFullNameResponse::from)
				.toList())
			.build();
	}
}
