package com.motd.be.module.director.chat_room.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.module.director.chat_room.dto.response.ChatRoomFindAllResponseForDirector;
import com.motd.be.module.director.chat_room.dto.response.ChatRoomFindChatRoomServicesResponseForDirector;
import com.motd.be.module.director.chat_room.dto.response.ChatRoomResponseForDirector;
import com.motd.be.module.director.chat_room.dto.response.ChatRoomUnreadCountResponseForDirector;
import com.motd.be.module.director.director_service.dto.response.DirectorServiceWithFullNameResponseForDirector;
import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.director_service_mapping.entity.DirectorServiceMapping;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class ChatRoomControllerForDirectorTest extends BaseIntegrationTest {

	@Test
	@DisplayName("디렉터는 참여한 채팅방 목록을 조회할 수 있다. ")
	void findAllWhenHasRoleDirector() throws Exception {
		// given
		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo2);

		DirectorInfo directorInfo3 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director3 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo3);

		DirectorInfo directorInfo4 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member requester = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo4);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		// 서비스 매핑 저장
		DirectorServiceMapping mapping1 = directorServiceMappingProvider.save(directorInfo1, directorService1);
		DirectorServiceMapping mapping2 = directorServiceMappingProvider.save(directorInfo2, directorService1);
		DirectorServiceMapping mapping3 = directorServiceMappingProvider.save(directorInfo3, directorService1);

		DirectorServiceMapping mapping4 = directorServiceMappingProvider.save(directorInfo2, directorService2);
		DirectorServiceMapping mapping5 = directorServiceMappingProvider.save(directorInfo3, directorService2);

		DirectorServiceMapping mapping6 = directorServiceMappingProvider.save(directorInfo4, directorService2);

		// 요청 저장
		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService1, requester);
		ServiceRequest serviceRequest2 = serviceRequestProvider.saveWithIsCompletedTrue(directorService2, requester,
			LocalDateTime.now());

		ServiceRequest serviceRequest3 = serviceRequestProvider.savePending(directorService2, director1);
		ServiceRequest serviceRequest4 = serviceRequestProvider.saveWithIsCompletedTrue(directorService2, director2,
			LocalDateTime.now());

		// 제안 저장
		ServiceEstimate estimate1 = serviceEstimateProvider.save(directorInfo1, serviceRequest1);
		ServiceEstimate estimate2 = serviceEstimateProvider.save(directorInfo2, serviceRequest1);
		ServiceEstimate estimate3 = serviceEstimateProvider.save(directorInfo3, serviceRequest1);

		ServiceEstimate estimate4 = serviceEstimateProvider.saveDirectorDone(directorInfo2, serviceRequest2,
			LocalDateTime.now());
		ServiceEstimate estimate5 = serviceEstimateProvider.save(directorInfo3, serviceRequest2);

		ServiceEstimate estimate6 = serviceEstimateProvider.save(directorInfo4, serviceRequest3);
		ServiceEstimate estimate7 = serviceEstimateProvider.save(directorInfo4, serviceRequest4);

		// 채팅방 저장
		ChatRoom chatRoom1 = chatRoomProvider.save();
		ChatRoom chatRoom2 = chatRoomProvider.save();
		ChatRoom chatRoom3 = chatRoomProvider.save();
		ChatRoom chatRoom4 = chatRoomProvider.save();
		ChatRoom chatRoom5 = chatRoomProvider.save();
		ChatRoom chatRoom6 = chatRoomProvider.save();
		ChatRoom chatRoom7 = chatRoomProvider.save();

		// 채팅방 제안 매핑 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom1, estimate1);
		chatRoomServiceEstimateMappingProvider.save(chatRoom2, estimate2);
		chatRoomServiceEstimateMappingProvider.save(chatRoom3, estimate3);

		chatRoomServiceEstimateMappingProvider.save(chatRoom4, estimate4);
		chatRoomServiceEstimateMappingProvider.save(chatRoom5, estimate5);

		chatRoomServiceEstimateMappingProvider.save(chatRoom6, estimate6);
		chatRoomServiceEstimateMappingProvider.save(chatRoom7, estimate7);

		// 채팅방 참여자 매핑 저장
		ChatRoomMember chatRoomMemberForRoom1Member = chatRoomMemberProvider.saveMember(chatRoom1, requester);
		ChatRoomMember chatRoomMemberForRoom1Director = chatRoomMemberProvider.saveDirector(chatRoom1, director1);

		ChatRoomMember chatRoomMemberForRoom2Member = chatRoomMemberProvider.saveMember(chatRoom2, requester);
		ChatRoomMember chatRoomMemberForRoom2Director = chatRoomMemberProvider.saveDirector(chatRoom2, director2);

		ChatRoomMember chatRoomMemberForRoom3Member = chatRoomMemberProvider.saveMember(chatRoom3, requester);
		ChatRoomMember chatRoomMemberForRoom3Director = chatRoomMemberProvider.saveDirector(chatRoom3, director3);

		ChatRoomMember chatRoomMemberForRoom4Member = chatRoomMemberProvider.saveMember(chatRoom4, requester);
		ChatRoomMember chatRoomMemberForRoom4Director = chatRoomMemberProvider.saveDirector(chatRoom4, director2);

		ChatRoomMember chatRoomMemberForRoom5Member = chatRoomMemberProvider.saveMember(chatRoom5, requester);
		ChatRoomMember chatRoomMemberForRoom5Director = chatRoomMemberProvider.saveDirector(chatRoom5, director3);

		ChatRoomMember chatRoomMemberForRoom6Member = chatRoomMemberProvider.saveMember(chatRoom6, director1);
		ChatRoomMember chatRoomMemberForRoom6Director = chatRoomMemberProvider.saveDirector(chatRoom6, requester);

		ChatRoomMember chatRoomMemberForRoom7Member = chatRoomMemberProvider.saveMember(chatRoom7, director2);
		ChatRoomMember chatRoomMemberForRoom7Director = chatRoomMemberProvider.saveDirector(chatRoom7, requester);

		// 채팅방 메세지 저장
		ChatMessage chatMessage1ForRoom1 = chatMessageProvider.saveTextType(chatRoom1, chatRoomMemberForRoom1Member,
			LocalDateTime.now().minusDays(3));
		ChatMessage chatMessage2ForRoom1 = chatMessageProvider.saveTextType(chatRoom1, chatRoomMemberForRoom1Director,
			LocalDateTime.now().minusDays(2));
		ChatMessage chatMessage3ForRoom1 = chatMessageProvider.saveTextType(chatRoom1, chatRoomMemberForRoom1Director,
			LocalDateTime.now().minusDays(1));

		ChatMessage chatMessage1ForRoom2 = chatMessageProvider.saveTextType(chatRoom2, chatRoomMemberForRoom2Member,
			LocalDateTime.now().minusMinutes(3));
		ChatMessage chatMessage2ForRoom2 = chatMessageProvider.saveTextType(chatRoom2, chatRoomMemberForRoom2Director,
			LocalDateTime.now().minusMinutes(2));

		ChatMessage chatMessage1ForRoom3 = chatMessageProvider.saveTextType(chatRoom3, chatRoomMemberForRoom3Director,
			LocalDateTime.now().minusWeeks(3));
		ChatMessage chatMessage2ForRoom3 = chatMessageProvider.saveTextType(chatRoom3, chatRoomMemberForRoom3Director,
			LocalDateTime.now().minusWeeks(2));
		ChatMessage chatMessage3ForRoom3 = chatMessageProvider.saveTextType(chatRoom3, chatRoomMemberForRoom3Director,
			LocalDateTime.now().minusWeeks(1));

		// 고용된 채팅
		ChatMessage chatMessage1ForRoom4 = chatMessageProvider.saveTextType(chatRoom4, chatRoomMemberForRoom4Director,
			LocalDateTime.now().minusMonths(4));

		// 나간 채팅
		ChatMessage chatMessage1ForRoom5 = chatMessageProvider.saveTextType(chatRoom5, chatRoomMemberForRoom5Director,
			LocalDateTime.now().minusWeeks(1));

		ChatMessage chatMessage1ForRoom6 = chatMessageProvider.saveTextType(chatRoom6, chatRoomMemberForRoom6Director,
			LocalDateTime.now().minusWeeks(6));

		ChatMessage chatMessage1ForRoom7 = chatMessageProvider.saveTextType(chatRoom7, chatRoomMemberForRoom7Director,
			LocalDateTime.now().minusWeeks(5));

		// 채팅방별 마지막 메세지 저장
		chatRoom1.updateLastMessage(chatMessage3ForRoom1);
		chatRoom2.updateLastMessage(chatMessage2ForRoom2);
		chatRoom3.updateLastMessage(chatMessage3ForRoom3);
		chatRoom4.updateLastMessage(chatMessage1ForRoom4);
		chatRoom5.updateLastMessage(chatMessage1ForRoom5);
		chatRoom6.updateLastMessage(chatMessage1ForRoom6);
		chatRoom7.updateLastMessage(chatMessage1ForRoom7);

		// 채팅방 회원별 마지막 읽은 메세지 저장
		chatRoomMemberForRoom1Member.updateLastReadMessage(chatMessage1ForRoom1); // 채팅 메세지 일부 읽음 (2개남음)
		chatRoomMemberForRoom1Member.updateLastVisibleMessage(chatMessage3ForRoom1);

		chatRoomMemberForRoom2Member.updateLastReadMessage(chatMessage2ForRoom2); // 채팅 메세지 모두 읽음 (0개남음)
		chatRoomMemberForRoom2Member.updateLastVisibleMessage(chatMessage2ForRoom2);

		chatRoomMemberForRoom3Member.updateLastReadMessage(null); // 채팅 메세지 모두 안읽음 (3개남음)
		chatRoomMemberForRoom3Member.updateLastVisibleMessage(chatMessage3ForRoom3);

		chatRoomMemberForRoom5Member.updateToDeleteChatRoom();

		chatRoomMemberForRoom6Director.updateLastVisibleMessage(chatMessage1ForRoom6);

		chatRoomMemberForRoom7Director.updateLastVisibleMessage(chatMessage1ForRoom7);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/chat-rooms")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ChatRoomFindAllResponseForDirector response = objectMapper.readValue(responseJson,
			ChatRoomFindAllResponseForDirector.class);

		// then

		// 채팅방 회원별 마지막 읽은 메세지 갯수 확인
		List<ChatRoomResponseForDirector> chatRooms = response.getChatRooms();

		assertThat(chatRooms.size()).isEqualTo(2); // 디렉터의 역활로서 참여한 채팅방 2 개가 조회된다.

		// 채팅방 순서는 2,1,3
		assertThat(chatRooms.get(0).getId()).isEqualTo(chatRoom7.getId());
		assertThat(chatRooms.get(0).getLastMessage()).isEqualTo(chatMessage1ForRoom6.getContent());
		assertThat(chatRooms.get(0).getUnreadCount()).isEqualTo(1);

		assertThat(chatRooms.get(1).getId()).isEqualTo(chatRoom6.getId());
		assertThat(chatRooms.get(1).getLastMessage()).isEqualTo(chatMessage1ForRoom7.getContent());
		assertThat(chatRooms.get(1).getUnreadCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("디렉터는 참여한 채팅방 목록을 페이지네이션으로 조회할 수 있다. (하나의 채팅방에 여러 개의 제안이 매핑된 경우)")
	void findAllWithPagination() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_3_STR, parent);

		DirectorServiceMapping mapping1 = directorServiceMappingProvider.save(directorInfo, directorService1);
		DirectorServiceMapping mapping2 = directorServiceMappingProvider.save(directorInfo, directorService2);

		// 총 15개의 채팅방 생성 (페이지 크기 10으로 테스트하기 위함)
		List<ChatRoom> chatRooms = new ArrayList<>();
		for (int i = 0; i < 15; i++) {
			ChatRoom chatRoom = chatRoomProvider.save();
			chatRooms.add(chatRoom);

			for (int j = 0; j < 2; j++) {
				ServiceRequest serviceRequest = serviceRequestProvider.savePending(
					j % 2 == 0 ? directorService1 : directorService2,
					requester
				);

				// 각 채팅방마다 2개의 제안 매핑
				ServiceEstimate estimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

				chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);
			}

			ChatRoomMember directorMember = chatRoomMemberProvider.saveDirector(chatRoom, director);
			ChatRoomMember requesterMember = chatRoomMemberProvider.saveMember(chatRoom, requester);

			// 최신순 정렬을 위해 시간 설정
			ChatMessage chatMessage = chatMessageProvider.saveTextType(
				chatRoom,
				requesterMember,
				LocalDateTime.now().minusMinutes(15 - i)
			);

			chatRoom.updateLastMessage(chatMessage);
			directorMember.updateLastVisibleMessage(chatMessage);
		}

		entityManager.flush();
		entityManager.clear();

		// when - 첫 페이지 (0페이지, 크기 10)
		String responseJson1 = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/chat-rooms")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param("page", "0")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		ChatRoomFindAllResponseForDirector response1 = objectMapper.readValue(responseJson1,
			ChatRoomFindAllResponseForDirector.class);

		// when - 두 번째 페이지 (1페이지, 크기 10)
		String responseJson2 = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/chat-rooms")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param("page", "1")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		ChatRoomFindAllResponseForDirector response2 = objectMapper.readValue(responseJson2,
			ChatRoomFindAllResponseForDirector.class);

		// then
		assertThat(response1.getChatRooms()).hasSize(10);
		assertThat(response2.getChatRooms()).hasSize(5);

		// 첫 페이지와 두 번째 페이지의 채팅방 ID가 다른지 확인
		List<Long> page1Ids = response1.getChatRooms().stream()
			.map(ChatRoomResponseForDirector::getId)
			.toList();
		List<Long> page2Ids = response2.getChatRooms().stream()
			.map(ChatRoomResponseForDirector::getId)
			.toList();

		assertThat(page1Ids).doesNotContainAnyElementsOf(page2Ids);
	}

	@Test
	@DisplayName("디렉터는 참여한 채팅방 목록을 조회할 수 있다. (디렉터 서비스로 필터링)")
	void findAllFilteredByService() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_3_STR, parent);

		DirectorServiceMapping mapping1 = directorServiceMappingProvider.save(directorInfo, directorService1);
		DirectorServiceMapping mapping2 = directorServiceMappingProvider.save(directorInfo, directorService2);

		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService1, requester);
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService2, requester);

		ServiceEstimate estimate1 = serviceEstimateProvider.save(directorInfo, serviceRequest1);
		ServiceEstimate estimate2 = serviceEstimateProvider.save(directorInfo, serviceRequest2);

		ChatRoom chatRoom1 = chatRoomProvider.save();
		ChatRoom chatRoom2 = chatRoomProvider.save();

		chatRoomServiceEstimateMappingProvider.save(chatRoom1, estimate1);
		chatRoomServiceEstimateMappingProvider.save(chatRoom2, estimate2);

		ChatRoomMember directorMember1 = chatRoomMemberProvider.saveDirector(chatRoom1, director);
		ChatRoomMember requesterMember1 = chatRoomMemberProvider.saveMember(chatRoom1, requester);

		ChatRoomMember directorMember2 = chatRoomMemberProvider.saveDirector(chatRoom2, director);
		ChatRoomMember requesterMember2 = chatRoomMemberProvider.saveMember(chatRoom2, requester);

		ChatMessage message1 = chatMessageProvider.saveTextType(chatRoom1, requesterMember1, LocalDateTime.now());
		ChatMessage message2 = chatMessageProvider.saveTextType(chatRoom2, requesterMember2, LocalDateTime.now());

		chatRoom1.updateLastMessage(message1);
		chatRoom2.updateLastMessage(message2);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/chat-rooms")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param("directorServiceId", directorService1.getId().toString())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		ChatRoomFindAllResponseForDirector response = objectMapper.readValue(responseJson,
			ChatRoomFindAllResponseForDirector.class);

		// then
		assertThat(response.getChatRooms()).hasSize(1);
		assertThat(response.getChatRooms().get(0).getId()).isEqualTo(chatRoom1.getId());
	}

	@Test
	@DisplayName("디렉터는 참여한 채팅방 목록을 조회할 수 있다. (읽지 않은 것만 필터링)")
	void findAllFilteredByShowOnlyUnread() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);

		Member blockedMember = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		DirectorServiceMapping mapping = directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService, requester);
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService, requester);

		ServiceRequest serviceRequest3 = serviceRequestProvider.savePending(directorService, blockedMember);

		ServiceEstimate estimate1 = serviceEstimateProvider.save(directorInfo, serviceRequest1);
		ServiceEstimate estimate2 = serviceEstimateProvider.save(directorInfo, serviceRequest2);
		ServiceEstimate estimate3 = serviceEstimateProvider.save(directorInfo, serviceRequest3);

		ChatRoom chatRoom1 = chatRoomProvider.save();
		ChatRoom chatRoom2 = chatRoomProvider.save();
		ChatRoom chatRoom3 = chatRoomProvider.save();

		chatRoomServiceEstimateMappingProvider.save(chatRoom1, estimate1);
		chatRoomServiceEstimateMappingProvider.save(chatRoom2, estimate2);
		chatRoomServiceEstimateMappingProvider.save(chatRoom3, estimate3);

		ChatRoomMember directorMember1 = chatRoomMemberProvider.saveDirector(chatRoom1, director);
		ChatRoomMember requesterMember1 = chatRoomMemberProvider.saveMember(chatRoom1, requester);

		ChatRoomMember directorMember2 = chatRoomMemberProvider.saveDirector(chatRoom2, director);
		ChatRoomMember requesterMember2 = chatRoomMemberProvider.saveMember(chatRoom2, requester);

		ChatRoomMember directorMember3 = chatRoomMemberProvider.saveDirector(chatRoom3, director);
		ChatRoomMember blockedRequestMember = chatRoomMemberProvider.saveMember(chatRoom3, blockedMember);

		ChatMessage message1 = chatMessageProvider.saveTextType(chatRoom1, requesterMember1,
			LocalDateTime.now().minusMinutes(2));
		ChatMessage message2 = chatMessageProvider.saveTextType(chatRoom1, requesterMember1,
			LocalDateTime.now().minusMinutes(1));
		ChatMessage message3 = chatMessageProvider.saveTextType(chatRoom2, requesterMember2, LocalDateTime.now());

		ChatMessage message4 = chatMessageProvider.saveWithIsVisibleToOpponentFalse(chatRoom3, blockedRequestMember);

		chatRoom1.updateLastMessage(message2);
		chatRoom2.updateLastMessage(message3);
		chatRoom3.updateLastMessage(message4);

		directorMember1.updateLastReadMessage(message1); // 읽지 않은 메시지 1개
		directorMember1.updateLastVisibleMessage(message2);

		directorMember2.updateLastReadMessage(message3); // 모두 읽음
		directorMember2.updateLastVisibleMessage(message3);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/chat-rooms")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param("showOnlyUnread", "true")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		ChatRoomFindAllResponseForDirector response = objectMapper.readValue(responseJson,
			ChatRoomFindAllResponseForDirector.class);

		// then
		assertThat(response.getChatRooms()).hasSize(1);
		assertThat(response.getChatRooms().get(0).getId()).isEqualTo(chatRoom1.getId());
	}

	@Test
	@DisplayName("디렉터는 참여한 채팅방 목록을 조회할 수 있다. (상태로 필터링 - PENDING)")
	void findAllFilteredByStatusPending() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		DirectorServiceMapping mapping = directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService, requester);
		ServiceRequest serviceRequest2 = serviceRequestProvider.saveWithIsCompletedTrue(directorService, requester,
			LocalDateTime.now());

		ServiceEstimate estimate1 = serviceEstimateProvider.save(directorInfo, serviceRequest1);
		ServiceEstimate estimate2 = serviceEstimateProvider.saveDirectorDone(directorInfo, serviceRequest2,
			LocalDateTime.now());

		ChatRoom chatRoom1 = chatRoomProvider.save();
		ChatRoom chatRoom2 = chatRoomProvider.save();

		chatRoomServiceEstimateMappingProvider.save(chatRoom1, estimate1);
		chatRoomServiceEstimateMappingProvider.save(chatRoom2, estimate2);

		ChatRoomMember directorMember1 = chatRoomMemberProvider.saveDirector(chatRoom1, director);
		ChatRoomMember requesterMember1 = chatRoomMemberProvider.saveMember(chatRoom1, requester);

		ChatRoomMember directorMember2 = chatRoomMemberProvider.saveDirector(chatRoom2, director);
		ChatRoomMember requesterMember2 = chatRoomMemberProvider.saveMember(chatRoom2, requester);

		ChatMessage message1 = chatMessageProvider.saveTextType(chatRoom1, requesterMember1, LocalDateTime.now());
		ChatMessage message2 = chatMessageProvider.saveTextType(chatRoom2, requesterMember2, LocalDateTime.now());

		chatRoom1.updateLastMessage(message1);
		chatRoom2.updateLastMessage(message2);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/chat-rooms")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param("status", "PENDING")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		ChatRoomFindAllResponseForDirector response = objectMapper.readValue(responseJson,
			ChatRoomFindAllResponseForDirector.class);

		// then
		assertThat(response.getChatRooms()).hasSize(1);
		assertThat(response.getChatRooms().get(0).getId()).isEqualTo(chatRoom1.getId());
	}

	@Test
	@DisplayName("디렉터는 참여한 채팅방 목록을 조회할 수 있다. (상태로 필터링 - ONGOING)")
	void findAllFilteredByStatusOngoing() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		DirectorServiceMapping mapping = directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest1 = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now());
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService, requester);

		ServiceEstimate estimate1 = serviceEstimateProvider.saveOngoing(directorInfo, serviceRequest1,
			LocalDateTime.now());
		ServiceEstimate estimate2 = serviceEstimateProvider.saveDirectorDone(directorInfo, serviceRequest2,
			LocalDateTime.now());

		ChatRoom chatRoom1 = chatRoomProvider.save();
		ChatRoom chatRoom2 = chatRoomProvider.save();

		chatRoomServiceEstimateMappingProvider.save(chatRoom1, estimate1);
		chatRoomServiceEstimateMappingProvider.save(chatRoom2, estimate2);

		ChatRoomMember directorMember1 = chatRoomMemberProvider.saveDirector(chatRoom1, director);
		ChatRoomMember requesterMember1 = chatRoomMemberProvider.saveMember(chatRoom1, requester);

		ChatRoomMember directorMember2 = chatRoomMemberProvider.saveDirector(chatRoom2, director);
		ChatRoomMember requesterMember2 = chatRoomMemberProvider.saveMember(chatRoom2, requester);

		ChatMessage message1 = chatMessageProvider.saveTextType(chatRoom1, requesterMember1, LocalDateTime.now());
		ChatMessage message2 = chatMessageProvider.saveTextType(chatRoom2, requesterMember2, LocalDateTime.now());

		chatRoom1.updateLastMessage(message1);
		chatRoom2.updateLastMessage(message2);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/chat-rooms")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param("status", "ONGOING")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		ChatRoomFindAllResponseForDirector response = objectMapper.readValue(responseJson,
			ChatRoomFindAllResponseForDirector.class);

		// then
		assertThat(response.getChatRooms()).hasSize(2);
	}

	@Test
	@DisplayName("디렉터는 참여한 채팅방 목록을 조회할 수 있다. (키워드로 검색 - 닉네임)")
	void findAllFilteredByWordWithNickname() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member requester1 = memberProvider.saveMember(SignInPlatform.KAKAO);
		requester1.updateNickname("테스트유저");

		Member requester2 = memberProvider.saveMember(SignInPlatform.KAKAO);
		requester2.updateNickname("일반유저");

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		DirectorServiceMapping mapping = directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService, requester1);
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService, requester2);

		ServiceEstimate estimate1 = serviceEstimateProvider.save(directorInfo, serviceRequest1);
		ServiceEstimate estimate2 = serviceEstimateProvider.save(directorInfo, serviceRequest2);

		ChatRoom chatRoom1 = chatRoomProvider.save();
		ChatRoom chatRoom2 = chatRoomProvider.save();

		chatRoomServiceEstimateMappingProvider.save(chatRoom1, estimate1);
		chatRoomServiceEstimateMappingProvider.save(chatRoom2, estimate2);

		ChatRoomMember directorMember1 = chatRoomMemberProvider.saveDirector(chatRoom1, director);
		ChatRoomMember requesterMember1 = chatRoomMemberProvider.saveMember(chatRoom1, requester1);

		ChatRoomMember directorMember2 = chatRoomMemberProvider.saveDirector(chatRoom2, director);
		ChatRoomMember requesterMember2 = chatRoomMemberProvider.saveMember(chatRoom2, requester2);

		ChatMessage message1 = chatMessageProvider.saveTextType(chatRoom1, requesterMember1, LocalDateTime.now());
		ChatMessage message2 = chatMessageProvider.saveTextType(chatRoom2, requesterMember2, LocalDateTime.now());

		chatRoom1.updateLastMessage(message1);
		chatRoom2.updateLastMessage(message2);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/chat-rooms")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param("word", "테스트")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		ChatRoomFindAllResponseForDirector response = objectMapper.readValue(responseJson,
			ChatRoomFindAllResponseForDirector.class);

		// then
		assertThat(response.getChatRooms()).hasSize(1);
		assertThat(response.getChatRooms().get(0).getId()).isEqualTo(chatRoom1.getId());
	}

	@Test
	@DisplayName("디렉터는 참여한 채팅방 목록을 조회할 수 있다. (키워드로 검색 - 디렉터 서비스 명)")
	void findAllFilteredByWordWithDirectorServiceName() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member requester1 = memberProvider.saveMember(SignInPlatform.KAKAO);
		requester1.updateNickname("테스트유저");

		Member requester2 = memberProvider.saveMember(SignInPlatform.KAKAO);
		requester2.updateNickname("일반유저");

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_3_STR, parent);

		DirectorServiceMapping mapping = directorServiceMappingProvider.save(directorInfo, directorService);
		DirectorServiceMapping mapping2 = directorServiceMappingProvider.save(directorInfo, directorService2);

		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService, requester1);
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService2, requester2);

		ServiceEstimate estimate1 = serviceEstimateProvider.save(directorInfo, serviceRequest1);
		ServiceEstimate estimate2 = serviceEstimateProvider.save(directorInfo, serviceRequest2);

		ChatRoom chatRoom1 = chatRoomProvider.save();
		ChatRoom chatRoom2 = chatRoomProvider.save();

		chatRoomServiceEstimateMappingProvider.save(chatRoom1, estimate1);
		chatRoomServiceEstimateMappingProvider.save(chatRoom2, estimate2);

		ChatRoomMember directorMember1 = chatRoomMemberProvider.saveDirector(chatRoom1, director);
		ChatRoomMember requesterMember1 = chatRoomMemberProvider.saveMember(chatRoom1, requester1);

		ChatRoomMember directorMember2 = chatRoomMemberProvider.saveDirector(chatRoom2, director);
		ChatRoomMember requesterMember2 = chatRoomMemberProvider.saveMember(chatRoom2, requester2);

		ChatMessage message1 = chatMessageProvider.saveTextType(chatRoom1, requesterMember1, LocalDateTime.now());
		ChatMessage message2 = chatMessageProvider.saveTextType(chatRoom2, requesterMember2, LocalDateTime.now());

		chatRoom1.updateLastMessage(message1);
		chatRoom2.updateLastMessage(message2);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/chat-rooms")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param("word", SERVICE_NAME_3_STR)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		ChatRoomFindAllResponseForDirector response = objectMapper.readValue(responseJson,
			ChatRoomFindAllResponseForDirector.class);

		// then
		assertThat(response.getChatRooms()).hasSize(1);
		assertThat(response.getChatRooms().get(0).getId()).isEqualTo(chatRoom2.getId());
	}

	@Test
	@DisplayName("디렉터는 참여한 채팅방 목록을 조회할 수 있다. (여러 필터 조합)")
	void findAllWithMultipleFilters() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		requester.updateNickname("테스트유저");

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_3_STR, parent);

		DirectorServiceMapping mapping1 = directorServiceMappingProvider.save(directorInfo, directorService1);
		DirectorServiceMapping mapping2 = directorServiceMappingProvider.save(directorInfo, directorService2);

		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService1, requester);
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService2, requester);

		ServiceEstimate estimate1 = serviceEstimateProvider.save(directorInfo, serviceRequest1);
		ServiceEstimate estimate2 = serviceEstimateProvider.save(directorInfo, serviceRequest2);

		ChatRoom chatRoom1 = chatRoomProvider.save();
		ChatRoom chatRoom2 = chatRoomProvider.save();

		chatRoomServiceEstimateMappingProvider.save(chatRoom1, estimate1);
		chatRoomServiceEstimateMappingProvider.save(chatRoom2, estimate2);

		ChatRoomMember directorMember1 = chatRoomMemberProvider.saveDirector(chatRoom1, director);
		ChatRoomMember requesterMember1 = chatRoomMemberProvider.saveMember(chatRoom1, requester);

		ChatRoomMember directorMember2 = chatRoomMemberProvider.saveDirector(chatRoom2, director);
		ChatRoomMember requesterMember2 = chatRoomMemberProvider.saveMember(chatRoom2, requester);

		ChatMessage message1 = chatMessageProvider.saveTextType(chatRoom1, requesterMember1,
			LocalDateTime.now().minusMinutes(1));
		ChatMessage message2 = chatMessageProvider.saveTextType(chatRoom2, requesterMember2, LocalDateTime.now());

		chatRoom1.updateLastMessage(message1);
		chatRoom2.updateLastMessage(message2);

		// 읽지 않은 메시지 1개 남김
		directorMember1.updateLastVisibleMessage(message1);

		directorMember2.updateLastReadMessage(message2); // chatRoom2는 읽음 처리
		directorMember2.updateLastVisibleMessage(message2);

		entityManager.flush();
		entityManager.clear();

		// when - 서비스 필터링 + 읽지 않은 것만 필터링 + 상태 필터링
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/chat-rooms")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param("directorServiceId", directorService1.getId().toString())
				.param("showOnlyUnread", "true")
				.param("status", "PENDING")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		ChatRoomFindAllResponseForDirector response = objectMapper.readValue(responseJson,
			ChatRoomFindAllResponseForDirector.class);

		// then
		assertThat(response.getChatRooms()).hasSize(1);
		assertThat(response.getChatRooms().get(0).getId()).isEqualTo(chatRoom1.getId());
	}

	@Test
	@DisplayName("디렉터는 채팅방에서 사용된 서비스 목록을 조회할 수 있다.")
	void findChatRoomServices() throws Exception {
		// given
		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo2);

		DirectorInfo directorInfo3 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member requester = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo3);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_3_STR, parent);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_3_STR, parent);
		DirectorService directorService4 = directorCategoryProvider.save(SERVICE_NAME_3_STR, parent);
		DirectorService directorService5 = directorCategoryProvider.save(SERVICE_NAME_3_STR, parent);
		DirectorService directorService6 = directorCategoryProvider.save(SERVICE_NAME_3_STR, parent);

		// 서비스 매핑 저장 (디렉터가 제공하는 서비스)
		directorServiceMappingProvider.save(directorInfo1, directorService1);
		directorServiceMappingProvider.save(directorInfo2, directorService2);
		directorServiceMappingProvider.save(directorInfo2, directorService3);
		directorServiceMappingProvider.save(directorInfo2, directorService4);

		// requester 가 디렉터라면, 본인이 제안을 보냈던 서비스에 대해서는 회원 전용 채팅방 서비스 목록에 노출이 되면 안된다.
		directorServiceMappingProvider.save(directorInfo3, directorService5);
		directorServiceMappingProvider.save(directorInfo3, directorService6);

		// 요청 저장
		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService1, requester);
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService2, requester);
		ServiceRequest serviceRequest3 = serviceRequestProvider.savePending(directorService3, requester);
		ServiceRequest serviceRequest4 = serviceRequestProvider.savePending(directorService4, requester);

		ServiceRequest serviceRequest5 = serviceRequestProvider.savePending(directorService5, director1);
		ServiceRequest serviceRequest6 = serviceRequestProvider.savePending(directorService6, director2);

		// 제안 저장 (각 채팅방에서 사용된 서비스와 연결되는 제안)
		ServiceEstimate estimate1 = serviceEstimateProvider.save(directorInfo1, serviceRequest1);
		ServiceEstimate estimate2 = serviceEstimateProvider.save(directorInfo2, serviceRequest2);
		ServiceEstimate estimate3 = serviceEstimateProvider.save(directorInfo2, serviceRequest3);
		ServiceEstimate estimate4 = serviceEstimateProvider.save(directorInfo2, serviceRequest4);

		ServiceEstimate estimate5 = serviceEstimateProvider.save(directorInfo3, serviceRequest5);
		ServiceEstimate estimate6 = serviceEstimateProvider.save(directorInfo3, serviceRequest6);

		// 채팅방 저장
		ChatRoom chatRoom1 = chatRoomProvider.save();
		ChatRoom chatRoom2 = chatRoomProvider.save();
		ChatRoom chatRoom3 = chatRoomProvider.save();
		ChatRoom chatRoom4 = chatRoomProvider.save();
		ChatRoom chatRoom5 = chatRoomProvider.save();
		ChatRoom chatRoom6 = chatRoomProvider.save();

		// 채팅방 - 제안 매핑 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom1, estimate1);
		chatRoomServiceEstimateMappingProvider.save(chatRoom2, estimate2);
		chatRoomServiceEstimateMappingProvider.save(chatRoom3, estimate3);
		chatRoomServiceEstimateMappingProvider.save(chatRoom4, estimate4);
		chatRoomServiceEstimateMappingProvider.save(chatRoom5, estimate5);
		chatRoomServiceEstimateMappingProvider.save(chatRoom6, estimate6);

		// 채팅방 참여자 매핑 저장
		chatRoomMemberProvider.saveMember(chatRoom1, requester);
		chatRoomMemberProvider.saveDirector(chatRoom1, director1);

		chatRoomMemberProvider.saveMember(chatRoom2, requester);
		chatRoomMemberProvider.saveDirector(chatRoom2, director2);

		chatRoomMemberProvider.saveMemberWithRoomDeletedTrue(chatRoom3, requester);
		chatRoomMemberProvider.saveDirector(chatRoom3, director2);

		chatRoomMemberProvider.saveMember(chatRoom4, requester);
		chatRoomMemberProvider.saveDirector(chatRoom4, director2);

		chatRoomMemberProvider.saveMember(chatRoom5, director1);
		chatRoomMemberProvider.saveDirector(chatRoom5, requester);

		chatRoomMemberProvider.saveMember(chatRoom6, director2);
		chatRoomMemberProvider.saveDirectorWithRoomDeletedTrue(chatRoom6, requester);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/chat-rooms/services")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ChatRoomFindChatRoomServicesResponseForDirector response = objectMapper.readValue(responseJson,
			ChatRoomFindChatRoomServicesResponseForDirector.class);

		// then
		assertThat(response.getServices()).hasSize(1);

		List<Long> ids = response.getServices()
			.stream()
			.map(DirectorServiceWithFullNameResponseForDirector::getId)
			.toList();
		assertThat(ids).containsExactlyInAnyOrder(directorService5.getId());
	}

	@Test
	@DisplayName("디렉터는 읽지 않은 전체 채팅 메세지 개수를 조회할 수 있다. (나간 채팅방이 존재할경우)")
	void findTotalUnreadCountWhenDeletedChatRoomExist() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member opponent1 = memberProvider.saveMember(SignInPlatform.KAKAO);

		Member opponent2 = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorInfo directorInfo3 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member opponent3 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo3);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		// 채팅방 저장
		ChatRoom chatRoom1 = chatRoomProvider.save();
		ChatRoom chatRoom2 = chatRoomProvider.save();
		ChatRoom chatRoom3 = chatRoomProvider.save();
		ChatRoom chatRoom4 = chatRoomProvider.save();

		//채팅방 회원 저장
		ChatRoomMember directorChatRoomMember1 = chatRoomMemberProvider.saveDirector(chatRoom1, director);
		ChatRoomMember opponentMember1 = chatRoomMemberProvider.saveMember(chatRoom1, opponent1);

		ChatRoomMember directorChatRoomMember2 = chatRoomMemberProvider.saveDirector(chatRoom2, director);
		ChatRoomMember opponentMember2 = chatRoomMemberProvider.saveMember(chatRoom2, opponent2);

		ChatRoomMember directorChatRoomMember3 = chatRoomMemberProvider.saveDirector(chatRoom3, director);
		ChatRoomMember opponentMember3 = chatRoomMemberProvider.saveMember(chatRoom3, opponent3);

		ChatRoomMember directorChatRoomMember4 = chatRoomMemberProvider.saveDirectorWithRoomDeletedTrue(chatRoom4,
			director);
		ChatRoomMember opponentMember4 = chatRoomMemberProvider.saveMember(chatRoom4, opponent1);

		// 채팅 메세지 저장
		ChatMessage room1Message1 = chatMessageProvider.saveTextType(chatRoom1, opponentMember1,
			LocalDateTime.now().minusMinutes(5));
		ChatMessage room1Message2 = chatMessageProvider.saveTextType(chatRoom1, opponentMember1,
			LocalDateTime.now().minusMinutes(4));
		ChatMessage room1Message3 = chatMessageProvider.saveTextType(chatRoom1, opponentMember1,
			LocalDateTime.now().minusMinutes(3));

		ChatMessage room2Message1 = chatMessageProvider.saveTextType(chatRoom2, opponentMember2,
			LocalDateTime.now().minusMinutes(2));
		ChatMessage room2Message2 = chatMessageProvider.saveTextType(chatRoom2, opponentMember2,
			LocalDateTime.now().minusMinutes(1));

		ChatMessage room3Message1 = chatMessageProvider.saveTextType(chatRoom3, opponentMember3,
			LocalDateTime.now().minusMinutes(6));

		ChatMessage room4Message1 = chatMessageProvider.saveTextType(chatRoom4, opponentMember4,
			LocalDateTime.now().minusMinutes(7));

		// 회원의 마지막 읽은 메세지 설정
		directorChatRoomMember1.updateLastReadMessage(room1Message1);
		directorChatRoomMember1.updateLastVisibleMessage(room1Message3);

		directorChatRoomMember2.updateLastReadMessage(room2Message2);
		directorChatRoomMember2.updateLastVisibleMessage(room2Message2);

		directorChatRoomMember3.updateLastVisibleMessage(room3Message1);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/chat-rooms/unread-count")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ChatRoomUnreadCountResponseForDirector response = objectMapper.readValue(responseJson,
			ChatRoomUnreadCountResponseForDirector.class);

		// then
		assertThat(response.getTotalUnreadCount()).isEqualTo(3); // 2개 + 0개 + 1개
	}

	@Test
	@DisplayName("회원은 읽지 않은 전체 채팅 메세지 개수를 조회할 수 있다. (차단한 사용자로부터 받은 읽지않은 메세지가 존재할 경우)")
	void findTotalUnreadCountWhenBlockedMemberUnreadMessageExist() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member opponent1 = memberProvider.saveMember(SignInPlatform.KAKAO);

		Member opponent2 = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorInfo directorInfo3 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member opponent3 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo3);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		// 채팅방 저장
		ChatRoom chatRoom1 = chatRoomProvider.save();
		ChatRoom chatRoom2 = chatRoomProvider.save();
		ChatRoom chatRoom3 = chatRoomProvider.save();
		ChatRoom chatRoom4 = chatRoomProvider.save();

		//채팅방 회원 저장
		ChatRoomMember directorChatRoomMember1 = chatRoomMemberProvider.saveDirector(chatRoom1, director);
		ChatRoomMember opponentMember1 = chatRoomMemberProvider.saveMember(chatRoom1, opponent1);

		ChatRoomMember directorChatRoomMember2 = chatRoomMemberProvider.saveDirector(chatRoom2, director);
		ChatRoomMember opponentMember2 = chatRoomMemberProvider.saveMember(chatRoom2, opponent2);

		ChatRoomMember directorChatRoomMember3 = chatRoomMemberProvider.saveDirector(chatRoom3, director);
		ChatRoomMember opponentMember3 = chatRoomMemberProvider.saveMember(chatRoom3, opponent3);

		ChatRoomMember directorChatRoomMember4 = chatRoomMemberProvider.saveDirectorWithRoomDeletedTrue(chatRoom4,
			director);
		ChatRoomMember opponentMember4 = chatRoomMemberProvider.saveMember(chatRoom4, opponent1);

		// 채팅 메세지 저장
		ChatMessage room1Message1 = chatMessageProvider.saveTextType(chatRoom1, opponentMember1,
			LocalDateTime.now().minusMinutes(5));
		ChatMessage room1Message2 = chatMessageProvider.saveTextType(chatRoom1, opponentMember1,
			LocalDateTime.now().minusMinutes(4));
		ChatMessage room1Message3 = chatMessageProvider.saveTextType(chatRoom1, opponentMember1,
			LocalDateTime.now().minusMinutes(3));

		ChatMessage room2Message1 = chatMessageProvider.saveTextType(chatRoom2, opponentMember2,
			LocalDateTime.now().minusMinutes(2));
		ChatMessage room2Message2 = chatMessageProvider.saveWithIsVisibleToOpponentFalse(chatRoom2, opponentMember2);

		ChatMessage room3Message1 = chatMessageProvider.saveTextType(chatRoom3, opponentMember3,
			LocalDateTime.now().minusMinutes(6));

		ChatMessage room4Message1 = chatMessageProvider.saveTextType(chatRoom4, opponentMember4,
			LocalDateTime.now().minusMinutes(7));

		// 회원의 마지막 읽은 메세지 설정
		directorChatRoomMember1.updateLastReadMessage(room1Message1);
		directorChatRoomMember1.updateLastVisibleMessage(room1Message3);

		directorChatRoomMember2.updateLastReadMessage(room2Message1);
		directorChatRoomMember2.updateLastVisibleMessage(room2Message1);

		// 차단 저장
		memberBlockProvider.save(director, opponent2);

		directorChatRoomMember3.updateLastVisibleMessage(room3Message1);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/chat-rooms/unread-count")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ChatRoomUnreadCountResponseForDirector response = objectMapper.readValue(responseJson,
			ChatRoomUnreadCountResponseForDirector.class);

		// then
		assertThat(response.getTotalUnreadCount()).isEqualTo(3); // 2개 + 0개 + 1개
	}

	@Test
	@DisplayName("회원은 읽지 않은 전체 채팅 메세지 개수를 조회할 수 있다. (내가 디렉터로 받은 읽지않은 메세지가 존재하는 경우)")
	void findTotalUnreadCountWhenUnreadMessageExistWithRoleDirector() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member opponent1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member opponent2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo2);

		DirectorInfo directorInfo3 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member opponent3 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo3);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		// 채팅방 저장
		ChatRoom chatRoom1 = chatRoomProvider.save();
		ChatRoom chatRoom2 = chatRoomProvider.save();
		ChatRoom chatRoom3 = chatRoomProvider.save();
		ChatRoom chatRoom4 = chatRoomProvider.save();
		ChatRoom chatRoom5 = chatRoomProvider.save();

		//채팅방 회원 저장
		ChatRoomMember directorChatRoomMember1 = chatRoomMemberProvider.saveDirector(chatRoom1, director);
		ChatRoomMember opponentMember1 = chatRoomMemberProvider.saveMember(chatRoom1, opponent1);

		ChatRoomMember directorChatRoomMember2 = chatRoomMemberProvider.saveDirector(chatRoom2, director);
		ChatRoomMember opponentMember2 = chatRoomMemberProvider.saveMember(chatRoom2, opponent2);

		ChatRoomMember directorChatRoomMember3 = chatRoomMemberProvider.saveDirector(chatRoom3, director);
		ChatRoomMember opponentMember3 = chatRoomMemberProvider.saveMember(chatRoom3, opponent3);

		ChatRoomMember directorChatRoomMember4 = chatRoomMemberProvider.saveDirectorWithRoomDeletedTrue(chatRoom4,
			director);
		ChatRoomMember opponentMember4 = chatRoomMemberProvider.saveMember(chatRoom4, opponent1);

		ChatRoomMember opponentMember5 = chatRoomMemberProvider.saveDirector(chatRoom5, opponent1);
		ChatRoomMember memberChatRoomMember5 = chatRoomMemberProvider.saveMember(chatRoom5, director);

		// 채팅 메세지 저장
		ChatMessage room1Message1 = chatMessageProvider.saveTextType(chatRoom1, opponentMember1,
			LocalDateTime.now().minusMinutes(5));
		ChatMessage room1Message2 = chatMessageProvider.saveTextType(chatRoom1, opponentMember1,
			LocalDateTime.now().minusMinutes(4));
		ChatMessage room1Message3 = chatMessageProvider.saveTextType(chatRoom1, opponentMember1,
			LocalDateTime.now().minusMinutes(3));

		ChatMessage room2Message1 = chatMessageProvider.saveTextType(chatRoom2, opponentMember2,
			LocalDateTime.now().minusMinutes(2));
		ChatMessage room2Message2 = chatMessageProvider.saveWithIsVisibleToOpponentFalse(chatRoom2, opponentMember2);

		ChatMessage room3Message1 = chatMessageProvider.saveTextType(chatRoom3, opponentMember3,
			LocalDateTime.now().minusMinutes(6));

		ChatMessage room4Message1 = chatMessageProvider.saveTextType(chatRoom4, opponentMember4,
			LocalDateTime.now().minusMinutes(7));

		ChatMessage room5Message1 = chatMessageProvider.saveTextType(chatRoom5, opponentMember1,
			LocalDateTime.now().minusMinutes(8));
		ChatMessage room5Message2 = chatMessageProvider.saveTextType(chatRoom5, opponentMember1,
			LocalDateTime.now().minusMinutes(9));

		// 회원의 마지막 읽은 메세지 설정
		directorChatRoomMember1.updateLastReadMessage(room1Message1);
		directorChatRoomMember1.updateLastVisibleMessage(room1Message3);

		directorChatRoomMember2.updateLastReadMessage(room2Message1);
		directorChatRoomMember2.updateLastVisibleMessage(room2Message1);

		directorChatRoomMember3.updateLastVisibleMessage(room3Message1);

		// 차단 저장
		memberBlockProvider.save(director, opponent2);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/chat-rooms/unread-count")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ChatRoomUnreadCountResponseForDirector response = objectMapper.readValue(responseJson,
			ChatRoomUnreadCountResponseForDirector.class);

		// then
		assertThat(response.getTotalUnreadCount()).isEqualTo(3); // 2개 + 0개 + 1개
	}
}
