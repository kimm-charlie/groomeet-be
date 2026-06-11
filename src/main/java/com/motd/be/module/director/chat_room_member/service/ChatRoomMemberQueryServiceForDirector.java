package com.motd.be.module.director.chat_room_member.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ChatRoomMemberException;
import com.motd.be.module.director.chat_room_member.repository.ChatRoomMemberRepositoryForDirector;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomMemberQueryServiceForDirector {

	private final ChatRoomMemberRepositoryForDirector chatRoomMemberRepositoryForDirector;

	public List<ChatRoomMember> findAllByChatRoomId(ChatRoom chatRoom) {
		return chatRoomMemberRepositoryForDirector.findAllByChatRoomId(chatRoom);
	}

	public int countTotalUnreadCount(Member member, Boolean isForDirector) {
		return Math.toIntExact(chatRoomMemberRepositoryForDirector.countTotalUnreadCount(member, isForDirector));
	}

	public List<ChatRoomMember> findAllByMemberAndIsDirectorTrueWithChatRoom(Member member) {
		return chatRoomMemberRepositoryForDirector.findAllByMemberAndIsDirectorTrueWithChatRoom(member);
	}

	public Map<Long, Integer> findUnreadCounts(List<ChatRoom> chatRooms, Member member) {
		return chatRoomMemberRepositoryForDirector.findUnreadCounts(chatRooms, member)
			.stream()
			.collect(Collectors.toMap(row -> ((Number)row[0]).longValue(),  // chatRoomId
				row -> ((Number)row[1]).intValue()    // unreadCount
			));
	}

	public ChatRoomMember findByChatRoomAndMember(ChatRoom chatRoom, Member sender) {
		return chatRoomMemberRepositoryForDirector.findByChatRoomAndMember(chatRoom, sender)
			.orElseThrow(() -> new CustomRuntimeException(ChatRoomMemberException.NOT_IN_CHAT_ROOM));
	}

}
