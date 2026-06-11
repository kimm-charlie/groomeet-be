package com.motd.be.module.member.chat_room.dto.response;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.member.entity.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomFindAllResponse {

	private int page;
	private Boolean hasNext;
	private List<ChatRoomResponse> chatRooms;

	public static ChatRoomFindAllResponse of(Slice<ChatRoom> chatRooms, Map<Long, Integer> unreadCountMap,
		Member member) {
		return ChatRoomFindAllResponse.builder()
			.page(chatRooms.getNumber())
			.hasNext(chatRooms.hasNext())
			.chatRooms(
				ChatRoomResponse.ofList(chatRooms.getContent(), unreadCountMap, member))
			.build();
	}
}
