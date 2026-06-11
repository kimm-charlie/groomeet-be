package com.motd.be.module.director.chat_room.facade;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.director.chat_room.dto.response.ChatRoomFindAllResponseForDirector;
import com.motd.be.module.director.chat_room.dto.response.ChatRoomFindChatRoomServicesResponseForDirector;
import com.motd.be.module.director.chat_room.dto.response.ChatRoomUnreadCountResponseForDirector;
import com.motd.be.module.director.chat_room.service.ChatRoomServiceForDirector;
import com.motd.be.module.director.chat_room_member.service.ChatRoomMemberQueryServiceForDirector;
import com.motd.be.module.director.member.service.MemberQueryServiceForDirector;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomFacadeForDirector {

	private final MemberQueryServiceForDirector memberQueryServiceForDirector;
	private final ChatRoomServiceForDirector chatRoomServiceForDirector;
	private final ChatRoomMemberQueryServiceForDirector chatRoomMemberQueryServiceForDirector;

	public ChatRoomFindAllResponseForDirector findAll(Long memberId, Long directorServiceId, boolean showOnlyUnread,
		String word,
		String status, int page) {
		Member member = memberQueryServiceForDirector.findByIdWithDirector(
			memberId);
		return chatRoomServiceForDirector.findAllForDirector(member, directorServiceId, showOnlyUnread, word, status,
			page);
	}

	public ChatRoomFindChatRoomServicesResponseForDirector findChatRoomServices(Long memberId) {
		Member member = memberQueryServiceForDirector.findByIdWithDirector(memberId);
		List<ChatRoomMember> chatRoomMembers = chatRoomMemberQueryServiceForDirector.findAllByMemberAndIsDirectorTrueWithChatRoom(
			member);
		return ChatRoomFindChatRoomServicesResponseForDirector.from(chatRoomMembers);
	}

	public ChatRoomUnreadCountResponseForDirector countTotalUnreadMessages(Long memberId) {
		Member member = memberQueryServiceForDirector.findByIdWithDirector(memberId);
		int totalUnreadCount = chatRoomMemberQueryServiceForDirector.countTotalUnreadCount(member, Boolean.TRUE);
		return ChatRoomUnreadCountResponseForDirector.from(totalUnreadCount);
	}
}
