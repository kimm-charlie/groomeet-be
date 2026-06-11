package com.motd.be.module.member.chat_room.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.Constants.ERROR_CODE;
import static com.motd.be.Constants.ERROR_MESSAGE;
import static com.motd.be.Constants.ERROR_STATUS;
import static com.motd.be.common.constants.Constants.*;
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
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.ChatRoomMemberException;
import com.motd.be.module.member.chat_file.entity.ChatFile;
import com.motd.be.module.member.chat_message.dto.response.ChatMessageFindAllResponse;
import com.motd.be.module.member.chat_message.dto.response.ChatMessageResponse;
import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_room.dto.response.ChatRoomFindAllResponse;
import com.motd.be.module.member.chat_room.dto.response.ChatRoomFindChatRoomServicesResponse;
import com.motd.be.module.member.chat_room.dto.response.ChatRoomFindDetailResponse;
import com.motd.be.module.member.chat_room.dto.response.ChatRoomResponse;
import com.motd.be.module.member.chat_room.dto.response.ChatRoomUnreadCountResponse;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.dto.response.DirectorServiceWithFullNameResponse;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.director_service_mapping.entity.DirectorServiceMapping;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.dto.response.MemberResponse;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.Role;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_request.dto.response.ServiceRequestForChatRoomFindDetailResponse;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.sse.SseEventType;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class ChatRoomControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("회원은 참여한 채팅방 목록을 조회할 수 있다.")
	void findAll() throws Exception {
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

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		// 서비스 매핑 저장
		DirectorServiceMapping mapping1 = directorServiceMappingProvider.save(directorInfo1, directorService1);
		DirectorServiceMapping mapping2 = directorServiceMappingProvider.save(directorInfo2, directorService1);
		DirectorServiceMapping mapping3 = directorServiceMappingProvider.save(directorInfo3, directorService1);

		DirectorServiceMapping mapping4 = directorServiceMappingProvider.save(directorInfo2, directorService2);
		DirectorServiceMapping mapping5 = directorServiceMappingProvider.save(directorInfo3, directorService2);

		// 요청 저장
		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService1, requester);

		ServiceRequest serviceRequest2 = serviceRequestProvider.saveWithIsCompletedTrue(directorService2, requester,
			LocalDateTime.now());

		// 제안 저장
		ServiceEstimate estimate1 = serviceEstimateProvider.save(directorInfo1, serviceRequest1);
		ServiceEstimate estimate2 = serviceEstimateProvider.save(directorInfo2, serviceRequest1);
		ServiceEstimate estimate3 = serviceEstimateProvider.saveDirectorDone(directorInfo3, serviceRequest1,
			LocalDateTime.now());

		ServiceEstimate estimate4 = serviceEstimateProvider.saveDirectorDone(directorInfo2, serviceRequest2,
			LocalDateTime.now());
		ServiceEstimate estimate5 = serviceEstimateProvider.saveDirectorDone(directorInfo3, serviceRequest2,
			LocalDateTime.now());

		// 채팅방 저장
		ChatRoom chatRoom1 = chatRoomProvider.save();
		ChatRoom chatRoom2 = chatRoomProvider.save();
		ChatRoom chatRoom3 = chatRoomProvider.save();
		ChatRoom chatRoom4 = chatRoomProvider.save();
		ChatRoom chatRoom5 = chatRoomProvider.save();

		// 채팅방 제안 매핑 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom1, estimate1);
		chatRoomServiceEstimateMappingProvider.save(chatRoom2, estimate2);
		chatRoomServiceEstimateMappingProvider.save(chatRoom3, estimate3);

		chatRoomServiceEstimateMappingProvider.save(chatRoom4, estimate4);
		chatRoomServiceEstimateMappingProvider.save(chatRoom5, estimate5);

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

		// 채팅방별 마지막 메세지 저장
		chatRoom1.updateLastMessage(chatMessage3ForRoom1);
		chatRoom2.updateLastMessage(chatMessage2ForRoom2);
		chatRoom3.updateLastMessage(chatMessage3ForRoom3);
		chatRoom4.updateLastMessage(chatMessage1ForRoom4);
		chatRoom5.updateLastMessage(chatMessage1ForRoom5);

		// 채팅방 회원별 마지막 읽은 메세지 저장
		chatRoomMemberForRoom1Member.updateLastReadMessage(chatMessage1ForRoom1); // 채팅 메세지 일부 읽음 (2개남음)
		chatRoomMemberForRoom1Member.updateLastVisibleMessage(chatMessage3ForRoom1); // 채팅 메세지 일부 읽음 (2개남음)
		chatRoomMemberForRoom1Director.updateLastReadMessage(chatMessage3ForRoom1);
		chatRoomMemberForRoom1Director.updateLastVisibleMessage(chatMessage3ForRoom1);

		chatRoomMemberForRoom2Member.updateLastReadMessage(chatMessage2ForRoom2); // 채팅 메세지 모두 읽음 (0개남음)
		chatRoomMemberForRoom2Member.updateLastVisibleMessage(chatMessage2ForRoom2);

		chatRoomMemberForRoom3Member.updateLastReadMessage(null); // 채팅 메세지 모두 안읽음 (3개남음)
		chatRoomMemberForRoom3Member.updateLastVisibleMessage(chatMessage3ForRoom3);

		chatRoomMemberForRoom4Member.updateLastVisibleMessage(chatMessage1ForRoom4);

		chatRoomMemberForRoom5Member.updateToDeleteChatRoom();

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/chat-rooms")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ChatRoomFindAllResponse response = objectMapper.readValue(responseJson, ChatRoomFindAllResponse.class);

		// then

		// 채팅방 회원별 마지막 읽은 메세지 갯수 확인
		List<ChatRoomResponse> chatRooms = response.getChatRooms();

		assertThat(chatRooms.size()).isEqualTo(4); // 삭제된 채팅방 제외 4개

		// 채팅방 순서는 2,1,3
		assertThat(chatRooms.get(0).getId()).isEqualTo(chatRoom2.getId());
		assertThat(chatRooms.get(0).getLastMessage()).isEqualTo(chatMessage2ForRoom2.getContent());
		assertThat(chatRooms.get(0).getUnreadCount()).isEqualTo(0);

		assertThat(chatRooms.get(1).getId()).isEqualTo(chatRoom1.getId());
		assertThat(chatRooms.get(1).getLastMessage()).isEqualTo(chatMessage3ForRoom1.getContent());
		assertThat(chatRooms.get(1).getUnreadCount()).isEqualTo(2);

		assertThat(chatRooms.get(2).getId()).isEqualTo(chatRoom3.getId());
		assertThat(chatRooms.get(2).getLastMessage()).isEqualTo(chatMessage3ForRoom3.getContent());
		assertThat(chatRooms.get(2).getUnreadCount()).isEqualTo(3);

		assertThat(chatRooms.get(3).getId()).isEqualTo(chatRoom4.getId());
		assertThat(chatRooms.get(3).getLastMessage()).isEqualTo(chatMessage1ForRoom4.getContent());
		assertThat(chatRooms.get(3).getUnreadCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("회원은 참여한 채팅방 목록을 조회할 수 있다. (본인이 디렉터로 참여한 채팅방 또한 존재 하는 경우)")
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
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

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
			LocalDateTime.now().minusWeeks(5));

		ChatMessage chatMessage1ForRoom7 = chatMessageProvider.saveTextType(chatRoom7, chatRoomMemberForRoom7Director,
			LocalDateTime.now().minusWeeks(6));

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
		chatRoomMemberForRoom1Member.updateLastVisibleMessage(chatMessage3ForRoom1); // 채팅 메세지 일부 읽음 (2개남음)
		chatRoomMemberForRoom1Director.updateLastReadMessage(chatMessage3ForRoom1);
		chatRoomMemberForRoom1Director.updateLastVisibleMessage(chatMessage3ForRoom1);

		chatRoomMemberForRoom2Member.updateLastReadMessage(chatMessage2ForRoom2); // 채팅 메세지 모두 읽음 (0개남음)
		chatRoomMemberForRoom2Member.updateLastVisibleMessage(chatMessage2ForRoom2);

		chatRoomMemberForRoom3Member.updateLastReadMessage(null); // 채팅 메세지 모두 안읽음 (3개남음)
		chatRoomMemberForRoom3Member.updateLastVisibleMessage(chatMessage3ForRoom3);

		chatRoomMemberForRoom4Member.updateLastVisibleMessage(chatMessage1ForRoom4);

		chatRoomMemberForRoom5Member.updateToDeleteChatRoom();

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/chat-rooms")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ChatRoomFindAllResponse response = objectMapper.readValue(responseJson, ChatRoomFindAllResponse.class);

		// then

		// 채팅방 회원별 마지막 읽은 메세지 갯수 확인
		List<ChatRoomResponse> chatRooms = response.getChatRooms();

		assertThat(chatRooms.size()).isEqualTo(4); // 삭제된 채팅방 1개 및 디렉터의 역활로서 참여한 채팅방 2 개를 제외한 총 4개의 채팅방을 조회할 수 있어야 한다.

		// 채팅방 순서는 2,1,3
		assertThat(chatRooms.get(0).getId()).isEqualTo(chatRoom2.getId());
		assertThat(chatRooms.get(0).getLastMessage()).isEqualTo(chatMessage2ForRoom2.getContent());
		assertThat(chatRooms.get(0).getUnreadCount()).isEqualTo(0);

		assertThat(chatRooms.get(1).getId()).isEqualTo(chatRoom1.getId());
		assertThat(chatRooms.get(1).getLastMessage()).isEqualTo(chatMessage3ForRoom1.getContent());
		assertThat(chatRooms.get(1).getUnreadCount()).isEqualTo(2);

		assertThat(chatRooms.get(2).getId()).isEqualTo(chatRoom3.getId());
		assertThat(chatRooms.get(2).getLastMessage()).isEqualTo(chatMessage3ForRoom3.getContent());
		assertThat(chatRooms.get(2).getUnreadCount()).isEqualTo(3);

		assertThat(chatRooms.get(3).getId()).isEqualTo(chatRoom4.getId());
		assertThat(chatRooms.get(3).getLastMessage()).isEqualTo(chatMessage1ForRoom4.getContent());
		assertThat(chatRooms.get(3).getUnreadCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("회원은 참여한 채팅방 목록을 조회할 수 있다. (디렉터 서비스로 필터링)")
	void findAllFilteredByService() throws Exception {
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

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		// 서비스 매핑 저장
		DirectorServiceMapping mapping1 = directorServiceMappingProvider.save(directorInfo1, directorService1);
		DirectorServiceMapping mapping2 = directorServiceMappingProvider.save(directorInfo2, directorService1);
		DirectorServiceMapping mapping3 = directorServiceMappingProvider.save(directorInfo3, directorService1);

		DirectorServiceMapping mapping4 = directorServiceMappingProvider.save(directorInfo2, directorService2);
		DirectorServiceMapping mapping5 = directorServiceMappingProvider.save(directorInfo3, directorService2);

		// 요청 저장
		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService1, requester);

		ServiceRequest serviceRequest2 = serviceRequestProvider.saveWithIsCompletedTrue(directorService2, requester,
			LocalDateTime.now());

		// 제안 저장
		ServiceEstimate estimate1 = serviceEstimateProvider.save(directorInfo1, serviceRequest1);
		ServiceEstimate estimate2 = serviceEstimateProvider.save(directorInfo2, serviceRequest1);
		ServiceEstimate estimate3 = serviceEstimateProvider.save(directorInfo3, serviceRequest1);

		ServiceEstimate estimate4 = serviceEstimateProvider.saveDirectorDone(directorInfo2, serviceRequest2,
			LocalDateTime.now());
		ServiceEstimate estimate5 = serviceEstimateProvider.save(directorInfo3, serviceRequest2);

		// 채팅방 저장
		ChatRoom chatRoom1 = chatRoomProvider.save();
		ChatRoom chatRoom2 = chatRoomProvider.save();
		ChatRoom chatRoom3 = chatRoomProvider.save();
		ChatRoom chatRoom4 = chatRoomProvider.save();
		ChatRoom chatRoom5 = chatRoomProvider.save();

		// 채팅방 제안 매핑 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom1, estimate1);
		chatRoomServiceEstimateMappingProvider.save(chatRoom2, estimate2);
		chatRoomServiceEstimateMappingProvider.save(chatRoom3, estimate3);

		chatRoomServiceEstimateMappingProvider.save(chatRoom4, estimate4);
		chatRoomServiceEstimateMappingProvider.save(chatRoom5, estimate5);

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

		// 채팅방별 마지막 메세지 저장
		chatRoom1.updateLastMessage(chatMessage3ForRoom1);
		chatRoom2.updateLastMessage(chatMessage2ForRoom2);
		chatRoom3.updateLastMessage(chatMessage3ForRoom3);
		chatRoom4.updateLastMessage(chatMessage1ForRoom4);
		chatRoom5.updateLastMessage(chatMessage1ForRoom5);

		// 채팅방 회원별 마지막 읽은 메세지 저장
		chatRoomMemberForRoom1Member.updateLastReadMessage(chatMessage1ForRoom1); // 채팅 메세지 일부 읽음 (2개남음)
		chatRoomMemberForRoom1Member.updateLastVisibleMessage(chatMessage3ForRoom1); // 채팅 메세지 일부 읽음 (2개남음)
		chatRoomMemberForRoom1Director.updateLastReadMessage(chatMessage3ForRoom1);
		chatRoomMemberForRoom1Director.updateLastVisibleMessage(chatMessage3ForRoom1);

		chatRoomMemberForRoom2Member.updateLastReadMessage(chatMessage2ForRoom2); // 채팅 메세지 모두 읽음 (0개남음)
		chatRoomMemberForRoom2Member.updateLastVisibleMessage(chatMessage2ForRoom2);

		chatRoomMemberForRoom3Member.updateLastReadMessage(null); // 채팅 메세지 모두 안읽음 (3개남음)
		chatRoomMemberForRoom3Member.updateLastVisibleMessage(chatMessage3ForRoom3);

		chatRoomMemberForRoom5Member.updateToDeleteChatRoom();

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/chat-rooms")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(DIRECTOR_SERVICE_ID, String.valueOf(directorService1.getId()))
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ChatRoomFindAllResponse response = objectMapper.readValue(responseJson, ChatRoomFindAllResponse.class);

		// then

		// 채팅방 회원별 마지막 읽은 메세지 갯수 확인
		List<ChatRoomResponse> chatRooms = response.getChatRooms();

		assertThat(chatRooms.size()).isEqualTo(3);

		// 채팅방 순서는 2,1,3
		assertThat(chatRooms.get(0).getId()).isEqualTo(chatRoom2.getId());
		assertThat(chatRooms.get(0).getLastMessage()).isEqualTo(chatMessage2ForRoom2.getContent());
		assertThat(chatRooms.get(0).getUnreadCount()).isEqualTo(0);

		assertThat(chatRooms.get(1).getId()).isEqualTo(chatRoom1.getId());
		assertThat(chatRooms.get(1).getLastMessage()).isEqualTo(chatMessage3ForRoom1.getContent());
		assertThat(chatRooms.get(1).getUnreadCount()).isEqualTo(2);

		assertThat(chatRooms.get(2).getId()).isEqualTo(chatRoom3.getId());
		assertThat(chatRooms.get(2).getLastMessage()).isEqualTo(chatMessage3ForRoom3.getContent());
		assertThat(chatRooms.get(2).getUnreadCount()).isEqualTo(3);
	}

	@Test
	@DisplayName("회원은 참여한 채팅방 목록을 조회할 수 있다. 읽지 않은 채팅방만 필터링")
	void findAllFilteredByShowOnlyUnread() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService service = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, service);

		ServiceRequest req = serviceRequestProvider.savePending(service, requester);
		ServiceEstimate est = serviceEstimateProvider.save(directorInfo, req);

		ServiceRequest req1 = serviceRequestProvider.savePending(service, requester);
		ServiceEstimate est1 = serviceEstimateProvider.save(directorInfo, req1);

		ChatRoom roomRead = chatRoomProvider.save();
		ChatRoom roomUnread = chatRoomProvider.save();

		chatRoomServiceEstimateMappingProvider.save(roomRead, est);
		chatRoomServiceEstimateMappingProvider.save(roomUnread, est1);

		ChatRoomMember mRead = chatRoomMemberProvider.saveMember(roomRead, requester);
		ChatRoomMember dRead = chatRoomMemberProvider.saveDirector(roomRead, director);

		ChatRoomMember mUnread = chatRoomMemberProvider.saveMember(roomUnread, requester);
		ChatRoomMember dUnread = chatRoomMemberProvider.saveDirector(roomUnread, director);

		ChatMessage msgRead1 = chatMessageProvider.saveTextType(roomRead, dRead, LocalDateTime.now().minusMinutes(2));
		ChatMessage msgRead2 = chatMessageProvider.saveTextType(roomRead, dRead, LocalDateTime.now().minusMinutes(1));

		ChatMessage msgUnread1 = chatMessageProvider.saveTextType(roomUnread, dUnread, LocalDateTime.now());

		roomRead.updateLastMessage(msgRead2);
		roomUnread.updateLastMessage(msgUnread1);

		mRead.updateLastReadMessage(msgRead2);   // 모두 읽음
		mRead.updateLastVisibleMessage(msgRead2);

		mUnread.updateLastReadMessage(null);     // 하나도 안읽음
		mUnread.updateLastVisibleMessage(msgUnread1);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/chat-rooms")
				.param(SHOW_ONLY_UNREAD, "true")
				.cookie(new Cookie(ACCESS_TOKEN, jwt.getAccessToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ChatRoomFindAllResponse response = objectMapper.readValue(responseJson, ChatRoomFindAllResponse.class);

		// then
		List<ChatRoomResponse> chatRooms = response.getChatRooms();
		assertThat(chatRooms).hasSize(1);
		assertThat(chatRooms.get(0).getId()).isEqualTo(roomUnread.getId());
		assertThat(chatRooms.get(0).getUnreadCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("회원은 참여한 채팅방 목록을 조회할 수 있다. (status 로 필터링 - PENDING)")
	void findAllFilteredByStatusPending() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService service = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, service);

		ServiceRequest pendingReq = serviceRequestProvider.savePending(service, requester);
		ServiceRequest ongoingReq = serviceRequestProvider.saveWithIsOngoingTrue(service, requester,
			LocalDateTime.now());

		ServiceEstimate estPending = serviceEstimateProvider.save(directorInfo, pendingReq);
		ServiceEstimate estOngoing = serviceEstimateProvider.saveOngoing(directorInfo, ongoingReq, LocalDateTime.now());

		ChatRoom roomPending = chatRoomProvider.save();
		ChatRoom roomOngoing = chatRoomProvider.save();

		chatRoomServiceEstimateMappingProvider.save(roomPending, estPending);
		chatRoomServiceEstimateMappingProvider.save(roomOngoing, estOngoing);

		ChatRoomMember m1 = chatRoomMemberProvider.saveMember(roomPending, requester);
		ChatRoomMember d1 = chatRoomMemberProvider.saveDirector(roomPending, director);

		ChatRoomMember m2 = chatRoomMemberProvider.saveMember(roomOngoing, requester);
		ChatRoomMember d2 = chatRoomMemberProvider.saveDirector(roomOngoing, director);

		ChatMessage msg1 = chatMessageProvider.saveTextType(roomPending, d1, LocalDateTime.now().minusMinutes(1));
		ChatMessage msg2 = chatMessageProvider.saveTextType(roomOngoing, d2, LocalDateTime.now());

		roomPending.updateLastMessage(msg1);
		roomOngoing.updateLastMessage(msg2);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/chat-rooms")
				.param(STATUS, "PENDING")
				.cookie(new Cookie(ACCESS_TOKEN, jwt.getAccessToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ChatRoomFindAllResponse response = objectMapper.readValue(responseJson, ChatRoomFindAllResponse.class);

		// then
		List<ChatRoomResponse> chatRooms = response.getChatRooms();
		assertThat(chatRooms).hasSize(1);
		assertThat(chatRooms.get(0).getId()).isEqualTo(roomPending.getId());
	}

	@Test
	@DisplayName("회원은 참여한 채팅방 목록을 조회할 수 있다. (검색어로 필터링 - 디렉터 닉네임 및 서비스명)")
	void findAllFilteredByWord() throws Exception {
		// given
		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo1);
		director1.updateNickname("테스트디렉터");

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo2);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService service1 = directorCategoryProvider.save("테스트서비스", parent);
		DirectorService service2 = directorCategoryProvider.save(SERVICE_NAME_3_STR, parent);

		directorServiceMappingProvider.save(directorInfo1, service1);
		directorServiceMappingProvider.save(directorInfo2, service2);

		ServiceRequest req1 = serviceRequestProvider.savePending(service1, requester);
		ServiceRequest req2 = serviceRequestProvider.savePending(service2, requester);

		ServiceEstimate est1 = serviceEstimateProvider.save(directorInfo1, req1);
		ServiceEstimate est2 = serviceEstimateProvider.save(directorInfo2, req2);

		ChatRoom room1 = chatRoomProvider.save();
		ChatRoom room2 = chatRoomProvider.save();

		chatRoomServiceEstimateMappingProvider.save(room1, est1);
		chatRoomServiceEstimateMappingProvider.save(room2, est2);

		ChatRoomMember m1 = chatRoomMemberProvider.saveMember(room1, requester);
		ChatRoomMember d1 = chatRoomMemberProvider.saveDirector(room1, director1);

		ChatRoomMember m2 = chatRoomMemberProvider.saveMember(room2, requester);
		ChatRoomMember d2 = chatRoomMemberProvider.saveDirector(room2, director2);

		ChatMessage msg1 = chatMessageProvider.saveTextType(room1, d1, LocalDateTime.now().minusMinutes(1));
		ChatMessage msg2 = chatMessageProvider.saveTextType(room2, d2, LocalDateTime.now());

		room1.updateLastMessage(msg1);
		room2.updateLastMessage(msg2);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/chat-rooms")
				.param(WORD, "테스트")
				.cookie(new Cookie(ACCESS_TOKEN, jwt.getAccessToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ChatRoomFindAllResponse response = objectMapper.readValue(responseJson, ChatRoomFindAllResponse.class);

		// then
		List<ChatRoomResponse> chatRooms = response.getChatRooms();
		assertThat(chatRooms).hasSize(1);
		assertThat(chatRooms.get(0).getId()).isEqualTo(room1.getId());
	}

	@Test
	@DisplayName("회원은 채팅방에서 사용된 서비스 목록을 조회할 수 있다.")
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

		// 서비스 매핑 저장 (디렉터가 제공하는 서비스)
		directorServiceMappingProvider.save(directorInfo1, directorService1);
		directorServiceMappingProvider.save(directorInfo2, directorService2);
		directorServiceMappingProvider.save(directorInfo2, directorService3);
		directorServiceMappingProvider.save(directorInfo2, directorService4);

		// requester 가 디렉터라면, 본인이 제안을 보냈던 서비스에 대해서는 회원 전용 채팅방 서비스 목록에 노출이 되면 안된다.
		directorServiceMappingProvider.save(directorInfo3, directorService5);

		// 요청 저장
		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService1, requester);
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService2, requester);
		ServiceRequest serviceRequest3 = serviceRequestProvider.savePending(directorService3, requester);
		ServiceRequest serviceRequest4 = serviceRequestProvider.savePending(directorService4, requester);

		ServiceRequest serviceRequest5 = serviceRequestProvider.savePending(directorService5, director2);

		// 제안 저장 (각 채팅방에서 사용된 서비스와 연결되는 제안)
		ServiceEstimate estimate1 = serviceEstimateProvider.save(directorInfo1, serviceRequest1);
		ServiceEstimate estimate2 = serviceEstimateProvider.save(directorInfo2, serviceRequest2);
		ServiceEstimate estimate3 = serviceEstimateProvider.save(directorInfo2, serviceRequest3);
		ServiceEstimate estimate4 = serviceEstimateProvider.save(directorInfo2, serviceRequest4);

		ServiceEstimate estimate5 = serviceEstimateProvider.save(directorInfo3, serviceRequest5);

		// 채팅방 저장
		ChatRoom chatRoom1 = chatRoomProvider.save();
		ChatRoom chatRoom2 = chatRoomProvider.save();
		ChatRoom chatRoom3 = chatRoomProvider.save();
		ChatRoom chatRoom4 = chatRoomProvider.save();
		ChatRoom chatRoom5 = chatRoomProvider.save();

		// 채팅방 - 제안 매핑 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom1, estimate1);
		chatRoomServiceEstimateMappingProvider.save(chatRoom2, estimate2);
		chatRoomServiceEstimateMappingProvider.save(chatRoom3, estimate3);
		chatRoomServiceEstimateMappingProvider.save(chatRoom4, estimate4);
		chatRoomServiceEstimateMappingProvider.save(chatRoom5, estimate5);

		// 채팅방 참여자 매핑 저장
		chatRoomMemberProvider.saveMember(chatRoom1, requester);
		chatRoomMemberProvider.saveDirector(chatRoom1, director1);

		chatRoomMemberProvider.saveMember(chatRoom2, requester);
		chatRoomMemberProvider.saveDirector(chatRoom2, director2);

		chatRoomMemberProvider.saveMemberWithRoomDeletedTrue(chatRoom3, requester);
		chatRoomMemberProvider.saveDirector(chatRoom3, director2);

		chatRoomMemberProvider.saveMember(chatRoom4, requester);
		chatRoomMemberProvider.saveDirector(chatRoom4, director2);

		chatRoomMemberProvider.saveMember(chatRoom5, director2);
		chatRoomMemberProvider.saveDirector(chatRoom5, requester);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/chat-rooms/services")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ChatRoomFindChatRoomServicesResponse response = objectMapper.readValue(responseJson,
			ChatRoomFindChatRoomServicesResponse.class);

		// then
		assertThat(response.getServices()).hasSize(3);

		List<Long> ids = response.getServices().stream().map(DirectorServiceWithFullNameResponse::getId).toList();
		assertThat(ids).containsExactlyInAnyOrder(directorService1.getId(), directorService2.getId(),
			directorService4.getId());
	}

	@Test
	@DisplayName("회원은 채팅방 상세 정보를 조회할 수 있다.")
	void findDetail() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			directorInfo);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, requester);
		ServiceEstimate estimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);

		ChatRoomMember chatRoomMemberForRequester = chatRoomMemberProvider.saveMember(chatRoom, requester);
		ChatRoomMember chatRoomMemberForDirector = chatRoomMemberProvider.saveDirector(chatRoom, director);

		// 1. 일반 메세지 저장
		ChatMessage textMessage1 = chatMessageProvider.saveTextType(chatRoom, chatRoomMemberForRequester,
			LocalDateTime.now().minusHours(1));
		ChatMessage textMessage2 = chatMessageProvider.saveTextType(chatRoom, chatRoomMemberForDirector,
			LocalDateTime.now().minusHours(2));
		ChatMessage textMessage3 = chatMessageProvider.saveTextType(chatRoom, chatRoomMemberForRequester,
			LocalDateTime.now().minusHours(3));

		// 2. 사진 메세지 저장
		ChatMessage imageMessage1 = chatMessageProvider.saveImageType(chatRoom, chatRoomMemberForDirector,
			LocalDateTime.now().minusHours(4));

		ChatFile chatFile1 = chatFileProvider.saveWithChatMessage(director, imageMessage1);
		ChatFile chatFile2 = chatFileProvider.saveWithChatMessage(director, imageMessage1);

		// 3. estimate 저장
		ChatMessage estimateMessage = chatMessageProvider.saveEstimateType(chatRoom, chatRoomMemberForDirector,
			estimate, LocalDateTime.now().minusHours(5));

		// 4. 상대방이 보낸 일반 텍스트 메세지
		ChatMessage requesterMessage = chatMessageProvider.saveTextType(chatRoom, chatRoomMemberForRequester,
			LocalDateTime.now().minusHours(8));

		// 5. 삭제된 메세지 저장
		ChatMessage deletedChatMessage = chatMessageProvider.saveWithIsDeletedTrue(chatRoom, chatRoomMemberForDirector,
			LocalDateTime.now().minusHours(7));

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/chat-rooms/{chatRoomId}", chatRoom.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		entityManager.flush();
		entityManager.clear();

		ChatRoomFindDetailResponse response = objectMapper.readValue(responseJson, ChatRoomFindDetailResponse.class);

		// then
		MemberResponse opponent = response.getOpponent();
		assertThat(opponent.getId()).isEqualTo(director.getId());

		ServiceRequestForChatRoomFindDetailResponse request = response.getRequest();
		assertThat(request.getId()).isEqualTo(serviceRequest.getId());

		ChatMessageFindAllResponse chatMessageResponse = response.getMessageResponse();
		List<ChatMessageResponse> chatMessages = chatMessageResponse.getChatMessages();

		assertThat(chatMessages.size()).isEqualTo(6);

		// 메시지는 최신순으로 정렬되어야 함
		List<Long> messageIds = chatMessages.stream()
			.map(ChatMessageResponse::getId)
			.toList();

		assertThat(messageIds).containsExactly(
			textMessage1.getId(),
			textMessage2.getId(),
			textMessage3.getId(),
			imageMessage1.getId(),
			estimateMessage.getId(),
			requesterMessage.getId()
		);

		// 회원의 마지막읽은 메세지 업데이트 여부 확인
		ChatRoomMember updatedChatRoomMember = chatRoomMemberProvider.findById(chatRoomMemberForRequester.getId());

		assertThat(updatedChatRoomMember.getLastReadMessage().getId()).isEqualTo(textMessage1.getId());

		// SSE 발행 검증 (REFRESH_NAV_CHAT_COUNT)
		verify(sseService, atLeastOnce()).refreshNavChatCount(
			argThat(payload ->
				payload.getEventName() == SseEventType.REFRESH_NAV_CHAT_COUNT &&
					payload.getReceiverId().equals(requester.getId()) && payload.getReceiverRole() == Role.MEMBER));
	}

	@Test
	@DisplayName("디렉터는 채팅방 상세 정보를 조회할 수 있다.")
	void findDetailWithDirector() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			directorInfo);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, requester);
		ServiceEstimate estimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);

		ChatRoomMember chatRoomMemberForRequester = chatRoomMemberProvider.saveMember(chatRoom, requester);
		ChatRoomMember chatRoomMemberForDirector = chatRoomMemberProvider.saveDirector(chatRoom, director);

		// 1. 일반 메세지 저장
		ChatMessage textMessage1 = chatMessageProvider.saveTextType(chatRoom, chatRoomMemberForRequester,
			LocalDateTime.now().minusHours(1));
		ChatMessage textMessage2 = chatMessageProvider.saveTextType(chatRoom, chatRoomMemberForDirector,
			LocalDateTime.now().minusHours(2));
		ChatMessage textMessage3 = chatMessageProvider.saveTextType(chatRoom, chatRoomMemberForRequester,
			LocalDateTime.now().minusHours(3));

		// 2. 사진 메세지 저장
		ChatMessage imageMessage1 = chatMessageProvider.saveImageType(chatRoom, chatRoomMemberForDirector,
			LocalDateTime.now().minusHours(4));

		ChatFile chatFile1 = chatFileProvider.saveWithChatMessage(director, imageMessage1);
		ChatFile chatFile2 = chatFileProvider.saveWithChatMessage(director, imageMessage1);

		// 3. estimate 저장
		ChatMessage estimateMessage = chatMessageProvider.saveEstimateType(chatRoom, chatRoomMemberForDirector,
			estimate, LocalDateTime.now().minusHours(5));

		// 4. 상대방이 보낸 일반 텍스트 메세지
		ChatMessage requesterMessage = chatMessageProvider.saveTextType(chatRoom, chatRoomMemberForRequester,
			LocalDateTime.now().minusHours(8));

		// 5. 삭제된 메세지 저장
		ChatMessage deletedChatMessage = chatMessageProvider.saveWithIsDeletedTrue(chatRoom, chatRoomMemberForDirector,
			LocalDateTime.now().minusHours(7));

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/chat-rooms/{chatRoomId}", chatRoom.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		entityManager.flush();
		entityManager.clear();

		ChatRoomFindDetailResponse response = objectMapper.readValue(responseJson, ChatRoomFindDetailResponse.class);

		// then
		MemberResponse opponent = response.getOpponent();
		assertThat(opponent.getId()).isEqualTo(requester.getId());

		ServiceRequestForChatRoomFindDetailResponse request = response.getRequest();
		assertThat(request.getId()).isEqualTo(serviceRequest.getId());

		ChatMessageFindAllResponse chatMessageResponse = response.getMessageResponse();
		List<ChatMessageResponse> chatMessages = chatMessageResponse.getChatMessages();

		assertThat(chatMessages.size()).isEqualTo(6);

		// 메시지는 최신순으로 정렬되어야 함
		List<Long> messageIds = chatMessages.stream()
			.map(ChatMessageResponse::getId)
			.toList();

		assertThat(messageIds).containsExactly(
			textMessage1.getId(),
			textMessage2.getId(),
			textMessage3.getId(),
			imageMessage1.getId(),
			estimateMessage.getId(),
			requesterMessage.getId()
		);

		// 회원의 마지막읽은 메세지 업데이트 여부 확인
		ChatRoomMember updatedChatRoomDirector = chatRoomMemberProvider.findById(chatRoomMemberForDirector.getId());

		assertThat(updatedChatRoomDirector.getLastReadMessage().getId()).isEqualTo(textMessage1.getId());

		// SSE 발행 검증 (REFRESH_NAV_CHAT_COUNT)
		verify(sseService, atLeastOnce()).refreshNavChatCount(
			argThat(payload ->
				payload.getEventName() == SseEventType.REFRESH_NAV_CHAT_COUNT &&
					payload.getReceiverId().equals(director.getId()) && payload.getReceiverRole() == Role.DIRECTOR));
	}

	@Test
	@DisplayName("회원은 채팅방 상세 정보를 조회할 수 있다. (채팅방 회원의 lastReadMessage 가 null 이 아닐때)")
	void findDetailWhenChatRoomMemberLastReadMessageNotNull() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			directorInfo);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, requester);
		ServiceEstimate estimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);

		ChatRoomMember chatRoomMemberForRequester = chatRoomMemberProvider.saveMember(chatRoom, requester);
		ChatRoomMember chatRoomMemberForDirector = chatRoomMemberProvider.saveDirector(chatRoom, director);

		List<ChatMessage> messages = new ArrayList<>();
		// 1. 일반 메세지 저장
		IntStream.range(0, 30).forEach(i -> {
			ChatMessage chatMessages = chatMessageProvider.saveTextType(chatRoom, chatRoomMemberForRequester,
				LocalDateTime.now().plusMinutes(i));

			messages.add(chatMessages);
		});

		// 3. lastMessage 업데이트
		chatRoomMemberForRequester.updateLastReadMessage(messages.get(28)); // 마지막 읽은 메세지는 28번쨰 메세지

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/chat-rooms/{chatRoomId}", chatRoom.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		entityManager.flush();
		entityManager.clear();

		// then

		// 회원의 마지막읽은 메세지 업데이트 여부 확인
		ChatRoomMember updatedChatRoomMember = chatRoomMemberProvider.findById(chatRoomMemberForRequester.getId());

		assertThat(updatedChatRoomMember.getLastReadMessage().getId()).isEqualTo(messages.get(29).getId());

	}

	@Test
	@DisplayName("회원은 채팅방 상세 정보를 조회할 수 있다. (lastMessageId 가 null 이 아닐때)")
	void findDetailWithLastMessageId() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			directorInfo);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, requester);
		ServiceEstimate estimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);

		ChatRoomMember chatRoomMemberForRequester = chatRoomMemberProvider.saveMember(chatRoom, requester);
		ChatRoomMember chatRoomMemberForDirector = chatRoomMemberProvider.saveDirector(chatRoom, director);

		List<ChatMessage> messages = new ArrayList<>();
		// 1. 일반 메세지 저장
		IntStream.range(0, 30).forEach(i -> {
			ChatMessage chatMessages = chatMessageProvider.saveTextType(chatRoom, chatRoomMemberForRequester,
				LocalDateTime.now().plusMinutes(i));

			messages.add(chatMessages);
		});

		// 3. lastMessage 업데이트
		chatRoomMemberForRequester.updateLastReadMessage(messages.get(29)); // 마지막 읽은 메세지는 29번쨰 메세지

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/chat-rooms/{chatRoomId}", chatRoom.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(LAST_MESSAGE_ID, String.valueOf(messages.get(8).getId()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		entityManager.flush();
		entityManager.clear();

		ChatRoomFindDetailResponse response = objectMapper.readValue(responseJson, ChatRoomFindDetailResponse.class);

		// then
		List<ChatMessageResponse> chatMessages = response.getMessageResponse().getChatMessages();

		// chatMessage 의 id id 는 messageIds.get(0) ~ messageIds.get(7) 까지 존재해야함 (총 8개)
		assertThat(chatMessages.size()).isEqualTo(8);

		List<Long> returnedMessageIds = chatMessages.stream()
			.map(ChatMessageResponse::getId)
			.toList();

		assertThat(returnedMessageIds).containsExactly(
			messages.get(7).getId(),
			messages.get(6).getId(),
			messages.get(5).getId(),
			messages.get(4).getId(),
			messages.get(3).getId(),
			messages.get(2).getId(),
			messages.get(1).getId(),
			messages.get(0).getId()
		);

		// 회원의 마지막읽은 메세지는 업데이트 되면 안된다.
		ChatRoomMember updatedChatRoomMember = chatRoomMemberProvider.findById(chatRoomMemberForRequester.getId());
		assertThat(updatedChatRoomMember.getLastReadMessage().getId()).isEqualTo(messages.get(29).getId());

	}

	@Test
	@DisplayName("회원은 채팅방 상세 정보를 조회할 수 있다. (request 가 2개 존재할때)")
	void findDetailWhenRequestExistMoreThan2() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			directorInfo);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, requester);
		ServiceRequest serviceRequest2 = serviceRequestProvider.saveWithIsCompletedTrue(directorService, requester,
			LocalDateTime.now().minusWeeks(1));

		ServiceEstimate estimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);

		ChatRoomMember chatRoomMemberForRequester = chatRoomMemberProvider.saveMember(chatRoom, requester);
		ChatRoomMember chatRoomMemberForDirector = chatRoomMemberProvider.saveDirector(chatRoom, director);

		// 1. 일반 메세지 저장
		ChatMessage textMessage1 = chatMessageProvider.saveTextType(chatRoom, chatRoomMemberForRequester,
			LocalDateTime.now().minusHours(1));
		ChatMessage textMessage2 = chatMessageProvider.saveTextType(chatRoom, chatRoomMemberForDirector,
			LocalDateTime.now().minusHours(2));
		ChatMessage textMessage3 = chatMessageProvider.saveTextType(chatRoom, chatRoomMemberForRequester,
			LocalDateTime.now().minusHours(3));

		// 2. 사진 메세지 저장
		ChatMessage imageMessage1 = chatMessageProvider.saveImageType(chatRoom, chatRoomMemberForDirector,
			LocalDateTime.now().minusHours(4));

		ChatFile chatFile1 = chatFileProvider.saveWithChatMessage(director, imageMessage1);
		ChatFile chatFile2 = chatFileProvider.saveWithChatMessage(director, imageMessage1);

		// 3. estimate 저장
		ChatMessage estimateMessage = chatMessageProvider.saveEstimateType(chatRoom, chatRoomMemberForDirector,
			estimate, LocalDateTime.now().minusHours(5));

		// 4. 상대방이 보낸 일반 텍스트 메세지
		ChatMessage requesterMessage = chatMessageProvider.saveTextType(chatRoom, chatRoomMemberForRequester,
			LocalDateTime.now().minusHours(8));

		// 5. 삭제된 메세지 저장
		ChatMessage deletedChatMessage = chatMessageProvider.saveWithIsDeletedTrue(chatRoom, chatRoomMemberForDirector,
			LocalDateTime.now().minusHours(7));

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/chat-rooms/{chatRoomId}", chatRoom.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		entityManager.flush();
		entityManager.clear();

		ChatRoomFindDetailResponse response = objectMapper.readValue(responseJson, ChatRoomFindDetailResponse.class);

		// then
		MemberResponse opponent = response.getOpponent();
		assertThat(opponent.getId()).isEqualTo(director.getId());

		ServiceRequestForChatRoomFindDetailResponse request = response.getRequest();
		assertThat(request.getId()).isEqualTo(serviceRequest.getId());

		ChatMessageFindAllResponse chatMessageResponse = response.getMessageResponse();
		List<ChatMessageResponse> chatMessages = chatMessageResponse.getChatMessages();

		assertThat(chatMessages.size()).isEqualTo(6);

		// 메시지는 최신순으로 정렬되어야 함
		List<Long> messageIds = chatMessages.stream()
			.map(ChatMessageResponse::getId)
			.toList();

		assertThat(messageIds).containsExactly(
			textMessage1.getId(),
			textMessage2.getId(),
			textMessage3.getId(),
			imageMessage1.getId(),
			estimateMessage.getId(),
			requesterMessage.getId()
		);

		// 회원의 마지막읽은 메세지 업데이트 여부 확인
		ChatRoomMember updatedChatRoomMember = chatRoomMemberProvider.findById(chatRoomMemberForRequester.getId());

		assertThat(updatedChatRoomMember.getLastReadMessage().getId()).isEqualTo(textMessage1.getId());

	}

	@Test
	@DisplayName("회원은 참여하지 않은 채팅방의 상세 정보를 조회할 수 없다.")
	void findDetailNotParticipant() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			directorInfo);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member otherMember = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(otherMember.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, requester);
		ServiceEstimate estimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);

		chatRoomMemberProvider.saveMember(chatRoom, requester);
		chatRoomMemberProvider.saveDirector(chatRoom, director);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.get("/api/chat-rooms/{chatRoomId}", chatRoom.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isForbidden())
			.andExpect(
				jsonPath(ERROR_STATUS).value(ChatRoomMemberException.NOT_IN_CHAT_ROOM.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ChatRoomMemberException.NOT_IN_CHAT_ROOM.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ChatRoomMemberException.NOT_IN_CHAT_ROOM.getCode()));
	}

	@Test
	@DisplayName("회원은 채팅방을 나갈 수 있다.")
	void delete() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, requester);
		ServiceEstimate estimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);

		ChatRoomMember chatRoomMemberForRequester = chatRoomMemberProvider.saveMember(chatRoom, requester);
		ChatRoomMember chatRoomMemberForDirector = chatRoomMemberProvider.saveDirector(chatRoom, director);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/chat-rooms/{chatRoomId}", chatRoom.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		ChatRoomMember updatedRequesterMember = chatRoomMemberProvider.findById(chatRoomMemberForRequester.getId());
		assertThat(updatedRequesterMember.getIsChatRoomDeleted()).isTrue();

		ChatRoomMember updatedDirectorMember = chatRoomMemberProvider.findById(chatRoomMemberForDirector.getId());
		assertThat(updatedDirectorMember.getIsChatRoomDeleted()).isFalse();

		// SSE 발행 검증 (REFRESH_CHAT_ROOM_LIST)
		await()
			.atMost(Duration.ofSeconds(2))
			.untilAsserted(() ->
				verify(sseService, atLeastOnce()).notifyChatRoomLeft(
					argThat(payload ->
						payload.getEventName() == SseEventType.LEAVE_CHAT_ROOM &&
							Objects.equals(payload.getReceiverId(), requester.getId()) &&
							Objects.equals(payload.getReceiverRole(), Role.MEMBER)
					)
				)
			);
	}

	@Test
	@DisplayName("회원은 참여하지 않은 채팅방을 나갈 수 없다.")
	void deleteNotParticipant() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member otherMember = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(otherMember.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, requester);
		ServiceEstimate estimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);

		chatRoomMemberProvider.saveMember(chatRoom, requester);
		chatRoomMemberProvider.saveDirector(chatRoom, director);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/chat-rooms/{chatRoomId}", chatRoom.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(
				jsonPath(ERROR_STATUS).value(ChatRoomMemberException.NOT_FOUND_IN_CHAT_ROOM.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ChatRoomMemberException.NOT_FOUND_IN_CHAT_ROOM.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ChatRoomMemberException.NOT_FOUND_IN_CHAT_ROOM.getCode()));
	}

	@Test
	@DisplayName("회원은 읽지 않은 전체 채팅 메세지 개수를 조회할 수 있다. (나간 채팅방이 존재할경우)")
	void findTotalUnreadCountWhenDeletedChatRoomExist() throws Exception {
		// given
		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member opponent1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member opponent2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo2);

		DirectorInfo directorInfo3 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member opponent3 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo3);

		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		// 채팅방 저장
		ChatRoom chatRoom1 = chatRoomProvider.save();
		ChatRoom chatRoom2 = chatRoomProvider.save();
		ChatRoom chatRoom3 = chatRoomProvider.save();
		ChatRoom chatRoom4 = chatRoomProvider.save();

		//채팅방 회원 저장
		ChatRoomMember requesterMember1 = chatRoomMemberProvider.saveMember(chatRoom1, requester);
		ChatRoomMember opponentMember1 = chatRoomMemberProvider.saveDirector(chatRoom1, opponent1);

		ChatRoomMember requesterMember2 = chatRoomMemberProvider.saveMember(chatRoom2, requester);
		ChatRoomMember opponentMember2 = chatRoomMemberProvider.saveDirector(chatRoom2, opponent2);

		ChatRoomMember requesterMember3 = chatRoomMemberProvider.saveMember(chatRoom3, requester);
		ChatRoomMember opponentMember3 = chatRoomMemberProvider.saveDirector(chatRoom3, opponent3);

		ChatRoomMember requesterMember4 = chatRoomMemberProvider.saveMemberWithRoomDeletedTrue(chatRoom4, requester);
		ChatRoomMember opponentMember4 = chatRoomMemberProvider.saveDirector(chatRoom4, opponent1);

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
		requesterMember1.updateLastReadMessage(room1Message1);
		requesterMember1.updateLastVisibleMessage(room1Message3);

		requesterMember2.updateLastReadMessage(room2Message2);
		requesterMember2.updateLastVisibleMessage(room2Message2);

		requesterMember3.updateLastVisibleMessage(room3Message1);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/chat-rooms/unread-count")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ChatRoomUnreadCountResponse response = objectMapper.readValue(responseJson,
			ChatRoomUnreadCountResponse.class);

		// then
		assertThat(response.getTotalUnreadCount()).isEqualTo(3); // 2개 + 0개 + 1개
	}

	@Test
	@DisplayName("회원은 읽지 않은 전체 채팅 메세지 개수를 조회할 수 있다. (차단한 사용자로부터 받은 읽지않은 메세지가 존재할 경우)")
	void findTotalUnreadCountWhenBlockedMemberUnreadMessageExist() throws Exception {
		// given
		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member opponent1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member opponent2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo2);

		DirectorInfo directorInfo3 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member opponent3 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo3);

		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		// 채팅방 저장
		ChatRoom chatRoom1 = chatRoomProvider.save();
		ChatRoom chatRoom2 = chatRoomProvider.save();
		ChatRoom chatRoom3 = chatRoomProvider.save();
		ChatRoom chatRoom4 = chatRoomProvider.save();

		//채팅방 회원 저장
		ChatRoomMember requesterMember1 = chatRoomMemberProvider.saveMember(chatRoom1, requester);
		ChatRoomMember opponentMember1 = chatRoomMemberProvider.saveDirector(chatRoom1, opponent1);

		ChatRoomMember requesterMember2 = chatRoomMemberProvider.saveMember(chatRoom2, requester);
		ChatRoomMember opponentMember2 = chatRoomMemberProvider.saveDirector(chatRoom2, opponent2);

		ChatRoomMember requesterMember3 = chatRoomMemberProvider.saveMember(chatRoom3, requester);
		ChatRoomMember opponentMember3 = chatRoomMemberProvider.saveDirector(chatRoom3, opponent3);

		ChatRoomMember requesterMember4 = chatRoomMemberProvider.saveMemberWithRoomDeletedTrue(chatRoom4, requester);
		ChatRoomMember opponentMember4 = chatRoomMemberProvider.saveDirector(chatRoom4, opponent1);

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
		requesterMember1.updateLastReadMessage(room1Message1);
		requesterMember1.updateLastVisibleMessage(room1Message3);

		requesterMember2.updateLastReadMessage(room2Message1);
		requesterMember2.updateLastVisibleMessage(room2Message1);

		requesterMember3.updateLastVisibleMessage(room3Message1);

		// 차단 저장
		memberBlockProvider.save(requester, opponent2);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/chat-rooms/unread-count")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ChatRoomUnreadCountResponse response = objectMapper.readValue(responseJson,
			ChatRoomUnreadCountResponse.class);

		// then
		assertThat(response.getTotalUnreadCount()).isEqualTo(3); // 2개 + 0개 + 1개
	}

	@Test
	@DisplayName("회원은 읽지 않은 전체 채팅 메세지 개수를 조회할 수 있다. (내가 디렉터로 받은 읽지않은 메세지가 존재하는 경우)")
	void findTotalUnreadCountWhenUnreadMessageExistWithRoleDirector() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member requester = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member opponent1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member opponent2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo2);

		DirectorInfo directorInfo3 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member opponent3 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo3);

		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		// 채팅방 저장
		ChatRoom chatRoom1 = chatRoomProvider.save();
		ChatRoom chatRoom2 = chatRoomProvider.save();
		ChatRoom chatRoom3 = chatRoomProvider.save();
		ChatRoom chatRoom4 = chatRoomProvider.save();
		ChatRoom chatRoom5 = chatRoomProvider.save();

		//채팅방 회원 저장
		ChatRoomMember requesterMember1 = chatRoomMemberProvider.saveMember(chatRoom1, requester);
		ChatRoomMember opponentMember1 = chatRoomMemberProvider.saveDirector(chatRoom1, opponent1);

		ChatRoomMember requesterMember2 = chatRoomMemberProvider.saveMember(chatRoom2, requester);
		ChatRoomMember opponentMember2 = chatRoomMemberProvider.saveDirector(chatRoom2, opponent2);

		ChatRoomMember requesterMember3 = chatRoomMemberProvider.saveMember(chatRoom3, requester);
		ChatRoomMember opponentMember3 = chatRoomMemberProvider.saveDirector(chatRoom3, opponent3);

		ChatRoomMember requesterMember4 = chatRoomMemberProvider.saveMemberWithRoomDeletedTrue(chatRoom4, requester);
		ChatRoomMember opponentMember4 = chatRoomMemberProvider.saveDirector(chatRoom4, opponent1);

		ChatRoomMember opponentMember5 = chatRoomMemberProvider.saveMember(chatRoom5, opponent1);
		ChatRoomMember requesterMember5 = chatRoomMemberProvider.saveDirector(chatRoom5, requester);

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
		requesterMember1.updateLastReadMessage(room1Message1);
		requesterMember1.updateLastVisibleMessage(room1Message3);

		requesterMember2.updateLastReadMessage(room2Message1);
		requesterMember2.updateLastVisibleMessage(room2Message1);

		requesterMember3.updateLastVisibleMessage(room3Message1);

		// 차단 저장
		memberBlockProvider.save(requester, opponent2);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/chat-rooms/unread-count")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ChatRoomUnreadCountResponse response = objectMapper.readValue(responseJson,
			ChatRoomUnreadCountResponse.class);

		// then
		assertThat(response.getTotalUnreadCount()).isEqualTo(3); // 2개 + 0개 + 1개
	}

}
