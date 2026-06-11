package com.motd.be.module.member.chat_room_member.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ChatRoomMemberException;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.chat_room_member.repository.ChatRoomMemberRepository;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomMemberQueryService {

	private final ChatRoomMemberRepository chatRoomMemberRepository;

	public boolean isMemberInChatRoom(Long memberId, Long chatRoomId) {
		return chatRoomMemberRepository.isMemberInChatRoom(memberId, chatRoomId);
	}

	public ChatRoomMember findByChatRoomAndMember(ChatRoom chatRoom, Member sender) {
		return chatRoomMemberRepository.findByChatRoomAndMember(chatRoom, sender)
			.orElseThrow(() -> new CustomRuntimeException(ChatRoomMemberException.NOT_IN_CHAT_ROOM));
	}

	public Map<Long, Integer> findUnreadCounts(List<ChatRoom> chatRooms, Member member) {
		return chatRoomMemberRepository.findUnreadCounts(chatRooms, member)
			.stream()
			.collect(Collectors.toMap(row -> ((Number)row[0]).longValue(),  // chatRoomId
				row -> ((Number)row[1]).intValue()    // unreadCount
			));
	}

	public int countTotalUnreadCount(Member member, Boolean isForDirector) {
		return Math.toIntExact(chatRoomMemberRepository.countTotalUnreadCount(member, isForDirector));
	}

	/**
	 * 일반 회원용 채팅방 서비스 조회 로직이다.
	 *
	 * @param member
	 * @return
	 */
	public List<ChatRoomMember> findAllByMemberAndIsDirectorFalseWithChatRoom(Member member) {
		return chatRoomMemberRepository.findAllByMemberAndIsDirectorFalseWithChatRoom(member);
	}

	public List<ChatRoomMember> findByChatRoomIdWithChatRoomWithLock(Long chatRoomId) {
		return chatRoomMemberRepository.findByChatRoomIdWithChatRoomWithLock(chatRoomId);
	}

}
