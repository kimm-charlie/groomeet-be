package com.motd.be.module.member.chat_message.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.ValidationConstants.*;
import static com.motd.be.common.constants.ValidationMessages.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.ChatFileException;
import com.motd.be.exception.exceptions.ChatMessageException;
import com.motd.be.exception.exceptions.ChatRoomMemberException;
import com.motd.be.exception.exceptions.HandlerException;
import com.motd.be.exception.exceptions.MemberException;
import com.motd.be.module.member.chat_file.entity.ChatFile;
import com.motd.be.module.member.chat_message.dto.request.ChatMessageSendFileRequest;
import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_message.entity.ChatMessageType;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.sse.SseEventType;
import com.motd.be.shared.aws.enums.UploadFileType;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class ChatMessageControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("회원은 채팅방의 메시지 목록을 조회할 수 있다")
	void findAllChatMessages() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, member);
		ServiceEstimate serviceEstimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		ChatRoom chatRoom = chatRoomProvider.save();
		ChatRoomMember directorChatRoomMember = chatRoomMemberProvider.saveDirector(chatRoom, director);
		ChatRoomMember memberChatRoomMember = chatRoomMemberProvider.saveMember(chatRoom, member);

		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		// 메시지 3개 생성
		LocalDateTime now = LocalDateTime.now();
		ChatMessage message1 = chatMessageProvider.saveTextType(chatRoom, directorChatRoomMember,
			now.minusMinutes(10));
		ChatMessage message2 = chatMessageProvider.saveTextType(chatRoom, memberChatRoomMember, now.minusMinutes(5));
		ChatMessage message3 = chatMessageProvider.saveTextType(chatRoom, directorChatRoomMember,
			now.minusMinutes(1));

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.get("/api/chat-messages")
				.param("chatRoomId", String.valueOf(chatRoom.getId()))
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.page").value(0))
			.andExpect(jsonPath("$.hasNext").value(false))
			.andExpect(jsonPath("$.chatMessages").isArray())
			.andExpect(jsonPath("$.chatMessages.length()").value(3))
			// 최신순 정렬 확인
			.andExpect(jsonPath("$.chatMessages[0].id").value(message3.getId()))
			.andExpect(jsonPath("$.chatMessages[1].id").value(message2.getId()))
			.andExpect(jsonPath("$.chatMessages[2].id").value(message1.getId()));
	}

	@Test
	@DisplayName("회원은 커서 기반으로 이전 메시지를 조회할 수 있다")
	void findAllChatMessagesWithCursor() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, member);
		ServiceEstimate serviceEstimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		ChatRoom chatRoom = chatRoomProvider.save();
		ChatRoomMember directorChatRoomMember = chatRoomMemberProvider.saveDirector(chatRoom, director);
		ChatRoomMember memberChatRoomMember = chatRoomMemberProvider.saveMember(chatRoom, member);

		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		// 메시지 5개 생성
		LocalDateTime now = LocalDateTime.now();
		ChatMessage message1 = chatMessageProvider.saveTextType(chatRoom, directorChatRoomMember,
			now.minusMinutes(20));
		ChatMessage message2 = chatMessageProvider.saveTextType(chatRoom, memberChatRoomMember,
			now.minusMinutes(15));
		ChatMessage message3 = chatMessageProvider.saveTextType(chatRoom, directorChatRoomMember,
			now.minusMinutes(10));
		ChatMessage message4 = chatMessageProvider.saveTextType(chatRoom, memberChatRoomMember, now.minusMinutes(5));
		ChatMessage message5 = chatMessageProvider.saveTextType(chatRoom, directorChatRoomMember,
			now.minusMinutes(1));

		entityManager.flush();
		entityManager.clear();

		// when - message3보다 이전 메시지 조회
		mockMvc.perform(MockMvcRequestBuilders.get("/api/chat-messages")
				.param("chatRoomId", String.valueOf(chatRoom.getId()))
				.param("lastMessageId", String.valueOf(message3.getId()))
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.page").value(0))
			.andExpect(jsonPath("$.chatMessages").isArray())
			.andExpect(jsonPath("$.chatMessages.length()").value(2))
			// message3보다 이전 메시지만 조회되어야 함
			.andExpect(jsonPath("$.chatMessages[0].id").value(message2.getId()))
			.andExpect(jsonPath("$.chatMessages[1].id").value(message1.getId()));
	}

	@Test
	@DisplayName("채팅방에 참여하지 않은 회원은 메시지를 조회할 수 없다")
	void findAllChatMessages_NotInChatRoom() throws Exception {
		// given
		Member member1 = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member member2 = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member outsider = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(outsider.getId());

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, member1);
		chatRoomMemberProvider.saveMember(chatRoom, member2);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.get("/api/chat-messages")
				.param("chatRoomId", String.valueOf(chatRoom.getId()))
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.status").value(ChatRoomMemberException.NOT_IN_CHAT_ROOM.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(ChatRoomMemberException.NOT_IN_CHAT_ROOM.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(ChatRoomMemberException.NOT_IN_CHAT_ROOM.getCode()));
	}

	@Test
	@DisplayName("존재하지 않는 채팅방의 메시지는 조회할 수 없다")
	void findAllChatMessages_ChatRoomNotFound() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		Long nonExistentChatRoomId = 999999L;

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.get("/api/chat-messages")
				.param("chatRoomId", String.valueOf(nonExistentChatRoomId))
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.status").value(ChatRoomMemberException.NOT_IN_CHAT_ROOM.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(ChatRoomMemberException.NOT_IN_CHAT_ROOM.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(ChatRoomMemberException.NOT_IN_CHAT_ROOM.getCode()));
	}

	@Test
	@DisplayName("회원은 채팅방에 이미지 메시지를 전송할 수 있다")
	void sendImageMessage() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member sender = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Member receiver = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(sender.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, receiver);

		ChatRoom chatRoom = chatRoomProvider.save();
		ChatRoomMember senderMember = chatRoomMemberProvider.saveDirector(chatRoom, sender);
		ChatRoomMember receiverMember = chatRoomMemberProvider.saveMember(chatRoom, receiver);

		// 제안서 저장
		ServiceEstimate serviceEstimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		// 채팅방 제안 매핑 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		// 이미지 저장
		ChatFile image1 = chatFileProvider.save(sender);
		ChatFile image2 = chatFileProvider.save(sender);

		ChatMessageSendFileRequest request = ChatMessageSendFileRequest.builder()
			.fileIds(List.of(image1.getId(), image2.getId()))
			.chatRoomId(chatRoom.getId())
			.fileType(UploadFileType.IMAGE.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/chat-messages/files", chatRoom.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isCreated());

		// then
		entityManager.flush();
		entityManager.clear();

		ChatRoom updatedChatRoom = chatRoomProvider.findById(chatRoom.getId());
		assertThat(updatedChatRoom.getLastMessage()).isNotNull();
		assertThat(updatedChatRoom.getLastMessage().getMessageType().name()).isEqualTo(ChatMessageType.IMAGE.name());

		List<ChatFile> chatFiles = chatFileProvider.findAll();
		assertThat(chatFiles).hasSize(2);
		assertThat(chatFiles.get(0).getChatMessage().getId()).isEqualTo(updatedChatRoom.getLastMessage().getId());

		// SSE 발행 검증 (REFRESH_CHAT_ROOM_LIST)
		await()
			.atMost(Duration.ofSeconds(2))
			.untilAsserted(() ->
				verify(sseService, atLeastOnce()).refreshChatRoomList(
					argThat(payload ->
						payload.getEventName() == SseEventType.REFRESH_CHAT_ROOM_LIST &&
							Objects.equals(payload.getReceiverId(), receiver.getId()))));

		// SSE 발행 검증 (REFRESH_NAV_CHAT_COUNT)
		await()
			.atMost(Duration.ofSeconds(2))
			.untilAsserted(() ->
				verify(sseService, atLeastOnce()).refreshNavChatCount(
					argThat(payload ->
						payload.getEventName() == SseEventType.REFRESH_NAV_CHAT_COUNT &&
							payload.getReceiverId().equals(receiver.getId()))));

		// chatRoomMember 의 lastVisibleMessageId 가 업데이트 되었는지 검증
		ChatRoomMember updatedReceiverMember = chatRoomMemberProvider.findById(receiverMember.getId());
		assertThat(updatedReceiverMember.getLastVisibleMessage().getId()).isEqualTo(
			updatedChatRoom.getLastMessage().getId());

		ChatRoomMember updatedSenderMember = chatRoomMemberProvider.findById(senderMember.getId());
		assertThat(updatedSenderMember.getLastVisibleMessage().getId()).isEqualTo(
			updatedChatRoom.getLastMessage().getId());
	}

	@Test
	@DisplayName("회원은 채팅방에 이미지 메시지를 전송할 수 있다. (파일이 제한갯수 초과일때)")
	void sendImageMessageWithExceededFileLimitCount() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member sender = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Member receiver = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(sender.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, receiver);

		ChatRoom chatRoom = chatRoomProvider.save();
		ChatRoomMember senderMember = chatRoomMemberProvider.saveDirector(chatRoom, sender);
		ChatRoomMember receiverMember = chatRoomMemberProvider.saveMember(chatRoom, receiver);

		// 제안서 저장
		ServiceEstimate serviceEstimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		// 채팅방 제안 매핑 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		// 이미지 저장
		List<ChatFile> chatFiles = new ArrayList<>();
		for (int i = 0; i < CHAT_MESSAGE_IMAGE_MAX_COUNT + 1; i++) {
			chatFiles.add(chatFileProvider.save(sender));
		}

		ChatMessageSendFileRequest request = ChatMessageSendFileRequest.builder()
			.fileIds(chatFiles.stream().map(ChatFile::getId).toList())
			.chatRoomId(chatRoom.getId())
			.fileType(UploadFileType.IMAGE.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/chat-messages/files", chatRoom.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(CHAT_MESSAGE_IMAGE_MAX_COUNT_MSG))
			.andExpect(jsonPath("$.code").value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("회원은 채팅방에 이미지 메시지를 전송할 수 있다 (디렉터가 결제하기 전인 경우)")
	void sendImageMessageBeforeChatStartPaidAndSendByMember() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR);
		Member receiver = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Member sender = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(sender.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, sender);

		ChatRoom chatRoom = chatRoomProvider.save();
		ChatRoomMember senderMember = chatRoomMemberProvider.saveDirector(chatRoom, receiver);
		ChatRoomMember receiverMember = chatRoomMemberProvider.saveMember(chatRoom, sender);

		// 제안서 저장
		ServiceEstimate serviceEstimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		// 채팅방 제안 매핑 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		// 이미지 저장
		ChatFile image1 = chatFileProvider.save(sender);
		ChatFile image2 = chatFileProvider.save(sender);

		ChatMessageSendFileRequest request = ChatMessageSendFileRequest.builder()
			.fileIds(List.of(image1.getId(), image2.getId()))
			.chatRoomId(chatRoom.getId())
			.fileType(UploadFileType.IMAGE.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/chat-messages/files", chatRoom.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isCreated());

		// then
		entityManager.flush();
		entityManager.clear();

		ChatRoom updatedChatRoom = chatRoomProvider.findById(chatRoom.getId());
		assertThat(updatedChatRoom.getLastMessage()).isNotNull();
		assertThat(updatedChatRoom.getLastMessage().getMessageType().name()).isEqualTo(ChatMessageType.IMAGE.name());

		List<ChatFile> chatFiles = chatFileProvider.findAll();
		assertThat(chatFiles).hasSize(2);
		assertThat(chatFiles.get(0).getChatMessage().getId()).isEqualTo(updatedChatRoom.getLastMessage().getId());

		// SSE 발행 검증 (REFRESH_CHAT_ROOM_LIST)
		await()
			.atMost(Duration.ofSeconds(2))
			.untilAsserted(() ->
				verify(sseService, atLeastOnce()).refreshChatRoomList(
					argThat(payload ->
						payload.getEventName() == SseEventType.REFRESH_CHAT_ROOM_LIST &&
							Objects.equals(payload.getReceiverId(), receiver.getId()))));

		// SSE 발행 검증 (REFRESH_NAV_CHAT_COUNT)
		await()
			.atMost(Duration.ofSeconds(2))
			.untilAsserted(() ->
				verify(sseService, atLeastOnce()).refreshNavChatCount(
					argThat(payload ->
						payload.getEventName() == SseEventType.REFRESH_NAV_CHAT_COUNT &&
							payload.getReceiverId().equals(receiver.getId()))));
	}

	@Test
	@DisplayName("회원은 채팅방에 이미지 메시지를 전송할 수 있다 (디렉터가 결제를 한 경우)")
	void sendImageMessageWhenChatStartPaidAndSendByDirector() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR);
		Member sender = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Member receiver = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(sender.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, receiver);

		ChatRoom chatRoom = chatRoomProvider.save();
		ChatRoomMember senderMember = chatRoomMemberProvider.saveDirector(chatRoom, sender);
		ChatRoomMember receiverMember = chatRoomMemberProvider.saveMember(chatRoom, receiver);

		chatRoom.updateChatRoomStatusAfterChatStartPaid();

		// 제안서 저장
		ServiceEstimate serviceEstimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		// 채팅방 제안 매핑 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		// 이미지 저장
		ChatFile image1 = chatFileProvider.save(sender);
		ChatFile image2 = chatFileProvider.save(sender);

		ChatMessageSendFileRequest request = ChatMessageSendFileRequest.builder()
			.fileIds(List.of(image1.getId(), image2.getId()))
			.chatRoomId(chatRoom.getId())
			.fileType(UploadFileType.IMAGE.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/chat-messages/files", chatRoom.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isCreated());

		// then
		entityManager.flush();
		entityManager.clear();

		ChatRoom updatedChatRoom = chatRoomProvider.findById(chatRoom.getId());
		assertThat(updatedChatRoom.getLastMessage()).isNotNull();
		assertThat(updatedChatRoom.getLastMessage().getMessageType().name()).isEqualTo(ChatMessageType.IMAGE.name());

		List<ChatFile> chatFiles = chatFileProvider.findAll();
		assertThat(chatFiles).hasSize(2);
		assertThat(chatFiles.get(0).getChatMessage().getId()).isEqualTo(updatedChatRoom.getLastMessage().getId());

		// SSE 발행 검증 (REFRESH_CHAT_ROOM_LIST)
		await()
			.atMost(Duration.ofSeconds(2))
			.untilAsserted(() ->
				verify(sseService, atLeastOnce()).refreshChatRoomList(
					argThat(payload ->
						payload.getEventName() == SseEventType.REFRESH_CHAT_ROOM_LIST &&
							Objects.equals(payload.getReceiverId(), receiver.getId()))));

		// SSE 발행 검증 (REFRESH_NAV_CHAT_COUNT)
		await()
			.atMost(Duration.ofSeconds(2))
			.untilAsserted(() ->
				verify(sseService, atLeastOnce()).refreshNavChatCount(
					argThat(payload ->
						payload.getEventName() == SseEventType.REFRESH_NAV_CHAT_COUNT &&
							payload.getReceiverId().equals(receiver.getId()))));
	}


	@Test
	@DisplayName("회원은 채팅방에 이미지 메시지를 전송할 수 있다 (바디에 파일타입과 실제 파일타입이 다를경우)")
	void sendImageMessageWithMissMatchFileType() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member sender = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Member receiver = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(sender.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, receiver);

		ChatRoom chatRoom = chatRoomProvider.save();
		ChatRoomMember senderMember = chatRoomMemberProvider.saveDirector(chatRoom, sender);
		ChatRoomMember receiverMember = chatRoomMemberProvider.saveMember(chatRoom, receiver);

		// 제안서 저장
		ServiceEstimate serviceEstimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		// 채팅방 제안 매핑 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		// 이미지 저장
		ChatFile image1 = chatFileProvider.save(sender);
		ChatFile image2 = chatFileProvider.save(sender);

		ChatMessageSendFileRequest request = ChatMessageSendFileRequest.builder()
			.fileIds(List.of(image1.getId(), image2.getId()))
			.chatRoomId(chatRoom.getId())
			.fileType(UploadFileType.DOCUMENT.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/chat-messages/files", chatRoom.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(ChatFileException.INVALID_FILE_TYPE.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(ChatFileException.INVALID_FILE_TYPE.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(ChatFileException.INVALID_FILE_TYPE.getCode()));
	}

	@Test
	@DisplayName("탈퇴한 회원에게는 이미지 메시지를 전송할 수 없다")
	void cannotSendImageMessageToWithdrawnMember() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member sender = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Member receiver = memberProvider.saveMemberWithdrawalTrue(LocalDateTime.now(), SignInPlatform.KAKAO);

		Jwt jwt = generateTokenWithMemberIdRoleMember(sender.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, receiver);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, sender);
		chatRoomMemberProvider.saveMember(chatRoom, receiver);

		// 제안서 저장
		ServiceEstimate serviceEstimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		// 채팅방 제안 매핑 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		// 이미지 저장
		ChatFile image1 = chatFileProvider.save(sender);
		ChatFile image2 = chatFileProvider.save(sender);

		ChatMessageSendFileRequest request = ChatMessageSendFileRequest.builder()
			.fileIds(List.of(image1.getId(), image2.getId()))
			.chatRoomId(chatRoom.getId())
			.fileType(UploadFileType.IMAGE.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/chat-messages/files", chatRoom.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(MemberException.WITHDRAWAL_MEMBER.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(MemberException.WITHDRAWAL_MEMBER.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(MemberException.WITHDRAWAL_MEMBER.getCode()));
	}

	@Test
	@DisplayName("회원은 채팅방에 이미지 메시지를 전송할 수 있다 (상대방이 채팅방을 나간 상태라면, 다시 참여시킨다)")
	void sendImageMessageWhenOpponent() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member sender = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Member receiver = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(sender.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, receiver);

		ChatRoom chatRoom = chatRoomProvider.save();
		ChatRoomMember senderMember = chatRoomMemberProvider.saveDirector(chatRoom, sender);
		ChatRoomMember receiverMember = chatRoomMemberProvider.saveMemberWithRoomDeletedTrue(chatRoom, receiver);

		// 제안서 저장
		ServiceEstimate serviceEstimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		// 채팅방 제안 매핑 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		// 이미지 저장
		ChatFile image1 = chatFileProvider.save(sender);
		ChatFile image2 = chatFileProvider.save(sender);

		ChatMessageSendFileRequest request = ChatMessageSendFileRequest.builder()
			.fileIds(List.of(image1.getId(), image2.getId()))
			.chatRoomId(chatRoom.getId())
			.fileType(UploadFileType.IMAGE.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/chat-messages/files", chatRoom.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isCreated());

		// then
		entityManager.flush();
		entityManager.clear();

		ChatRoom updatedChatRoom = chatRoomProvider.findById(chatRoom.getId());
		assertThat(updatedChatRoom.getLastMessage()).isNotNull();
		assertThat(updatedChatRoom.getLastMessage().getMessageType().name()).isEqualTo(ChatMessageType.IMAGE.name());

		List<ChatFile> chatFiles = chatFileProvider.findAll();
		assertThat(chatFiles).hasSize(2);
		assertThat(chatFiles.get(0).getChatMessage().getId()).isEqualTo(updatedChatRoom.getLastMessage().getId());

		// receiver 가 다시 참여한 상태인지 검증한다.
		ChatRoomMember updatedReceiverMember = chatRoomMemberProvider.findById(receiverMember.getId());

		assertThat(updatedReceiverMember.getIsChatRoomDeleted()).isFalse();
	}

	@Test
	@DisplayName("회원은 채팅방에 이미지 메시지를 전송할 수 있다(상대방이 송신자를 차단한 상태일때)")
	void sendImageMessageWhenSenderBlockedByOpponent() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member sender = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Member receiver = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(sender.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, receiver);

		ChatRoom chatRoom = chatRoomProvider.save();
		ChatRoomMember senderMember = chatRoomMemberProvider.saveDirector(chatRoom, sender);
		ChatRoomMember receiverMember = chatRoomMemberProvider.saveMemberWithRoomDeletedTrue(chatRoom, receiver);

		// 제안서 저장
		ServiceEstimate serviceEstimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		// 채팅방 제안 매핑 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		// 이미지 저장
		ChatFile image1 = chatFileProvider.save(sender);
		ChatFile image2 = chatFileProvider.save(sender);

		ChatMessageSendFileRequest request = ChatMessageSendFileRequest.builder()
			.fileIds(List.of(image1.getId(), image2.getId()))
			.chatRoomId(chatRoom.getId())
			.fileType(UploadFileType.IMAGE.name())
			.build();

		// receiver 가 sender 를 차단한 상태
		memberBlockProvider.save(receiver, sender);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/chat-messages/files", chatRoom.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isCreated());

		// then
		entityManager.flush();
		entityManager.clear();

		ChatRoom updatedChatRoom = chatRoomProvider.findById(chatRoom.getId());
		assertThat(updatedChatRoom.getLastMessage()).isNotNull();
		assertThat(updatedChatRoom.getLastMessage().getMessageType().name()).isEqualTo(ChatMessageType.IMAGE.name());

		List<ChatFile> chatFiles = chatFileProvider.findAll();
		assertThat(chatFiles).hasSize(2);
		assertThat(chatFiles.get(0).getChatMessage().getId()).isEqualTo(updatedChatRoom.getLastMessage().getId());

		// 채팅 메세지의 isVisibleToOpponent 가 false 인지 검증
		ChatMessage chatMessage = chatMessageProvider.findById(updatedChatRoom.getLastMessage().getId());
		assertThat(chatMessage.getIsVisibleToOpponent()).isFalse();

		// 상대방이 다시 채팅방에 들어오지 않았는지 검증
		ChatRoomMember updatedReceiverMember = chatRoomMemberProvider.findById(receiverMember.getId());
		assertThat(updatedReceiverMember.getIsChatRoomDeleted()).isTrue();
		assertThat(updatedReceiverMember.getLastVisibleMessage()).isNull();

		ChatRoomMember updatedSenderMember = chatRoomMemberProvider.findById(senderMember.getId());
		assertThat(updatedSenderMember.getLastVisibleMessage().getId()).isEqualTo(
			updatedChatRoom.getLastMessage().getId());

		// SSE 요청이 가면 안된다. (차단)
		verify(sseService, never()).refreshChatRoomList(any());
		verify(sseService, never()).refreshNavChatCount(any());
	}

	@Test
	@DisplayName("채팅방에 참여하지 않은 회원은 이미지 메시지를 전송할 수 없다")
	void sendImageMessage_NotInChatRoom() throws Exception {
		// given
		Member sender = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member receiver = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member outsider = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(outsider.getId());

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, sender);
		chatRoomMemberProvider.saveMember(chatRoom, receiver);

		ChatFile image1 = chatFileProvider.save(sender);

		ChatMessageSendFileRequest request = ChatMessageSendFileRequest.builder()
			.fileIds(List.of(image1.getId()))
			.chatRoomId(chatRoom.getId())
			.fileType(UploadFileType.IMAGE.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/chat-messages/files", chatRoom.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.status").value(ChatRoomMemberException.NOT_IN_CHAT_ROOM.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(ChatRoomMemberException.NOT_IN_CHAT_ROOM.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(ChatRoomMemberException.NOT_IN_CHAT_ROOM.getCode()));
	}

	@Test
	@DisplayName("다른 회원의 이미지로는 메시지를 전송할 수 없다")
	void sendImageMessage_NotImageOwner() throws Exception {
		// given
		Member sender = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member receiver = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member otherMember = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(sender.getId());

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, sender);
		chatRoomMemberProvider.saveMember(chatRoom, receiver);

		// 다른 회원의 이미지
		ChatFile otherImage = chatFileProvider.save(otherMember);

		ChatMessageSendFileRequest request = ChatMessageSendFileRequest.builder()
			.fileIds(List.of(otherImage.getId()))
			.chatRoomId(chatRoom.getId())
			.fileType(UploadFileType.IMAGE.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/chat-messages/files", chatRoom.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.status").value(ChatFileException.NOT_OWNED.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(ChatFileException.NOT_OWNED.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(ChatFileException.NOT_OWNED.getCode()));
	}

	@Test
	@DisplayName("회원은 자신이 보낸 채팅 메시지를 삭제할 수 있다")
	void deleteChatMessage() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member sender = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member receiver = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(sender.getId());

		ChatRoom chatRoom = chatRoomProvider.save();
		ChatRoomMember chatRoomSender = chatRoomMemberProvider.saveDirector(chatRoom, sender);
		ChatRoomMember chatRoomReceiver = chatRoomMemberProvider.saveMember(chatRoom, receiver);

		// 텍스트 메시지 생성
		ChatMessage chatMessage = chatMessageProvider.saveTextType(chatRoom, chatRoomSender, LocalDateTime.now());

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/chat-messages/{chatMessageId}", chatMessage.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isNoContent());

		// then
		entityManager.flush();
		entityManager.clear();

		ChatMessage deletedMessage = chatMessageProvider.findById(chatMessage.getId());
		assertThat(deletedMessage.getIsDeleted()).isTrue();
	}

	@Test
	@DisplayName("회원은 자신이 보낸 이미지 메시지를 삭제할 수 있다")
	void deleteImageChatMessage() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member sender = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member receiver = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(sender.getId());

		ChatRoom chatRoom = chatRoomProvider.save();
		ChatRoomMember chatRoomSender = chatRoomMemberProvider.saveDirector(chatRoom, sender);
		ChatRoomMember chatRoomReceiver = chatRoomMemberProvider.saveMember(chatRoom, receiver);

		// 이미지 메시지 생성
		ChatMessage chatMessage = chatMessageProvider.saveImageType(chatRoom, chatRoomSender, LocalDateTime.now());

		chatFileProvider.saveWithChatMessage(sender, chatMessage);
		chatFileProvider.saveWithChatMessage(sender, chatMessage);
		chatFileProvider.saveWithChatMessage(sender, chatMessage);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/chat-messages/{chatMessageId}", chatMessage.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isNoContent());

		// then
		entityManager.flush();
		entityManager.clear();

		ChatMessage deletedMessage = chatMessageProvider.findById(chatMessage.getId());
		assertThat(deletedMessage.getIsDeleted()).isTrue();

		List<ChatFile> deletedImage = chatFileProvider.findAll();
		assertThat(deletedImage).allSatisfy(image -> assertThat(image.getIsDeleted()).isTrue());
	}

	@Test
	@DisplayName("다른 회원이 보낸 메시지는 삭제할 수 없다")
	void deleteChatMessage_NotOwner() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member sender = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member receiver = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(receiver.getId());

		ChatRoom chatRoom = chatRoomProvider.save();
		ChatRoomMember chatRoomSender = chatRoomMemberProvider.saveDirector(chatRoom, sender);
		ChatRoomMember chatRoomReceiver = chatRoomMemberProvider.saveMember(chatRoom, receiver);

		// 텍스트 메시지 생성
		ChatMessage chatMessage = chatMessageProvider.saveTextType(chatRoom, chatRoomSender, LocalDateTime.now());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/chat-messages/{chatMessageId}", chatMessage.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.status").value(ChatMessageException.NOT_OWNED_BY.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(ChatMessageException.NOT_OWNED_BY.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(ChatMessageException.NOT_OWNED_BY.getCode()));
	}

	@Test
	@DisplayName("회원은 자신이 보낸 이미지를 삭제 할 수 있다. (텍스트 또는 이미지 타입이 아닐 경우)")
	void deleteChatMessage_NotImageOrTextType() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member sender = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member receiver = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(receiver.getId());

		ChatRoom chatRoom = chatRoomProvider.save();
		ChatRoomMember chatRoomSender = chatRoomMemberProvider.saveDirector(chatRoom, sender);
		ChatRoomMember chatRoomReceiver = chatRoomMemberProvider.saveMember(chatRoom, receiver);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, receiver);
		ServiceEstimate estimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		// 텍스트 메시지 생성
		ChatMessage chatMessage = chatMessageProvider.saveEstimateType(chatRoom, chatRoomSender, estimate,
			LocalDateTime.now());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/chat-messages/{chatMessageId}", chatMessage.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(
				ChatMessageException.CANNOT_DELETE_CHAT_MESSAGE_CAUSE_TYPE.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(
				ChatMessageException.CANNOT_DELETE_CHAT_MESSAGE_CAUSE_TYPE.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(ChatMessageException.CANNOT_DELETE_CHAT_MESSAGE_CAUSE_TYPE.getCode()));
	}

	@Test
	@DisplayName("존재하지 않는 메시지는 삭제할 수 없다")
	void deleteChatMessage_NotFound() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		Long nonExistentMessageId = 999999L;

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/chat-messages/{chatMessageId}", nonExistentMessageId)
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(ChatMessageException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(ChatMessageException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(ChatMessageException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("이미 삭제된 메시지는 다시 삭제할 수 없다")
	void deleteChatMessage_AlreadyDeleted() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member sender = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member receiver = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(sender.getId());

		ChatRoom chatRoom = chatRoomProvider.save();
		ChatRoomMember chatRoomSender = chatRoomMemberProvider.saveDirector(chatRoom, sender);
		ChatRoomMember chatRoomReceiver = chatRoomMemberProvider.saveMember(chatRoom, receiver);

		// 텍스트 메시지 생성
		ChatMessage chatMessage = chatMessageProvider.saveWithIsDeletedTrue(chatRoom, chatRoomSender,
			LocalDateTime.now());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/chat-messages/{chatMessageId}", chatMessage.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(ChatMessageException.ALREADY_DELETED.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(ChatMessageException.ALREADY_DELETED.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(ChatMessageException.ALREADY_DELETED.getCode()));
	}
}
