package com.motd.be.module.member.chat_stomp.handler;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static java.util.concurrent.TimeUnit.*;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.stomp.StompHeaders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.motd.be.BaseAsyncTest;
import com.motd.be.annotation.AsyncTest;
import com.motd.be.exception.exceptions.ChatRoomMemberException;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.chat_stomp.SimpleFrameHandler;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;

@AsyncTest
public class SubscribeCommandHandlerTest extends BaseAsyncTest {

	@Test
	@DisplayName("회원은 채팅방 구독을 할 수 있다.")
	void subscribe() throws InterruptedException, ExecutionException, TimeoutException {
		// given

		// 회원 저장
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member sender = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Member receiver = memberProvider.saveMember(SignInPlatform.APPLE);

		// 채팅방 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		// 채팅 방 멤버 저장
		ChatRoomMember directorChatRoomMember = chatRoomMemberProvider.saveDirector(chatRoom, sender);
		ChatRoomMember requesterChatRoomMember = chatRoomMemberProvider.saveMember(chatRoom, receiver);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(receiver.getId());

		// when -----------------------------
		BlockingQueue<String> messageQueue = new LinkedBlockingDeque<>();
		BlockingQueue<String> errorQueue = new LinkedBlockingDeque<>();

		// Handshake + STOMP CONNECT
		session = connectWithCookies(jwtCreatedBySavedMember.getAccessToken(),
			jwtCreatedBySavedMember.getRefreshToken(), errorQueue);

		StompHeaders headers = buildSubscribeChatRoomHeaders(chatRoom.getId());

		// 구독 시도
		session.subscribe(headers, new SimpleFrameHandler(messageQueue));

		// then -----------------------------
		// 1) redis 구독 저장 및 접속정보 확인
		await()
			.atMost(1, SECONDS)
			.untilAsserted(() -> {
					assertThat(
						redisChatRoomSubscribeProvider.countSubscriptions(chatRoom.getId(), receiver.getId())).isEqualTo(
						1L);

					assertThat(redisChatSessionInfoProvider.countAllSessions()).isEqualTo(1);
				}
			);

		// 3~5초 내에 에러 프레임이 오지 않아야 함
		String error = errorQueue.poll(300, MILLISECONDS);
		assertThat(error).isNull(); // 에러가 오면 실패
	}

	@Test
	@DisplayName("회원은 채팅방 구독을 할 수 있다. (자신이 속한 채팅방이 아닐떄)")
	void subscribeWhenNotInChatRoomMember() throws
		ExecutionException,
		InterruptedException,
		TimeoutException,
		JsonProcessingException {
		// given

		// 회원 저장
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member sender = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Member receiver = memberProvider.saveMember(SignInPlatform.APPLE);

		Member otherSender = memberProvider.saveMember(SignInPlatform.GOOGLE);

		// 채팅방 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		// 채팅 방 멤버 저장
		ChatRoomMember directorChatRoomMember = chatRoomMemberProvider.saveDirector(chatRoom, sender);
		ChatRoomMember requesterChatRoomMember = chatRoomMemberProvider.saveMember(chatRoom, receiver);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(otherSender.getId());

		// when -----------------------------
		BlockingQueue<String> messageQueue = new LinkedBlockingDeque<>();
		BlockingQueue<String> errorQueue = new LinkedBlockingDeque<>();

		// Handshake + STOMP CONNECT
		session = connectWithCookies(jwtCreatedBySavedMember.getAccessToken(),
			jwtCreatedBySavedMember.getRefreshToken(), errorQueue);

		StompHeaders headers = buildSubscribeChatRoomHeaders(chatRoom.getId());

		// 구독 시도
		session.subscribe(headers, new SimpleFrameHandler(messageQueue));

		// then -----------------------------
		// 1) redis 구독 저장 및 접속정보 확인
		await()
			.atMost(1, SECONDS)
			.untilAsserted(() -> {
					assertThat(
						redisChatRoomSubscribeProvider.countSubscriptions(chatRoom.getId(), sender.getId())).isEqualTo(0);

					assertThat(redisChatSessionInfoProvider.countAllSessions()).isEqualTo(0);
				}
			);

		// 3~5초 내에 에러 프레임이 와야한다.
		String errorPayload = errorQueue.poll(3, SECONDS);

		Map<String, Object> payloadMap = objectMapper.readValue(errorPayload, new TypeReference<>() {
		});
		assertThat(payloadMap.get(CODE_STR)).isEqualTo(ChatRoomMemberException.NOT_IN_CHAT_ROOM.getCode());
		assertThat(payloadMap.get(MESSAGE_STR)).isEqualTo(ChatRoomMemberException.NOT_IN_CHAT_ROOM.getErrorMessage());
		assertThat(payloadMap.get(ERROR_TYPE_STR)).isEqualTo(
			ChatRoomMemberException.NOT_IN_CHAT_ROOM.getClass().getSimpleName());
	}
}
