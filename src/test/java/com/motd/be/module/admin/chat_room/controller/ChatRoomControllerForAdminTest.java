package com.motd.be.module.admin.chat_room.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.Constants.EMAIL;
import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.provider.module.admin.AdminTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.dreamsecurity.json.JSONObject;
import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.module.admin.admin.entity.Admin;
import com.motd.be.module.admin.chat_message.dto.response.ChatMessageFindAllResponseForAdmin;
import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

@ControllerIntegrationTest
public class ChatRoomControllerForAdminTest extends BaseIntegrationTest {

	@Test
	@DisplayName("관리자는 serviceEstimateId로 채팅방 상세 정보를 조회할 수 있다.")
	void findDetail() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		DirectorService parentService = directorServiceProvider.save("헬스케어", null);
		DirectorService childService = directorServiceProvider.save("PT", parentService);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(childService, member);
		ServiceEstimate serviceEstimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		// 채팅방 생성 및 매핑
		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveMember(chatRoom, member);
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/chat-rooms")
					.param("serviceEstimateId", String.valueOf(serviceEstimate.getId()))
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		assertThat(jsonObject.getLong("chatRoomId")).isEqualTo(chatRoom.getId());
		assertThat(jsonObject.getLong("serviceEstimateId")).isEqualTo(serviceEstimate.getId());
		assertThat(jsonObject.has("director")).isTrue();
		assertThat(jsonObject.has("member")).isTrue();
		assertThat(jsonObject.getJSONObject("director").getLong("id")).isEqualTo(director.getId());
		assertThat(jsonObject.getJSONObject("member").getLong("id")).isEqualTo(member.getId());
	}

	@Test
	@DisplayName("존재하지 않는 serviceEstimateId로 조회 시 404 에러가 발생한다.")
	void findDetail_notFound() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		Long invalidServiceEstimateId = 99999L;

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/chat-rooms")
					.param("serviceEstimateId", String.valueOf(invalidServiceEstimateId))
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("관리자 권한 없이 채팅방 조회 시 401 에러가 발생한다.")
	void findDetail_unauthorized() throws Exception {
		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/chat-rooms")
					.param("serviceEstimateId", "1")
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("관리자는 serviceEstimateId로 채팅 메시지를 조회할 수 있다.")
	void findMessages() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		DirectorService parentService = directorServiceProvider.save("헬스케어", null);
		DirectorService childService = directorServiceProvider.save("PT", parentService);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(childService, member);
		ServiceEstimate serviceEstimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		ChatRoom chatRoom = chatRoomProvider.save();
		ChatRoomMember chatRoomMemberForMember = chatRoomMemberProvider.saveMember(chatRoom, member);
		ChatRoomMember chatRoomMemberForDirector = chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		LocalDateTime now = LocalDateTime.now();
		ChatMessage message1 = chatMessageProvider.saveTextType(chatRoom, chatRoomMemberForMember,
			now.minusMinutes(5));
		ChatMessage message2 = chatMessageProvider.saveTextType(chatRoom, chatRoomMemberForDirector,
			now.minusMinutes(3));
		ChatMessage message3 = chatMessageProvider.saveTextType(chatRoom, chatRoomMemberForMember,
			now.minusMinutes(1));

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/chat-rooms/messages")
					.param(SERVICE_ESTIMATE_ID, String.valueOf(serviceEstimate.getId()))
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		ChatMessageFindAllResponseForAdmin response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			ChatMessageFindAllResponseForAdmin.class);

		assertThat(response.getChatMessages()).hasSize(3);
		// 최신 순으로 정렬되어야 함
		assertThat(response.getChatMessages().get(0).getId()).isEqualTo(message3.getId());
		assertThat(response.getChatMessages().get(1).getId()).isEqualTo(message2.getId());
		assertThat(response.getChatMessages().get(2).getId()).isEqualTo(message1.getId());
		assertThat(response.getHasNext()).isFalse();
	}

	@Test
	@DisplayName("관리자는 채팅 메시지를 커서 기반으로 페이징 조회할 수 있다.")
	void findMessages_withCursor() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		DirectorService parentService = directorServiceProvider.save("헬스케어", null);
		DirectorService childService = directorServiceProvider.save("PT", parentService);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(childService, member);
		ServiceEstimate serviceEstimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		ChatRoom chatRoom = chatRoomProvider.save();
		ChatRoomMember chatRoomMemberForMember = chatRoomMemberProvider.saveMember(chatRoom, member);
		ChatRoomMember chatRoomMemberForDirector = chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		LocalDateTime now = LocalDateTime.now();
		ChatMessage message1 = chatMessageProvider.saveTextType(chatRoom, chatRoomMemberForMember,
			now.minusMinutes(10));
		ChatMessage message2 = chatMessageProvider.saveTextType(chatRoom, chatRoomMemberForDirector,
			now.minusMinutes(8));
		ChatMessage message3 = chatMessageProvider.saveTextType(chatRoom, chatRoomMemberForMember,
			now.minusMinutes(6));

		entityManager.flush();
		entityManager.clear();

		// when - lastMessageId로 커서 페이징
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/chat-rooms/messages")
					.param(SERVICE_ESTIMATE_ID, String.valueOf(serviceEstimate.getId()))
					.param(LAST_MESSAGE_ID, String.valueOf(message3.getId()))
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then - message3보다 이전 메시지들만 조회됨
		ChatMessageFindAllResponseForAdmin response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			ChatMessageFindAllResponseForAdmin.class);

		assertThat(response.getChatMessages()).hasSize(2);
		assertThat(response.getChatMessages().get(0).getId()).isEqualTo(message2.getId());
		assertThat(response.getChatMessages().get(1).getId()).isEqualTo(message1.getId());
		assertThat(response.getHasNext()).isFalse();
	}

	@Test
	@DisplayName("관리자는 삭제된 메시지도 조회할 수 있다.")
	void findMessages_includesDeletedMessages() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		DirectorService parentService = directorServiceProvider.save("헬스케어", null);
		DirectorService childService = directorServiceProvider.save("PT", parentService);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(childService, member);
		ServiceEstimate serviceEstimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		ChatRoom chatRoom = chatRoomProvider.save();
		ChatRoomMember chatRoomMemberForMember = chatRoomMemberProvider.saveMember(chatRoom, member);
		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		LocalDateTime now = LocalDateTime.now();
		ChatMessage message1 = chatMessageProvider.saveTextType(chatRoom, chatRoomMemberForMember,
			now.minusMinutes(5));
		ChatMessage message2 = chatMessageProvider.saveWithIsDeletedTrue(chatRoom, chatRoomMemberForMember,
			now.minusMinutes(3));
		ChatMessage message3 = chatMessageProvider.saveTextType(chatRoom, chatRoomMemberForMember,
			now.minusMinutes(1));

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/chat-rooms/messages")
					.param(SERVICE_ESTIMATE_ID, String.valueOf(serviceEstimate.getId()))
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then - 관리자는 삭제된 메시지도 볼 수 있어야 함
		ChatMessageFindAllResponseForAdmin response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			ChatMessageFindAllResponseForAdmin.class);

		assertThat(response.getChatMessages()).hasSize(3);
		assertThat(response.getChatMessages().get(0).getId()).isEqualTo(message3.getId());
		assertThat(response.getChatMessages().get(1).getId()).isEqualTo(message2.getId());
		assertThat(response.getChatMessages().get(2).getId()).isEqualTo(message1.getId());
	}

	@Test
	@DisplayName("관리자는 isVisibleToOpponent가 false인 메시지도 조회할 수 있다.")
	void findMessages_includesInvisibleMessages() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		DirectorService parentService = directorServiceProvider.save("헬스케어", null);
		DirectorService childService = directorServiceProvider.save("PT", parentService);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(childService, member);
		ServiceEstimate serviceEstimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		ChatRoom chatRoom = chatRoomProvider.save();
		ChatRoomMember chatRoomMemberForMember = chatRoomMemberProvider.saveMember(chatRoom, member);
		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		ChatMessage message1 = chatMessageProvider.saveTextType(chatRoom, chatRoomMemberForMember,
			LocalDateTime.now().minusMinutes(5));
		ChatMessage message2 = chatMessageProvider.saveWithIsVisibleToOpponentFalse(chatRoom, chatRoomMemberForMember);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/chat-rooms/messages")
					.param(SERVICE_ESTIMATE_ID, String.valueOf(serviceEstimate.getId()))
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then - 관리자는 isVisibleToOpponent=false 메시지도 볼 수 있어야 함
		ChatMessageFindAllResponseForAdmin response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			ChatMessageFindAllResponseForAdmin.class);

		assertThat(response.getChatMessages()).hasSize(2);
	}
}
