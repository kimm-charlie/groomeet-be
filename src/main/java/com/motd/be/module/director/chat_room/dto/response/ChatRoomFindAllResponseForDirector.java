package com.motd.be.module.director.chat_room.dto.response;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.member.entity.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomFindAllResponseForDirector {

	private int page;
	private Boolean hasNext;
	private List<ChatRoomResponseForDirector> chatRooms;

	public static ChatRoomFindAllResponseForDirector of(Slice<ChatRoom> chatRooms, Map<Long, Integer> unreadCountMap,
		Member member) {
		return ChatRoomFindAllResponseForDirector.builder()
			.page(chatRooms.getNumber())
			.hasNext(chatRooms.hasNext())
			.chatRooms(
				ChatRoomResponseForDirector.ofList(chatRooms.getContent(), unreadCountMap, member))
			.build();
	}
}
