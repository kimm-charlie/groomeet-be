package com.motd.be.module.director.chat_room.service;

import static com.motd.be.common.constants.PageSizeConstants.*;

import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.module.director.chat_room.dto.response.ChatRoomFindAllResponseForDirector;
import com.motd.be.module.director.chat_room_member.service.ChatRoomMemberQueryServiceForDirector;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room.validator.ChatRoomValidator;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.chat_room_member.validator.ChatRoomMemberValidator;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceForDirector {

	private final ChatRoomCommandServiceForDirector chatRoomCommandServiceForDirector;
	private final ChatRoomQueryServiceForDirector chatRoomQueryServiceForDirector;
	private final ChatRoomMemberValidator chatRoomMemberValidator;
	private final ChatRoomMemberQueryServiceForDirector chatRoomMemberQueryServiceForDirector;
	private final ChatRoomValidator chatRoomValidator;

	public ChatRoom saveOrFind(Member director, Member member) {
		return chatRoomQueryServiceForDirector.findByDirectorAndMemberWithChatMemberOptional(director, member)
			.orElseGet(() -> chatRoomCommandServiceForDirector.save(ChatRoom.builder()
				.isDirectorPaid(Boolean.FALSE)
				.build()));
	}

	public ChatRoom findByIdWithMemberValidation(Long chatRoomId, Long memberId) {
		chatRoomMemberValidator.validateMemberInChatRoom(memberId, chatRoomId);
		return chatRoomQueryServiceForDirector.findByIdWithChatRoomMember(chatRoomId);
	}

	public ChatRoomFindAllResponseForDirector findAllForDirector(Member member, Long directorServiceId,
		boolean showOnlyUnread,
		String word, String status, int page) {
		Pageable pageable = PageRequest.of(page, CHAT_ROOM_FIND_ALL_SIZE);

		// 1. chatRoom 조회
		Slice<ChatRoom> chatRooms = chatRoomQueryServiceForDirector.findAllForDirector(member, directorServiceId,
			showOnlyUnread,
			word, status, pageable);

		// 2. 채팅방별 안읽은 메세지 개수 계산
		Map<Long, Integer> unreadCountMap = chatRoomMemberQueryServiceForDirector.findUnreadCounts(
			chatRooms.getContent(), member);

		return ChatRoomFindAllResponseForDirector.of(chatRooms, unreadCountMap, member);
	}

	public void validatePaymentRequirement(ChatRoom chatRoom, Member sender) {
		if (chatRoom.isDirectorPaid()) {
			return;
		}

		ChatRoomMember senderChatRoomMember = chatRoom.getChatRoomMember(sender);

		if (senderChatRoomMember.getIsDirector()) {
			chatRoomValidator.validateChatAvailabilityForDirector(chatRoom);
		}
	}
}
