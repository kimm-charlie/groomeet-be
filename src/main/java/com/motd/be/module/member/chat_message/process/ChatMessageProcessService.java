package com.motd.be.module.member.chat_message.process;

import java.util.Set;

import org.apache.commons.lang3.function.TriFunction;
import org.springframework.stereotype.Service;

import com.motd.be.module.member.chat_message.dto.response.ChatMessageSendResponse;
import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_message.entity.ChatMessageEventType;
import com.motd.be.module.member.chat_message.service.ChatMessageService;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_member.service.ChatRoomMemberService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.validator.MemberValidator;
import com.motd.be.module.member.member_block.service.MemberBlockQueryService;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.redis.domain.brocker.ChatMessagePublisher;
import com.motd.be.redis.domain.payload.ChatMessagePayload;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageProcessService {

	private final ChatMessageService chatMessageService;
	private final ChatRoomMemberService chatRoomMemberService;
	private final ChatMessagePublisher chatMessagePublisher;
	private final MemberBlockQueryService memberBlockQueryService;
	private final MemberValidator memberValidator;

	/**
	 * 채팅 메세지 처리
	 *
	 * @param chatRoom
	 * @param sender
	 * @param serviceEstimate
	 * @param onlineMemberIds
	 * @param messageCreator
	 * @param responseCreator
	 */
	public ChatMessage processChatMessages(ChatRoom chatRoom, Member sender, ServiceEstimate serviceEstimate,
		Set<Long> onlineMemberIds,
		TriFunction<ChatRoom, Member, Boolean, ChatMessage> messageCreator,
		TriFunction<ChatRoom, ChatMessage, Set<Long>, ChatMessageSendResponse> responseCreator) {

		// 차단 여부 검증
		Member receiver = memberValidator.isWithdrawalMember(chatRoom.getOtherMember(sender));
		Boolean isBlockedOrBlock = memberBlockQueryService.existsByBlockerOrBlocked(sender, receiver);

		// 채팅 메세지 저장
		ChatMessage chatMessage = messageCreator.apply(chatRoom, sender, isBlockedOrBlock);

		// 채팅방 회원 설정 업데이트 (마지막 읽은 메세지 업데이트)
		chatRoomMemberService.updateLastReadMessageForAllChatMember(chatRoom, chatMessage, sender, onlineMemberIds);

		// 채팅방 회원 설정 업데이트 (채팅방 나간 회원 처리)
		chatRoomMemberService.updateChatRoomMembersWhoLeftChatRoom(chatRoom.getChatRoomMembers(), chatMessage);

		// 채팅방 최신 메세지 업데이트
		chatRoom.updateLastMessage(chatMessage);

		// 채팅방 refresh 를 위한 sse 요청 보내기
		chatMessageService.sendChatRoomRefreshEventIfNeeded(chatRoom, chatRoom.getOtherMember(sender),
			sender, chatMessage, serviceEstimate);

		// 웹소켓으로 메세지 전송
		ChatMessageSendResponse response = responseCreator.apply(chatRoom, chatMessage, onlineMemberIds);
		chatMessagePublisher.publish(
			ChatMessagePayload.ofForSend(ChatMessageEventType.SEND, response, chatRoom.getId()));

		return chatMessage;
	}
}
