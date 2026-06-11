package com.motd.be.module.member.chat_stomp;

import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.ValidationConstants.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static java.util.concurrent.TimeUnit.*;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.motd.be.BaseAsyncTest;
import com.motd.be.annotation.AsyncTest;
import com.motd.be.exception.exceptions.ChatMessageException;
import com.motd.be.exception.exceptions.ChatRoomMemberException;
import com.motd.be.exception.exceptions.MemberException;
import com.motd.be.module.member.chat_message.dto.response.ChatMessageSendResponse;
import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_message.entity.ChatMessageEventType;
import com.motd.be.module.member.chat_message.entity.ChatMessageType;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.chat_stomp.dto.request.ChatMessageSendMessageRequest;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.dto.response.MemberResponse;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.sse.SseEventType;
import com.motd.be.redis.domain.brocker.ChatMessagePublisher;
import com.motd.be.redis.domain.brocker.ChatMessageSubscriber;
import com.motd.be.redis.domain.payload.ChatMessagePayload;

/**
 * 이름은 controller 지만 stomp 의 send 커맨드를 처리하는 controller 의 테스트 이다.
 */
@AsyncTest
public class ChatStompControllerTest extends BaseAsyncTest {

	@Autowired
	private ChatMessagePublisher chatMessagePublisher;
	@MockitoSpyBean
	private ChatMessageSubscriber chatMessageSubscriber; // 실제 구독중인 subscriber 스파이 주입

	@Test
	@DisplayName("회원은 채팅 메시지를 보낼 수 있다. (상대방이 온라인 상태가 아닌경우)")
	void sendMessageWhenOpponentNotOnlineStatus() throws
		ExecutionException,
		InterruptedException,
		TimeoutException {
		// given

		// 회원 저장
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member sender = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Member receiver = memberProvider.saveMember(SignInPlatform.GOOGLE);

		// 채팅방 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		redisChatRoomSubscribeProvider.subscribe(chatRoom.getId(), sender.getId(), SESSION_ID_STR);

		// 채팅 방 멤버 저장
		ChatRoomMember senderChatRoomMember = chatRoomMemberProvider.saveDirector(chatRoom, sender);
		ChatRoomMember receiverChatRoomMember = chatRoomMemberProvider.saveMember(chatRoom, receiver);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(sender.getId());

		DirectorService parent = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService1 = directorServiceProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService directorService2 = directorServiceProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService1, receiver);
		ServiceEstimate estimate1 = serviceEstimateProvider.save(directorInfo, serviceRequest1);

		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate1);

		//  when -----------------------------

		BlockingQueue<String> messageQueue = new LinkedBlockingDeque<>();
		BlockingQueue<String> errorQueue = new LinkedBlockingDeque<>();

		//1. Handshake + STOMP CONNECT
		session = connectWithCookies(jwtCreatedBySavedMember.getAccessToken(),
			jwtCreatedBySavedMember.getRefreshToken(), errorQueue);

		//2. 구독
		StompHeaders headers = buildSubscribeChatRoomHeaders(chatRoom.getId());
		StompHeaders errorHeaders = buildSubscribeErrorHeaders(errorQueue);

		session.subscribe(headers, new SimpleFrameHandler(messageQueue));
		session.subscribe(errorHeaders, new SimpleFrameHandler(messageQueue));

		ChatMessageSendMessageRequest request = ChatMessageSendMessageRequest.builder()
			.chatRoomId(chatRoom.getId())
			.content(CONTENT_STR)
			.chatMessageType(ChatMessageType.TEXT.name())
			.build();

		// 3. send 요청
		session.send("/pub/message", request);

		// then -----------------------------

		await()
			.atMost(4, SECONDS)
			.untilAsserted(() -> {
					// DB에 채팅 메시지가 저장되었는지 검증
					List<ChatMessage> chatMessages = chatMessageProvider.findAll();

					assertThat(chatMessages).hasSize(1);

					// 보낸 회원의 마지막 읽은 메세지 업데이트 여부 확인
					ChatRoomMember updatedSender = chatRoomMemberProvider.findById(senderChatRoomMember.getId());
					assertThat(updatedSender.getLastReadMessage().getId()).isEqualTo(chatMessages.get(0).getId());

					// 상대방이 온라인 상태가 아닌경우 마지막 읽은 메세지가 업데이트 되지 않았는지 확인한다.
					ChatRoomMember updatedReceiver = chatRoomMemberProvider.findById(receiverChatRoomMember.getId());
					assertThat(updatedReceiver.getLastReadMessage()).isNull();

					// 채팅방의 마지막 메세지가 업데이트 되었는지 확인한다.
					ChatRoom updatedChatRoom = chatRoomProvider.findById(chatRoom.getId());
					assertThat(updatedChatRoom.getLastMessage().getId()).isEqualTo(chatMessages.get(0).getId());

					// SSE 발행 검증 (REFRESH_CHAT_ROOM_LIST)
					verify(sseService, atLeastOnce()).refreshChatRoomList(
						argThat(payload ->
							payload.getEventName() == SseEventType.REFRESH_CHAT_ROOM_LIST &&
								Objects.equals(payload.getReceiverId(), receiver.getId())));

					// SSE 발행 검증 (REFRESH_NAV_CHAT_COUNT)
					verify(sseService, atLeastOnce()).refreshNavChatCount(
						argThat(payload ->
							payload.getEventName() == SseEventType.REFRESH_NAV_CHAT_COUNT &&
								payload.getReceiverId().equals(receiver.getId())));
				}
			);

		// 응답 검증
		String result = messageQueue.poll(1, SECONDS);
		assertThat(result).contains(CONTENT_STR);
	}

	@Test
	@DisplayName("회원은 채팅 메시지를 보낼 수 있다. (상대방이 송신자를 차단한 경우)")
	void sendMessageWhenOpponentBlockedSender() throws
		ExecutionException,
		InterruptedException,
		TimeoutException {
		// given

		// 회원 저장
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member sender = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Member receiver = memberProvider.saveMember(SignInPlatform.GOOGLE);

		// 채팅방 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		redisChatRoomSubscribeProvider.subscribe(chatRoom.getId(), sender.getId(), SESSION_ID_STR);

		// 채팅 방 멤버 저장
		ChatRoomMember senderChatRoomMember = chatRoomMemberProvider.saveDirector(chatRoom, sender);
		ChatRoomMember receiverChatRoomMember = chatRoomMemberProvider.saveMember(chatRoom, receiver);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(sender.getId());

		DirectorService parent = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService1 = directorServiceProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService directorService2 = directorServiceProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService1, receiver);
		ServiceEstimate estimate1 = serviceEstimateProvider.save(directorInfo, serviceRequest1);

		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate1);

		// 차단 로직 추가
		memberBlockProvider.save(receiver, sender);

		//  when -----------------------------

		BlockingQueue<String> messageQueue = new LinkedBlockingDeque<>();
		BlockingQueue<String> errorQueue = new LinkedBlockingDeque<>();

		//1. Handshake + STOMP CONNECT
		session = connectWithCookies(jwtCreatedBySavedMember.getAccessToken(),
			jwtCreatedBySavedMember.getRefreshToken(), errorQueue);

		//2. 구독
		StompHeaders headers = buildSubscribeChatRoomHeaders(chatRoom.getId());
		StompHeaders errorHeaders = buildSubscribeErrorHeaders(errorQueue);

		session.subscribe(headers, new SimpleFrameHandler(messageQueue));
		session.subscribe(errorHeaders, new SimpleFrameHandler(messageQueue));

		ChatMessageSendMessageRequest request = ChatMessageSendMessageRequest.builder()
			.chatRoomId(chatRoom.getId())
			.content(CONTENT_STR)
			.chatMessageType(ChatMessageType.TEXT.name())
			.build();

		// 3. send 요청
		session.send("/pub/message", request);

		// then -----------------------------

		await()
			.atMost(4, SECONDS)
			.untilAsserted(() -> {
					// DB에 채팅 메시지가 저장되었는지 검증
					List<ChatMessage> chatMessages = chatMessageProvider.findAll();

					assertThat(chatMessages).hasSize(1);

					// 채팅 메세지의 isVisibleToOpponent 가 false 인지 확인
					assertThat(chatMessages.get(0).getIsVisibleToOpponent()).isFalse();

					// 보낸 회원의 마지막 읽은 메세지 업데이트 여부 확인
					ChatRoomMember updatedSender = chatRoomMemberProvider.findById(senderChatRoomMember.getId());
					assertThat(updatedSender.getLastReadMessage().getId()).isEqualTo(chatMessages.get(0).getId());

					// 상대방이 마지막 읽은 메세지가 업데이트 되지 않았는지 확인한다.
					ChatRoomMember updatedReceiver = chatRoomMemberProvider.findById(receiverChatRoomMember.getId());
					assertThat(updatedReceiver.getLastReadMessage()).isNull();

					// 채팅방의 마지막 메세지가 업데이트 되었는지 확인한다.
					ChatRoom updatedChatRoom = chatRoomProvider.findById(chatRoom.getId());
					assertThat(updatedChatRoom.getLastMessage().getId()).isEqualTo(chatMessages.get(0).getId());
				}
			);

		// 응답 검증
		String result = messageQueue.poll(1, SECONDS);
		assertThat(result).contains(CONTENT_STR);
	}

	@Test
	@DisplayName("디렉터가 결제를 한 경우 디렉터가 채팅 보내는게 가능하다.")
	void sendMessageToMemberAfterChatStartPaid() throws ExecutionException, InterruptedException, TimeoutException {
		// given

		// 회원 저장
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR);
		Member sender = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Member receiver = memberProvider.saveMember(SignInPlatform.GOOGLE);

		// 채팅방 저장
		ChatRoom chatRoom = chatRoomProvider.saveWithPaidTrue();

		redisChatRoomSubscribeProvider.subscribe(chatRoom.getId(), sender.getId(), SESSION_ID_STR);

		// 채팅 방 멤버 저장
		ChatRoomMember senderChatRoomMember = chatRoomMemberProvider.saveDirector(chatRoom, sender);
		ChatRoomMember receiverChatRoomMember = chatRoomMemberProvider.saveMember(chatRoom, receiver);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(sender.getId());

		DirectorService parent = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService1 = directorServiceProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService directorService2 = directorServiceProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService1, receiver);
		ServiceEstimate estimate1 = serviceEstimateProvider.save(directorInfo, serviceRequest1);

		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate1);

		//  when -----------------------------

		BlockingQueue<String> messageQueue = new LinkedBlockingDeque<>();
		BlockingQueue<String> errorQueue = new LinkedBlockingDeque<>();

		//1. Handshake + STOMP CONNECT
		session = connectWithCookies(jwtCreatedBySavedMember.getAccessToken(),
			jwtCreatedBySavedMember.getRefreshToken(), errorQueue);

		//2. 구독
		StompHeaders headers = buildSubscribeChatRoomHeaders(chatRoom.getId());
		StompHeaders errorHeaders = buildSubscribeErrorHeaders(errorQueue);

		session.subscribe(headers, new SimpleFrameHandler(messageQueue));
		session.subscribe(errorHeaders, new SimpleFrameHandler(errorQueue));

		ChatMessageSendMessageRequest request = ChatMessageSendMessageRequest.builder()
			.chatRoomId(chatRoom.getId())
			.content(CONTENT_STR)
			.chatMessageType(ChatMessageType.TEXT.name())
			.build();

		// 3. send 요청
		session.send("/pub/message", request);

		// then -----------------------------

		await()
			.atMost(4, SECONDS)
			.untilAsserted(() -> {
					// DB에 채팅 메시지가 저장되었는지 검증
					List<ChatMessage> chatMessages = chatMessageProvider.findAll();

					assertThat(chatMessages).hasSize(1);

					// 보낸 회원의 마지막 읽은 메세지 업데이트 여부 확인
					ChatRoomMember updatedSender = chatRoomMemberProvider.findById(senderChatRoomMember.getId());
					assertThat(updatedSender.getLastReadMessage().getId()).isEqualTo(chatMessages.get(0).getId());

					// 상대방이 온라인 상태가 아닌경우 마지막 읽은 메세지가 업데이트 되지 않았는지 확인한다.
					ChatRoomMember updatedReceiver = chatRoomMemberProvider.findById(receiverChatRoomMember.getId());
					assertThat(updatedReceiver.getLastReadMessage()).isNull();

					// 채팅방의 마지막 메세지가 업데이트 되었는지 확인한다.
					ChatRoom updatedChatRoom = chatRoomProvider.findById(chatRoom.getId());
					assertThat(updatedChatRoom.getLastMessage().getId()).isEqualTo(chatMessages.get(0).getId());

					// SSE 발행 검증 (REFRESH_CHAT_ROOM_LIST)
					verify(sseService, atLeastOnce()).refreshChatRoomList(
						argThat(payload ->
							payload.getEventName() == SseEventType.REFRESH_CHAT_ROOM_LIST &&
								Objects.equals(payload.getReceiverId(), receiver.getId())));

					// SSE 발행 검증 (REFRESH_NAV_CHAT_COUNT)
					verify(sseService, atLeastOnce()).refreshNavChatCount(
						argThat(payload ->
							payload.getEventName() == SseEventType.REFRESH_NAV_CHAT_COUNT &&
								payload.getReceiverId().equals(receiver.getId())));
				}
			);

		// 응답 검증
		String result = messageQueue.poll(1, SECONDS);
		assertThat(result).contains(CONTENT_STR);
	}


	@Test
	@DisplayName("탈퇴한 회원에게는 채팅 메시지를 보낼 수 없다")
	void sendMessageToWithdrawnMember() throws ExecutionException, InterruptedException, TimeoutException {
		// given

		// 회원 저장
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member sender = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Member receiver = memberProvider.saveMemberWithdrawalTrue(LocalDateTime.now(), SignInPlatform.GOOGLE);

		// 채팅방 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		redisChatRoomSubscribeProvider.subscribe(chatRoom.getId(), sender.getId(), SESSION_ID_STR);

		// 채팅 방 멤버 저장
		chatRoomMemberProvider.saveDirector(chatRoom, sender);
		chatRoomMemberProvider.saveMember(chatRoom, receiver);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(sender.getId());

		DirectorService parent = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService1 = directorServiceProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService directorService2 = directorServiceProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService1, receiver);
		ServiceEstimate estimate1 = serviceEstimateProvider.save(directorInfo, serviceRequest1);

		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate1);

		//  when -----------------------------

		BlockingQueue<String> messageQueue = new LinkedBlockingDeque<>();
		BlockingQueue<String> errorQueue = new LinkedBlockingDeque<>();

		//1. Handshake + STOMP CONNECT
		session = connectWithCookies(jwtCreatedBySavedMember.getAccessToken(),
			jwtCreatedBySavedMember.getRefreshToken(), errorQueue);

		//2. 구독
		StompHeaders headers = buildSubscribeChatRoomHeaders(chatRoom.getId());
		StompHeaders errorHeaders = buildSubscribeErrorHeaders(errorQueue);

		session.subscribe(headers, new SimpleFrameHandler(messageQueue));
		session.subscribe(errorHeaders, new SimpleFrameHandler(errorQueue));

		ChatMessageSendMessageRequest request = ChatMessageSendMessageRequest.builder()
			.chatRoomId(chatRoom.getId())
			.content(CONTENT_STR)
			.chatMessageType(ChatMessageType.TEXT.name())
			.build();

		// 3. send 요청
		session.send("/pub/message", request);

		// then -----------------------------

		String result = errorQueue.poll(1, SECONDS);
		assertThat(result).contains(MemberException.WITHDRAWAL_MEMBER.getErrorMessage());
	}

	@Test
	@DisplayName("탈퇴한 회원에게는 채팅 메시지를 보낼 수 없다(채팅이 500자가 넘는 경우)")
	void sendMessageWithMoreThanLimitContentLength() throws ExecutionException, InterruptedException, TimeoutException {
		// given

		// 회원 저장
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member sender = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Member receiver = memberProvider.saveMemberWithdrawalTrue(LocalDateTime.now(), SignInPlatform.GOOGLE);

		// 채팅방 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		redisChatRoomSubscribeProvider.subscribe(chatRoom.getId(), sender.getId(), SESSION_ID_STR);

		// 채팅 방 멤버 저장
		chatRoomMemberProvider.saveDirector(chatRoom, sender);
		chatRoomMemberProvider.saveMember(chatRoom, receiver);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(sender.getId());

		DirectorService parent = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService1 = directorServiceProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService directorService2 = directorServiceProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService1, receiver);
		ServiceEstimate estimate1 = serviceEstimateProvider.save(directorInfo, serviceRequest1);

		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate1);

		//  when -----------------------------

		BlockingQueue<String> messageQueue = new LinkedBlockingDeque<>();
		BlockingQueue<String> errorQueue = new LinkedBlockingDeque<>();

		//1. Handshake + STOMP CONNECT
		session = connectWithCookies(jwtCreatedBySavedMember.getAccessToken(),
			jwtCreatedBySavedMember.getRefreshToken(), errorQueue);

		//2. 구독
		StompHeaders headers = buildSubscribeChatRoomHeaders(chatRoom.getId());
		StompHeaders errorHeaders = buildSubscribeErrorHeaders(errorQueue);

		session.subscribe(headers, new SimpleFrameHandler(messageQueue));
		session.subscribe(errorHeaders, new SimpleFrameHandler(errorQueue));

		ChatMessageSendMessageRequest request = ChatMessageSendMessageRequest.builder()
			.chatRoomId(chatRoom.getId())
			.content("a".repeat(CHAT_MESSAGE_MAX_LENGTH + 1))
			.chatMessageType(ChatMessageType.TEXT.name())
			.build();

		// 3. send 요청
		session.send("/pub/message", request);

		// then -----------------------------

		String result = errorQueue.poll(1, SECONDS);
		assertThat(result).contains(ChatMessageException.CONTENT_LENGTH_EXCEEDED.getErrorMessage());
	}

	@Test
	@DisplayName("탈퇴한 회원에게는 채팅 메시지를 보낼 수 없다(채팅이 0글자인경우)")
	void sendMessageWithContent0Length() throws ExecutionException, InterruptedException, TimeoutException {
		// given

		// 회원 저장
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member sender = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Member receiver = memberProvider.saveMemberWithdrawalTrue(LocalDateTime.now(), SignInPlatform.GOOGLE);

		// 채팅방 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		redisChatRoomSubscribeProvider.subscribe(chatRoom.getId(), sender.getId(), SESSION_ID_STR);

		// 채팅 방 멤버 저장
		chatRoomMemberProvider.saveDirector(chatRoom, sender);
		chatRoomMemberProvider.saveMember(chatRoom, receiver);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(sender.getId());

		DirectorService parent = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService1 = directorServiceProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService directorService2 = directorServiceProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService1, receiver);
		ServiceEstimate estimate1 = serviceEstimateProvider.save(directorInfo, serviceRequest1);

		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate1);

		//  when -----------------------------

		BlockingQueue<String> messageQueue = new LinkedBlockingDeque<>();
		BlockingQueue<String> errorQueue = new LinkedBlockingDeque<>();

		//1. Handshake + STOMP CONNECT
		session = connectWithCookies(jwtCreatedBySavedMember.getAccessToken(),
			jwtCreatedBySavedMember.getRefreshToken(), errorQueue);

		//2. 구독
		StompHeaders headers = buildSubscribeChatRoomHeaders(chatRoom.getId());
		StompHeaders errorHeaders = buildSubscribeErrorHeaders(errorQueue);

		session.subscribe(headers, new SimpleFrameHandler(messageQueue));
		session.subscribe(errorHeaders, new SimpleFrameHandler(errorQueue));

		ChatMessageSendMessageRequest request = ChatMessageSendMessageRequest.builder()
			.chatRoomId(chatRoom.getId())
			.content("")
			.chatMessageType(ChatMessageType.TEXT.name())
			.build();

		// 3. send 요청
		session.send("/pub/message", request);

		// then -----------------------------

		String result = errorQueue.poll(1, SECONDS);
		assertThat(result).contains(ChatMessageException.CONTENT_REQUIRED.getErrorMessage());
	}

	@Test
	@DisplayName("디렉터가 아직 결제를 하지 않은 경우 일반 사용자는 채팅을 보낼수 있다.")
	void sendMessageToDirectorBeforeChatStartPaid() throws ExecutionException, InterruptedException, TimeoutException {
		// given
		// 회원 저장
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR);
		Member receiver = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Member sender = memberProvider.saveMember(SignInPlatform.GOOGLE);

		// 채팅방 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		redisChatRoomSubscribeProvider.subscribe(chatRoom.getId(), sender.getId(), SESSION_ID_STR);

		// 채팅 방 멤버 저장
		ChatRoomMember receiverChatRoomMember = chatRoomMemberProvider.saveDirector(chatRoom, receiver);
		ChatRoomMember senderChatRoomMember = chatRoomMemberProvider.saveMember(chatRoom, sender);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(sender.getId());

		DirectorService parent = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService1 = directorServiceProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService directorService2 = directorServiceProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService1, sender);
		ServiceEstimate estimate1 = serviceEstimateProvider.save(directorInfo, serviceRequest1);

		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate1);

		//  when -----------------------------

		BlockingQueue<String> messageQueue = new LinkedBlockingDeque<>();
		BlockingQueue<String> errorQueue = new LinkedBlockingDeque<>();

		//1. Handshake + STOMP CONNECT
		session = connectWithCookies(jwtCreatedBySavedMember.getAccessToken(),
			jwtCreatedBySavedMember.getRefreshToken(), errorQueue);

		//2. 구독
		StompHeaders headers = buildSubscribeChatRoomHeaders(chatRoom.getId());
		StompHeaders errorHeaders = buildSubscribeErrorHeaders(errorQueue);

		session.subscribe(headers, new SimpleFrameHandler(messageQueue));
		session.subscribe(errorHeaders, new SimpleFrameHandler(errorQueue));

		ChatMessageSendMessageRequest request = ChatMessageSendMessageRequest.builder()
			.chatRoomId(chatRoom.getId())
			.content(CONTENT_STR)
			.chatMessageType(ChatMessageType.TEXT.name())
			.build();

		// 3. send 요청
		session.send("/pub/message", request);

		// then -----------------------------
		await()
			.atMost(4, SECONDS)
			.untilAsserted(() -> {
					// DB에 채팅 메시지가 저장되었는지 검증
					List<ChatMessage> chatMessages = chatMessageProvider.findAll();

					assertThat(chatMessages).hasSize(1);

					// 보낸 회원의 마지막 읽은 메세지 업데이트 여부 확인
					ChatRoomMember updatedSender = chatRoomMemberProvider.findById(senderChatRoomMember.getId());
					assertThat(updatedSender.getLastReadMessage().getId()).isEqualTo(chatMessages.get(0).getId());

					// 상대방이 온라인 상태가 아닌경우 마지막 읽은 메세지가 업데이트 되지 않았는지 확인한다.
					ChatRoomMember updatedReceiver = chatRoomMemberProvider.findById(receiverChatRoomMember.getId());
					assertThat(updatedReceiver.getLastReadMessage()).isNull();

					// 채팅방의 마지막 메세지가 업데이트 되었는지 확인한다.
					ChatRoom updatedChatRoom = chatRoomProvider.findById(chatRoom.getId());
					assertThat(updatedChatRoom.getLastMessage().getId()).isEqualTo(chatMessages.get(0).getId());

					// receiver 가 다시 참여한 상태인지 검증한다.
					ChatRoomMember updatedReceiverMember = chatRoomMemberProvider.findById(receiverChatRoomMember.getId());

					assertThat(updatedReceiverMember.getIsChatRoomDeleted()).isFalse();
				}
			);

		// 응답 검증
		String result = messageQueue.poll(1, SECONDS);
		assertThat(result).contains(CONTENT_STR);
	}

	@Test
	@DisplayName("회원은 채팅 메시지를 보낼 수 있다. (상대방이 채팅방을 나간 상태인 경우)")
	void sendMessageWhenOpponentDeletedChatRoom() throws
		ExecutionException,
		InterruptedException,
		TimeoutException {
		// given

		// 회원 저장
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member sender = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Member receiver = memberProvider.saveMember(SignInPlatform.GOOGLE);

		// 채팅방 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		redisChatRoomSubscribeProvider.subscribe(chatRoom.getId(), sender.getId(), SESSION_ID_STR);

		// 채팅 방 멤버 저장
		ChatRoomMember senderChatRoomMember = chatRoomMemberProvider.saveDirector(chatRoom, sender);
		ChatRoomMember receiverChatRoomMember = chatRoomMemberProvider.saveMemberWithRoomDeletedTrue(chatRoom,
			receiver);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(sender.getId());

		DirectorService parent = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService1 = directorServiceProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService directorService2 = directorServiceProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService1, receiver);
		ServiceEstimate estimate1 = serviceEstimateProvider.save(directorInfo, serviceRequest1);

		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate1);

		//  when -----------------------------

		BlockingQueue<String> messageQueue = new LinkedBlockingDeque<>();
		BlockingQueue<String> errorQueue = new LinkedBlockingDeque<>();

		//1. Handshake + STOMP CONNECT
		session = connectWithCookies(jwtCreatedBySavedMember.getAccessToken(),
			jwtCreatedBySavedMember.getRefreshToken(), errorQueue);

		//2. 구독
		StompHeaders headers = buildSubscribeChatRoomHeaders(chatRoom.getId());
		StompHeaders errorHeaders = buildSubscribeErrorHeaders(errorQueue);

		session.subscribe(headers, new SimpleFrameHandler(messageQueue));
		session.subscribe(errorHeaders, new SimpleFrameHandler(messageQueue));

		ChatMessageSendMessageRequest request = ChatMessageSendMessageRequest.builder()
			.chatRoomId(chatRoom.getId())
			.content(CONTENT_STR)
			.chatMessageType(ChatMessageType.TEXT.name())
			.build();

		// 3. send 요청
		session.send("/pub/message", request);

		// then -----------------------------

		await()
			.atMost(4, SECONDS)
			.untilAsserted(() -> {
					// DB에 채팅 메시지가 저장되었는지 검증
					List<ChatMessage> chatMessages = chatMessageProvider.findAll();

					assertThat(chatMessages).hasSize(1);

					// 보낸 회원의 마지막 읽은 메세지 업데이트 여부 확인
					ChatRoomMember updatedSender = chatRoomMemberProvider.findById(senderChatRoomMember.getId());
					assertThat(updatedSender.getLastReadMessage().getId()).isEqualTo(chatMessages.get(0).getId());

					// 상대방이 온라인 상태가 아닌경우 마지막 읽은 메세지가 업데이트 되지 않았는지 확인한다.
					ChatRoomMember updatedReceiver = chatRoomMemberProvider.findById(receiverChatRoomMember.getId());
					assertThat(updatedReceiver.getLastReadMessage()).isNull();

					// 채팅방의 마지막 메세지가 업데이트 되었는지 확인한다.
					ChatRoom updatedChatRoom = chatRoomProvider.findById(chatRoom.getId());
					assertThat(updatedChatRoom.getLastMessage().getId()).isEqualTo(chatMessages.get(0).getId());

					// receiver 가 다시 참여한 상태인지 검증한다.
					ChatRoomMember updatedReceiverMember = chatRoomMemberProvider.findById(receiverChatRoomMember.getId());

					assertThat(updatedReceiverMember.getIsChatRoomDeleted()).isFalse();
				}
			);

		// 응답 검증
		String result = messageQueue.poll(1, SECONDS);
		assertThat(result).contains(CONTENT_STR);
	}

	@Test
	@DisplayName("회원은 채팅 메시지를 보낼 수 있다. (상대방이 온라인 상태 인 경우)")
	void sendMessageWhenOpponentOnlineStatus() throws
		ExecutionException,
		InterruptedException,
		TimeoutException {
		// given

		// 회원 저장
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member sender = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Member receiver = memberProvider.saveMember(SignInPlatform.APPLE);

		// 채팅방 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		// 채팅 방 멤버 저장
		ChatRoomMember senderChatRoomMember = chatRoomMemberProvider.saveDirector(chatRoom, sender);
		ChatRoomMember receiverChatRoomMember = chatRoomMemberProvider.saveMember(chatRoom, receiver);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(sender.getId());

		// redis 에 상대방 온라인 상태로 저장
		redisChatRoomSubscribeProvider.subscribe(chatRoom.getId(), sender.getId(), SESSION_ID_STR);
		redisChatRoomSubscribeProvider.subscribe(chatRoom.getId(), receiver.getId(), SESSION_ID_STR);

		DirectorService parent = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService1 = directorServiceProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService directorService2 = directorServiceProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService1, receiver);
		ServiceEstimate estimate1 = serviceEstimateProvider.save(directorInfo, serviceRequest1);

		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate1);

		//  when -----------------------------

		BlockingQueue<String> messageQueue = new LinkedBlockingDeque<>();
		BlockingQueue<String> errorQueue = new LinkedBlockingDeque<>();

		//1. Handshake + STOMP CONNECT
		session = connectWithCookies(jwtCreatedBySavedMember.getAccessToken(),
			jwtCreatedBySavedMember.getRefreshToken(), errorQueue);

		//2. 구독
		StompHeaders headers = buildSubscribeChatRoomHeaders(chatRoom.getId());
		StompHeaders errorHeaders = buildSubscribeErrorHeaders(errorQueue);

		session.subscribe(headers, new SimpleFrameHandler(messageQueue));
		session.subscribe(errorHeaders, new SimpleFrameHandler(messageQueue));

		ChatMessageSendMessageRequest request = ChatMessageSendMessageRequest.builder()
			.chatRoomId(chatRoom.getId())
			.content(CONTENT_STR)
			.chatMessageType(ChatMessageType.TEXT.name())
			.build();

		// 3. send 요청
		session.send("/pub/message", request);

		// then -----------------------------

		await()
			.atMost(4, SECONDS)
			.untilAsserted(() -> {
					// DB에 채팅 메시지가 저장되었는지 검증
					List<ChatMessage> chatMessages = chatMessageProvider.findAll();

					assertThat(chatMessages).hasSize(1);

					// 보낸 회원의 마지막 읽은 메세지 업데이트 여부 확인
					ChatRoomMember updatedSender = chatRoomMemberProvider.findById(senderChatRoomMember.getId());
					assertThat(updatedSender.getLastReadMessage().getId()).isEqualTo(chatMessages.get(0).getId());

					// 상대방이 온라인 상태인경우 마지막 필드가 업데이트 되야 한다
					ChatRoomMember updatedReceiver = chatRoomMemberProvider.findById(receiverChatRoomMember.getId());
					assertThat(updatedReceiver.getLastReadMessage().getId()).isEqualTo(chatMessages.get(0).getId());

					// 채팅방의 마지막 메세지가 업데이트 되었는지 확인한다.
					ChatRoom updatedChatRoom = chatRoomProvider.findById(chatRoom.getId());
					assertThat(updatedChatRoom.getLastMessage().getId()).isEqualTo(chatMessages.get(0).getId());
				}
			);

		// 응답 검증
		String result = messageQueue.poll(1, SECONDS);
		assertThat(result).contains(CONTENT_STR);
	}

	@Test
	@DisplayName("회원은 채팅 메시지를 보낼 수 있다. (채팅방에 없는 사용자가 메세지를 보내려고 할때)")
	void sendMessageSenderNotExistingInChatRoom() throws
		ExecutionException,
		InterruptedException,
		TimeoutException {
		// given

		// 회원 저장
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member sender = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Member receiver = memberProvider.saveMember(SignInPlatform.APPLE);
		Member otherMember = memberProvider.saveMember(SignInPlatform.GOOGLE);

		// 채팅방 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		// 채팅 방 멤버 저장
		ChatRoomMember senderChatRoomMember = chatRoomMemberProvider.saveDirector(chatRoom, sender);
		ChatRoomMember receiverChatRoomMember = chatRoomMemberProvider.saveMember(chatRoom, receiver);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(otherMember.getId());

		//  when -----------------------------

		BlockingQueue<String> messageQueue = new LinkedBlockingDeque<>();
		BlockingQueue<String> errorQueue = new LinkedBlockingDeque<>();

		//1. Handshake + STOMP CONNECT
		session = connectWithCookies(jwtCreatedBySavedMember.getAccessToken(),
			jwtCreatedBySavedMember.getRefreshToken(), errorQueue);

		//2. 구독
		StompHeaders headers = buildSubscribeChatRoomHeaders(chatRoom.getId());
		StompHeaders errorHeaders = buildSubscribeErrorHeaders(errorQueue);

		session.subscribe(headers, new SimpleFrameHandler(messageQueue));
		session.subscribe(errorHeaders, new SimpleFrameHandler(messageQueue));

		ChatMessageSendMessageRequest request = ChatMessageSendMessageRequest.builder()
			.chatRoomId(chatRoom.getId())
			.content(CONTENT_STR)
			.chatMessageType(ChatMessageType.TEXT.name())
			.build();

		// 3. send 요청
		session.send("/pub/message", request);

		// then -----------------------------

		// 응답 검증
		String result = errorQueue.poll(1, SECONDS);
		assertThat(result).contains(ChatRoomMemberException.NOT_IN_CHAT_ROOM.getErrorMessage());
	}

	@Test
	@DisplayName("Redis Pub/Sub으로 채팅 메시지가 publish-subscribe된다")
	void publishAndSubscribeChatMessage() {
		// given
		ChatMessagePayload<ChatMessageSendResponse> payload = ChatMessagePayload.<ChatMessageSendResponse>builder()
			.chatRoomId(123L)
			.data(
				ChatMessageSendResponse.builder()
					.chatRoomId(123L)
					.sender(MemberResponse.builder()
						.id(ID)
						.nickname(NICKNAME_STR)
						.profileImageUrl(PROFILE_IMAGE_STR)
						.build())
					.content("테스트 메시지")
					.chatMessageType("TEXT")
					.build()
			)
			.eventType(ChatMessageEventType.SEND)
			.build();

		// when
		chatMessagePublisher.publish(payload);

		// then
		await()
			.atMost(3, TimeUnit.SECONDS)
			.untilAsserted(() ->
				verify(chatMessageSubscriber, atLeastOnce())
					.onMessage(any(Message.class), any())
			);
	}
}

