package com.motd.be.module.member.chat_room.processor;

import java.util.List;

import org.springframework.stereotype.Component;

import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room.service.ChatRoomService;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.chat_room_member.service.ChatRoomMemberService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.Role;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatRoomProcessor {

	private final ChatRoomMemberService chatRoomMemberService;
	private final ChatRoomService chatRoomService;

	/**
	 * 채팅방에서 회원을 나가게 처리하고 필요시 채팅방을 삭제하며 SSE 이벤트를 발행합니다.
	 *
	 * @param member          나갈 회원
	 * @param chatRoomMembers 채팅방 회원 목록
	 * @param memberId        나갈 회원 ID (SSE 이벤트용)
	 */
	public void processLeave(Member member, List<ChatRoomMember> chatRoomMembers, Long memberId) {
		Role memberRole = chatRoomMembers.stream()
			.filter(chatRoomMember -> chatRoomMember.getMember().getId().equals(member.getId()))
			.findFirst()
			.map(chatRoomMember -> Boolean.TRUE.equals(chatRoomMember.getIsDirector())
				? Role.DIRECTOR
				: Role.MEMBER)
			.orElse(Role.MEMBER);

		// 채팅방 회원 검증 및 삭제처리
		chatRoomMemberService.validateAndDeleteChatRoomMember(member, chatRoomMembers);

		// SSE 알림 전파
		if (!chatRoomMembers.isEmpty()) {
			ChatRoom chatRoom = chatRoomMembers.get(0).getChatRoom();
			chatRoomService.publishChatRoomLeaveEvent(memberId, memberRole, chatRoom.getId());
		}
	}
}
