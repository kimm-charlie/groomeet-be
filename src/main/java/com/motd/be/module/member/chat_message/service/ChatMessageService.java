package com.motd.be.module.member.chat_message.service;

import static com.motd.be.common.constants.PageSizeConstants.*;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.module.member.chat_file.entity.ChatFile;
import com.motd.be.module.member.chat_file.service.ChatFileCommandService;
import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_message.entity.ChatMessageType;
import com.motd.be.module.member.chat_message.validator.ChatMessageValidator;
import com.motd.be.module.member.chat_room.dto.response.ChatRoomResponse;
import com.motd.be.module.member.chat_room.dto.response.ChatRoomUnreadCountResponse;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room.service.ChatRoomQueryService;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.chat_room_member.service.ChatRoomMemberQueryService;
import com.motd.be.module.member.chat_stomp.dto.request.ChatMessageSendMessageRequest;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.Role;
import com.motd.be.module.member.review.entity.Review;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.sse.SseEventType;
import com.motd.be.redis.domain.brocker.SseEventPublisher;
import com.motd.be.redis.domain.payload.SsePayload;
import com.motd.be.shared.aws.enums.UploadFileType;
import com.motd.be.shared.firebase.dto.FirebasePushEvent;
import com.motd.be.shared.firebase.policy.ActivityAgreedPolicy;
import com.motd.be.shared.firebase.policy.ChatVisibilityPolicy;
import com.motd.be.shared.firebase.policy.CompositePushSendPolicy;
import com.motd.be.shared.firebase.policy.PushContext;
import com.motd.be.shared.firebase.policy.PushSendPolicy;
import com.motd.be.shared.firebase.policy.ReceiverOfflinePolicy;
import com.motd.be.shared.firebase.service.FirebaseEventPublisher;
import com.motd.be.shared.firebase.service.FirebasePushFactory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

	private final ChatMessageCommandService chatMessageCommandService;
	private final ChatRoomMemberQueryService chatRoomMemberQueryService;
	private final ChatFileCommandService chatFileCommandService;
	private final SseEventPublisher sseEventPublisher;
	private final ChatRoomQueryService chatRoomQueryService;
	private final ChatMessageQueryService chatMessageQueryService;
	private final ChatMessageValidator chatMessageValidator;
	private final FirebaseEventPublisher firebaseEventPublisher;
	private final FirebasePushFactory firebasePushFactory;

	/**
	 * 디렉터가 요청를 보낼때 사용되는 제안 메세지 저장 매커니즘 이다.
	 *
	 * @param chatRoom
	 * @param chatRoomMember
	 * @return
	 */
	public ChatMessage saveChatMessageWithEstimate(ChatRoom chatRoom, ChatRoomMember chatRoomMember,
		ServiceEstimate serviceEstimate, ChatMessageType chatMessageType, Boolean isBlockedOrBlock) {
		ChatMessage chatMessage = ChatMessage.ofWithEstimate(chatRoom, chatRoomMember, serviceEstimate,
			chatMessageType);

		if (isBlockedOrBlock) {
			chatMessage.hideFromOpponent();
		}

		return chatMessageCommandService.save(chatMessage);
	}

	public ChatMessage saveChatMessage(ChatRoom chatRoom, Member sender,
		ChatMessageSendMessageRequest chatMessageSendRequest, ChatMessageType chatMessageType,
		Boolean isBlockedOrBlock) {
		// chatRoomMember 조회
		ChatRoomMember chatRoomMember = chatRoomMemberQueryService.findByChatRoomAndMember(chatRoom, sender);

		// chatMessage 저장
		ChatMessage chatMessage = ChatMessage.ofWithText(chatRoom, chatRoomMember,
			chatMessageSendRequest.getContent(), chatMessageType);

		if (isBlockedOrBlock) {
			chatMessage.hideFromOpponent();
		}

		return chatMessageCommandService.save(chatMessage);
	}

	public ChatMessage saveChatMessageWithReview(ChatRoom chatRoom, Member sender,
		Review review, ChatMessageType chatMessageType, Boolean isBlockedOrBlock) {
		// chatRoomMember 조회
		ChatRoomMember chatRoomMember = chatRoomMemberQueryService.findByChatRoomAndMember(chatRoom, sender);

		// chatMessage 저장
		ChatMessage chatMessage = ChatMessage.ofWithReview(chatRoom, chatRoomMember, review, chatMessageType);

		if (isBlockedOrBlock) {
			chatMessage.hideFromOpponent();
		}

		return chatMessageCommandService.save(chatMessage);
	}

	/**
	 * 이경우는 어쩔수없이 mapping 을 할때 chatMessage 가 필요하기때문에 chatMessage 서비스 단에서 매핑 처리를 해준다.
	 *
	 * @param chatRoom
	 * @param chatFiles
	 * @param sender
	 * @param isBlockedOrBlock
	 * @param uploadFileType
	 * @return
	 */
	public ChatMessage saveChatMessageWithFileAndMap(ChatRoom chatRoom, List<ChatFile> chatFiles, Member sender,
		Boolean isBlockedOrBlock,
		UploadFileType uploadFileType) {
		// chatRoomMember 조회
		ChatRoomMember chatRoomMember = chatRoomMemberQueryService.findByChatRoomAndMember(chatRoom, sender);

		// 파일 타입 확인
		ChatMessageType chatMessageType = ChatMessageType.valueOf(uploadFileType.name());

		// chatMessage 저장
		ChatMessage chatMessage = ChatMessage.ofWithFile(chatRoom, chatRoomMember, chatFiles, chatMessageType);

		if (isBlockedOrBlock) {
			chatMessage.hideFromOpponent();
		}

		ChatMessage savedChatMessage = chatMessageCommandService.save(chatMessage);

		// chatFile 과 chatMessage 매핑
		chatFileCommandService.mapChatMessageByIds(chatMessage, chatFiles);

		return savedChatMessage;
	}

	public void sendChatRoomRefreshEventIfNeeded(ChatRoom chatRoom, Member receiver, Member opponent,
		ChatMessage chatMessage, ServiceEstimate serviceEstimate) {

		if (!Boolean.TRUE.equals(chatMessage.getIsVisibleToOpponent())) {
			return;
		}

		int unreadCount = chatRoomQueryService.countUnreadMessagesByMember(chatRoom, receiver);
		ChatRoomMember receiverChatRoomMember = chatRoomMemberQueryService.findByChatRoomAndMember(chatRoom,
			receiver);

		ChatMessage lastVisibleMessage = receiverChatRoomMember.getLastVisibleMessage();
		Role receiverRole = Boolean.TRUE.equals(receiverChatRoomMember.getIsDirector())
			? Role.DIRECTOR
			: Role.MEMBER;

		sseEventPublisher.publishSseEvent(
			SsePayload.of(SseEventType.REFRESH_CHAT_ROOM_LIST, receiver.getId(), receiverRole,
				ChatRoomResponse.of(chatRoom, lastVisibleMessage, unreadCount, opponent, serviceEstimate)));

		int totalUnreadCount = chatRoomMemberQueryService.countTotalUnreadCount(receiver,
			receiverChatRoomMember.getIsDirector());

		sseEventPublisher.publishSseEvent(
			SsePayload.of(SseEventType.REFRESH_NAV_CHAT_COUNT, receiver.getId(), receiverRole,
				ChatRoomUnreadCountResponse.from(totalUnreadCount)));
	}

	public Slice<ChatMessage> findAllByChatRoomId(ChatRoom chatRoom, Member member, Long lastMessageId) {
		Pageable pageable = PageRequest.of(0, CHAT_MESSAGE_FIND_ALL_SIZE);

		// lastMessageId 가 주어지면 해당 메세지가 채팅방에 속하는지 검증
		chatMessageValidator.validateChatMessage(chatRoom, lastMessageId);

		return chatMessageQueryService.findAllByChatRoomId(chatRoom.getId(), lastMessageId, pageable,
			member.getId());
	}

	public ChatMessage validateAndDelete(Long chatMessageId, Long memberId) {
		// 채팅메세지 조회
		ChatMessage chatMessage = chatMessageQueryService.findByIdWithChatRoomMember(chatMessageId);

		// 채팅 메세지 상태 검증
		chatMessageValidator.isChatMessageDeletable(chatMessage);

		// 채팅 메세지 소유권 확인
		chatMessageValidator.isChatMessageOwnedByMember(chatMessage, memberId);

		// 채팅 삭제
		chatMessage.delete();

		return chatMessage;
	}

	public void sendChatPushToOpponentIfVisible(ChatRoom chatRoom, Member sender, ChatMessage chatMessage,
		Set<Long> onlineMemberIds) {
		Member receiver = chatRoom.getOtherMember(sender);

		// 정책 검사
		PushContext pushContext = PushContext.of(sender, receiver, onlineMemberIds, chatMessage);

		PushSendPolicy pushSendPolicy =
			CompositePushSendPolicy.of(List.of(
				new ChatVisibilityPolicy(),
				new ActivityAgreedPolicy(),
				new ReceiverOfflinePolicy()
			));

		if (!pushSendPolicy.canSend(pushContext)) {
			return;
		}

		ChatRoomMember senderChatRoomMember = chatRoom.getChatRoomMember(sender);

		FirebasePushEvent firebasePushEvent;
		if (senderChatRoomMember.getIsDirector()) {
			firebasePushEvent = firebasePushFactory.chatMessageForTextOrFileToMember(sender, receiver, chatRoom.getId(),
				chatMessage.getContent());
		} else {
			firebasePushEvent = firebasePushFactory.chatMessageForTextOrFileToDirector(sender, receiver,
				chatRoom.getId(), chatMessage.getContent());
		}

		firebaseEventPublisher.sendPush(firebasePushEvent);
	}
}
