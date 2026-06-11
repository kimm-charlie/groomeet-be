package com.motd.be.module.member.sse.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static java.util.concurrent.TimeUnit.*;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.motd.be.BaseAsyncTest;
import com.motd.be.annotation.AsyncTest;
import com.motd.be.module.member.chat_room.dto.response.ChatRoomResponse;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.Role;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.sse.SseEventType;
import com.motd.be.redis.domain.payload.SsePayload;

import jakarta.servlet.http.Cookie;

@AsyncTest
public class SseControllerTest extends BaseAsyncTest {

	@Test
	@DisplayName("회원은 SSE 연결을 할 수 있다.")
	void connect() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		// when
		MvcResult result = mockMvc.perform(get("/api/sse/connect")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.param("role", Role.MEMBER.name())
				.accept(MediaType.TEXT_EVENT_STREAM_VALUE))
			.andExpect(status().isOk())
			.andExpect(request().asyncStarted())
			.andReturn();

		// then
		assertThat(result.getRequest().isAsyncStarted()).isTrue();
		assertThat(result.getResponse().getContentType()).isEqualTo(MediaType.TEXT_EVENT_STREAM_VALUE);

		// then
		// subscriber 가 수신했는지 확인
		await().atMost(2, SECONDS)
			.untilAsserted(() -> {
				verify(sseService, atLeastOnce()).incrementConnectionCount(any());
			});
	}

	@Test
	@DisplayName("디렉터는 SSE 연결을 할 수 있다.")
	void connectAsDirector() throws Exception {
		// given
		Member director = memberProvider.saveMemberWithDirectorInfo(
			SignInPlatform.KAKAO,
			directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, LocalDate.now())
		);

		Jwt jwtCreatedBySavedDirector = generateTokenWithMemberIdRoleDirector(director.getId());

		// when
		MvcResult result = mockMvc.perform(get("/api/sse/connect")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedDirector.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedDirector.getRefreshToken()))
				.param("role", Role.DIRECTOR.name())
				.accept(MediaType.TEXT_EVENT_STREAM_VALUE))
			.andExpect(status().isOk())
			.andExpect(request().asyncStarted())
			.andReturn();

		// then
		assertThat(result.getRequest().isAsyncStarted()).isTrue();
		assertThat(result.getResponse().getContentType()).isEqualTo(MediaType.TEXT_EVENT_STREAM_VALUE);

		await().atMost(2, SECONDS)
			.untilAsserted(() -> {
				verify(sseService, atLeastOnce()).incrementConnectionCount(any());
			});
	}

	@Test
	@DisplayName("인증되지 않은 사용자는 SSE 연결을 할 수 없다.")
	void connectWithoutAuthentication() throws Exception {
		// when & then
		mockMvc.perform(get("/api/sse/connect")
				.param("role", Role.MEMBER.name())
				.accept(MediaType.TEXT_EVENT_STREAM_VALUE))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("EventListener 로부터 SSE 이벤트를 수신하여 해당 회원에게 이벤트를 전송한다.")
	void redisToSseEventFlow() {
		Long senderId = 1L;
		Long receiverId = 2L;
		SsePayload payload = SsePayload.of(
			SseEventType.REFRESH_CHAT_ROOM_LIST,
			receiverId,
			Role.MEMBER,
			ChatRoomResponse.builder().build()
		);

		// when
		sseEventPublisher.publishSseEvent(payload);

		// then
		await().atMost(2, SECONDS).untilAsserted(() ->
			verify(sseService, atLeastOnce())
		);

		// then
		ArgumentCaptor<SsePayload<?>> captor = ArgumentCaptor.forClass(SsePayload.class);
		await().atMost(2, SECONDS).untilAsserted(() -> {
			verify(sseService, atLeastOnce()).refreshChatRoomList(captor.capture());
		});

		SsePayload received = captor.getValue();
		assertThat(received.getReceiverId()).isEqualTo(receiverId);
		assertThat(received.getEventName()).isEqualTo(payload.getEventName());
		assertThat(received.getReceiverRole()).isEqualTo(Role.MEMBER);
	}
}
