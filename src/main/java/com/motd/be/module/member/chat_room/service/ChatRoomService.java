package com.motd.be.module.member.chat_room.service;

import static com.motd.be.common.constants.PageSizeConstants.*;

import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.module.member.chat_room.dto.response.ChatRoomFindAllResponse;
import com.motd.be.module.member.chat_room.dto.response.ChatRoomLeaveEvent;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room.validator.ChatRoomValidator;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.chat_room_member.service.ChatRoomMemberQueryService;
import com.motd.be.module.member.chat_room_member.validator.ChatRoomMemberValidator;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.Role;
import com.motd.be.module.member.sse.SseEventType;
import com.motd.be.redis.domain.brocker.SseEventPublisher;
import com.motd.be.redis.domain.payload.SsePayload;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

	private final ChatRoomQueryService chatRoomQueryService;
	private final ChatRoomMemberValidator chatRoomMemberValidator;
	private final ChatRoomMemberQueryService chatRoomMemberQueryService;
	private final SseEventPublisher sseEventPublisher;
	private final ChatRoomValidator chatRoomValidator;

	public ChatRoom findByIdWithMemberValidation(Long chatRoomId, Long memberId) {
		chatRoomMemberValidator.validateMemberInChatRoom(memberId, chatRoomId);
		return chatRoomQueryService.findByIdWithChatRoomMember(chatRoomId);
	}

	public ChatRoomFindAllResponse findAllForPublic(Member member, Long directorServiceId, boolean showOnlyUnread,
		String word, String status, int page) {
		Pageable pageable = PageRequest.of(page, CHAT_ROOM_FIND_ALL_SIZE);

		// 1. chatRoom 조회
		Slice<ChatRoom> chatRooms = chatRoomQueryService.findAllForPublic(member, directorServiceId, showOnlyUnread,
			word, status, pageable);

		// 2. 채팅방별 안읽은 메세지 개수 계산
		Map<Long, Integer> unreadCountMap = chatRoomMemberQueryService.findUnreadCounts(chatRooms.getContent(), member);

		return ChatRoomFindAllResponse.of(chatRooms, unreadCountMap, member);
	}

	public ChatRoom findDetail(Member member, Long chatRoomId) {
		// 권한 검증
		chatRoomMemberValidator.validateMemberInChatRoom(member.getId(), chatRoomId);

		return chatRoomQueryService.findByIdWithChatRoomMemberAndServiceRequest(chatRoomId);
	}

	public void publishChatRoomLeaveEvent(Long memberId, Role memberRole, Long chatRoomId) {
		sseEventPublisher.publishSseEvent(
			SsePayload.of(SseEventType.LEAVE_CHAT_ROOM, memberId, memberRole, ChatRoomLeaveEvent.of(chatRoomId)));
	}

	public ChatRoom validateToUseCashForChatStart(Member member, Long referenceId) {
		// 채팅방 조회
		ChatRoom chatRoom = chatRoomQueryService.findByIdWithIsDeletedFalse(referenceId);

		// 채팅방에 이미 참여중인지 검증
		chatRoomMemberValidator.validateMemberInChatRoom(member.getId(), chatRoom.getId());

		// 채팅방이 아직 결제되지 않았는지 검증
		chatRoomValidator.notYetPaid(chatRoom);

		return chatRoom;
	}

	public void updateChatRoomStatusAfterChatStartPaid(ChatRoom chatRoom) {
		chatRoom.updateChatRoomStatusAfterChatStartPaid();
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
