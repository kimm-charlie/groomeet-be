package com.motd.be.module.member.chat_room_member.validator;

import java.util.List;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ChatRoomMemberException;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.chat_room_member.service.ChatRoomMemberQueryService;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatRoomMemberValidator {

	private final ChatRoomMemberQueryService chatRoomMemberQueryService;

	public void validateMemberInChatRoom(Long memberId, Long chatRoomId) {
		if (!chatRoomMemberQueryService.isMemberInChatRoom(memberId, chatRoomId)) {
			throw new CustomRuntimeException(ChatRoomMemberException.NOT_IN_CHAT_ROOM);
		}
	}

	public ChatRoomMember validateMemberInChatRoomMembers(Member member, List<ChatRoomMember> chatRoomMembers) {
		if (chatRoomMembers == null || chatRoomMembers.isEmpty()) {
			throw new CustomRuntimeException(ChatRoomMemberException.NOT_FOUND_IN_CHAT_ROOM);
		}

		// 채팅방 회원 검증
		return chatRoomMembers.stream()
			.filter(m -> m.getMember().getId().equals(member.getId()))
			.findFirst()
			.orElseThrow(() -> new CustomRuntimeException(ChatRoomMemberException.NOT_FOUND_IN_CHAT_ROOM));
	}
}
