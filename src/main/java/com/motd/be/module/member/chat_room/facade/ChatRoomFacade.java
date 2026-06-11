package com.motd.be.module.member.chat_room.facade;

import java.util.List;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_message.service.ChatMessageService;
import com.motd.be.module.member.chat_room.dto.response.ChatRoomFindAllResponse;
import com.motd.be.module.member.chat_room.dto.response.ChatRoomFindChatRoomServicesResponse;
import com.motd.be.module.member.chat_room.dto.response.ChatRoomFindDetailResponse;
import com.motd.be.module.member.chat_room.dto.response.ChatRoomUnreadCountResponse;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room.processor.ChatRoomProcessor;
import com.motd.be.module.member.chat_room.service.ChatRoomService;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.chat_room_member.service.ChatRoomMemberQueryService;
import com.motd.be.module.member.chat_room_member.service.ChatRoomMemberService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.Role;
import com.motd.be.module.member.member.service.MemberQueryService;
import com.motd.be.module.member.sse.SseEventType;
import com.motd.be.redis.domain.brocker.SseEventPublisher;
import com.motd.be.redis.domain.payload.SsePayload;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomFacade {

	private final MemberQueryService memberQueryService;
	private final ChatRoomService chatRoomService;
	private final ChatRoomMemberQueryService chatRoomMemberQueryService;
	private final ChatMessageService chatMessageService;
	private final ChatRoomMemberService chatRoomMemberService;
	private final ChatRoomProcessor chatRoomLeaveProcessor;
	private final SseEventPublisher sseEventPublisher;

	public ChatRoomFindAllResponse findAllForPublic(Long memberId, Long directorServiceId, boolean showOnlyUnread,
		String word, String status, int page) {
		//1. 회원조회
		Member member = memberQueryService.findById(memberId);

		//2. 나가지 않은 전체 채팅방 조회
		return chatRoomService.findAllForPublic(member, directorServiceId, showOnlyUnread, word, status, page);
	}

	public ChatRoomFindChatRoomServicesResponse findChatRoomServicesForPublic(Long memberId) {
		// 1. 회원조회
		Member member = memberQueryService.findById(memberId);

		// 2. 채팅방 회원 조회
		List<ChatRoomMember> chatRoomMembers = chatRoomMemberQueryService.findAllByMemberAndIsDirectorFalseWithChatRoom(
			member);

		// 3. 응답생성
		return ChatRoomFindChatRoomServicesResponse.from(chatRoomMembers);
	}

	@Transactional
	public ChatRoomFindDetailResponse findDetail(Long lastMessageId, Long memberId, Long chatRoomId) {
		// 회원 조회
		Member member = memberQueryService.findById(memberId);

		// 채팅방 상세 조회
		ChatRoom chatRoom = chatRoomService.findDetail(member, chatRoomId);

		// 채팅방 메세지 조회
		Slice<ChatMessage> chatMessages = chatMessageService.findAllByChatRoomId(chatRoom, member, lastMessageId);

		// 회원의 마지막 읽은 메세지 업데이트 (모든 메세지 읽음 처리)
		ChatRoomMember chatRoomMember = chatRoom.getChatRoomMember(member);
		chatRoomMemberService.updateLastReadMessage(chatRoomMember, chatMessages.getContent());
		publishNavChatCountEvent(member, chatRoomMember);

		return ChatRoomFindDetailResponse.of(member, chatRoom, chatMessages);
	}

	@Transactional
	public void delete(Long memberId, Long chatRoomId) {
		// 1. 회원 조회
		Member member = memberQueryService.findById(memberId);

		// 2. 채팅방 회원 조회
		List<ChatRoomMember> chatRoomMembers = chatRoomMemberQueryService.findByChatRoomIdWithChatRoomWithLock(
			chatRoomId);

		// 3. 채팅방 나가기 처리
		chatRoomLeaveProcessor.processLeave(member, chatRoomMembers, memberId);
	}

	public ChatRoomUnreadCountResponse countTotalUnreadMessagesForPublic(Long memberId) {
		// 회원 조회
		Member member = memberQueryService.findById(memberId);

		// 총 안읽은 메세지 수 조회
		int totalUnreadCount = chatRoomMemberQueryService.countTotalUnreadCount(member, Boolean.FALSE);

		return ChatRoomUnreadCountResponse.from(totalUnreadCount);
	}

	private void publishNavChatCountEvent(Member member, ChatRoomMember chatRoomMember) {
		boolean isDirector = Boolean.TRUE.equals(chatRoomMember.getIsDirector());
		int totalUnreadCount = chatRoomMemberQueryService.countTotalUnreadCount(member, isDirector);
		Role receiverRole = isDirector ? Role.DIRECTOR : Role.MEMBER;

		sseEventPublisher.publishSseEvent(
			SsePayload.of(SseEventType.REFRESH_NAV_CHAT_COUNT, member.getId(), receiverRole,
				ChatRoomUnreadCountResponse.from(totalUnreadCount)));
	}
}
