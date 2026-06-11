package com.motd.be.module.member.chat_stomp.handler;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static java.util.concurrent.TimeUnit.*;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;

import java.time.LocalDate;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.stomp.StompHeaders;

import com.motd.be.BaseAsyncTest;
import com.motd.be.annotation.AsyncTest;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.chat_stomp.SimpleFrameHandler;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;

@AsyncTest
public class DisconnectCommandHandlerTest extends BaseAsyncTest {

	@Test
	@DisplayName("회원은 웹소켓 연결을 끊을 수 있다.")
	void disconnect() throws InterruptedException, ExecutionException, TimeoutException {
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

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(sender.getId());

		// when -----------------------------
		BlockingQueue<String> messageQueue = new LinkedBlockingDeque<>();
		BlockingQueue<String> errorQueue = new LinkedBlockingDeque<>();

		// Handshake + STOMP CONNECT
		session = connectWithCookies(jwtCreatedBySavedMember.getAccessToken(),
			jwtCreatedBySavedMember.getRefreshToken(), errorQueue);

		// 구독 시도
		StompHeaders subscribeHeaders = buildSubscribeChatRoomHeaders(chatRoom.getId());

		session.subscribe(subscribeHeaders, new SimpleFrameHandler(messageQueue));

		// 연결 끊기 시도
		StompHeaders disconnectHeaders = buildDisconnectHeaders(chatRoom.getId());
		session.disconnect(disconnectHeaders);

		// then -----------------------------
		// 1) redis 구독 저장 및 접속정보 확인
		await()
			.atMost(2, SECONDS)
			.untilAsserted(() ->
				assertThat(
					redisChatRoomSubscribeProvider.countSubscriptions(chatRoom.getId(), sender.getId())).isEqualTo(0)
			);

		// 3~5초 내에 에러 프레임이 오지 않아야 함
		String error = errorQueue.poll(300, MILLISECONDS);
		assertThat(error).isNull(); // 에러가 오면 실패
	}

	@Test
	@DisplayName("회원은 웹소켓 연결을 끊을 수 있다. (회원이 2개이상의 세션을 가지고 있는 경우)")
	void disconnectWhenMemberHasMoreThanOneSession() throws InterruptedException, ExecutionException, TimeoutException {
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

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(sender.getId());

		// 회원 세션
		// redis 에 상대방 온라인 상태로 저장
		redisChatRoomSubscribeProvider.subscribe(chatRoom.getId(), sender.getId(), SESSION_ID_STR);
		redisChatRoomSubscribeProvider.subscribe(chatRoom.getId(), sender.getId(), SESSION_ID_STR + "OTHER");

		// when -----------------------------
		BlockingQueue<String> messageQueue = new LinkedBlockingDeque<>();
		BlockingQueue<String> errorQueue = new LinkedBlockingDeque<>();

		// Handshake + STOMP CONNECT
		session = connectWithCookies(jwtCreatedBySavedMember.getAccessToken(),
			jwtCreatedBySavedMember.getRefreshToken(), errorQueue);

		// 구독 시도
		StompHeaders subscribeHeaders = buildSubscribeChatRoomHeaders(chatRoom.getId());

		session.subscribe(subscribeHeaders, new SimpleFrameHandler(messageQueue));

		// 연결 끊기 시도
		StompHeaders disconnectHeaders = buildDisconnectHeaders(chatRoom.getId());
		session.disconnect(disconnectHeaders);

		// then -----------------------------
		// 1) redis 구독 저장 및 접속정보 확인
		await()
			.atMost(2, SECONDS)
			.untilAsserted(() ->
				assertThat(
					// 3개의 세션중 하나만 disconnect 되었으므로 2개가 남아있어야 함
					redisChatRoomSubscribeProvider.countSubscriptions(chatRoom.getId(), sender.getId())).isEqualTo(2)
			);

		// 3~5초 내에 에러 프레임이 오지 않아야 함
		String error = errorQueue.poll(300, MILLISECONDS);
		assertThat(error).isNull(); // 에러가 오면 실패
	}
}
