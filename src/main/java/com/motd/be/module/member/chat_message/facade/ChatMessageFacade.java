package com.motd.be.module.member.chat_message.facade;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.chat_file.entity.ChatFile;
import com.motd.be.module.member.chat_file.service.ChatFileService;
import com.motd.be.module.member.chat_message.dto.request.ChatMessageSendFileRequest;
import com.motd.be.module.member.chat_message.dto.response.ChatMessageDeleteResponse;
import com.motd.be.module.member.chat_message.dto.response.ChatMessageFindAllResponse;
import com.motd.be.module.member.chat_message.dto.response.ChatMessageSendResponse;
import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_message.entity.ChatMessageEventType;
import com.motd.be.module.member.chat_message.entity.ChatMessageType;
import com.motd.be.module.member.chat_message.process.ChatMessageProcessService;
import com.motd.be.module.member.chat_message.service.ChatMessageService;
import com.motd.be.module.member.chat_message.validator.ChatMessageValidator;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room.service.ChatRoomService;
import com.motd.be.module.member.chat_stomp.dto.request.ChatMessageSendMessageRequest;
import com.motd.be.module.member.chat_stomp.dto.request.StompContext;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.redis.domain.brocker.ChatMessagePublisher;
import com.motd.be.redis.domain.payload.ChatMessagePayload;
import com.motd.be.redis.domain.repository.RedisChatRoomSubscribeRepository;
import com.motd.be.shared.aws.enums.UploadFileType;
import com.motd.be.shared.forbidden_word.validator.ForbiddenWordValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageFacade {

	private final ChatRoomService chatRoomService;
	private final MemberQueryService memberQueryService;
	private final ChatMessageService chatMessageService;
	private final ChatFileService chatFileService;
	private final ChatMessagePublisher chatMessagePublisher;
	private final ChatMessageProcessService chatMessageProcessService;
	private final ChatMessageValidator chatMessageValidator;
	private final RedisChatRoomSubscribeRepository redisChatRoomSubscribeRepository;
	private final ForbiddenWordValidator forbiddenWordValidator;

	@Transactional
	public void sendMessage(ChatMessageSendMessageRequest chatMessageSendRequest, StompContext stompContext) {
		//1. 회원 조회
		Member member = memberQueryService.findById(stompContext.getMemberId());

		//2. chatRoomId 가 존재하는지 검증
		ChatRoom chatRoom = chatRoomService.findByIdWithMemberValidation(chatMessageSendRequest.getChatRoomId(),
			stompContext.getMemberId());

		// 채팅 content 길이 검증
		chatMessageValidator.validateContentLength(chatMessageSendRequest.getContent());

		// 금칙어 검증
		forbiddenWordValidator.validate(chatMessageSendRequest.getContent());

		ServiceEstimate serviceEstimate = chatRoom.getLatestEstimate();

		// redis  에서 온라인인 회원 조회
		Set<Long> onlineMemberIds = redisChatRoomSubscribeRepository.findAllMemberIdsByChatRoomId(chatRoom.getId());

		ChatMessage chatMessage = chatMessageProcessService.processChatMessages(
			chatRoom,
			member,
			serviceEstimate,
			onlineMemberIds,
			// 메시지 생성
			(room, sender, isBlockedOrBlock) -> chatMessageService.saveChatMessage(room, sender, chatMessageSendRequest,
				ChatMessageType.TEXT, isBlockedOrBlock),
			// 응답 생성
			(room, msg, set) -> ChatMessageSendResponse.ofWithTextType(member, room, msg, onlineMemberIds,
				serviceEstimate)
		);

		// push 전송
		chatMessageService.sendChatPushToOpponentIfVisible(chatRoom, member, chatMessage, onlineMemberIds);
	}

	@Transactional
	public void sendFileMessage(Long memberId, ChatMessageSendFileRequest request) {
		// 1. 회원 조회
		Member member = memberQueryService.findById(memberId);

		// 2. chatRoomId 가 존재하는지 검증
		ChatRoom chatRoom = chatRoomService.findByIdWithMemberValidation(request.getChatRoomId(), member.getId());

		// 채팅 파일 조회
		UploadFileType uploadFileType = UploadFileType.from(request.getFileType());
		List<ChatFile> chatFiles = chatFileService.findAllByIdsAndValidate(request.getFileIds(), member,
			uploadFileType);

		ServiceEstimate serviceEstimate = chatRoom.getLatestEstimate();

		// redis  에서 온라인인 회원 조회
		Set<Long> onlineMemberIds = redisChatRoomSubscribeRepository.findAllMemberIdsByChatRoomId(chatRoom.getId());

		ChatMessage chatMessage = chatMessageProcessService.processChatMessages(
			chatRoom,
			member,
			serviceEstimate,
			onlineMemberIds,
			// 메시지 생성
			(room, sender, isBlockedOrBlock) -> chatMessageService.saveChatMessageWithFileAndMap(room, chatFiles,
				sender,
				isBlockedOrBlock,
				uploadFileType),
			// 응답 생성
			(room, msg, set) -> ChatMessageSendResponse.ofWithFileType(member, room, msg, onlineMemberIds,
				serviceEstimate)
		);

		// push 전송
		chatMessageService.sendChatPushToOpponentIfVisible(chatRoom, member, chatMessage, onlineMemberIds);
	}

	@Transactional
	public void delete(Long memberId, Long chatMessageId) {
		// 회원 조회
		Member member = memberQueryService.findById(memberId);

		// chatMessage 조회 및 삭제
		ChatMessage chatMessage = chatMessageService.validateAndDelete(chatMessageId, memberId);

		// 이미지 채팅이라면 이미지 삭제처리
		chatFileService.deleteIfNeeded(chatMessage, member);

		// 웹소켓으로 메세지 전송
		chatMessagePublisher.publish(
			ChatMessagePayload.ofForDelete(ChatMessageEventType.DELETE, ChatMessageDeleteResponse.from(chatMessage),
				chatMessage.getChatRoom().getId()));
	}

	public ChatMessageFindAllResponse findAllByChatRoomId(Long memberId, Long chatRoomId, Long lastMessageId) {
		// 회원 조회
		Member member = memberQueryService.findById(memberId);

		// 채팅방 조회 및 권한 검증
		ChatRoom chatRoom = chatRoomService.findByIdWithMemberValidation(chatRoomId, memberId);

		// 메시지 조회
		Slice<ChatMessage> chatMessages = chatMessageService.findAllByChatRoomId(chatRoom, member, lastMessageId);

		return ChatMessageFindAllResponse.from(chatMessages);
	}
}
