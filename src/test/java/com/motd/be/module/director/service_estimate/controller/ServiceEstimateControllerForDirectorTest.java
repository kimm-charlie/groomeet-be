package com.motd.be.module.director.service_estimate.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.Constants.ERROR_CODE;
import static com.motd.be.Constants.ERROR_MESSAGE;
import static com.motd.be.Constants.ERROR_STATUS;
import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.constants.ValidationConstants.*;
import static com.motd.be.common.constants.ValidationMessages.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static com.motd.be.common.utils.DateFormatUtils.*;
import static com.motd.be.provider.module.member.ServiceRequestProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.DirectorServiceException;
import com.motd.be.exception.exceptions.ForbiddenWordException;
import com.motd.be.exception.exceptions.HandlerException;
import com.motd.be.exception.exceptions.MemberException;
import com.motd.be.exception.exceptions.ServiceEstimateException;
import com.motd.be.exception.exceptions.ServiceEstimateFileException;
import com.motd.be.exception.exceptions.ServiceRequestException;
import com.motd.be.exception.exceptions.TimeSlotException;
import com.motd.be.module.director.service_estimate.dto.request.ServiceEstimateSaveAdditionalRequestForDirector;
import com.motd.be.module.director.service_estimate.dto.request.ServiceEstimateSaveRequestForDirector;
import com.motd.be.module.director.service_estimate.dto.request.ServiceEstimateUpdateRequestForDirector;
import com.motd.be.module.director.service_estimate.dto.response.ServiceEstimateFindAllResponseForDirector;
import com.motd.be.module.director.service_estimate.dto.response.ServiceEstimateFindCountsResponseForDirector;
import com.motd.be.module.director.service_estimate.dto.response.ServiceEstimateFindDetailResponseForDirector;
import com.motd.be.module.director.service_estimate.dto.response.ServiceEstimateHistoriesResponseForDirector;
import com.motd.be.module.director.service_estimate.dto.response.ServiceEstimateHistoryResponseForDirector;
import com.motd.be.module.director.service_estimate.dto.response.ServiceEstimateResponseForDirector;
import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_message.entity.ChatMessageType;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.chat_room_service_estimate_mapping.entity.ChatRoomServiceEstimateMapping;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.entity.LocationType;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.Role;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.notification.entity.Notification;
import com.motd.be.module.member.notification.entity.NotificationReceiverType;
import com.motd.be.module.member.notification.entity.NotificationType;
import com.motd.be.module.member.request_location_mapping.entity.RequestLocationMapping;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateReminderStatus;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.motd.be.module.member.service_estimate_file.entity.ServiceEstimateFile;
import com.motd.be.module.member.service_estimate_template.entity.ServiceEstimateTemplate;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;
import com.motd.be.module.member.service_request_wish_time.entity.ServiceRequestWishTime;
import com.motd.be.module.member.sse.SseEventType;
import com.motd.be.shared.firebase.dto.FirebasePushEvent;
import com.motd.be.shared.firebase.entity.FirebaseCampaignSpec;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class ServiceEstimateControllerForDirectorTest extends BaseIntegrationTest {

	@Test
	@DisplayName("디렉터가 서비스 제안을 정상적으로 저장할 수 있다. (isAddedForAutoInfo: false, 채팅방이 존재하지 않는 경우)")
	void saveWithIsAddedForAutoInfoFalseAndChatRoomNotExist() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, member);

		ServiceEstimateSaveRequestForDirector request = ServiceEstimateSaveRequestForDirector.builder()
			.serviceRequestId(serviceRequest.getId())
			.title(TITLE_STR)
			.price(10000L)
			.content(CONTENT_STR)
			.scheduledAt(DEFAULT_SCHEDULED_AT)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		entityManager.flush();
		entityManager.clear();

		//then
		List<ServiceEstimate> estimates = serviceEstimateProvider.findAll();

		assertThat(estimates).hasSize(1);

		ServiceEstimate savedEstimate = estimates.get(0);
		assertThat(savedEstimate.getDirectorInfo().getId()).isEqualTo(directorInfo.getId());
		assertThat(savedEstimate.getServiceRequest().getId()).isEqualTo(serviceRequest.getId());
		assertThat(savedEstimate.getTitle()).isEqualTo(TITLE_STR);
		assertThat(savedEstimate.getPrice()).isEqualTo(10000L);
		assertThat(savedEstimate.getContent()).isEqualTo(CONTENT_STR);
		assertThat(savedEstimate.getActiveUniqueKey()).isNotNull();

		// 채팅방 및 첫 제안 메세지가 저장 되었는지 확인 한다.
		List<ChatRoom> chatRooms = chatRoomProvider.findAll();

		assertThat(chatRooms).hasSize(1);
		ChatRoom chatRoom = chatRooms.get(0);

		// 채팅방 제안 매핑이 잘 저장되었는지 확인 한다.
		List<ChatRoomServiceEstimateMapping> mappings = chatRoomServiceEstimateMappingProvider.findAll();
		assertThat(mappings).hasSize(1);

		// 채팅방 회원이 저장 되었는지 확인 한다.
		List<ChatRoomMember> chatRoomMembers = chatRoomMemberProvider.findAll();

		assertThat(chatRoomMembers).hasSize(2);

		// 채팅방 회원중 director 가 isDirector 가 true 인지 확인한다.
		ChatRoomMember chatRoomMember = chatRoomMembers.stream()
			.filter(member1 -> member1.getMember().getId().equals(director.getId()))
			.findFirst()
			.orElseThrow();

		assertThat(chatRoomMember.getIsDirector()).isTrue();

		// 채팅 첫 제안 메세지가 저장 되었는지 확인한다.
		List<ChatMessage> chatMessages = chatMessageProvider.findAll();

		assertThat(chatMessages).hasSize(1);
		assertThat(chatMessages.get(0).getMessageType()).isEqualTo(ChatMessageType.ESTIMATE);
		assertThat(chatMessages.get(0).getServiceEstimate().getId()).isEqualTo(savedEstimate.getId());

		// 채팅방의 마지막 메세지가 업데이트 되었는지 확인한다.
		ChatRoom updatedChatRoom = chatRoomProvider.findById(chatRoom.getId());
		assertThat(updatedChatRoom.getLastMessage().getId()).isEqualTo(chatMessages.get(0).getId());

		// 회원의 마지막 메세지가 업데이트 되었는지 확인한다.
		List<ChatRoomMember> updatedChatRooms = chatRoomMemberProvider.findAll();

		ChatRoomMember updatedChatRoomSender = updatedChatRooms.stream()
			.filter(m -> m.getMember().getId().equals(director.getId()))
			.findFirst()
			.orElseThrow();

		assertThat(updatedChatRoomSender.getLastReadMessage().getId()).isEqualTo(chatMessages.get(0).getId());
		assertThat(updatedChatRoomSender.getLastVisibleMessage().getId()).isEqualTo(chatMessages.get(0).getId());

		ChatRoomMember updatedChatRoomReceiver = updatedChatRooms.stream()
			.filter(m -> m.getMember().getId().equals(member.getId()))
			.findFirst()
			.orElseThrow();

		assertThat(updatedChatRoomReceiver.getLastVisibleMessage().getId()).isEqualTo(chatMessages.get(0).getId());

		// 알림 존재 여부 확인
		List<Notification> notifications = notificationProvider.findAll();

		assertThat(notifications.get(0).getReceiverType()).isEqualTo(NotificationReceiverType.MEMBER);
		assertThat(notifications.get(0).getType()).isEqualTo(NotificationType.ESTIMATE_ARRIVED);

		// serviceRequest 의 receivedEstimateCound 가 1 로 변경되었는지 확인한다.
		ServiceRequest updatedServiceRequest = serviceRequestProvider.findById(serviceRequest.getId());
		assertThat(updatedServiceRequest.getReceivedEstimateCount()).isEqualTo(1);

		// SSE 발행 검증 (REFRESH_CHAT_ROOM_LIST)
		verify(sseService, atLeastOnce()).refreshChatRoomList(
			argThat(payload ->
				payload.getEventName() == SseEventType.REFRESH_CHAT_ROOM_LIST &&
					Objects.equals(payload.getReceiverId(), member.getId()) &&
					Objects.equals(payload.getReceiverRole(), Role.MEMBER)));

		// SSE 발행 검증 (REFRESH_NAV_CHAT_COUNT)
		verify(sseService, atLeastOnce()).refreshNavChatCount(
			argThat(payload ->
				payload.getEventName() == SseEventType.REFRESH_NAV_CHAT_COUNT &&
					payload.getReceiverId().equals(member.getId()) &&
					Objects.equals(payload.getReceiverRole(), Role.MEMBER)));

		// SSE 발행 검증 (REFRESH_NOTIFICATION_COUNT)
		verify(sseService, atLeastOnce()).refreshNotificationCount(
			argThat(payload ->
				payload.getEventName() == SseEventType.REFRESH_NOTIFICATION_COUNT &&
					Objects.equals(payload.getReceiverId(), member.getId()) &&
					Objects.equals(payload.getReceiverRole(), Role.MEMBER)));
	}

	@Test
	@DisplayName("디렉터가 서비스 제안을 정상적으로 저장할 수 있다. (자기자신에게 제안을 보내는 경우)")
	void saveFailWhenSelfEstimate() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		// 본인이 작성한 요청
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, director);

		ServiceEstimateSaveRequestForDirector request = ServiceEstimateSaveRequestForDirector.builder()
			.serviceRequestId(serviceRequest.getId())
			.title(TITLE_STR)
			.price(10000L)
			.content(CONTENT_STR)
			.scheduledAt(DEFAULT_SCHEDULED_AT)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceEstimateException.SELF_ESTIMATE_NOT_ALLOWED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceEstimateException.SELF_ESTIMATE_NOT_ALLOWED.getHttpStatus().toString()))
			.andExpect(
				jsonPath(ERROR_MESSAGE).value(ServiceEstimateException.SELF_ESTIMATE_NOT_ALLOWED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateException.SELF_ESTIMATE_NOT_ALLOWED.getCode()));
	}

	@Test
	@DisplayName("디렉터가 서비스 제안을 정상적으로 저장할 수 있다. - (둘사이 진행중인 제안이 이미 존재하는 경우)")
	void saveFailWhenOngoingEstimateExists() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, member);

		// 이미 진행중인 제안 생성
		ServiceEstimate ongoingEstimate = serviceEstimateProvider.saveOngoing(directorInfo, serviceRequest,
			LocalDateTime.now().plusHours(1));

		ServiceEstimateSaveRequestForDirector request = ServiceEstimateSaveRequestForDirector.builder()
			.serviceRequestId(serviceRequest.getId())
			.title(TITLE_STR)
			.price(10000L)
			.content(CONTENT_STR)
			.scheduledAt(DEFAULT_SCHEDULED_AT)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceEstimateException.ALREADY_EXISTS_ONGOING_ESTIMATE_BY_DIRECTOR.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceEstimateException.SELF_ESTIMATE_NOT_ALLOWED.getHttpStatus().toString()))
			.andExpect(
				jsonPath(ERROR_MESSAGE).value(
					ServiceEstimateException.ALREADY_EXISTS_ONGOING_ESTIMATE_BY_DIRECTOR.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(
				ServiceEstimateException.ALREADY_EXISTS_ONGOING_ESTIMATE_BY_DIRECTOR.getCode()));
	}

	@Test
	@DisplayName("디렉터가 추가 제안을 저장할 수 있다. ( 진행중인 제안이 이미 존재함)")
	void saveAdditionalEstimateFailWhenOngoingEstimateExists() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		// 지역저장
		Location allCity = locationProvider.save("전국", LocationType.ALL_CITY);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);

		// 이미 진행중인 제안 생성 (25시간 전 생성된 것으로 설정)
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, member);
		serviceRequestProvider.setCreatedAtForTest(serviceRequest, LocalDateTime.now().minusHours(25));
		ServiceEstimate ongoingEstimate = serviceEstimateProvider.saveOngoing(directorInfo, serviceRequest,
			LocalDateTime.now().plusHours(1));

		// 채팅방 생성
		ChatRoom chatRoom = chatRoomProvider.save();

		ChatRoomMember directorChatRoomMember = chatRoomMemberProvider.saveDirector(chatRoom, director);
		ChatRoomMember memberChatRoomMember = chatRoomMemberProvider.saveMember(chatRoom, member);

		ChatRoomServiceEstimateMapping chatRoomServiceEstimateMapping = chatRoomServiceEstimateMappingProvider.save(
			chatRoom, ongoingEstimate);

		ServiceEstimateSaveAdditionalRequestForDirector request = ServiceEstimateSaveAdditionalRequestForDirector.builder()
			.serviceId(directorService2.getId())
			.title(TITLE_STR)
			.price(10000L)
			.content(CONTENT_STR)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/directors/chat-rooms/{chatRoomId}/service-estimates", chatRoom.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceEstimateException.ALREADY_EXISTS_ONGOING_ESTIMATE_BY_DIRECTOR.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceEstimateException.SELF_ESTIMATE_NOT_ALLOWED.getHttpStatus().toString()))
			.andExpect(
				jsonPath(ERROR_MESSAGE).value(
					ServiceEstimateException.ALREADY_EXISTS_ONGOING_ESTIMATE_BY_DIRECTOR.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(
				ServiceEstimateException.ALREADY_EXISTS_ONGOING_ESTIMATE_BY_DIRECTOR.getCode()));
	}

	@Test
	@DisplayName("디렉터가 서비스 제안을 정상적으로 저장할 수 있다. (서로간의 차단이 존재하는 경우)")
	void saveWhenBlockedOrBlockExist() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, member);

		// 차단 추가
		memberBlockProvider.save(member, director);

		ServiceEstimateSaveRequestForDirector request = ServiceEstimateSaveRequestForDirector.builder()
			.serviceRequestId(serviceRequest.getId())
			.title(TITLE_STR)
			.price(10000L)
			.content(CONTENT_STR)
			.scheduledAt(DEFAULT_SCHEDULED_AT)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceEstimateException.FAIL_TO_SAVE_BY_BLOCK.getHttpStatus().toString()))
			.andExpect(
				jsonPath(ERROR_MESSAGE).value(ServiceEstimateException.FAIL_TO_SAVE_BY_BLOCK.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateException.FAIL_TO_SAVE_BY_BLOCK.getCode()));
	}

	@Test
	@DisplayName("디렉터가 서비스 제안을 정상적으로 저장할 수 있다. (채팅방이 존재하지 않는 경우, 디렉터가 결제하기 전인경우)")
	void saveWithIsAddedForAutoInfoFalseAndChatRoomNotExistAndBeforeChatStartPaid() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, member);

		ServiceEstimateSaveRequestForDirector request = ServiceEstimateSaveRequestForDirector.builder()
			.serviceRequestId(serviceRequest.getId())
			.title(TITLE_STR)
			.price(10000L)
			.content(CONTENT_STR)
			.scheduledAt(DEFAULT_SCHEDULED_AT)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		entityManager.flush();
		entityManager.clear();

		//then
		List<ServiceEstimate> estimates = serviceEstimateProvider.findAll();

		assertThat(estimates).hasSize(1);

		ServiceEstimate savedEstimate = estimates.get(0);
		assertThat(savedEstimate.getDirectorInfo().getId()).isEqualTo(directorInfo.getId());
		assertThat(savedEstimate.getServiceRequest().getId()).isEqualTo(serviceRequest.getId());
		assertThat(savedEstimate.getTitle()).isEqualTo(TITLE_STR);
		assertThat(savedEstimate.getPrice()).isEqualTo(10000L);
		assertThat(savedEstimate.getContent()).isEqualTo(CONTENT_STR);
		assertThat(savedEstimate.getActiveUniqueKey()).isNotNull();

		// 채팅방 및 첫 제안 메세지가 저장 되었는지 확인 한다.
		List<ChatRoom> chatRooms = chatRoomProvider.findAll();

		assertThat(chatRooms).hasSize(1);
		ChatRoom chatRoom = chatRooms.get(0);

		// 채팅방 제안 매핑이 잘 저장되었는지 확인 한다.
		List<ChatRoomServiceEstimateMapping> mappings = chatRoomServiceEstimateMappingProvider.findAll();
		assertThat(mappings).hasSize(1);

		// 채팅방 회원이 저장 되었는지 확인 한다.
		List<ChatRoomMember> chatRoomMembers = chatRoomMemberProvider.findAll();

		assertThat(chatRoomMembers).hasSize(2);

		// 채팅방 회원중 director 가 isDirector 가 true 인지 확인한다.
		ChatRoomMember chatRoomMember = chatRoomMembers.stream()
			.filter(member1 -> member1.getMember().getId().equals(director.getId()))
			.findFirst()
			.orElseThrow();

		assertThat(chatRoomMember.getIsDirector()).isTrue();

		// 채팅 첫 제안 메세지가 저장 되었는지 확인한다.
		List<ChatMessage> chatMessages = chatMessageProvider.findAll();

		assertThat(chatMessages).hasSize(1);
		assertThat(chatMessages.get(0).getMessageType()).isEqualTo(ChatMessageType.ESTIMATE);
		assertThat(chatMessages.get(0).getServiceEstimate().getId()).isEqualTo(savedEstimate.getId());

		// 채팅방의 마지막 메세지가 업데이트 되었는지 확인한다.
		ChatRoom updatedChatRoom = chatRoomProvider.findById(chatRoom.getId());
		assertThat(updatedChatRoom.getLastMessage().getId()).isEqualTo(chatMessages.get(0).getId());

		// 회원의 마지막 메세지가 업데이트 되었는지 확인한다.
		List<ChatRoomMember> updatedChatRooms = chatRoomMemberProvider.findAll();

		ChatRoomMember updatedChatRoomSender = updatedChatRooms.stream()
			.filter(m -> m.getMember().getId().equals(director.getId()))
			.findFirst()
			.orElseThrow();

		assertThat(updatedChatRoomSender.getLastReadMessage().getId()).isEqualTo(chatMessages.get(0).getId());

		// 알림 존재 여부 확인
		List<Notification> notifications = notificationProvider.findAll();

		assertThat(notifications.get(0).getReceiverType()).isEqualTo(NotificationReceiverType.MEMBER);
		assertThat(notifications.get(0).getType()).isEqualTo(NotificationType.ESTIMATE_ARRIVED);

		// SSE 발행 검증 (REFRESH_CHAT_ROOM_LIST)
		verify(sseService, atLeastOnce()).refreshChatRoomList(
			argThat(payload ->
				payload.getEventName() == SseEventType.REFRESH_CHAT_ROOM_LIST &&
					Objects.equals(payload.getReceiverId(), member.getId()) &&
					Objects.equals(payload.getReceiverRole(), Role.MEMBER)));

		// SSE 발행 검증 (REFRESH_NAV_CHAT_COUNT)
		verify(sseService, atLeastOnce()).refreshNavChatCount(
			argThat(payload ->
				payload.getEventName() == SseEventType.REFRESH_NAV_CHAT_COUNT &&
					payload.getReceiverId().equals(member.getId()) &&
					Objects.equals(payload.getReceiverRole(), Role.MEMBER)));
	}

	@Test
	@DisplayName("디렉터가 서비스 제안을 정상적으로 저장할 수 있다. (자기 자신의 요청에 제안을 보내는 경우)")
	void saveSelfEstimate() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, director);

		ServiceEstimateSaveRequestForDirector request = ServiceEstimateSaveRequestForDirector.builder()
			.serviceRequestId(serviceRequest.getId())
			.title(TITLE_STR)
			.price(10000L)
			.content(CONTENT_STR)
			.scheduledAt(DEFAULT_SCHEDULED_AT)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceEstimateException.SELF_ESTIMATE_NOT_ALLOWED.getHttpStatus().toString()))
			.andExpect(
				jsonPath(ERROR_MESSAGE).value(ServiceEstimateException.SELF_ESTIMATE_NOT_ALLOWED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateException.SELF_ESTIMATE_NOT_ALLOWED.getCode()));
	}

	@Test
	@DisplayName("디렉터가 서비스 제안을 정상적으로 저장할 수 있다. (제목이 30자 이상인 경우)")
	void saveWithExceededTitleCount() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, requester);

		// ServiceEstimateTemplate 저장
		ServiceEstimateTemplate serviceEstimateTemplate = serviceEstimateTemplateProvider.save(directorInfo,
			directorService2);

		// serviceEstimateTemplate 용 이미지 저장
		ServiceEstimateFile serviceEstimateFileForTemplate1 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, serviceEstimateTemplate, 0);
		ServiceEstimateFile serviceEstimateFileForTemplate2 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, serviceEstimateTemplate, 1);
		ServiceEstimateFile serviceEstimateFileForTemplate3 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, serviceEstimateTemplate, 2);

		// 요청용 이미지 저장
		ServiceEstimateFile serviceEstimateImageForEstimate1 = serviceEstimateFileProvider.saveWithEstimateType(
			director);

		// 제목 생성
		StringBuilder longTitleBuilder = new StringBuilder();
		for (int i = 0; i < 31; i++) {
			longTitleBuilder.append("A");
		}

		ServiceEstimateSaveRequestForDirector request = ServiceEstimateSaveRequestForDirector.builder()
			.serviceRequestId(serviceRequest.getId())
			.title(longTitleBuilder.toString())
			.price(10000L)
			.content(CONTENT_STR)
			.fileIds(Arrays.asList(serviceEstimateImageForEstimate1.getId(),
				serviceEstimateFileForTemplate2.getId()))
			.scheduledAt(DEFAULT_SCHEDULED_AT)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(SERVICE_ESTIMATE_TITLE_LENGTH_MSG))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("디렉터가 서비스 제안을 정상적으로 저장할 수 있다. (가격이 100억 이상인 경우)")
	void saveWithExceededPrice() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, requester);

		// ServiceEstimateTemplate 저장
		ServiceEstimateTemplate serviceEstimateTemplate = serviceEstimateTemplateProvider.save(directorInfo,
			directorService2);

		// serviceEstimateTemplate 용 이미지 저장
		ServiceEstimateFile serviceEstimateFileForTemplate1 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, serviceEstimateTemplate, 0);
		ServiceEstimateFile serviceEstimateFileForTemplate2 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, serviceEstimateTemplate, 1);
		ServiceEstimateFile serviceEstimateFileForTemplate3 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, serviceEstimateTemplate, 2);

		// 요청용 이미지 저장
		ServiceEstimateFile serviceEstimateImageForEstimate1 = serviceEstimateFileProvider.saveWithEstimateType(
			director);

		// 가격 생성
		StringBuilder longTitleBuilder = new StringBuilder();
		for (int i = 0; i < 11; i++) {
			longTitleBuilder.append("1");
		}
		long exceededPrice = Long.parseLong(longTitleBuilder.toString());

		ServiceEstimateSaveRequestForDirector request = ServiceEstimateSaveRequestForDirector.builder()
			.serviceRequestId(serviceRequest.getId())
			.title(longTitleBuilder.toString())
			.price(exceededPrice)
			.content(CONTENT_STR)
			.fileIds(Arrays.asList(serviceEstimateImageForEstimate1.getId(),
				serviceEstimateFileForTemplate2.getId()))
			.scheduledAt(DEFAULT_SCHEDULED_AT)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(SERVICE_ESTIMATE_MAX_PRICE_MSG))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("디렉터가 서비스 제안을 정상적으로 저장할 수 있다. (자신이 올리지 않은 템플릿 이미지가 포함된 경우)")
	void saveWithIsAddedForAutoInfoTrueWhenNotOwnedImageExist() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, otherDirectorInfo);

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, requester);

		// ServiceEstimateTemplate 저장
		ServiceEstimateTemplate serviceEstimateTemplate = serviceEstimateTemplateProvider.save(directorInfo,
			directorService2);

		// serviceEstimateTemplate 용 이미지 저장
		ServiceEstimateFile serviceEstimateFileForTemplate1 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, serviceEstimateTemplate, 0);
		ServiceEstimateFile serviceEstimateFileForTemplate2 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, serviceEstimateTemplate, 1);
		ServiceEstimateFile serviceEstimateFileForTemplate3 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, serviceEstimateTemplate, 2);

		// 요청용 이미지 저장
		ServiceEstimateFile serviceEstimateImageForEstimate1 = serviceEstimateFileProvider.saveWithEstimateType(
			director);
		ServiceEstimateFile serviceEstimateImageForEstimate2 = serviceEstimateFileProvider.saveWithEstimateType(
			otherDirector);

		ServiceEstimateSaveRequestForDirector request = ServiceEstimateSaveRequestForDirector.builder()
			.serviceRequestId(serviceRequest.getId())
			.title(TITLE_STR)
			.price(10000L)
			.content(CONTENT_STR)
			.fileIds(Arrays.asList(serviceEstimateImageForEstimate2.getId(), serviceEstimateImageForEstimate1.getId(),
				serviceEstimateFileForTemplate2.getId()))
			.scheduledAt(DEFAULT_SCHEDULED_AT)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(ServiceEstimateFileException.NOT_OWNED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceEstimateFileException.NOT_OWNED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateFileException.NOT_OWNED.getCode()));
	}

	@Test
	@DisplayName("디렉터가 서비스 제안을 정상적으로 저장할 수 있다. (본인인증을 하지 않은 경우)")
	void saveWhenDirectorIsAuthenticatedFalse() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfoAndIsAuthenticatedFalse(SignInPlatform.APPLE,
			directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, otherDirectorInfo);

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, requester);

		// ServiceEstimateTemplate 저장
		ServiceEstimateTemplate serviceEstimateTemplate = serviceEstimateTemplateProvider.save(directorInfo,
			directorService2);

		// serviceEstimateTemplate 용 이미지 저장
		ServiceEstimateFile serviceEstimateFileForTemplate1 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, serviceEstimateTemplate, 0);
		ServiceEstimateFile serviceEstimateFileForTemplate2 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, serviceEstimateTemplate, 1);
		ServiceEstimateFile serviceEstimateFileForTemplate3 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, serviceEstimateTemplate, 2);

		// 요청용 이미지 저장
		ServiceEstimateFile serviceEstimateImageForEstimate1 = serviceEstimateFileProvider.saveWithEstimateType(
			director);
		ServiceEstimateFile serviceEstimateImageForEstimate2 = serviceEstimateFileProvider.saveWithEstimateType(
			director);

		ServiceEstimateSaveRequestForDirector request = ServiceEstimateSaveRequestForDirector.builder()
			.serviceRequestId(serviceRequest.getId())
			.title(TITLE_STR)
			.price(10000L)
			.content(CONTENT_STR)
			.fileIds(Arrays.asList(serviceEstimateImageForEstimate2.getId(), serviceEstimateImageForEstimate1.getId(),
				serviceEstimateFileForTemplate2.getId()))
			.scheduledAt(DEFAULT_SCHEDULED_AT)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(MemberException.NOT_AUTHENTICATED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(MemberException.NOT_AUTHENTICATED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(MemberException.NOT_AUTHENTICATED.getCode()));
	}

	@Test
	@DisplayName("디렉터가 서비스 제안을 정상적으로 저장할 수 있다. (존재하지 않은 이미지를 요청에 추가한 경우)")
	void saveWithIsAddedForAutoInfoTrueWhenNotExistingImageIncluded() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, requester);

		// ServiceEstimateTemplate 저장
		ServiceEstimateTemplate serviceEstimateTemplate = serviceEstimateTemplateProvider.save(directorInfo,
			directorService2);

		// serviceEstimateTemplate 용 이미지 저장
		ServiceEstimateFile serviceEstimateFileForTemplate1 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, serviceEstimateTemplate, 0);

		ServiceEstimateFile serviceEstimateFileForTemplate2 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, serviceEstimateTemplate, 1);

		ServiceEstimateFile serviceEstimateFileForTemplate3 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, serviceEstimateTemplate, 2);

		// 요청용 이미지 저장
		ServiceEstimateFile serviceEstimateFileForEstimate1 = serviceEstimateFileProvider.saveWithEstimateType(
			director);
		ServiceEstimateFile serviceEstimateFileForEstimate2 = serviceEstimateFileProvider.saveWithEstimateType(
			director);

		ServiceEstimateSaveRequestForDirector request = ServiceEstimateSaveRequestForDirector.builder()
			.serviceRequestId(serviceRequest.getId())
			.title(TITLE_STR)
			.price(10000L)
			.content(CONTENT_STR)
			.fileIds(Arrays.asList(serviceEstimateFileForEstimate2.getId(), serviceEstimateFileForEstimate1.getId(),
				serviceEstimateFileForTemplate2.getId(), 999999L))
			.scheduledAt(DEFAULT_SCHEDULED_AT)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceEstimateFileException.INVALID_IMAGE_COUNT.getHttpStatus().toString()))
			.andExpect(
				jsonPath(ERROR_MESSAGE).value(ServiceEstimateFileException.INVALID_IMAGE_COUNT.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateFileException.INVALID_IMAGE_COUNT.getCode()));
	}

	@Test
	@DisplayName("디렉터가 서비스 제안을 정상적으로 저장할 수 있다. (isAddedForAutoInfo: false, 채팅방이 이미 존재하는 경우)")
	void saveWithIsAddedForAutoInfoFalseAndChatRoomExist() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, member);

		// chat room 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		// chat room member 저장
		ChatRoomMember chatRoomDirector = chatRoomMemberProvider.saveDirector(chatRoom, director);
		ChatRoomMember chatRoomMember = chatRoomMemberProvider.saveMember(chatRoom, member);

		ServiceEstimateSaveRequestForDirector request = ServiceEstimateSaveRequestForDirector.builder()
			.serviceRequestId(serviceRequest.getId())
			.title(TITLE_STR)
			.price(10000L)
			.content(CONTENT_STR)
			.scheduledAt(DEFAULT_SCHEDULED_AT)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		entityManager.flush();
		entityManager.clear();

		//then
		List<ServiceEstimate> estimates = serviceEstimateProvider.findAll();

		assertThat(estimates).hasSize(1);

		ServiceEstimate savedEstimate = estimates.get(0);
		assertThat(savedEstimate.getDirectorInfo().getId()).isEqualTo(directorInfo.getId());
		assertThat(savedEstimate.getServiceRequest().getId()).isEqualTo(serviceRequest.getId());
		assertThat(savedEstimate.getTitle()).isEqualTo(TITLE_STR);
		assertThat(savedEstimate.getPrice()).isEqualTo(10000L);
		assertThat(savedEstimate.getContent()).isEqualTo(CONTENT_STR);
		assertThat(savedEstimate.getActiveUniqueKey()).isNotNull();

		// 새로운 채팅방이 저장되지 않았는지 확인한다.
		List<ChatRoom> chatRooms = chatRoomProvider.findAll();

		assertThat(chatRooms).hasSize(1);

		// 채팅방 회원이 그대로 2명인지 확인한다.
		List<ChatRoomMember> chatRoomMembers = chatRoomMemberProvider.findAll();

		assertThat(chatRoomMembers).hasSize(2);

		// 채팅방 제안 매핑이 잘 저장되었는지 확인 한다.
		List<ChatRoomServiceEstimateMapping> mappings = chatRoomServiceEstimateMappingProvider.findAll();
		assertThat(mappings).hasSize(1);

		// 채팅 첫 제안 메세지가 저장 되었는지 확인한다.
		List<ChatMessage> chatMessages = chatMessageProvider.findAll();

		assertThat(chatMessages).hasSize(1);
		assertThat(chatMessages.get(0).getMessageType()).isEqualTo(ChatMessageType.ESTIMATE);
	}

	@Test
	@DisplayName("디렉터가 서비스 제안을 정상적으로 저장할 수 있다. (isAddedForAutoInfo: false, 채팅방이 존재하지만 둘다 나간 경우)")
	void saveWithIsAddedForAutoInfoFalseAndChatRoomExistAndChatRoomMembersIsChatRoomDeletedTrue() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, member);

		// chat room 저장
		ChatRoom chatRoom = chatRoomProvider.saveWithIsDeletedTrue();

		// chat room member 저장
		ChatRoomMember chatRoomDirector = chatRoomMemberProvider.saveDirectorWithRoomDeletedTrue(chatRoom, director);
		ChatRoomMember chatRoomMember = chatRoomMemberProvider.saveDirectorWithRoomDeletedTrue(chatRoom, member);

		ServiceEstimateSaveRequestForDirector request = ServiceEstimateSaveRequestForDirector.builder()
			.serviceRequestId(serviceRequest.getId())
			.title(TITLE_STR)
			.price(10000L)
			.content(CONTENT_STR)
			.scheduledAt(DEFAULT_SCHEDULED_AT)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		entityManager.flush();
		entityManager.clear();

		//then
		List<ServiceEstimate> estimates = serviceEstimateProvider.findAll();

		assertThat(estimates).hasSize(1);

		ServiceEstimate savedEstimate = estimates.get(0);
		assertThat(savedEstimate.getDirectorInfo().getId()).isEqualTo(directorInfo.getId());
		assertThat(savedEstimate.getServiceRequest().getId()).isEqualTo(serviceRequest.getId());
		assertThat(savedEstimate.getTitle()).isEqualTo(TITLE_STR);
		assertThat(savedEstimate.getPrice()).isEqualTo(10000L);
		assertThat(savedEstimate.getContent()).isEqualTo(CONTENT_STR);
		assertThat(savedEstimate.getActiveUniqueKey()).isNotNull();

		// 새로운 채팅방이 저장되지 않았는지 확인한다.
		List<ChatRoom> chatRooms = chatRoomProvider.findAll();

		assertThat(chatRooms).hasSize(2);

		// 채팅방 회원이 그대로 새로운 채팅방으로 인해 4명인지 확인한다.
		List<ChatRoomMember> chatRoomMembers = chatRoomMemberProvider.findAll();

		assertThat(chatRoomMembers).hasSize(4);

		chatRoomMembers.stream()
			.filter(crm -> !crm.getChatRoom().getIsDeleted())
			.forEach(updatedChatRoomMember -> {
				assertThat(updatedChatRoomMember.getIsChatRoomDeleted()).isFalse();
			});

		// 채팅방 제안 매핑이 잘 저장되었는지 확인 한다.
		List<ChatRoomServiceEstimateMapping> mappings = chatRoomServiceEstimateMappingProvider.findAll();
		assertThat(mappings).hasSize(1);

		// 채팅 첫 제안 메세지가 저장 되었는지 확인한다.
		List<ChatMessage> chatMessages = chatMessageProvider.findAll();

		assertThat(chatMessages).hasSize(1);
		assertThat(chatMessages.get(0).getMessageType()).isEqualTo(ChatMessageType.ESTIMATE);
	}

	@Test
	@DisplayName("디렉터가 서비스 제안을 정상적으로 저장할 수 있다. (isAddedForAutoInfo: true)")
	void saveWithIsAddedForAutoInfoTrue() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, member);

		ServiceEstimateSaveRequestForDirector request = ServiceEstimateSaveRequestForDirector.builder()
			.serviceRequestId(serviceRequest.getId())
			.title(TITLE_STR)
			.price(10000L)
			.content(CONTENT_STR)
			.scheduledAt(DEFAULT_SCHEDULED_AT)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		entityManager.flush();
		entityManager.clear();

		//then
		List<ServiceEstimate> estimates = serviceEstimateProvider.findAll();

		assertThat(estimates).hasSize(1);

		ServiceEstimate savedEstimate = estimates.get(0);
		assertThat(savedEstimate.getDirectorInfo().getId()).isEqualTo(directorInfo.getId());
		assertThat(savedEstimate.getServiceRequest().getId()).isEqualTo(serviceRequest.getId());
		assertThat(savedEstimate.getTitle()).isEqualTo(TITLE_STR);
		assertThat(savedEstimate.getPrice()).isEqualTo(10000L);
		assertThat(savedEstimate.getContent()).isEqualTo(CONTENT_STR);
		assertThat(savedEstimate.getActiveUniqueKey()).isNotNull();
	}

	@Test
	@DisplayName("디렉터가 서비스 제안을 정상적으로 저장할 수 있다. (요청가 현재 제안 받는 중이 아닐 경우)")
	void saveWithIsEstimateReceivingFalse() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.savePendingAndIsReceivingEstimateFalse(directorService2,
			member);

		ServiceEstimateSaveRequestForDirector request = ServiceEstimateSaveRequestForDirector.builder()
			.serviceRequestId(serviceRequest.getId())
			.title(TITLE_STR)
			.price(10000L)
			.content(CONTENT_STR)
			.scheduledAt(DEFAULT_SCHEDULED_AT)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(
				jsonPath(ERROR_STATUS).value(ServiceRequestException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceRequestException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceRequestException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("디렉터가 서비스 제안을 정상적으로 저장할 수 있다. (요청가 진행중인 상태일떄 예외가 발생한다.)")
	void saveWithOngoingRequest() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService2, member,
			LocalDateTime.now().minusDays(1));

		ServiceEstimateSaveRequestForDirector request = ServiceEstimateSaveRequestForDirector.builder()
			.serviceRequestId(serviceRequest.getId())
			.title(TITLE_STR)
			.price(10000L)
			.content(CONTENT_STR)
			.scheduledAt(DEFAULT_SCHEDULED_AT)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(
				jsonPath(ERROR_STATUS).value(ServiceRequestException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceRequestException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceRequestException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("디렉터가 서비스 제안을 정상적으로 저장할 수 있다. (존재하지 않는 요청에 대한 제안 생성시)")
	void save_notFoundServiceRequest() throws Exception {
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		ServiceEstimateSaveRequestForDirector request = ServiceEstimateSaveRequestForDirector.builder()
			.serviceRequestId(99999999L)
			.title(TITLE_STR)
			.price(10000L)
			.content(CONTENT_STR)
			.scheduledAt(DEFAULT_SCHEDULED_AT)
			.build();

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(
				jsonPath(ERROR_STATUS).value(ServiceRequestException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceRequestException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceRequestException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("디렉터가 서비스 제안을 정상적으로 저장할 수 있다. (디렉터가 제공하는 서비스가 아닐 경우)")
	void save_notProvidedServiceByDirector() throws Exception {
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, member);

		ServiceEstimateSaveRequestForDirector request = ServiceEstimateSaveRequestForDirector.builder()
			.serviceRequestId(serviceRequest.getId())
			.title(TITLE_STR)
			.price(10000L)
			.content(CONTENT_STR)
			.scheduledAt(DEFAULT_SCHEDULED_AT)
			.build();

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(
					DirectorServiceException.DIRECTOR_SERVICE_NOT_FOUND.getHttpStatus().toString()))
			.andExpect(
				jsonPath(ERROR_MESSAGE).value(DirectorServiceException.DIRECTOR_SERVICE_NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(DirectorServiceException.DIRECTOR_SERVICE_NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("디렉터가 서비스 제안을 정상적으로 저장할 수 있다. (끝나지 않은 제안이 있으며, 이때 제안 을 보내려고 할때.)")
	void save_duplicateEstimate() throws Exception {
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, member);

		ServiceEstimateSaveRequestForDirector request = ServiceEstimateSaveRequestForDirector.builder()
			.serviceRequestId(serviceRequest.getId())
			.title(TITLE_STR)
			.price(10000L)
			.content(CONTENT_STR)
			.scheduledAt(DEFAULT_SCHEDULED_AT)
			.build();

		// 이미 제안 저장
		serviceEstimateProvider.save(director.getDirectorInfo(), serviceRequest);

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceEstimateException.ALREADY_EXISTS_ONGOING_ESTIMATE_BY_DIRECTOR.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(
				ServiceEstimateException.ALREADY_EXISTS_ONGOING_ESTIMATE_BY_DIRECTOR.getErrorMessage()))
			.andExpect(
				jsonPath(ERROR_CODE).value(
					ServiceEstimateException.ALREADY_EXISTS_ONGOING_ESTIMATE_BY_DIRECTOR.getCode()));
	}

	@Test
	@DisplayName("디렉터가 서비스 제안을 정상적으로 저장할 수 있다. (끝난 제안이 있으며, 이때 제안 을 보내려고 할때.)")
	void save_duplicateEstimateWhenEndedEstimateExist() throws Exception {
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService2, member);

		ServiceEstimateSaveRequestForDirector request = ServiceEstimateSaveRequestForDirector.builder()
			.serviceRequestId(serviceRequest.getId())
			.title(TITLE_STR)
			.price(10000L)
			.content(CONTENT_STR)
			.scheduledAt(DEFAULT_SCHEDULED_AT)
			.build();

		// 이미 제안 저장
		serviceEstimateProvider.saveCanceled(director.getDirectorInfo(), serviceRequest,
			LocalDateTime.now().plusDays(1));

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceEstimateException.FAIL_TO_SAVE_WHEN_ALREADY_SEND_ESTIMATE.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(
				ServiceEstimateException.FAIL_TO_SAVE_WHEN_ALREADY_SEND_ESTIMATE.getErrorMessage()))
			.andExpect(
				jsonPath(ERROR_CODE).value(ServiceEstimateException.FAIL_TO_SAVE_WHEN_ALREADY_SEND_ESTIMATE.getCode()));
	}

	@Test
	@DisplayName("디렉터가 자신의 제안을 조회 할 수 있다. (pending 상태, directorServiceId 가 없을때)")
	void findAllStatusPendingAndWithoutDirectorServiceId() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, otherDirectorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService otherDirectorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);
		directorServiceMappingProvider.save(otherDirectorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);

		// 해당 디렉터가 ongoing 중인 request
		ServiceRequest ongoingServiceRequest1 = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now().minusDays(1));
		ServiceRequest ongoingServiceRequest2 = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now().minusDays(1));

		// 해당 디렉터가 completed 된 request
		ServiceRequest completedServiceRequest1 = serviceRequestProvider.saveWithIsCompletedTrue(directorService,
			requester, LocalDateTime.now().minusDays(1));

		// 해당 디렉터가 canceled 된 request
		ServiceRequest canceledServiceRequest = serviceRequestProvider.saveWithIsCanceledTrue(directorService,
			requester, LocalDateTime.now().minusDays(1));

		// expired 된 request
		ServiceRequest expiredServiceRequest = serviceRequestProvider.saveWithIsExpiredTrue(directorService,
			requester, LocalDateTime.now().minusDays(1));

		// pending 상태인 request
		ServiceRequest sr4 = serviceRequestProvider.savePending(directorService, requester);
		ServiceRequest sr5 = serviceRequestProvider.savePending(directorService, requester);
		ServiceRequest sr6 = serviceRequestProvider.savePending(directorService, requester);

		// 다른 서비스의 pending request
		ServiceRequest sr7 = serviceRequestProvider.savePending(otherDirectorService, requester);

		// 다른 디렉터에게 direct
		ServiceRequest sr8 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(directorService, requester,
			otherDirector);

		// 해당 디렉터에게 온 direct 요청
		ServiceRequest sr9 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(directorService, requester,
			director);
		ServiceRequest sr10 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(directorService, requester,
			director);

		// 제안 제공중인 pending 상태의 serviceEstimate (신규요청엔 뜨면 안됨)
		ServiceEstimate pendingEstimate = serviceEstimateProvider.save(director.getDirectorInfo(), sr4);

		// 제안 진행중인 estimate
		serviceEstimateProvider.saveOngoing(director.getDirectorInfo(), ongoingServiceRequest1,
			LocalDateTime.now().plusDays(1));
		serviceEstimateProvider.saveOngoing(director.getDirectorInfo(), ongoingServiceRequest2,
			LocalDateTime.now().plusDays(1));

		// 제안 완료된 estimate
		serviceEstimateProvider.saveDirectorDone(director.getDirectorInfo(), completedServiceRequest1,
			LocalDateTime.now().plusDays(2));

		// 제안 취소된 estimate
		serviceEstimateProvider.saveCanceled(director.getDirectorInfo(), canceledServiceRequest,
			LocalDateTime.now().plusDays(3)); // CANCELED

		// expired 된 estimate
		serviceEstimateProvider.saveExpired(director.getDirectorInfo(), expiredServiceRequest,
			LocalDateTime.now().plusDays(4));

		// chatRoom 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		// chatRoomServiceEstimateMapping 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom, pendingEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/service-estimates")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(STATUS_STR, ServiceEstimateStatus.PENDING.name())
					.param(PAGE_STR, ZERO_STR)
			)
			.andExpect(status().isOk())
			.andReturn();

		ServiceEstimateFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			ServiceEstimateFindAllResponseForDirector.class);

		// then pending 만 반환
		List<ServiceEstimateResponseForDirector> estimates = response.getServiceEstimates();

		assertThat(estimates).hasSize(1);

		List<Long> ids = estimates.stream().map(ServiceEstimateResponseForDirector::getId).toList();
		assertThat(ids).contains(pendingEstimate.getId());
	}

	@Test
	@DisplayName("디렉터가 자신의 제안을 조회 할 수 있다. (pending 상태)")
	void findAll_statusPending() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, otherDirectorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService otherDirectorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);
		directorServiceMappingProvider.save(otherDirectorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);

		// 해당 디렉터가 ongoing 중인 request
		ServiceRequest ongoingServiceRequest1 = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now().minusDays(1));
		ServiceRequest ongoingServiceRequest2 = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now().minusDays(1));

		// 해당 디렉터가 completed 된 request
		ServiceRequest completedServiceRequest1 = serviceRequestProvider.saveWithIsCompletedTrue(directorService,
			requester, LocalDateTime.now().minusDays(1));

		// 해당 디렉터가 canceled 된 request
		ServiceRequest canceledServiceRequest = serviceRequestProvider.saveWithIsCanceledTrue(directorService,
			requester, LocalDateTime.now().minusDays(1));

		// expired 된 request
		ServiceRequest expiredServiceRequest = serviceRequestProvider.saveWithIsExpiredTrue(directorService,
			requester, LocalDateTime.now().minusDays(1));

		// pending 상태인 request
		ServiceRequest sr4 = serviceRequestProvider.savePending(directorService, requester);
		ServiceRequest sr5 = serviceRequestProvider.savePending(directorService, requester);
		ServiceRequest sr6 = serviceRequestProvider.savePending(directorService, requester);

		// 다른 서비스의 pending request
		ServiceRequest sr7 = serviceRequestProvider.savePending(otherDirectorService, requester);

		// 다른 디렉터에게 direct
		ServiceRequest sr8 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(directorService, requester,
			otherDirector);

		// 해당 디렉터에게 온 direct 요청
		ServiceRequest sr9 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(directorService, requester,
			director);
		ServiceRequest sr10 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(directorService, requester,
			director);

		// 제안 제공중인 pending 상태의 serviceEstimate (신규요청엔 뜨면 안됨)
		ServiceEstimate pendingEstimate = serviceEstimateProvider.save(director.getDirectorInfo(), sr4);

		// 제안 진행중인 estimate
		serviceEstimateProvider.saveOngoing(director.getDirectorInfo(), ongoingServiceRequest1,
			LocalDateTime.now().plusDays(1));
		serviceEstimateProvider.saveOngoing(director.getDirectorInfo(), ongoingServiceRequest2,
			LocalDateTime.now().plusDays(1));

		// 제안 완료된 estimate
		serviceEstimateProvider.saveDirectorDone(director.getDirectorInfo(), completedServiceRequest1,
			LocalDateTime.now().plusDays(2));

		// 제안 취소된 estimate
		serviceEstimateProvider.saveCanceled(director.getDirectorInfo(), canceledServiceRequest,
			LocalDateTime.now().plusDays(3)); // CANCELED

		// expired 된 estimate
		serviceEstimateProvider.saveExpired(director.getDirectorInfo(), expiredServiceRequest,
			LocalDateTime.now().plusDays(4));

		// chatRoom 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		// chatRoomServiceEstimateMapping 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom, pendingEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/service-estimates")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(STATUS_STR, ServiceEstimateStatus.PENDING.name())
					.param(PAGE_STR, ZERO_STR)
					.param(DIRECTOR_SERVICE_ID_STR, String.valueOf(directorService.getId()))
			)
			.andExpect(status().isOk())
			.andReturn();

		ServiceEstimateFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			ServiceEstimateFindAllResponseForDirector.class);

		// then pending 만 반환
		List<ServiceEstimateResponseForDirector> estimates = response.getServiceEstimates();

		assertThat(estimates).hasSize(1);

		List<Long> ids = estimates.stream().map(ServiceEstimateResponseForDirector::getId).toList();
		assertThat(ids).contains(pendingEstimate.getId());
	}

	@Test
	@DisplayName("디렉터가 자신의 제안을 조회 할 수 있다. (pending 상태, 다이렉트 요청만)")
	void findAll_statusPending_showOnlyDirectRequest() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, otherDirectorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);
		directorServiceMappingProvider.save(otherDirectorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);

		// 일반 pending request (다이렉트 아님)
		ServiceRequest normalPendingRequest = serviceRequestProvider.savePending(directorService, requester);

		// 다른 디렉터에게 direct 요청
		ServiceRequest directRequestToOther = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(
			directorService, requester, otherDirector);

		// 해당 디렉터에게 온 direct 요청
		ServiceRequest directRequest1 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(
			directorService, requester, director);
		ServiceRequest directRequest2 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(
			directorService, requester, director);

		// 일반 pending 제안 (다이렉트 아님)
		ServiceEstimate normalPendingEstimate = serviceEstimateProvider.save(director.getDirectorInfo(),
			normalPendingRequest);

		// 다이렉트 요청에 대한 pending 제안
		ServiceEstimate directPendingEstimate1 = serviceEstimateProvider.save(director.getDirectorInfo(),
			directRequest1);
		ServiceEstimate directPendingEstimate2 = serviceEstimateProvider.save(director.getDirectorInfo(),
			directRequest2);

		// chatRoom 저장
		ChatRoom chatRoom1 = chatRoomProvider.save();
		ChatRoom chatRoom2 = chatRoomProvider.save();
		ChatRoom chatRoom3 = chatRoomProvider.save();

		// chatRoomServiceEstimateMapping 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom1, normalPendingEstimate);
		chatRoomServiceEstimateMappingProvider.save(chatRoom2, directPendingEstimate1);
		chatRoomServiceEstimateMappingProvider.save(chatRoom3, directPendingEstimate2);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/service-estimates")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(STATUS_STR, ServiceEstimateStatus.PENDING.name())
					.param(PAGE_STR, ZERO_STR)
					.param(DIRECTOR_SERVICE_ID_STR, String.valueOf(directorService.getId()))
					.param("showOnlyDirectRequest", "true")
			)
			.andExpect(status().isOk())
			.andReturn();

		ServiceEstimateFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			ServiceEstimateFindAllResponseForDirector.class);

		// then 다이렉트 요청만 반환
		List<ServiceEstimateResponseForDirector> estimates = response.getServiceEstimates();

		assertThat(estimates).hasSize(2);

		List<Long> ids = estimates.stream().map(ServiceEstimateResponseForDirector::getId).toList();
		assertThat(ids).containsExactlyInAnyOrder(directPendingEstimate1.getId(), directPendingEstimate2.getId());
		assertThat(ids).doesNotContain(normalPendingEstimate.getId());

		// 모든 결과가 다이렉트 요청인지 확인
		assertThat(estimates).allMatch(ServiceEstimateResponseForDirector::getIsDirectRequest);
	}

	@Test
	@DisplayName("디렉터가 자신의 제안을 조회 할 수 있다. (ongoing 상태)")
	void findAll_statusOngoing() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, otherDirectorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService otherDirectorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);
		directorServiceMappingProvider.save(otherDirectorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);

		// 해당 디렉터가 ongoing 중인 request
		ServiceRequest ongoingServiceRequest1 = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now().minusDays(1));
		ServiceRequest ongoingServiceRequest2 = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now().minusDays(1));
		ServiceRequest ongoingServiceRequest3 = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now().minusDays(1));

		// 해당 디렉터가 completed 된 request
		ServiceRequest completedServiceRequest1 = serviceRequestProvider.saveWithIsCompletedTrue(directorService,
			requester, LocalDateTime.now().minusDays(1));

		// 해당 디렉터가 canceled 된 request
		ServiceRequest canceledServiceRequest = serviceRequestProvider.saveWithIsCanceledTrue(directorService,
			requester, LocalDateTime.now().minusDays(1));

		// expired 된 request
		ServiceRequest expiredServiceRequest = serviceRequestProvider.saveWithIsExpiredTrue(directorService,
			requester, LocalDateTime.now().minusDays(1));

		// pending 상태인 request
		ServiceRequest sr4 = serviceRequestProvider.savePending(directorService, requester);
		ServiceRequest sr5 = serviceRequestProvider.savePending(directorService, requester);
		ServiceRequest sr6 = serviceRequestProvider.savePending(directorService, requester);

		// 다른 서비스의 pending request
		ServiceRequest sr7 = serviceRequestProvider.savePending(otherDirectorService, requester);

		// 다른 디렉터에게 direct
		ServiceRequest sr8 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(directorService, requester,
			otherDirector);

		// 해당 디렉터에게 온 direct 요청
		ServiceRequest sr9 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(directorService, requester,
			director);
		ServiceRequest sr10 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(directorService, requester,
			director);

		// 제안 제공중인 pending 상태의 serviceEstimate (신규요청엔 뜨면 안됨)
		ServiceEstimate pendingEstimate = serviceEstimateProvider.save(director.getDirectorInfo(), sr4);

		// 제안 진행중인 estimate
		ServiceEstimate ongoingEstimate1 = serviceEstimateProvider.saveOngoing(director.getDirectorInfo(),
			ongoingServiceRequest1,
			LocalDateTime.now().plusDays(1));
		ServiceEstimate ongoingEstimate2 = serviceEstimateProvider.saveOngoing(director.getDirectorInfo(),
			ongoingServiceRequest2,
			LocalDateTime.now().plusDays(1));

		// 제안 디렉터에 의해서 완료된 estimate
		ServiceEstimate directorDoneEstimate = serviceEstimateProvider.saveDirectorDone(director.getDirectorInfo(),
			ongoingServiceRequest3,
			LocalDateTime.now().plusDays(2));

		// 유저에 의해 완료된 estimate
		serviceEstimateProvider.saveMemberCompleted(director.getDirectorInfo(), completedServiceRequest1,
			LocalDateTime.now().plusDays(2));

		// 제안 취소된 estimate
		serviceEstimateProvider.saveCanceled(director.getDirectorInfo(), canceledServiceRequest,
			LocalDateTime.now().plusDays(3)); // CANCELED

		// expired 된 estimate
		serviceEstimateProvider.saveExpired(director.getDirectorInfo(), expiredServiceRequest,
			LocalDateTime.now().plusDays(4));

		// chatRoom 저장
		ChatRoom chatRoom1 = chatRoomProvider.save();
		ChatRoom chatRoom2 = chatRoomProvider.save();
		ChatRoom chatRoom3 = chatRoomProvider.save();

		// chatRoomServiceEstimateMapping 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom1, ongoingEstimate1);
		chatRoomServiceEstimateMappingProvider.save(chatRoom2, ongoingEstimate2);
		chatRoomServiceEstimateMappingProvider.save(chatRoom3, directorDoneEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/service-estimates")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(STATUS_STR, ServiceEstimateStatus.ONGOING.name())
					.param(PAGE_STR, ZERO_STR)
					.param(DIRECTOR_SERVICE_ID_STR, String.valueOf(directorService.getId()))
			)
			.andExpect(status().isOk())
			.andReturn();

		ServiceEstimateFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			ServiceEstimateFindAllResponseForDirector.class);

		// then: pending, ongoing 만 반환
		List<ServiceEstimateResponseForDirector> estimates = response.getServiceEstimates();

		assertThat(estimates).hasSize(3);

		List<Long> ids = estimates.stream().map(ServiceEstimateResponseForDirector::getId).toList();
		assertThat(ids).contains(ongoingEstimate1.getId(), ongoingEstimate2.getId(), directorDoneEstimate.getId());
	}

	@Test
	@DisplayName("디렉터가 자신의 제안을 조회 할 수 있다. (completed 상태)")
	void findAll_statusCompleted() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, otherDirectorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService otherDirectorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);
		directorServiceMappingProvider.save(otherDirectorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);

		// 해당 디렉터가 ongoing 중인 request
		ServiceRequest ongoingServiceRequest1 = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now().minusDays(1));
		ServiceRequest ongoingServiceRequest2 = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now().minusDays(1));

		// 해당 디렉터가 completed 된 request
		ServiceRequest completedServiceRequest1 = serviceRequestProvider.saveWithIsCompletedTrue(directorService,
			requester, LocalDateTime.now().minusDays(1));

		ServiceRequest completedServiceRequest2 = serviceRequestProvider.saveWithIsCompletedTrue(directorService,
			requester, LocalDateTime.now().minusDays(1));

		ServiceRequest completedServiceRequest3 = serviceRequestProvider.saveWithIsCompletedTrue(directorService,
			requester, LocalDateTime.now().minusDays(1));

		// 해당 디렉터가 canceled 된 request
		ServiceRequest canceledServiceRequest = serviceRequestProvider.saveWithIsCanceledTrue(directorService,
			requester, LocalDateTime.now().minusDays(1));

		// expired 된 request
		ServiceRequest expiredServiceRequest = serviceRequestProvider.saveWithIsExpiredTrue(directorService,
			requester, LocalDateTime.now().minusDays(1));

		// pending 상태인 request
		ServiceRequest sr4 = serviceRequestProvider.savePending(directorService, requester);
		ServiceRequest sr5 = serviceRequestProvider.savePending(directorService, requester);
		ServiceRequest sr6 = serviceRequestProvider.savePending(directorService, requester);

		// 다른 서비스의 pending request
		ServiceRequest sr7 = serviceRequestProvider.savePending(otherDirectorService, requester);

		// 다른 디렉터에게 direct
		ServiceRequest sr8 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(directorService, requester,
			otherDirector);

		// 해당 디렉터에게 온 direct 요청
		ServiceRequest sr9 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(directorService, requester,
			director);
		ServiceRequest sr10 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(directorService, requester,
			director);

		// 제안 제공중인 pending 상태의 serviceEstimate (신규요청엔 뜨면 안됨)
		ServiceEstimate pendingEstimate = serviceEstimateProvider.save(director.getDirectorInfo(), sr4);

		// 제안 진행중인 estimate
		ServiceEstimate ongoingEstimate1 = serviceEstimateProvider.saveOngoing(director.getDirectorInfo(),
			ongoingServiceRequest1,
			LocalDateTime.now().plusDays(1));
		ServiceEstimate ongoingEstimate2 = serviceEstimateProvider.saveOngoing(director.getDirectorInfo(),
			ongoingServiceRequest2,
			LocalDateTime.now().plusDays(1));
		ServiceEstimate directorCompletedEstimate = serviceEstimateProvider.saveDirectorDone(
			director.getDirectorInfo(),
			completedServiceRequest1,
			LocalDateTime.now().plusDays(2));

		// 제안 완료된 estimate
		ServiceEstimate memberCompletedEstimate = serviceEstimateProvider.saveMemberCompleted(
			director.getDirectorInfo(),
			completedServiceRequest2,
			LocalDateTime.now().plusDays(2));

		ServiceEstimate reviewCompletedEstimate = serviceEstimateProvider.saveReviewCompleted(
			director.getDirectorInfo(),
			completedServiceRequest3,
			LocalDateTime.now().plusDays(2));

		// 제안 취소된 estimate
		serviceEstimateProvider.saveCanceled(director.getDirectorInfo(), canceledServiceRequest,
			LocalDateTime.now().plusDays(3)); // CANCELED

		// expired 된 estimate
		serviceEstimateProvider.saveExpired(director.getDirectorInfo(), expiredServiceRequest,
			LocalDateTime.now().plusDays(4));

		// chatRoom 저장
		ChatRoom chatRoom1 = chatRoomProvider.save();
		ChatRoom chatRoom2 = chatRoomProvider.save();

		// chatRoomServiceEstimateMapping 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom1, memberCompletedEstimate);
		chatRoomServiceEstimateMappingProvider.save(chatRoom2, reviewCompletedEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/service-estimates")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(STATUS_STR, ServiceEstimateStatus.COMPLETED_BY_MEMBER.name())
					.param(PAGE_STR, ZERO_STR)
					.param(DIRECTOR_SERVICE_ID_STR, String.valueOf(directorService.getId()))
			)
			.andExpect(status().isOk())
			.andReturn();

		ServiceEstimateFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			ServiceEstimateFindAllResponseForDirector.class);

		// then: completed 만 반환
		List<ServiceEstimateResponseForDirector> estimates = response.getServiceEstimates();

		assertThat(estimates).hasSize(2);

		List<Long> ids = estimates.stream().map(ServiceEstimateResponseForDirector::getId).toList();
		assertThat(ids).contains(memberCompletedEstimate.getId(), reviewCompletedEstimate.getId());
	}

	@Test
	@DisplayName("디렉터가 자신의 제안을 조회 할 수 있다. (completed 상태, 디렉터가 사용자를 차단한 경우)")
	void findAll_statusCompletedAndDirectorBlockedMember() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, otherDirectorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService otherDirectorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);
		directorServiceMappingProvider.save(otherDirectorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);

		// 해당 디렉터가 ongoing 중인 request
		ServiceRequest ongoingServiceRequest1 = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now().minusDays(1));
		ServiceRequest ongoingServiceRequest2 = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now().minusDays(1));

		// 해당 디렉터가 completed 된 request
		ServiceRequest completedServiceRequest1 = serviceRequestProvider.saveWithIsCompletedTrue(directorService,
			requester, LocalDateTime.now().minusDays(1));

		ServiceRequest completedServiceRequest2 = serviceRequestProvider.saveWithIsCompletedTrue(directorService,
			requester, LocalDateTime.now().minusDays(1));

		ServiceRequest completedServiceRequest3 = serviceRequestProvider.saveWithIsCompletedTrue(directorService,
			requester, LocalDateTime.now().minusDays(1));

		// 해당 디렉터가 canceled 된 request
		ServiceRequest canceledServiceRequest = serviceRequestProvider.saveWithIsCanceledTrue(directorService,
			requester, LocalDateTime.now().minusDays(1));

		// expired 된 request
		ServiceRequest expiredServiceRequest = serviceRequestProvider.saveWithIsExpiredTrue(directorService,
			requester, LocalDateTime.now().minusDays(1));

		// pending 상태인 request
		ServiceRequest sr4 = serviceRequestProvider.savePending(directorService, requester);
		ServiceRequest sr5 = serviceRequestProvider.savePending(directorService, requester);
		ServiceRequest sr6 = serviceRequestProvider.savePending(directorService, requester);

		// 다른 서비스의 pending request
		ServiceRequest sr7 = serviceRequestProvider.savePending(otherDirectorService, requester);

		// 다른 디렉터에게 direct
		ServiceRequest sr8 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(directorService, requester,
			otherDirector);

		// 해당 디렉터에게 온 direct 요청
		ServiceRequest sr9 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(directorService, requester,
			director);
		ServiceRequest sr10 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(directorService, requester,
			director);

		// 제안 제공중인 pending 상태의 serviceEstimate (신규요청엔 뜨면 안됨)
		ServiceEstimate pendingEstimate = serviceEstimateProvider.save(director.getDirectorInfo(), sr4);

		// 제안 진행중인 estimate
		ServiceEstimate ongoingEstimate1 = serviceEstimateProvider.saveOngoing(director.getDirectorInfo(),
			ongoingServiceRequest1,
			LocalDateTime.now().plusDays(1));
		ServiceEstimate ongoingEstimate2 = serviceEstimateProvider.saveOngoing(director.getDirectorInfo(),
			ongoingServiceRequest2,
			LocalDateTime.now().plusDays(1));
		ServiceEstimate directorCompletedEstimate = serviceEstimateProvider.saveDirectorDone(
			director.getDirectorInfo(),
			completedServiceRequest1,
			LocalDateTime.now().plusDays(2));

		// 제안 완료된 estimate
		ServiceEstimate memberCompletedEstimate = serviceEstimateProvider.saveMemberCompleted(
			director.getDirectorInfo(),
			completedServiceRequest2,
			LocalDateTime.now().plusDays(2));

		ServiceEstimate reviewCompletedEstimate = serviceEstimateProvider.saveReviewCompleted(
			director.getDirectorInfo(),
			completedServiceRequest3,
			LocalDateTime.now().plusDays(2));

		// 제안 취소된 estimate
		serviceEstimateProvider.saveCanceled(director.getDirectorInfo(), canceledServiceRequest,
			LocalDateTime.now().plusDays(3)); // CANCELED

		// expired 된 estimate
		serviceEstimateProvider.saveExpired(director.getDirectorInfo(), expiredServiceRequest,
			LocalDateTime.now().plusDays(4));

		// 디렉터 사용자 차단
		memberBlockProvider.save(director, requester);

		// chatRoom 저장
		ChatRoom chatRoom1 = chatRoomProvider.save();
		ChatRoom chatRoom2 = chatRoomProvider.save();

		// chatRoomServiceEstimateMapping 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom1, memberCompletedEstimate);
		chatRoomServiceEstimateMappingProvider.save(chatRoom2, reviewCompletedEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/service-estimates")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(STATUS_STR, ServiceEstimateStatus.COMPLETED_BY_MEMBER.name())
					.param(PAGE_STR, ZERO_STR)
					.param(DIRECTOR_SERVICE_ID_STR, String.valueOf(directorService.getId()))
			)
			.andExpect(status().isOk())
			.andReturn();

		ServiceEstimateFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			ServiceEstimateFindAllResponseForDirector.class);

		// then: completed 만 반환
		List<ServiceEstimateResponseForDirector> estimates = response.getServiceEstimates();

		assertThat(estimates).hasSize(2);

		List<Long> ids = estimates.stream().map(ServiceEstimateResponseForDirector::getId).toList();
		assertThat(ids).contains(memberCompletedEstimate.getId(), reviewCompletedEstimate.getId());
	}

	@Test
	@DisplayName("디렉터가 자신의 제안을 조회 할 수 있다. (canceled 상태)")
	void findAll_statusCanceled() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, otherDirectorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService otherDirectorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);
		directorServiceMappingProvider.save(otherDirectorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);

		// 해당 디렉터가 ongoing 중인 request
		ServiceRequest ongoingServiceRequest1 = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now().minusDays(1));
		ServiceRequest ongoingServiceRequest2 = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now().minusDays(1));

		// 해당 디렉터가 completed 된 request
		ServiceRequest completedServiceRequest1 = serviceRequestProvider.saveWithIsCompletedTrue(directorService,
			requester, LocalDateTime.now().minusDays(1));

		// 해당 디렉터가 canceled 된 request
		ServiceRequest canceledServiceRequest = serviceRequestProvider.saveWithIsCanceledTrue(directorService,
			requester, LocalDateTime.now().minusDays(1));

		// expired 된 request
		ServiceRequest expiredServiceRequest = serviceRequestProvider.saveWithIsExpiredTrue(directorService,
			requester, LocalDateTime.now().minusDays(1));

		// pending 상태인 request
		ServiceRequest sr4 = serviceRequestProvider.savePending(directorService, requester);
		ServiceRequest sr5 = serviceRequestProvider.savePending(directorService, requester);
		ServiceRequest sr6 = serviceRequestProvider.savePending(directorService, requester);

		// 다른 서비스의 pending request
		ServiceRequest sr7 = serviceRequestProvider.savePending(otherDirectorService, requester);

		// 다른 디렉터에게 direct
		ServiceRequest sr8 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(directorService, requester,
			otherDirector);

		// 해당 디렉터에게 온 direct 요청
		ServiceRequest sr9 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(directorService, requester,
			director);
		ServiceRequest sr10 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(directorService, requester,
			director);

		// 제안 제공중인 pending 상태의 serviceEstimate (신규요청엔 뜨면 안됨)
		ServiceEstimate pendingEstimate = serviceEstimateProvider.save(director.getDirectorInfo(), sr4);

		// 제안 진행중인 estimate
		ServiceEstimate ongoingEstimate1 = serviceEstimateProvider.saveOngoing(director.getDirectorInfo(),
			ongoingServiceRequest1,
			LocalDateTime.now().plusDays(1));
		ServiceEstimate ongoingEstimate2 = serviceEstimateProvider.saveOngoing(director.getDirectorInfo(),
			ongoingServiceRequest2,
			LocalDateTime.now().plusDays(1));

		// 제안 완료된 estimate
		ServiceEstimate completedEstimate = serviceEstimateProvider.saveDirectorDone(director.getDirectorInfo(),
			completedServiceRequest1,
			LocalDateTime.now().plusDays(2));

		// 제안 취소된 estimate
		ServiceEstimate canceledEstimate = serviceEstimateProvider.saveCanceled(director.getDirectorInfo(),
			canceledServiceRequest,
			LocalDateTime.now().plusDays(3)); // CANCELED

		// expired 된 estimate
		ServiceEstimate expiredEstimate = serviceEstimateProvider.saveExpired(director.getDirectorInfo(),
			expiredServiceRequest,
			LocalDateTime.now().plusDays(4));

		// chatRoom 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		// chatRoomServiceEstimateMapping 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom, canceledEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/service-estimates")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(STATUS_STR, ServiceEstimateStatus.CANCELED.name())
					.param(PAGE_STR, ZERO_STR)
					.param(DIRECTOR_SERVICE_ID_STR, String.valueOf(directorService.getId()))
			)
			.andExpect(status().isOk())
			.andReturn();

		ServiceEstimateFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			ServiceEstimateFindAllResponseForDirector.class);

		// then: canceled 만 반환
		List<ServiceEstimateResponseForDirector> estimates = response.getServiceEstimates();

		assertThat(estimates).hasSize(1);

		List<Long> ids = estimates.stream().map(ServiceEstimateResponseForDirector::getId).toList();
		assertThat(ids).contains(canceledEstimate.getId());
	}

	@Test
	@DisplayName("디렉터가 자신의 제안을 조회 할 수 있다. (expired 상태)")
	void findAll_statusExpired() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, otherDirectorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService otherDirectorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);
		directorServiceMappingProvider.save(otherDirectorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);

		// 해당 디렉터가 ongoing 중인 request
		ServiceRequest ongoingServiceRequest1 = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now().minusDays(1));
		ServiceRequest ongoingServiceRequest2 = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now().minusDays(1));

		// 해당 디렉터가 completed 된 request
		ServiceRequest completedServiceRequest1 = serviceRequestProvider.saveWithIsCompletedTrue(directorService,
			requester, LocalDateTime.now().minusDays(1));

		// 해당 디렉터가 canceled 된 request
		ServiceRequest canceledServiceRequest = serviceRequestProvider.saveWithIsCanceledTrue(directorService,
			requester, LocalDateTime.now().minusDays(1));

		// expired 된 request
		ServiceRequest expiredServiceRequest = serviceRequestProvider.saveWithIsExpiredTrue(directorService,
			requester, LocalDateTime.now().minusDays(1));

		// pending 상태인 request
		ServiceRequest sr4 = serviceRequestProvider.savePending(directorService, requester);
		ServiceRequest sr5 = serviceRequestProvider.savePending(directorService, requester);
		ServiceRequest sr6 = serviceRequestProvider.savePending(directorService, requester);

		// 다른 서비스의 pending request
		ServiceRequest sr7 = serviceRequestProvider.savePending(otherDirectorService, requester);

		// 다른 디렉터에게 direct
		ServiceRequest sr8 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(directorService, requester,
			otherDirector);

		// 해당 디렉터에게 온 direct 요청
		ServiceRequest sr9 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(directorService, requester,
			director);
		ServiceRequest sr10 = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(directorService, requester,
			director);

		// 제안 제공중인 pending 상태의 serviceEstimate (신규요청엔 뜨면 안됨)
		ServiceEstimate pendingEstimate = serviceEstimateProvider.save(director.getDirectorInfo(), sr4);

		// 제안 진행중인 estimate
		ServiceEstimate ongoingEstimate1 = serviceEstimateProvider.saveOngoing(director.getDirectorInfo(),
			ongoingServiceRequest1,
			LocalDateTime.now().plusDays(1));
		ServiceEstimate ongoingEstimate2 = serviceEstimateProvider.saveOngoing(director.getDirectorInfo(),
			ongoingServiceRequest2,
			LocalDateTime.now().plusDays(1));

		// 제안 완료된 estimate
		ServiceEstimate completedEstimate = serviceEstimateProvider.saveDirectorDone(director.getDirectorInfo(),
			completedServiceRequest1,
			LocalDateTime.now().plusDays(2));

		// 제안 취소된 estimate
		ServiceEstimate canceledEstimate = serviceEstimateProvider.saveCanceled(director.getDirectorInfo(),
			canceledServiceRequest,
			LocalDateTime.now().plusDays(3)); // CANCELED

		// expired 된 estimate
		ServiceEstimate expiredEstimate = serviceEstimateProvider.saveExpired(director.getDirectorInfo(),
			expiredServiceRequest,
			LocalDateTime.now().plusDays(4));

		// chatRoom 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		// chatRoomServiceEstimateMapping 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom, expiredEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/service-estimates")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(STATUS_STR, ServiceEstimateStatus.EXPIRED.name())
					.param(PAGE_STR, ZERO_STR)
					.param(DIRECTOR_SERVICE_ID_STR, String.valueOf(directorService.getId()))
			)
			.andExpect(status().isOk())
			.andReturn();

		ServiceEstimateFindAllResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			ServiceEstimateFindAllResponseForDirector.class);

		// then: canceled 만 반환
		List<ServiceEstimateResponseForDirector> estimates = response.getServiceEstimates();

		assertThat(estimates).hasSize(1);

		List<Long> ids = estimates.stream().map(ServiceEstimateResponseForDirector::getId).toList();
		assertThat(ids).contains(expiredEstimate.getId());
	}

	@Test
	@DisplayName("디렉터가 자신의 제안을 조회 할 수 있다. (디렉터가 제공하는 서비스가 아닌 경우)")
	void findAllWithInvalidDirectorServiceId() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);

		// 요청자 및 요청 생성
		Member requester1 = memberProvider.saveMember(SignInPlatform.APPLE);

		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService, requester1);
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService, requester1);
		ServiceRequest serviceRequest3 = serviceRequestProvider.savePending(directorService, requester1);
		ServiceRequest serviceRequest4 = serviceRequestProvider.savePending(directorService, requester1);

		// 제안 생성
		ServiceEstimate pending = serviceEstimateProvider.save(director.getDirectorInfo(), serviceRequest1);

		ServiceEstimate ongoing = serviceEstimateProvider.saveOngoing(director.getDirectorInfo(), serviceRequest2,
			LocalDateTime.now().plusDays(1));

		ServiceEstimate completed = serviceEstimateProvider.saveDirectorDone(director.getDirectorInfo(),
			serviceRequest3,
			LocalDateTime.now().plusDays(2));

		ServiceEstimate canceled = serviceEstimateProvider.saveCanceled(director.getDirectorInfo(), serviceRequest4,
			LocalDateTime.now().plusDays(3));

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/service-estimates")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(STATUS_STR, ServiceEstimateStatus.PENDING.name())
					.param(PAGE_STR, ZERO_STR)
					.param(DIRECTOR_SERVICE_ID_STR, String.valueOf(99999999L))
			)
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(
					DirectorServiceException.DIRECTOR_SERVICE_NOT_FOUND.getHttpStatus().toString()))
			.andExpect(
				jsonPath(ERROR_MESSAGE).value(DirectorServiceException.DIRECTOR_SERVICE_NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(DirectorServiceException.DIRECTOR_SERVICE_NOT_FOUND.getCode()));

	}

	@Test
	@DisplayName("디렉터가 자신의 제안을 상세 조회 할 수 있다. (pending 상태인 제안 조회)")
	void findDetailWithPendingStatus() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);

		// 요청자 및 요청 생성
		Member requester1 = memberProvider.saveMember(SignInPlatform.APPLE);

		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService, requester1);
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService, requester1);
		ServiceRequest serviceRequest3 = serviceRequestProvider.savePending(directorService, requester1);
		ServiceRequest serviceRequest4 = serviceRequestProvider.savePending(directorService, requester1);

		// 제안 생성
		ServiceEstimate pending = serviceEstimateProvider.save(director.getDirectorInfo(), serviceRequest1);

		ServiceEstimate ongoing = serviceEstimateProvider.saveOngoing(director.getDirectorInfo(), serviceRequest2,
			LocalDateTime.now().plusDays(1));

		ServiceEstimate completed = serviceEstimateProvider.saveDirectorDone(director.getDirectorInfo(),
			serviceRequest3,
			LocalDateTime.now().plusDays(2));

		ServiceEstimate canceled = serviceEstimateProvider.saveCanceled(director.getDirectorInfo(), serviceRequest4,
			LocalDateTime.now().plusDays(3));

		// chatRoom 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		// chatRoomServiceEstimateMapping 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom, pending);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/service-estimates/{serviceEstimateId}", pending.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(PAGE_STR, ZERO_STR)
					.param(DIRECTOR_SERVICE_ID_STR, directorService.getId().toString()))
			.andReturn().getResponse().getContentAsString();

		entityManager.flush();
		entityManager.clear();

		//then

		ServiceEstimateFindDetailResponseForDirector response = objectMapper.readValue(responseJson,
			ServiceEstimateFindDetailResponseForDirector.class);

		assertThat(response.getId()).isEqualTo(pending.getId());

		//status 확인
		assertThat(response.getStatus()).isEqualTo(ServiceEstimateStatus.PENDING.getDescription());
	}

	@Test
	@DisplayName("디렉터가 자신의 제안을 상세 조회 할 수 있다. (ongoing 상태인 제안 조회)")
	void findDetailWithOngoingStatus() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);

		// 요청자 및 요청 생성
		Member requester1 = memberProvider.saveMember(SignInPlatform.APPLE);

		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService, requester1);
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService, requester1);
		ServiceRequest serviceRequest3 = serviceRequestProvider.savePending(directorService, requester1);
		ServiceRequest serviceRequest4 = serviceRequestProvider.savePending(directorService, requester1);

		// 제안 생성
		ServiceEstimate pending = serviceEstimateProvider.save(director.getDirectorInfo(), serviceRequest1);

		ServiceEstimate ongoing = serviceEstimateProvider.saveOngoing(director.getDirectorInfo(), serviceRequest2,
			LocalDateTime.now().plusDays(1));

		ServiceEstimate completed = serviceEstimateProvider.saveDirectorDone(director.getDirectorInfo(),
			serviceRequest3,
			LocalDateTime.now().plusDays(2));

		ServiceEstimate canceled = serviceEstimateProvider.saveCanceled(director.getDirectorInfo(), serviceRequest4,
			LocalDateTime.now().plusDays(3));

		// chatRoom 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		// chatRoomServiceEstimateMapping 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom, ongoing);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/service-estimates/{serviceEstimateId}", ongoing.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(PAGE_STR, ZERO_STR)
					.param(DIRECTOR_SERVICE_ID_STR, directorService.getId().toString()))
			.andReturn().getResponse().getContentAsString();

		entityManager.flush();
		entityManager.clear();

		//then

		ServiceEstimateFindDetailResponseForDirector response = objectMapper.readValue(responseJson,
			ServiceEstimateFindDetailResponseForDirector.class);

		assertThat(response.getId()).isEqualTo(ongoing.getId());

		//status 확인
		assertThat(response.getStatus()).isEqualTo(ServiceEstimateStatus.ONGOING.getDescription());
	}

	@Test
	@DisplayName("디렉터가 자신의 제안을 상세 조회 할 수 있다. (completed 상태인 제안 조회)")
	void findDetailWithCompletedStatus() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);

		// 요청자 및 요청 생성
		Member requester1 = memberProvider.saveMember(SignInPlatform.APPLE);

		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService, requester1);
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService, requester1);
		ServiceRequest serviceRequest3 = serviceRequestProvider.savePending(directorService, requester1);
		ServiceRequest serviceRequest4 = serviceRequestProvider.savePending(directorService, requester1);

		// 제안 생성
		ServiceEstimate pending = serviceEstimateProvider.save(director.getDirectorInfo(), serviceRequest1);

		ServiceEstimate ongoing = serviceEstimateProvider.saveOngoing(director.getDirectorInfo(), serviceRequest2,
			LocalDateTime.now().plusDays(1));

		ServiceEstimate completed = serviceEstimateProvider.saveDirectorDone(director.getDirectorInfo(),
			serviceRequest3,
			LocalDateTime.now().plusDays(2));

		ServiceEstimate canceled = serviceEstimateProvider.saveCanceled(director.getDirectorInfo(), serviceRequest4,
			LocalDateTime.now().plusDays(3));

		// chatRoom 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		// chatRoomServiceEstimateMapping 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom, completed);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/service-estimates/{serviceEstimateId}", completed.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(PAGE_STR, ZERO_STR)
					.param(DIRECTOR_SERVICE_ID_STR, directorService.getId().toString()))
			.andReturn().getResponse().getContentAsString();

		entityManager.flush();
		entityManager.clear();

		//then

		ServiceEstimateFindDetailResponseForDirector response = objectMapper.readValue(responseJson,
			ServiceEstimateFindDetailResponseForDirector.class);

		assertThat(response.getId()).isEqualTo(completed.getId());

		//status 확인
		assertThat(response.getStatus()).isEqualTo(ServiceEstimateStatus.DIRECTOR_DONE.getDescription());
	}

	@Test
	@DisplayName("디렉터가 자신의 제안을 상세 조회 할 수 있다. (canceled 상태인 제안 조회)")
	void findDetailWithCanceledStatus() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);

		// 요청자 및 요청 생성
		Member requester1 = memberProvider.saveMember(SignInPlatform.APPLE);

		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(directorService, requester1);
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(directorService, requester1);
		ServiceRequest serviceRequest3 = serviceRequestProvider.savePending(directorService, requester1);
		ServiceRequest serviceRequest4 = serviceRequestProvider.savePending(directorService, requester1);

		// 제안 생성
		ServiceEstimate pending = serviceEstimateProvider.save(director.getDirectorInfo(), serviceRequest1);

		ServiceEstimate ongoing = serviceEstimateProvider.saveOngoing(director.getDirectorInfo(), serviceRequest2,
			LocalDateTime.now().plusDays(1));

		ServiceEstimate completed = serviceEstimateProvider.saveDirectorDone(director.getDirectorInfo(),
			serviceRequest3,
			LocalDateTime.now().plusDays(2));

		ServiceEstimate canceled = serviceEstimateProvider.saveCanceled(director.getDirectorInfo(), serviceRequest4,
			LocalDateTime.now().plusDays(3));

		// chatRoom 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		// chatRoomServiceEstimateMapping 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom, canceled);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/service-estimates/{serviceEstimateId}", canceled.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(PAGE_STR, ZERO_STR)
					.param(DIRECTOR_SERVICE_ID_STR, directorService.getId().toString()))
			.andReturn().getResponse().getContentAsString();

		entityManager.flush();
		entityManager.clear();

		//then

		ServiceEstimateFindDetailResponseForDirector response = objectMapper.readValue(responseJson,
			ServiceEstimateFindDetailResponseForDirector.class);

		assertThat(response.getId()).isEqualTo(canceled.getId());

		//status 확인
		assertThat(response.getStatus()).isEqualTo(ServiceEstimateStatus.CANCELED.getDescription());
	}

	@Test
	@DisplayName("제안 카운트 조회 - 디렉터가 DISTRICT 선택 시 지역 매칭 (확장된 요청가 존재할때)")
	void findCounts_whenDirectorSelectsDistrictAndExpandedRequestExist() throws Exception {
		// ===== 디렉터 / 서비스 =====
		DirectorInfo directorInfo =
			directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
				LocalDate.now().plusMonths(1));
		Member director =
			memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_2_STR, null);
		DirectorService service = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);
		directorServiceMappingProvider.save(directorInfo, service);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);

		// ===== 지역 =====
		Location allCity = locationProvider.save("전국", LocationType.ALL_CITY);
		Location seoul = locationProvider.save("서울시", LocationType.CITY);
		Location gangnam = locationProvider.saveWithParent("강남구", LocationType.DISTRICT, seoul);
		Location songpa = locationProvider.saveWithParent("송파구", LocationType.DISTRICT, seoul);

		// 디렉터는 강남구
		directorLocationMappingProvider.save(directorInfo, gangnam);

		// ===== 요청 (PENDING, 신규 요청 기준 충족) =====
		ServiceRequest reqAll =
			serviceRequestProvider.savePending(service, requester);
		requestLocationMappingProvider.save(allCity, reqAll);

		ServiceRequest reqSeoul =
			serviceRequestProvider.savePending(service, requester);
		requestLocationMappingProvider.save(seoul, reqSeoul);

		ServiceRequest reqGangnam =
			serviceRequestProvider.savePending(service, requester);
		requestLocationMappingProvider.save(gangnam, reqGangnam);

		ServiceRequest reqSongpa =
			serviceRequestProvider.savePending(service, requester);
		requestLocationMappingProvider.save(songpa, reqSongpa);
		reqSongpa.expandLocation(seoul, LocalDateTime.now());

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson =
			mockMvc.perform(get("/api/directors/service-estimates/counts")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		ServiceEstimateFindCountsResponseForDirector response = objectMapper.readValue(
			responseJson,
			ServiceEstimateFindCountsResponseForDirector.class
		);

		// then 모든 요청가 다 보여야 한다.
		assertThat(response.getRequestCount()).isEqualTo(4);
	}

	@Test
	@DisplayName("제안 카운트 조회 - 디렉터가 DISTRICT 선택 시 지역 매칭")
	void findCounts_whenDirectorSelectsDistrict() throws Exception {
		// ===== 디렉터 / 서비스 =====
		DirectorInfo directorInfo =
			directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
				LocalDate.now().plusMonths(1));
		Member director =
			memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_2_STR, null);
		DirectorService service = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);
		directorServiceMappingProvider.save(directorInfo, service);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);

		// ===== 지역 =====
		Location allCity = locationProvider.save("전국", LocationType.ALL_CITY);
		Location seoul = locationProvider.save("서울시", LocationType.CITY);
		Location gangnam = locationProvider.saveWithParent("강남구", LocationType.DISTRICT, seoul);
		Location songpa = locationProvider.saveWithParent("송파구", LocationType.DISTRICT, seoul);

		// 디렉터는 강남구
		directorLocationMappingProvider.save(directorInfo, gangnam);

		// ===== 요청 (PENDING, 신규 요청 기준 충족) =====
		ServiceRequest reqAll = serviceRequestProvider.savePending(service, requester);
		requestLocationMappingProvider.save(allCity, reqAll);

		ServiceRequest reqSeoul = serviceRequestProvider.savePending(service, requester);
		requestLocationMappingProvider.save(seoul, reqSeoul);

		ServiceRequest reqGangnam = serviceRequestProvider.savePending(service, requester);
		requestLocationMappingProvider.save(gangnam, reqGangnam);

		ServiceRequest reqSongpa = serviceRequestProvider.savePending(service, requester);
		requestLocationMappingProvider.save(songpa, reqSongpa);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson =
			mockMvc.perform(get("/api/directors/service-estimates/counts")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		ServiceEstimateFindCountsResponseForDirector response = objectMapper.readValue(
			responseJson,
			ServiceEstimateFindCountsResponseForDirector.class
		);

		// then
		assertThat(response.getRequestCount()).isEqualTo(3);
	}

	@Test
	@DisplayName("제안 카운트 조회 - 디렉터가 CITY 선택 시 지역 매칭")
	void findCounts_whenDirectorSelectsCity() throws Exception {
		// ===== 디렉터 / 서비스 =====
		DirectorInfo directorInfo =
			directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
				LocalDate.now().plusMonths(1));
		Member director =
			memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_2_STR, null);
		DirectorService service = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);
		directorServiceMappingProvider.save(directorInfo, service);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);

		// ===== 지역 =====
		Location allCity = locationProvider.save("전국", LocationType.ALL_CITY);
		Location seoul = locationProvider.save("서울시", LocationType.CITY);
		Location gangnam = locationProvider.saveWithParent("강남구", LocationType.DISTRICT, seoul);
		Location songpa = locationProvider.saveWithParent("송파구", LocationType.DISTRICT, seoul);

		// 디렉터는 서울
		directorLocationMappingProvider.save(directorInfo, seoul);

		// ===== 요청 (PENDING, 신규 요청 기준 충족) =====
		ServiceRequest reqAll =
			serviceRequestProvider.savePending(service, requester);
		requestLocationMappingProvider.save(allCity, reqAll);

		ServiceRequest reqSeoul =
			serviceRequestProvider.savePending(service, requester);
		requestLocationMappingProvider.save(seoul, reqSeoul);

		ServiceRequest reqGangnam =
			serviceRequestProvider.savePending(service, requester);
		requestLocationMappingProvider.save(gangnam, reqGangnam);

		ServiceRequest reqSongpa =
			serviceRequestProvider.savePending(service, requester);
		requestLocationMappingProvider.save(songpa, reqSongpa);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson =
			mockMvc.perform(get("/api/directors/service-estimates/counts")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		ServiceEstimateFindCountsResponseForDirector response = objectMapper.readValue(
			responseJson,
			ServiceEstimateFindCountsResponseForDirector.class
		);

		// then

		// 요청: 전국 / 서울 / 강남 / 송파
		// 기대 requestCount = 4
		assertThat(response.getRequestCount()).isEqualTo(4);
	}

	@Test
	@DisplayName("제안 카운트 조회 - 디렉터가 ALL_CITY 선택 시 지역 매칭")
	void findCounts_whenDirectorSelectsAllCity() throws Exception {
		// ===== 디렉터 / 서비스 =====
		DirectorInfo directorInfo =
			directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
				LocalDate.now().plusMonths(1));
		Member director =
			memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);

		// ===== 지역 =====
		Location allCity = locationProvider.save("전국", LocationType.ALL_CITY);
		Location seoul = locationProvider.save("서울시", LocationType.CITY);
		Location gangnam = locationProvider.saveWithParent("강남구", LocationType.DISTRICT, seoul);
		Location songpa = locationProvider.saveWithParent("송파구", LocationType.DISTRICT, seoul);

		// 디렉터는 ALL CITY
		directorLocationMappingProvider.save(directorInfo, allCity);

		// ===== 요청 (PENDING, 신규 요청 기준 충족) =====
		ServiceRequest reqAll =
			serviceRequestProvider.savePending(directorService, requester);
		requestLocationMappingProvider.save(allCity, reqAll);

		ServiceRequest reqSeoul =
			serviceRequestProvider.savePending(directorService, requester);
		requestLocationMappingProvider.save(seoul, reqSeoul);

		ServiceRequest reqGangnam =
			serviceRequestProvider.savePending(directorService, requester);
		requestLocationMappingProvider.save(gangnam, reqGangnam);

		ServiceRequest reqSongpa =
			serviceRequestProvider.savePending(directorService, requester);
		requestLocationMappingProvider.save(songpa, reqSongpa);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson =
			mockMvc.perform(get("/api/directors/service-estimates/counts")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		ServiceEstimateFindCountsResponseForDirector response = objectMapper.readValue(
			responseJson,
			ServiceEstimateFindCountsResponseForDirector.class
		);

		// then

		// 요청: 전국 / 서울 / 강남 / 송파 / 부산 등
		// 기대 requestCount = 전체
		assertThat(response.getRequestCount()).isEqualTo(4);
	}

	@Test
	@DisplayName("디렉터는 메인페이지용 제안 기반 카운트를 조회할 수 있다.")
	void findCounts_forDirectorDashboard() throws Exception {
		// ===== 디렉터 / 서비스 세팅 =====
		DirectorInfo directorInfo =
			directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
				LocalDate.now().plusMonths(1));
		Member director =
			memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorInfo otherDirectorInfo =
			directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
				LocalDate.now().plusMonths(1));
		Member otherDirector =
			memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, otherDirectorInfo);

		DirectorService parentService =
			directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService targetService =
			directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);
		DirectorService otherService =
			directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);

		directorServiceMappingProvider.save(directorInfo, targetService);
		directorServiceMappingProvider.save(otherDirectorInfo, targetService);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);

		// ======================================================
		// 1. ONGOING 요청 (ongoingCount = 2)
		// ======================================================
		ServiceRequest ongoing1 =
			serviceRequestProvider.saveWithIsOngoingTrue(
				targetService, requester, LocalDateTime.now().minusDays(1));
		serviceEstimateProvider.saveOngoing(
			directorInfo, ongoing1, LocalDateTime.now().plusDays(1));

		ServiceRequest ongoing2 =
			serviceRequestProvider.saveWithIsOngoingTrue(
				targetService, requester, LocalDateTime.now().minusDays(1));
		serviceEstimateProvider.saveOngoing(
			directorInfo, ongoing2, LocalDateTime.now().plusDays(1));

		// ======================================================
		// 2. COMPLETED 요청 (completedCount = 3)
		// ======================================================
		ServiceRequest completed1 =
			serviceRequestProvider.saveWithIsCompletedTrue(
				targetService, requester, LocalDateTime.now().minusDays(1));
		serviceEstimateProvider.saveMemberCompleted(
			directorInfo, completed1, LocalDateTime.now().plusDays(2));

		ServiceRequest completed2 =
			serviceRequestProvider.saveWithIsCompletedTrue(
				targetService, requester, LocalDateTime.now().minusDays(1));
		serviceEstimateProvider.saveMemberCompleted(
			directorInfo, completed2, LocalDateTime.now().plusDays(2));

		ServiceRequest completed3 =
			serviceRequestProvider.saveWithIsCompletedTrue(
				targetService, requester, LocalDateTime.now().minusDays(1));
		serviceEstimateProvider.saveReviewCompleted(
			directorInfo, completed3, LocalDateTime.now().plusDays(2));

		// ======================================================
		// 3. CANCELED 요청 (canceledCount = 1)
		// ======================================================
		ServiceRequest canceled =
			serviceRequestProvider.saveWithIsCanceledTrue(
				targetService, requester, LocalDateTime.now().minusDays(1));
		serviceEstimateProvider.saveCanceled(
			directorInfo, canceled, LocalDateTime.now().plusDays(3));

		// ======================================================
		// 4. EXPIRED 요청 (expiredCount = 1)
		// ======================================================
		ServiceRequest expired =
			serviceRequestProvider.saveWithIsExpiredTrue(
				targetService, requester, LocalDateTime.now().minusDays(1));
		serviceEstimateProvider.saveExpired(
			directorInfo, expired, LocalDateTime.now().plusDays(4));

		// ======================================================
		// 5. PENDING + 이미 제안 존재 (pendingCount = 1)
		// - 신규 요청(requestCount)에는 포함되지 않음
		// ======================================================
		ServiceRequest pendingWithEstimate =
			serviceRequestProvider.savePending(targetService, requester);
		serviceEstimateProvider.save(directorInfo, pendingWithEstimate);

		// ======================================================
		// 6. PENDING 요청들 (모두 신규 요청 조건 불충족)
		// ======================================================
		serviceRequestProvider.savePending(targetService, requester);        // 제안 없음
		serviceRequestProvider.savePending(targetService, requester);        // 제안 없음
		serviceRequestProvider.savePending(otherService, requester);         // 다른 서비스
		serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(      // 다른 디렉터에게 direct
			targetService, requester, otherDirector);
		serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(      // 해당 디렉터에게 direct
			targetService, requester, director);
		serviceRequestProvider.savePending(targetService, director);         // 본인 요청
		serviceRequestProvider.savePendingAndIsReceivingEstimateFalse(       // receivingEstimate = false
			targetService, requester);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson =
			mockMvc.perform(get("/api/directors/service-estimates/counts")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		ServiceEstimateFindCountsResponseForDirector response =
			objectMapper.readValue(responseJson, ServiceEstimateFindCountsResponseForDirector.class);

		// then
		assertThat(response.getRequestCount()).isEqualTo(0);   // 신규 요청 조건 충족 없음
		assertThat(response.getPendingCount()).isEqualTo(1);   // pendingWithEstimate
		assertThat(response.getOngoingCount()).isEqualTo(2);   // ongoing1, ongoing2
		assertThat(response.getCompletedCount()).isEqualTo(3); // completed1
		assertThat(response.getCanceledCount()).isEqualTo(1);  // canceled
		assertThat(response.getExpiredCount()).isEqualTo(1);   // expired
	}

	@Test
	@DisplayName("디렉터 대시보드 카운트 조회 - hidden request 제외")
	void findCounts_forDirectorDashboard_excludingHiddenRequests_andIncludingChildLocations() throws Exception {
		// ===== 디렉터 / 서비스 / 지역 =====
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, otherDirectorInfo);

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService targetService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);
		DirectorService otherService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);

		Location city = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location district = locationProvider.saveWithParent(LOCATION_NAME_2_STR, LocationType.DISTRICT, city);

		directorLocationMappingProvider.save(directorInfo, city);
		directorServiceMappingProvider.save(directorInfo, targetService);
		directorServiceMappingProvider.save(otherDirectorInfo, targetService);

		Member requester1 = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member requester2 = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member requester3 = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member requester4 = memberProvider.saveMember(SignInPlatform.KAKAO);

		// ======================================================
		// 1. ONGOING 요청 (ongoingCount = 2)
		// ======================================================
		ServiceRequest ongoing1 =
			serviceRequestProvider.saveWithIsOngoingTrue(targetService, requester1, LocalDateTime.now().minusDays(1));
		requestLocationMappingProvider.save(city, ongoing1);
		serviceEstimateProvider.saveOngoing(directorInfo, ongoing1, LocalDateTime.now().plusDays(1));

		ServiceRequest ongoing2 =
			serviceRequestProvider.saveWithIsOngoingTrue(targetService, requester2, LocalDateTime.now().minusDays(1));
		requestLocationMappingProvider.save(city, ongoing2);
		serviceEstimateProvider.saveOngoing(directorInfo, ongoing2, LocalDateTime.now().plusDays(1));

		// ======================================================
		// 2. COMPLETED 요청 (completedCount = 1)
		// ======================================================
		ServiceRequest completed =
			serviceRequestProvider.saveWithIsCompletedTrue(targetService, requester1, LocalDateTime.now().minusDays(1));
		requestLocationMappingProvider.save(city, completed);
		serviceEstimateProvider.saveReviewCompleted(directorInfo, completed, LocalDateTime.now().plusDays(2));

		// ======================================================
		// 3. CANCELED 요청 (canceledCount = 1)
		// ======================================================
		ServiceRequest canceled =
			serviceRequestProvider.saveWithIsCanceledTrue(targetService, requester1, LocalDateTime.now().minusDays(1));
		requestLocationMappingProvider.save(city, canceled);
		serviceEstimateProvider.saveCanceled(directorInfo, canceled, LocalDateTime.now().plusDays(3));

		// ======================================================
		// 4. EXPIRED 요청 (expiredCount = 1)
		// ======================================================
		ServiceRequest expired =
			serviceRequestProvider.saveWithIsExpiredTrue(targetService, requester1, LocalDateTime.now().minusDays(1));
		requestLocationMappingProvider.save(city, expired);
		serviceEstimateProvider.saveExpired(directorInfo, expired, LocalDateTime.now().plusDays(4));

		// ======================================================
		// 5. PENDING + 이미 제안 존재 (pendingCount = 1)
		// ======================================================
		ServiceRequest pendingWithEstimate =
			serviceRequestProvider.savePending(targetService, requester1);
		requestLocationMappingProvider.save(city, pendingWithEstimate);
		serviceEstimateProvider.save(directorInfo, pendingWithEstimate);

		// ======================================================
		// 6. 신규 요청 (requestCount = 2)
		// - CITY 선택 디렉터 → DISTRICT 포함
		// ======================================================
		ServiceRequest pendingInDistrict =
			serviceRequestProvider.savePending(targetService, requester3);
		requestLocationMappingProvider.save(district, pendingInDistrict);

		ServiceRequest pendingInCity =
			serviceRequestProvider.savePending(targetService, requester3);
		requestLocationMappingProvider.save(city, pendingInCity);

		// ======================================================
		// 7. hidden 요청 (제외)
		// ======================================================
		ServiceRequest hiddenPending =
			serviceRequestProvider.savePending(targetService, requester4);
		requestLocationMappingProvider.save(city, hiddenPending);
		redisDirectorHideRequestProvider.save(directorInfo, hiddenPending.getId());

		// ======================================================
		// 8. 기타 제외 케이스
		// ======================================================
		serviceRequestProvider.savePending(otherService, requester1); // 다른 서비스
		serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(targetService, requester1, otherDirector);
		serviceRequestProvider.savePending(targetService, director); // 본인 요청
		serviceRequestProvider.savePendingAndIsReceivingEstimateFalse(targetService, requester1);

		entityManager.flush();
		entityManager.clear();

		// when
		ServiceEstimateFindCountsResponseForDirector response =
			objectMapper.readValue(
				mockMvc.perform(get("/api/directors/service-estimates/counts")
						.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
						.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString(),
				ServiceEstimateFindCountsResponseForDirector.class
			);

		// then
		assertThat(response.getRequestCount()).isEqualTo(2);
		assertThat(response.getPendingCount()).isEqualTo(1);
		assertThat(response.getOngoingCount()).isEqualTo(2);
		assertThat(response.getCompletedCount()).isEqualTo(1);
		assertThat(response.getCanceledCount()).isEqualTo(1);
		assertThat(response.getExpiredCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("디렉터 대시보드 카운트 조회 - 차단한 회원의 요청은 제외된다")
	void findCounts_forDirectorDashboard_whenBlockedMemberExists() throws Exception {
		// ===== 디렉터 / 서비스 / 지역 세팅 =====
		DirectorInfo directorInfo =
			directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
				LocalDate.now().plusMonths(1));
		Member director =
			memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorInfo otherDirectorInfo =
			directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
				LocalDate.now().plusMonths(1));
		Member otherDirector =
			memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, otherDirectorInfo);

		DirectorService parentService =
			directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService targetService =
			directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);
		DirectorService otherService =
			directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);

		directorServiceMappingProvider.save(directorInfo, targetService);
		directorServiceMappingProvider.save(otherDirectorInfo, targetService);

		// 지역 (단일 CITY 기준)
		Location city =
			locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		directorLocationMappingProvider.save(directorInfo, city);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member blockedRequester = memberProvider.saveMember(SignInPlatform.KAKAO);

		// ======================================================
		// 1. ONGOING 요청 (ongoingCount = 2)
		// - 차단되지 않은 회원만 카운트
		// ======================================================
		ServiceRequest ongoing1 =
			serviceRequestProvider.saveWithIsOngoingTrue(
				targetService, requester, LocalDateTime.now().minusDays(1));
		requestLocationMappingProvider.save(city, ongoing1);
		serviceEstimateProvider.saveOngoing(
			directorInfo, ongoing1, LocalDateTime.now().plusDays(1));

		ServiceRequest ongoing2 =
			serviceRequestProvider.saveWithIsOngoingTrue(
				targetService, requester, LocalDateTime.now().minusDays(1));
		requestLocationMappingProvider.save(city, ongoing2);
		serviceEstimateProvider.saveOngoing(
			directorInfo, ongoing2, LocalDateTime.now().plusDays(1));

		// ======================================================
		// 2. COMPLETED 요청 (completedCount = 3)
		// ======================================================
		ServiceRequest completedAllowed =
			serviceRequestProvider.saveWithIsCompletedTrue(
				targetService, requester, LocalDateTime.now().minusDays(1));
		requestLocationMappingProvider.save(city, completedAllowed);
		serviceEstimateProvider.saveMemberCompleted(
			directorInfo, completedAllowed, LocalDateTime.now().plusDays(2));

		ServiceRequest completedBlocked =
			serviceRequestProvider.saveWithIsCompletedTrue(
				targetService, blockedRequester, LocalDateTime.now().minusDays(1));
		requestLocationMappingProvider.save(city, completedBlocked);
		serviceEstimateProvider.saveMemberCompleted(
			directorInfo, completedBlocked, LocalDateTime.now().plusDays(2));

		ServiceRequest completedAllowed2 =
			serviceRequestProvider.saveWithIsCompletedTrue(
				targetService, requester, LocalDateTime.now().minusDays(1));
		requestLocationMappingProvider.save(city, completedAllowed2);
		serviceEstimateProvider.saveReviewCompleted(
			directorInfo, completedAllowed2, LocalDateTime.now().plusDays(2));

		// ======================================================
		// 3. CANCELED 요청
		// - 차단 회원의 요청 → 제외
		// ======================================================
		ServiceRequest canceledBlocked =
			serviceRequestProvider.saveWithIsCanceledTrue(
				targetService, blockedRequester, LocalDateTime.now().minusDays(1));
		requestLocationMappingProvider.save(city, canceledBlocked);
		serviceEstimateProvider.saveCanceled(
			directorInfo, canceledBlocked, LocalDateTime.now().plusDays(3));

		// ======================================================
		// 4. EXPIRED 요청 (expiredCount = 1)
		// ======================================================
		ServiceRequest expiredAllowed =
			serviceRequestProvider.saveWithIsExpiredTrue(
				targetService, requester, LocalDateTime.now().minusDays(1));
		requestLocationMappingProvider.save(city, expiredAllowed);
		serviceEstimateProvider.saveExpired(
			directorInfo, expiredAllowed, LocalDateTime.now().plusDays(4));

		// ======================================================
		// 5. PENDING + 이미 제안 존재 (pendingCount = 1)
		// ======================================================
		ServiceRequest pendingWithEstimate =
			serviceRequestProvider.savePending(targetService, requester);
		requestLocationMappingProvider.save(city, pendingWithEstimate);
		serviceEstimateProvider.save(directorInfo, pendingWithEstimate);

		// ======================================================
		// 6. 기타 PENDING / 제외 케이스
		// ======================================================
		ServiceRequest pendingBlocked =
			serviceRequestProvider.savePending(targetService, blockedRequester);
		requestLocationMappingProvider.save(city, pendingBlocked); // 차단 회원 → 제외

		serviceRequestProvider.savePending(targetService, requester);            // 제안 없음 → 신규요청 조건 불충족
		serviceRequestProvider.savePending(otherService, requester);             // 다른 서비스
		serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(          // 다른 디렉터에게 direct
			targetService, requester, otherDirector);
		serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(          // 해당 디렉터에게 direct
			targetService, requester, director);
		serviceRequestProvider.savePending(targetService, director);             // 본인 요청
		serviceRequestProvider.savePendingAndIsReceivingEstimateFalse(           // receivingEstimate=false
			targetService, requester);

		// ===== 차단 설정 =====
		memberBlockProvider.save(director, blockedRequester);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson =
			mockMvc.perform(get("/api/directors/service-estimates/counts")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		ServiceEstimateFindCountsResponseForDirector response =
			objectMapper.readValue(responseJson, ServiceEstimateFindCountsResponseForDirector.class);

		// then
		assertThat(response.getRequestCount()).isEqualTo(0);   // 신규 요청 없음
		assertThat(response.getPendingCount()).isEqualTo(1);   // pendingWithEstimate
		assertThat(response.getOngoingCount()).isEqualTo(2);   // ongoing1, ongoing2
		assertThat(response.getCompletedCount()).isEqualTo(3); // completedAllowed
		assertThat(response.getCanceledCount()).isEqualTo(1);  // canceledBlocked는 제외 → 기존 규칙 유지
		assertThat(response.getExpiredCount()).isEqualTo(1);   // expiredAllowed
	}

	@Test
	@DisplayName("디렉터 대시보드 카운트 조회 - 특정 서비스로 필터링")
	void findCounts_forDirectorDashboard_filteredByDirectorService() throws Exception {
		// ===== 디렉터 / 서비스 세팅 =====
		DirectorInfo directorInfo =
			directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
				LocalDate.now().plusMonths(1));
		Member director =
			memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorInfo otherDirectorInfo =
			directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
				LocalDate.now().plusMonths(1));
		Member otherDirector =
			memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, otherDirectorInfo);

		DirectorService parentService =
			directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService targetService =
			directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);
		DirectorService otherService =
			directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);

		// 디렉터는 두 서비스 모두 가능하지만, 이번 테스트는 targetService로 필터
		directorServiceMappingProvider.save(directorInfo, targetService);
		directorServiceMappingProvider.save(directorInfo, otherService);
		directorServiceMappingProvider.save(otherDirectorInfo, targetService);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);

		// ======================================================
		// 1. ONGOING 요청 (ongoingCount = 2, targetService만 집계)
		// ======================================================
		ServiceRequest ongoing1 =
			serviceRequestProvider.saveWithIsOngoingTrue(
				targetService, requester, LocalDateTime.now().minusDays(1));
		serviceEstimateProvider.saveOngoing(
			directorInfo, ongoing1, LocalDateTime.now().plusDays(1));

		ServiceRequest ongoing2 =
			serviceRequestProvider.saveWithIsOngoingTrue(
				targetService, requester, LocalDateTime.now().minusDays(1));
		serviceEstimateProvider.saveOngoing(
			directorInfo, ongoing2, LocalDateTime.now().plusDays(1));

		// ======================================================
		// 2. COMPLETED 요청 (필터 대상 서비스 아님 → completedCount = 0)
		// ======================================================
		ServiceRequest completedOtherService =
			serviceRequestProvider.saveWithIsOngoingTrue(
				otherService, requester, LocalDateTime.now().minusDays(1));
		serviceEstimateProvider.saveDirectorDone(
			directorInfo, completedOtherService, LocalDateTime.now().plusDays(2));

		// ======================================================
		// 3. CANCELED 요청 (canceledCount = 1)
		// ======================================================
		ServiceRequest canceled =
			serviceRequestProvider.saveWithIsCanceledTrue(
				targetService, requester, LocalDateTime.now().minusDays(1));
		serviceEstimateProvider.saveCanceled(
			directorInfo, canceled, LocalDateTime.now().plusDays(3));

		// ======================================================
		// 4. EXPIRED 요청 (expiredCount = 1)
		// ======================================================
		ServiceRequest expired =
			serviceRequestProvider.saveWithIsExpiredTrue(
				targetService, requester, LocalDateTime.now().minusDays(1));
		serviceEstimateProvider.saveExpired(
			directorInfo, expired, LocalDateTime.now().plusDays(4));

		// ======================================================
		// 5. PENDING + 이미 제안 존재 (pendingCount = 1)
		// ======================================================
		ServiceRequest pendingWithEstimate =
			serviceRequestProvider.savePending(targetService, requester);
		serviceEstimateProvider.save(directorInfo, pendingWithEstimate);

		// ======================================================
		// 6. 신규 요청 / 제외 케이스
		// ======================================================
		serviceRequestProvider.savePending(otherService, requester);          // 다른 서비스
		serviceRequestProvider.savePending(otherService, requester);          // 다른 서비스
		serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(       // 다른 디렉터에게 direct
			targetService, requester, otherDirector);
		serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(       // 해당 디렉터에게 direct
			targetService, requester, director);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson =
			mockMvc.perform(get("/api/directors/service-estimates/counts")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(DIRECTOR_SERVICE_ID_STR, targetService.getId().toString()))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		ServiceEstimateFindCountsResponseForDirector response =
			objectMapper.readValue(responseJson, ServiceEstimateFindCountsResponseForDirector.class);

		// then
		assertThat(response.getRequestCount()).isEqualTo(0);   // 신규 요청 없음 (필터 적용)
		assertThat(response.getPendingCount()).isEqualTo(1);   // pendingWithEstimate
		assertThat(response.getOngoingCount()).isEqualTo(2);   // ongoing1, ongoing2
		assertThat(response.getCompletedCount()).isEqualTo(0); // targetService 기준 완료 없음
		assertThat(response.getCanceledCount()).isEqualTo(1);  // canceled
		assertThat(response.getExpiredCount()).isEqualTo(1);   // expired
	}

	@Test
	@DisplayName("제안 카운트 조회 (다이렉트 요청만 필터링)")
	void findCountsForDirectRequest_onlyDirectRequests() throws Exception {
		// ===== 디렉터 / 서비스 / 지역 =====
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, otherDirectorInfo);

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService targetService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);
		directorServiceMappingProvider.save(directorInfo, targetService);

		Location allCity = locationProvider.save("전국", LocationType.ALL_CITY);
		directorLocationMappingProvider.save(directorInfo, allCity);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);

		Member directForPendingRequester = memberProvider.saveMember(SignInPlatform.KAKAO);

		// ===== 직접 요청 신규 요청 (requestCount = 1) =====
		ServiceRequest directNew =
			serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(targetService, directForPendingRequester,
				director);
		requestLocationMappingProvider.save(allCity, directNew);

		// ===== 직접 요청 제안 상태별 집계 =====
		ServiceRequest directPending =
			serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(targetService, requester, director);
		requestLocationMappingProvider.save(allCity, directPending);
		serviceEstimateProvider.save(directorInfo, directPending); // pendingCount = 1

		ServiceRequest directOngoing =
			serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(targetService, requester, director);
		requestLocationMappingProvider.save(allCity, directOngoing);
		serviceEstimateProvider.saveOngoing(directorInfo, directOngoing, LocalDateTime.now().plusDays(1));

		ServiceRequest directDirectorDone =
			serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(targetService, requester, director);
		requestLocationMappingProvider.save(allCity, directDirectorDone);
		serviceEstimateProvider.saveDirectorDone(directorInfo, directDirectorDone, LocalDateTime.now().plusDays(2));

		ServiceRequest directCompletedByMember =
			serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(targetService, requester, director);
		requestLocationMappingProvider.save(allCity, directCompletedByMember);
		serviceEstimateProvider.saveMemberCompleted(directorInfo, directCompletedByMember,
			LocalDateTime.now().plusDays(3));

		ServiceRequest directReviewCompleted =
			serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(targetService, requester, director);
		requestLocationMappingProvider.save(allCity, directReviewCompleted);
		serviceEstimateProvider.saveReviewCompleted(directorInfo, directReviewCompleted,
			LocalDateTime.now().plusDays(4));

		ServiceRequest directCanceled =
			serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(targetService, requester, director);
		requestLocationMappingProvider.save(allCity, directCanceled);
		serviceEstimateProvider.saveCanceled(directorInfo, directCanceled, LocalDateTime.now().plusDays(5));

		ServiceRequest directExpired =
			serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(targetService, requester, director);
		requestLocationMappingProvider.save(allCity, directExpired);
		serviceEstimateProvider.saveExpired(directorInfo, directExpired, LocalDateTime.now().plusDays(6));

		// ===== 제외 케이스 =====
		ServiceRequest otherDirectorDirect =
			serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(targetService, requester, otherDirector);
		requestLocationMappingProvider.save(allCity, otherDirectorDirect);

		ServiceRequest otherDirectorDirectWithEstimate =
			serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(targetService, requester, otherDirector);
		requestLocationMappingProvider.save(allCity, otherDirectorDirectWithEstimate);
		serviceEstimateProvider.save(otherDirectorInfo, otherDirectorDirectWithEstimate);

		ServiceRequest nonDirectWithEstimate = serviceRequestProvider.savePending(targetService, requester);
		requestLocationMappingProvider.save(allCity, nonDirectWithEstimate);
		serviceEstimateProvider.save(directorInfo, nonDirectWithEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson =
			mockMvc.perform(get("/api/directors/service-estimates/counts")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(SHOW_ONLY_DIRECT_REQUEST, "true"))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		ServiceEstimateFindCountsResponseForDirector response =
			objectMapper.readValue(responseJson, ServiceEstimateFindCountsResponseForDirector.class);

		// then
		assertThat(response.getRequestCount()).isEqualTo(1);
		assertThat(response.getPendingCount()).isEqualTo(1);
		assertThat(response.getOngoingCount()).isEqualTo(2);
		assertThat(response.getCompletedCount()).isEqualTo(2);
		assertThat(response.getCanceledCount()).isEqualTo(1);
		assertThat(response.getExpiredCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("디렉터가 hired 된 제안(ONGOING)을 취소하면 제안과 요청 모두 CANCELED 된다")
	void cancelForPublic_ongoingAndHired_cancelsBoth() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now());

		ServiceEstimate estimate = serviceEstimateProvider.saveOngoing(director.getDirectorInfo(),
			serviceRequest, LocalDateTime.now());

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, requester);
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/directors/service-estimates/{serviceEstimateId}/cancel",
						estimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		ServiceEstimate canceled = serviceEstimateProvider.findById(estimate.getId());
		assertThat(canceled.getStatus()).isEqualTo(ServiceEstimateStatus.CANCELED);
		assertThat(canceled.getCanceledAt()).isNotNull();

		ServiceRequest updatedServiceRequest = serviceRequestProvider.findById(serviceRequest.getId());
		assertThat(updatedServiceRequest.getStatus()).isEqualTo(ServiceRequestStatus.CANCELED);
	}

	@Test
	@DisplayName("디렉터가 PENDING 제안을 취소하면 제안만 CANCELED 된다")
	void cancelForPublic_pending_cancelsEstimateOnly() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.savePendingWithReceivedEstimateCount(directorService,
			requester, 1);

		ServiceEstimate estimate = serviceEstimateProvider.save(director.getDirectorInfo(), serviceRequest);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, requester);
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/directors/service-estimates/{serviceEstimateId}/cancel",
						estimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		ServiceEstimate canceled = serviceEstimateProvider.findById(estimate.getId());
		assertThat(canceled.getStatus()).isEqualTo(ServiceEstimateStatus.CANCELED);
		assertThat(canceled.getCanceledAt()).isNotNull();

		ServiceRequest updatedServiceRequest = serviceRequestProvider.findById(serviceRequest.getId());
		assertThat(updatedServiceRequest.getStatus()).isEqualTo(ServiceRequestStatus.PENDING);
		assertThat(updatedServiceRequest.getReceivedEstimateCount()).isEqualTo(0);
	}

	@Test
	@DisplayName("디렉터가 DIRECTOR_DONE 상태의 제안을 취소할 수 있다 (isHired가 아닌 경우 요청는 취소되지 않는다)")
	void cancelForPublic_directorDone_notHired_cancelsEstimateOnly() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithCompletedCount(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			10);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now());

		ServiceEstimate estimate = serviceEstimateProvider.saveDirectorDone(director.getDirectorInfo(),
			serviceRequest,
			LocalDateTime.now());

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, requester);
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/directors/service-estimates/{serviceEstimateId}/cancel",
						estimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		ServiceEstimate canceled = serviceEstimateProvider.findById(estimate.getId());
		assertThat(canceled.getStatus()).isEqualTo(ServiceEstimateStatus.CANCELED);
		assertThat(canceled.getCanceledAt()).isNotNull();

		// isHired가 아니므로 요청는 취소되지 않는다
		ServiceRequest updatedServiceRequest = serviceRequestProvider.findById(serviceRequest.getId());
		assertThat(updatedServiceRequest.getStatus()).isEqualTo(ServiceRequestStatus.ONGOING);

		// completedEstimateCount는 변경되지 않는다
		DirectorInfo updatedDirectorInfo = directorInfoProvider.findById(directorInfo.getId());
		assertThat(updatedDirectorInfo.getCompletedEstimateCount()).isEqualTo(10);
	}

	@Test
	@DisplayName("디렉터가 DIRECTOR_DONE + hired 상태의 제안을 취소하면 제안과 요청 모두 CANCELED 된다")
	void cancelForPublic_directorDone_hired_cancelsBoth() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now());

		ServiceEstimate estimate = serviceEstimateProvider.saveDirectorDoneWithHired(director.getDirectorInfo(),
			serviceRequest, LocalDateTime.now());

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, requester);
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/directors/service-estimates/{serviceEstimateId}/cancel",
						estimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		ServiceEstimate canceled = serviceEstimateProvider.findById(estimate.getId());
		assertThat(canceled.getStatus()).isEqualTo(ServiceEstimateStatus.CANCELED);
		assertThat(canceled.getCanceledAt()).isNotNull();

		// hired이므로 요청도 취소된다
		ServiceRequest updatedServiceRequest = serviceRequestProvider.findById(serviceRequest.getId());
		assertThat(updatedServiceRequest.getStatus()).isEqualTo(ServiceRequestStatus.CANCELED);
	}

	@Test
	@DisplayName("COMPLETED_BY_MEMBER 상태의 제안은 취소할 수 없다")
	void cancelForPublic_completedByMember_fails() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithCompletedCount(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			10);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, requester,
			LocalDateTime.now());

		ServiceEstimate estimate = serviceEstimateProvider.saveMemberCompleted(director.getDirectorInfo(),
			serviceRequest,
			LocalDateTime.now());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/directors/service-estimates/{serviceEstimateId}/cancel",
						estimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(ServiceEstimateException.NOT_CANCELLABLE_STATUS.getHttpStatus().toString()))
			.andExpect(
				jsonPath(ERROR_MESSAGE).value(ServiceEstimateException.NOT_CANCELLABLE_STATUS.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateException.NOT_CANCELLABLE_STATUS.getCode()));
	}

	@Test
	@DisplayName("본인 소유가 아닌 제안을 취소하면 실패한다")
	void cancel_serviceEstimateWithNotOwned() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, otherDirectorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(otherDirector.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, requester);

		ServiceEstimate estimate = serviceEstimateProvider.save(director.getDirectorInfo(), serviceRequest);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/directors/service-estimates/{serviceEstimateId}/cancel",
						estimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(ServiceEstimateException.NOT_OWNED_BY.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceEstimateException.NOT_OWNED_BY.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateException.NOT_OWNED_BY.getCode()));
	}

	@Test
	@DisplayName("이미 취소된 제안을 다시 취소하면 실패한다")
	void cancel_serviceEstimateWithAlreadyCanceled() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, requester);

		ServiceEstimate estimate = serviceEstimateProvider.saveCanceled(director.getDirectorInfo(), serviceRequest,
			LocalDateTime.now());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/directors/service-estimates/{serviceEstimateId}/cancel",
						estimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(ServiceEstimateException.ALREADY_CANCELED.getHttpStatus().toString()))
			.andExpect(
				jsonPath(ERROR_MESSAGE).value(ServiceEstimateException.ALREADY_CANCELED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateException.ALREADY_CANCELED.getCode()));
	}

	@Test
	@DisplayName("EXPIRED 상태의 제안은 취소할 수 없다")
	void cancelForPublic_expired_fails() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, requester);

		ServiceEstimate estimate = serviceEstimateProvider.saveExpired(director.getDirectorInfo(), serviceRequest,
			LocalDateTime.now());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/directors/service-estimates/{serviceEstimateId}/cancel",
						estimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(ServiceEstimateException.NOT_CANCELLABLE_STATUS.getHttpStatus().toString()))
			.andExpect(
				jsonPath(ERROR_MESSAGE).value(ServiceEstimateException.NOT_CANCELLABLE_STATUS.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateException.NOT_CANCELLABLE_STATUS.getCode()));
	}

	@Test
	@DisplayName("디렉터가 제안을 완료 처리할 수 있다. (정상 케이스)")
	void complete_success() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now().minusDays(1));

		ServiceEstimate estimate = serviceEstimateProvider.saveOngoing(director.getDirectorInfo(), serviceRequest,
			LocalDateTime.now().plusDays(1));

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, requester);

		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/directors/service-estimates/{serviceEstimateId}/complete",
						estimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		ServiceEstimate updatedEstimate = serviceEstimateProvider.findById(estimate.getId());
		assertThat(updatedEstimate.getDirectorDoneAt()).isNotNull();

		Member updatedDirector = memberProvider.findById(director.getId());
		assertThat(updatedDirector.getDirectorInfo().getCompletedEstimateCount()).isEqualTo(1L);

		// 이떄 serviceRequest 도 completed 상태인지 확인한다.
		ServiceRequest updatedServiceRequest = serviceRequestProvider.findById(serviceRequest.getId());
		assertThat(updatedServiceRequest.getStatus()).isEqualTo(ServiceRequestStatus.ONGOING);

		// SSE 발행 검증 (REFRESH_CHAT_ROOM_LIST)
		verify(sseService, atLeastOnce()).refreshChatRoomList(
			argThat(payload ->
				payload.getEventName() == SseEventType.REFRESH_CHAT_ROOM_LIST &&
					Objects.equals(payload.getReceiverId(), requester.getId()) &&
					Objects.equals(payload.getReceiverRole(), Role.MEMBER)));

		// SSE 발행 검증 (REFRESH_NAV_CHAT_COUNT)
		verify(sseService, atLeastOnce()).refreshNavChatCount(
			argThat(payload ->
				payload.getEventName() == SseEventType.REFRESH_NAV_CHAT_COUNT &&
					payload.getReceiverId().equals(requester.getId()) &&
					Objects.equals(payload.getReceiverRole(), Role.MEMBER)));
	}

	@Test
	@DisplayName("디렉터가 제안을 완료 처리할 수 있다. (자신의 제안이 아닌 경우 실패)")
	void complete_notOwnedEstimate() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, otherDirectorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(otherDirector.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now().minusDays(1));

		ServiceEstimate estimate = serviceEstimateProvider.saveOngoing(director.getDirectorInfo(), serviceRequest,
			LocalDateTime.now().plusDays(1));

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/directors/service-estimates/{serviceEstimateId}/complete",
						estimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(ServiceEstimateException.NOT_OWNED_BY.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceEstimateException.NOT_OWNED_BY.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateException.NOT_OWNED_BY.getCode()));
	}

	@Test
	@DisplayName("디렉터가 제안을 완료 처리할 수 있다. (ONGOING 상태가 아닌 경우 실패)")
	void complete_notOngoingStatus() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, requester);

		ServiceEstimate estimate = serviceEstimateProvider.save(director.getDirectorInfo(), serviceRequest);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/directors/service-estimates/{serviceEstimateId}/complete",
						estimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(ServiceEstimateException.NOT_ONGOING_STATUS.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceEstimateException.NOT_ONGOING_STATUS.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateException.NOT_ONGOING_STATUS.getCode()));
	}

	@Test
	@DisplayName("디렉터가 제안을 완료 처리할 수 있다. (이미 완료된 제안인 경우 실패)")
	void complete_alreadyCompletedEstimate() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, requester,
			LocalDateTime.now().minusDays(1));

		ServiceEstimate estimate = serviceEstimateProvider.saveDirectorDone(director.getDirectorInfo(),
			serviceRequest,
			LocalDateTime.now().plusDays(1));

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/directors/service-estimates/{serviceEstimateId}/complete",
						estimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(ServiceEstimateException.NOT_ONGOING_STATUS.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceEstimateException.NOT_ONGOING_STATUS.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateException.NOT_ONGOING_STATUS.getCode()));
	}

	@Test
	@DisplayName("디렉터가 제안을 완료 처리할 수 있다. (존재하지 않는 제안인 경우 실패)")
	void complete_notFoundEstimate() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/directors/service-estimates/{serviceEstimateId}/complete", 99999999L)
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isNotFound())
			.andExpect(jsonPath(ERROR_STATUS).value(ServiceEstimateException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceEstimateException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("본인인증되지 않은 디렉터는 추가 제안을 생성할 수 없다.")
	void saveAdditionalEstimateFailsWhenNotAuthenticated() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfoAndIsAuthenticatedFalse(SignInPlatform.KAKAO,
			directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		ServiceEstimateSaveAdditionalRequestForDirector request = ServiceEstimateSaveAdditionalRequestForDirector.builder()
			.title(TITLE_STR)
			.price(5000L)
			.content(CONTENT_STR)
			.serviceId(1L)
			.build();

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/chat-rooms/{chatRoomId}/service-estimates",
					1L)
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(MemberException.NOT_AUTHENTICATED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(MemberException.NOT_AUTHENTICATED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(MemberException.NOT_AUTHENTICATED.getCode()));
	}

	@Test
	@DisplayName("디렉터는 추가 제안을 생성할 수 있다. (정상 케이스)")
	void saveAdditionalEstimate() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			directorInfo);
		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);

		// 전체 지역 저장
		Location location = locationProvider.save(LOCATION_NAME_1_STR, LocationType.ALL_CITY);

		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrueAndOldCreatedAt(directorService,
			requester,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveMemberCompleted(directorInfo, serviceRequest,
			LocalDateTime.now());
		Long originalPrice = serviceEstimate.getPrice();

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, requester);

		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		redisChatRoomSubscribeProvider.subscribe(chatRoom.getId(), director.getId(), SESSION_ID_STR);
		redisChatRoomSubscribeProvider.subscribe(chatRoom.getId(), requester.getId(), SESSION_ID_STR);


		ServiceEstimateSaveAdditionalRequestForDirector request = ServiceEstimateSaveAdditionalRequestForDirector.builder()
			.title(TITLE_STR)
			.price(5000L)
			.content(CONTENT_STR)
			.serviceId(directorService.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post(
						"/api/directors/chat-rooms/{chatRoomId}/service-estimates",
						chatRoom.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		entityManager.flush();
		entityManager.clear();

		// 제안 가격 증가 검증
		List<ServiceEstimate> serviceEstimates = serviceEstimateProvider.findAll();
		ServiceEstimate savedEstimate = serviceEstimates.stream()
			.filter(se -> !se.getId().equals(serviceEstimate.getId()))
			.findFirst()
			.orElseThrow();

		assertThat(savedEstimate).isNotNull();
		assertThat(savedEstimate.getStatus()).isEqualTo(ServiceEstimateStatus.PENDING);

		// 요청가 저장 되었는지 검증
		List<ServiceRequest> serviceRequests = serviceRequestProvider.findAll();
		ServiceRequest savedServiceRequest = serviceRequests.stream()
			.filter(sr -> !sr.getId().equals(serviceRequest.getId()))
			.findFirst()
			.orElseThrow();

		assertThat(savedServiceRequest).isNotNull();
		assertThat(savedServiceRequest.getStatus()).isEqualTo(ServiceRequestStatus.PENDING);

		// 채팅메세지 발송 검증
		List<ChatMessage> chatMessages = chatMessageProvider.findAll();
		assertThat(chatMessages.size()).isEqualTo(1);

		ChatMessage chatMessage = chatMessages.get(0);
		assertThat(chatMessage.getMessageType()).isEqualTo(ChatMessageType.ESTIMATE);

		// 채팅방은 여전히 한개인지 검증
		List<ChatRoom> chatRooms = chatRoomProvider.findAll();
		assertThat(chatRooms).hasSize(1);

		// location 저장 여부 확인
		List<RequestLocationMapping> requestLocations = requestLocationMappingProvider.findAll();
		assertThat(requestLocations).hasSize(1);

		// SSE 발행 검증 (REFRESH_CHAT_ROOM_LIST)
		verify(sseService, atLeastOnce()).refreshChatRoomList(
			argThat(payload ->
				payload.getEventName() == SseEventType.REFRESH_CHAT_ROOM_LIST &&
					Objects.equals(payload.getReceiverId(), requester.getId()) &&
					Objects.equals(payload.getReceiverRole(), Role.MEMBER)));

		// SSE 발행 검증 (REFRESH_NAV_CHAT_COUNT)
		verify(sseService, atLeastOnce()).refreshNavChatCount(
			argThat(payload ->
				payload.getEventName() == SseEventType.REFRESH_NAV_CHAT_COUNT &&
					payload.getReceiverId().equals(requester.getId()) &&
					Objects.equals(payload.getReceiverRole(), Role.MEMBER)));
	}

	@Test
	@DisplayName("디렉터는 추가 제안을 생성할 수 있다. (파일 이 제한 갯수 초과일때)")
	void saveAdditionalEstimateWithExceededFileCount() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			directorInfo);
		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveOngoing(directorInfo, serviceRequest,
			LocalDateTime.now());
		Long originalPrice = serviceEstimate.getPrice();

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, requester);

		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		redisChatRoomSubscribeProvider.subscribe(chatRoom.getId(), director.getId(), SESSION_ID_STR);
		redisChatRoomSubscribeProvider.subscribe(chatRoom.getId(), requester.getId(), SESSION_ID_STR);


		List<ServiceEstimateFile> files = new ArrayList<>();
		for (int i = 0; i < SERVICE_ESTIMATE_FILE_MAX_COUNT + 1; i++) {
			files.add(serviceEstimateFileProvider.saveWithEstimateType(director));
		}

		ServiceEstimateSaveAdditionalRequestForDirector request = ServiceEstimateSaveAdditionalRequestForDirector.builder()
			.title(TITLE_STR)
			.price(5000L)
			.content(CONTENT_STR)
			.serviceId(directorService.getId())
			.fileIds(files.stream().map(ServiceEstimateFile::getId).toList())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post(
						"/api/directors/chat-rooms/{chatRoomId}/service-estimates",
						chatRoom.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(SERVICE_ESTIMATE_FILE_MAX_COUNT_MSG))
			.andExpect(jsonPath("$.code").value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("디렉터는 추가 제안을 생성할 수 있다. (디렉터와 회원 사이에 끝나지 않은 제안이 존재할때)")
	void saveAdditionalEstimateWhenNotEndedEstimateExist() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			directorInfo);
		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);

		// 전체 지역 저장
		Location location = locationProvider.save(LOCATION_NAME_1_STR, LocationType.ALL_CITY);

		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrueAndOldCreatedAt(directorService,
			requester,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveOngoing(directorInfo, serviceRequest,
			LocalDateTime.now());
		Long originalPrice = serviceEstimate.getPrice();

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, requester);

		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		redisChatRoomSubscribeProvider.subscribe(chatRoom.getId(), director.getId(), SESSION_ID_STR);
		redisChatRoomSubscribeProvider.subscribe(chatRoom.getId(), requester.getId(), SESSION_ID_STR);


		ServiceEstimateSaveAdditionalRequestForDirector request = ServiceEstimateSaveAdditionalRequestForDirector.builder()
			.title(TITLE_STR)
			.price(5000L)
			.content(CONTENT_STR)
			.serviceId(directorService.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post(
						"/api/directors/chat-rooms/{chatRoomId}/service-estimates",
						chatRoom.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(
				ServiceEstimateException.ALREADY_EXISTS_ONGOING_ESTIMATE_BY_DIRECTOR.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(
				ServiceEstimateException.ALREADY_EXISTS_ONGOING_ESTIMATE_BY_DIRECTOR.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(
				ServiceEstimateException.ALREADY_EXISTS_ONGOING_ESTIMATE_BY_DIRECTOR.getCode()));
	}

	@Test
	@DisplayName("디렉터는 추가 제안을 생성할 수 있다. (해당 사용자가 24시간 내에 동일한 서비스로 요청을 한 기록이 있는 경우)")
	void saveAdditionalEstimateWhenSameServiceRequestExistIn24Hours() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			directorInfo);
		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);

		// 전체 지역 저장
		Location location = locationProvider.save(LOCATION_NAME_1_STR, LocationType.ALL_CITY);

		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveOngoing(directorInfo, serviceRequest,
			LocalDateTime.now());
		Long originalPrice = serviceEstimate.getPrice();

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, requester);

		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		redisChatRoomSubscribeProvider.subscribe(chatRoom.getId(), director.getId(), SESSION_ID_STR);
		redisChatRoomSubscribeProvider.subscribe(chatRoom.getId(), requester.getId(), SESSION_ID_STR);


		ServiceEstimateSaveAdditionalRequestForDirector request = ServiceEstimateSaveAdditionalRequestForDirector.builder()
			.title(TITLE_STR)
			.price(5000L)
			.content(CONTENT_STR)
			.serviceId(directorService.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post(
						"/api/directors/chat-rooms/{chatRoomId}/service-estimates",
						chatRoom.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(
				ServiceRequestException.FAIL_TO_SAVE_BY_ADDITIONAL_ESTIMATE.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(
				ServiceRequestException.FAIL_TO_SAVE_BY_ADDITIONAL_ESTIMATE.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(
				ServiceRequestException.FAIL_TO_SAVE_BY_ADDITIONAL_ESTIMATE.getCode()));
	}

	@Test
	@DisplayName("디렉터는 추가 제안을 생성할 수 있다. (정상 케이스, 상대방이 디렉터를 차단한 경우)")
	void saveAdditionalEstimateAndReceiverBlockedDirector() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			directorInfo);
		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);

		// 전체 지역 저장
		Location location = locationProvider.save(LOCATION_NAME_1_STR, LocationType.ALL_CITY);

		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrueAndOldCreatedAt(directorService,
			requester,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveOngoing(directorInfo, serviceRequest,
			LocalDateTime.now());
		Long originalPrice = serviceEstimate.getPrice();

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, requester);

		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		redisChatRoomSubscribeProvider.subscribe(chatRoom.getId(), director.getId(), SESSION_ID_STR);
		redisChatRoomSubscribeProvider.subscribe(chatRoom.getId(), requester.getId(), SESSION_ID_STR);


		// 차단 로직 추가
		memberBlockProvider.save(requester, director);

		ServiceEstimateSaveAdditionalRequestForDirector request = ServiceEstimateSaveAdditionalRequestForDirector.builder()
			.title(TITLE_STR)
			.price(5000L)
			.content(CONTENT_STR)
			.serviceId(directorService.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post(
						"/api/directors/chat-rooms/{chatRoomId}/service-estimates",
						chatRoom.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath("$.status").value(ServiceEstimateException.FAIL_TO_SAVE_BY_BLOCK.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(ServiceEstimateException.FAIL_TO_SAVE_BY_BLOCK.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(ServiceEstimateException.FAIL_TO_SAVE_BY_BLOCK.getCode()));
	}

	@Test
	@DisplayName("디렉터는 추가 제안을 생성할 수 있다. (상대방이 방을 나간 상태 였을때)")
	void saveAdditionalEstimateWhenOpponentDeleteChatRoom() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			directorInfo);
		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService);

		// 전체 지역 저장
		Location location = locationProvider.save(LOCATION_NAME_1_STR, LocationType.ALL_CITY);

		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrueAndOldCreatedAt(directorService,
			requester,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveMemberCompleted(directorInfo, serviceRequest,
			LocalDateTime.now());
		Long originalPrice = serviceEstimate.getPrice();

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMemberWithRoomDeletedTrue(chatRoom, requester);

		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);


		ServiceEstimateSaveAdditionalRequestForDirector request = ServiceEstimateSaveAdditionalRequestForDirector.builder()
			.title(TITLE_STR)
			.price(5000L)
			.content(CONTENT_STR)
			.serviceId(directorService.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post(
						"/api/directors/chat-rooms/{chatRoomId}/service-estimates",
						chatRoom.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		entityManager.flush();
		entityManager.clear();

		// 제안 가격 증가 검증
		List<ServiceEstimate> serviceEstimates = serviceEstimateProvider.findAll();
		ServiceEstimate savedEstimate = serviceEstimates.stream()
			.filter(se -> !se.getId().equals(serviceEstimate.getId()))
			.findFirst()
			.orElseThrow();

		assertThat(savedEstimate).isNotNull();
		assertThat(savedEstimate.getStatus()).isEqualTo(ServiceEstimateStatus.PENDING);

		// 요청가 저장 되었는지 검증
		List<ServiceRequest> serviceRequests = serviceRequestProvider.findAll();
		ServiceRequest savedServiceRequest = serviceRequests.stream()
			.filter(sr -> !sr.getId().equals(serviceRequest.getId()))
			.findFirst()
			.orElseThrow();

		assertThat(savedServiceRequest).isNotNull();
		assertThat(savedServiceRequest.getStatus()).isEqualTo(ServiceRequestStatus.PENDING);

		// 채팅메세지 발송 검증
		List<ChatMessage> chatMessages = chatMessageProvider.findAll();
		assertThat(chatMessages.size()).isEqualTo(1);

		ChatMessage chatMessage = chatMessages.get(0);
		assertThat(chatMessage.getMessageType()).isEqualTo(ChatMessageType.ESTIMATE);

		// 채팅방은 여전히 한개인지 검증
		List<ChatRoom> chatRooms = chatRoomProvider.findAll();
		assertThat(chatRooms).hasSize(1);

		// 상대방이 다시 들어온 상태가 되었는지 검증
		List<ChatRoomMember> chatRoomMembers = chatRoomMemberProvider.findAll();
		ChatRoomMember requesterMember = chatRoomMembers.stream()
			.filter(cm -> cm.getMember().getId().equals(requester.getId()))
			.findFirst()
			.orElseThrow();
		assertThat(requesterMember.getIsChatRoomDeleted()).isFalse();
	}

	@Test
	@DisplayName("디렉터는 추가 제안을 생성할 수 있다. (일반 회원이 요청하는 경우)")
	void saveAdditionalEstimateWithMember() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceEstimateSaveAdditionalRequestForDirector request = ServiceEstimateSaveAdditionalRequestForDirector.builder()
			.title(TITLE_STR)
			.price(5000L)
			.content(CONTENT_STR)
			.serviceId(directorService.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post(
						"/api/directors/chat-rooms/{chatRoomId}/service-estimates", 1L)
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.status").value(HandlerException.FORBIDDEN.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(HandlerException.FORBIDDEN.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(HandlerException.FORBIDDEN.getCode()));
	}

	@Test
	@DisplayName("디렉터는 추가 제안을 생성할 수 있다. (필수 필드 누락 시)")
	void saveAdditionalEstimateWithMissingRequiredFields() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceEstimateSaveAdditionalRequestForDirector request = ServiceEstimateSaveAdditionalRequestForDirector.builder()
			.title(TITLE_STR)
			.price(5000L)
			.serviceId(directorService.getId())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post(
						"/api/directors/chat-rooms/{chatRoomId}/service-estimates", 1L)
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath("$.code").value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("디렉터는 서비스 진행 내역을 조회할 수 있다. (정상)")
	void findServiceEstimateHistoriesWithPaging() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest1 = serviceRequestProvider.saveWithIsCompletedTrue(directorService, member,
			LocalDateTime.now());
		serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest1,
			LocalDateTime.now().minusDays(17));

		ServiceRequest serviceRequest2 = serviceRequestProvider.saveWithIsOngoingTrue(directorService, member,
			LocalDateTime.now().plusDays(1));
		serviceEstimateProvider.saveDirectorDone(directorInfo, serviceRequest2, LocalDateTime.now().minusDays(2));

		// 15개의 완료된 제안 생성
		for (int i = 1; i < 16; i++) {
			ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, member,
				LocalDateTime.now().minusDays(i));
			serviceEstimateProvider.saveMemberCompleted(directorInfo, serviceRequest,
				LocalDateTime.now().minusDays(i));
		}

		entityManager.flush();
		entityManager.clear();

		// when & then
		String firstPageJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/service-estimates/histories")
					.param("page", "0")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ServiceEstimateHistoriesResponseForDirector firstPage = objectMapper.readValue(firstPageJson,
			ServiceEstimateHistoriesResponseForDirector.class);

		assertThat(firstPage.getPage()).isEqualTo(0);
		assertThat(firstPage.getHasNext()).isFalse();
		assertThat(firstPage.getHistories()).hasSize(16);
	}

	@Test
	@DisplayName("디렉터는 서비스 진행 내역을 조회할 수 있다. (정렬 검증)")
	void findServiceEstimateHistoriesValidateOrder() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest1 = serviceRequestProvider.saveWithIsCompletedTrue(directorService, member,
			LocalDateTime.now().minusDays(3));
		serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest1, LocalDateTime.now().minusDays(3));

		ServiceRequest serviceRequest2 = serviceRequestProvider.saveWithIsCompletedTrue(directorService, member,
			LocalDateTime.now().minusDays(2));
		serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest2, LocalDateTime.now().minusDays(2));

		ServiceRequest serviceRequest3 = serviceRequestProvider.saveWithIsCompletedTrue(directorService, member,
			LocalDateTime.now().minusDays(4));
		serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest3, LocalDateTime.now().minusDays(4));

		ServiceRequest serviceRequest4 = serviceRequestProvider.saveWithIsCompletedTrue(directorService, member,
			LocalDateTime.now().minusDays(1));
		serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest4, LocalDateTime.now().minusDays(1));

		ServiceRequest serviceRequest5 = serviceRequestProvider.saveWithIsCompletedTrue(directorService, member,
			LocalDateTime.now());
		serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest5, LocalDateTime.now());

		entityManager.flush();
		entityManager.clear();

		// when & then
		String firstPageJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/service-estimates/histories")
					.param("page", "0")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ServiceEstimateHistoriesResponseForDirector firstPage = objectMapper.readValue(firstPageJson,
			ServiceEstimateHistoriesResponseForDirector.class);

		assertThat(firstPage.getPage()).isEqualTo(0);
		assertThat(firstPage.getHasNext()).isFalse();
		assertThat(firstPage.getHistories()).hasSize(5);

		// 순서 검증
		List<Long> expectedOrder = List.of(
			serviceRequest5.getId(),
			serviceRequest4.getId(),
			serviceRequest2.getId(),
			serviceRequest1.getId(),
			serviceRequest3.getId()
		);
		List<Long> actualOrder = firstPage.getHistories().stream()
			.map(ServiceEstimateHistoryResponseForDirector::getServiceRequestId)
			.toList();

		assertThat(actualOrder).isEqualTo(expectedOrder);
	}

	@Test
	@DisplayName("디렉터는 서비스 진행 내역을 조회할 수 있다. (내역이 없는 경우)")
	void findServiceEstimateHistoriesWhenEmpty() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/service-estimates/histories")
					.param("page", "0")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ServiceEstimateHistoriesResponseForDirector response = objectMapper.readValue(responseJson,
			ServiceEstimateHistoriesResponseForDirector.class);

		// then
		assertThat(response.getPage()).isEqualTo(0);
		assertThat(response.getHasNext()).isFalse();
		assertThat(response.getHistories()).isEmpty();
	}

	@Test
	@DisplayName("제안 제목에 금칙어가 포함되어 있으면 저장할 수 없다.")
	void saveServiceEstimateWithForbiddenWordInTitle() throws Exception {
		// given
		forbiddenWordProvider.save(FORBIDDEN_WORD);

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt directorJwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);

		directorServiceMappingProvider.save(directorInfo, directorService);

		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, member);
		requestLocationMappingProvider.save(cityLocation, serviceRequest);

		ServiceEstimateSaveRequestForDirector request = ServiceEstimateSaveRequestForDirector.builder()
			.serviceRequestId(serviceRequest.getId())
			.title("제목에 " + FORBIDDEN_WORD + "가 포함")
			.price(AUTO_PRICE)
			.content(AUTO_CONTENT_STR)
			.fileIds(List.of())
			.scheduledAt(DEFAULT_SCHEDULED_AT)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, directorJwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, directorJwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ForbiddenWordException.CONTAINS_FORBIDDEN_WORD.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_CODE).value(ForbiddenWordException.CONTAINS_FORBIDDEN_WORD.getCode()));
	}

	@Test
	@DisplayName("제안 내용에 금칙어가 포함되어 있으면 저장할 수 없다.")
	void saveServiceEstimateWithForbiddenWordInContent() throws Exception {
		// given
		forbiddenWordProvider.save(FORBIDDEN_WORD);

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt directorJwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);

		directorServiceMappingProvider.save(directorInfo, directorService);

		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, member);
		requestLocationMappingProvider.save(cityLocation, serviceRequest);

		ServiceEstimateSaveRequestForDirector request = ServiceEstimateSaveRequestForDirector.builder()
			.serviceRequestId(serviceRequest.getId())
			.title(AUTO_TITLE_STR)
			.price(AUTO_PRICE)
			.content("내용에 " + FORBIDDEN_WORD + "가 포함")
			.fileIds(List.of())
			.scheduledAt(DEFAULT_SCHEDULED_AT)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, directorJwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, directorJwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ForbiddenWordException.CONTAINS_FORBIDDEN_WORD.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_CODE).value(ForbiddenWordException.CONTAINS_FORBIDDEN_WORD.getCode()));
	}

	@Test
	@DisplayName("디렉터가 서비스 제안을 저장할 수 없다. (받은 제안 수가 최대치를 초과한 경우)")
	void saveFailWhenExceededMaxReceivedEstimateCount() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, directorService);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.savePendingWithReceivedEstimateCount(
			directorService2, member, MAX_RECEIVED_ESTIMATE_COUNT);

		ServiceEstimateSaveRequestForDirector request = ServiceEstimateSaveRequestForDirector.builder()
			.serviceRequestId(serviceRequest.getId())
			.title(TITLE_STR)
			.price(10000L)
			.content(CONTENT_STR)
			.scheduledAt(DEFAULT_SCHEDULED_AT)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceEstimateException.EXCEEDED_MAX_RECEIVED_ESTIMATE_COUNT.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(
				ServiceEstimateException.EXCEEDED_MAX_RECEIVED_ESTIMATE_COUNT.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(
				ServiceEstimateException.EXCEEDED_MAX_RECEIVED_ESTIMATE_COUNT.getCode()));
	}

	// ========================== updateEstimate 통합 테스트 ==========================

	@Test
	@DisplayName("제안 수정 API - ONGOING 상태 제안 수정 시 리마인더 재설정 및 wishTime 업데이트")
	void updateEstimate_ongoing_resetsReminderAndUpdatesWishTime() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now());

		// confirmed wishTime 생성
		ServiceRequestWishTime confirmedWishTime = serviceRequestWishTimeProvider.save(serviceRequest,
			DEFAULT_WISH_DATE_TIME_1);
		confirmedWishTime.confirm();

		LocalDateTime oldScheduledAt = DEFAULT_WISH_DATE_TIME_1;
		LocalDateTime newScheduledAt = DEFAULT_WISH_DATE_TIME_2;

		ServiceEstimate estimate = serviceEstimateProvider.saveOngoingWithSentReminder(directorInfo,
			serviceRequest, LocalDateTime.now(), oldScheduledAt,
			oldScheduledAt.minusDays(1), LocalDateTime.now().minusHours(1));

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, requester);
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);

		Long newPrice = 70000L;
		ServiceEstimateUpdateRequestForDirector request = ServiceEstimateUpdateRequestForDirector.builder()
			.price(newPrice)
			.scheduledAt(formatToDateString(newScheduledAt))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/directors/service-estimates/{serviceEstimateId}",
						estimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		ServiceEstimate updated = serviceEstimateProvider.findById(estimate.getId());
		assertThat(updated.getPrice()).isEqualTo(newPrice);
		assertThat(updated.getScheduledAt()).isEqualTo(newScheduledAt);
		assertThat(updated.getReminderStatus()).isEqualTo(ServiceEstimateReminderStatus.PENDING);
		assertThat(updated.getReminderSentAt()).isNull();

		// confirmed wishTime 시간값 업데이트 검증
		List<ServiceRequestWishTime> wishTimes = serviceRequestWishTimeProvider.findAllByServiceRequest(
			serviceRequest);
		assertThat(wishTimes).anyMatch(wt -> Boolean.TRUE.equals(wt.getIsConfirmed())
			&& wt.getWishTime().equals(newScheduledAt));
	}

	@Test
	@DisplayName("제안 수정 API - price와 scheduledAt이 모두 동일하면 no-op으로 204를 반환하며 채팅 메시지가 없다")
	void updateEstimate_noChange_returnsNoContentWithoutChatMessage() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now());

		ServiceEstimate estimate = serviceEstimateProvider.saveOngoing(directorInfo, serviceRequest,
			LocalDateTime.now(), DEFAULT_WISH_DATE_TIME_1);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, requester);
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);

		// 기존 값과 동일한 요청 (price=10000L, scheduledAt=DEFAULT_WISH_DATE_TIME_1)
		ServiceEstimateUpdateRequestForDirector request = ServiceEstimateUpdateRequestForDirector.builder()
			.price(10000L)
			.scheduledAt(formatToDateString(DEFAULT_WISH_DATE_TIME_1))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/directors/service-estimates/{serviceEstimateId}",
						estimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		// then: 채팅 메시지 없음
		List<ChatMessage> chatMessages = chatMessageProvider.findAll();
		assertThat(chatMessages).noneMatch(msg -> msg.getMessageType() == ChatMessageType.ESTIMATE_UPDATED);
	}

	@Test
	@DisplayName("제안 수정 API - DIRECTOR_DONE 상태 제안은 수정할 수 없다")
	void updateEstimate_directorDone_fails() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now());

		ServiceEstimate estimate = serviceEstimateProvider.saveDirectorDone(directorInfo, serviceRequest,
			LocalDateTime.now());

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, requester);
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);

		ServiceEstimateUpdateRequestForDirector request = ServiceEstimateUpdateRequestForDirector.builder()
			.price(50000L)
			.scheduledAt(formatToDateString(DEFAULT_WISH_DATE_TIME_1))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/directors/service-estimates/{serviceEstimateId}",
						estimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceEstimateException.NOT_ONGOING_STATUS.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(
				ServiceEstimateException.NOT_ONGOING_STATUS.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(
				ServiceEstimateException.NOT_ONGOING_STATUS.getCode()));
	}

	@Test
	@DisplayName("제안 수정 API - 유효하지 않은 TimeSlot 시간으로 수정하면 실패한다")
	void updateEstimate_invalidTimeSlot_fails() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now());

		ServiceEstimate estimate = serviceEstimateProvider.saveOngoing(directorInfo, serviceRequest,
			LocalDateTime.now(), DEFAULT_WISH_DATE_TIME_1);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, requester);
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);

		// 유효하지 않은 TimeSlot (09:15 → 30분 단위가 아님)
		LocalDateTime invalidTimeSlot = DEFAULT_WISH_DATE_TIME_1.withHour(9).withMinute(15);
		ServiceEstimateUpdateRequestForDirector request = ServiceEstimateUpdateRequestForDirector.builder()
			.price(50000L)
			.scheduledAt(formatToDateString(invalidTimeSlot))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/directors/service-estimates/{serviceEstimateId}",
						estimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				TimeSlotException.INVALID_WISH_TIME_SLOT.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(
				TimeSlotException.INVALID_WISH_TIME_SLOT.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(
				TimeSlotException.INVALID_WISH_TIME_SLOT.getCode()));
	}

	@Test
	@DisplayName("제안 수정 API - 같은 시간대에 다른 ONGOING 제안이 존재하면 더블부킹으로 실패한다")
	void updateEstimate_doubleBooking_fails() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Member requester1 = memberProvider.saveMember(SignInPlatform.APPLE);
		Member requester2 = memberProvider.saveMember(SignInPlatform.APPLE);

		// 수정 대상 제안 (ONGOING)
		ServiceRequest serviceRequest1 = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester1,
			LocalDateTime.now());
		ServiceEstimate estimate = serviceEstimateProvider.saveOngoing(directorInfo,
			serviceRequest1, LocalDateTime.now(), DEFAULT_WISH_DATE_TIME_1);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, requester1);
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);

		// 다른 ONGOING 제안 (DEFAULT_WISH_DATE_TIME_2 시간대에 예약)
		ServiceRequest serviceRequest2 = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester2,
			LocalDateTime.now());
		serviceEstimateProvider.saveOngoing(directorInfo, serviceRequest2, LocalDateTime.now(),
			DEFAULT_WISH_DATE_TIME_2);

		// DEFAULT_WISH_DATE_TIME_2로 수정 시도 → 더블부킹
		ServiceEstimateUpdateRequestForDirector request = ServiceEstimateUpdateRequestForDirector.builder()
			.price(50000L)
			.scheduledAt(formatToDateString(DEFAULT_WISH_DATE_TIME_2))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/directors/service-estimates/{serviceEstimateId}",
						estimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceEstimateException.DIRECTOR_ALREADY_BOOKED_AT_TIME.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(
				ServiceEstimateException.DIRECTOR_ALREADY_BOOKED_AT_TIME.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(
				ServiceEstimateException.DIRECTOR_ALREADY_BOOKED_AT_TIME.getCode()));
	}

	@Test
	@DisplayName("제안 수정 API - 자기 제안의 기존 시간대로 수정하면 자기 제외 로직으로 더블부킹이 아닌 것으로 통과한다")
	void updateEstimate_selfExclude_success() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now());

		// DEFAULT_WISH_DATE_TIME_1에 scheduledAt이 설정된 제안 (ONGOING)
		ServiceEstimate estimate = serviceEstimateProvider.saveOngoing(directorInfo,
			serviceRequest, LocalDateTime.now(), DEFAULT_WISH_DATE_TIME_1);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, requester);
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);

		// 같은 시간대(DEFAULT_WISH_DATE_TIME_1)로 수정 → 자기 제외이므로 통과
		Long newPrice = 80000L;
		ServiceEstimateUpdateRequestForDirector request = ServiceEstimateUpdateRequestForDirector.builder()
			.price(newPrice)
			.scheduledAt(formatToDateString(DEFAULT_WISH_DATE_TIME_1))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/directors/service-estimates/{serviceEstimateId}",
						estimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		ServiceEstimate updated = serviceEstimateProvider.findById(estimate.getId());
		assertThat(updated.getPrice()).isEqualTo(newPrice);
		assertThat(updated.getScheduledAt()).isEqualTo(DEFAULT_WISH_DATE_TIME_1);
	}

	@Test
	@DisplayName("제안 수정 API - price만 변경되어도 멤버에게 예약 변경 Push가 전송된다")
	void updateEstimate_priceOnly_sendsPush() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now());

		ServiceEstimate estimate = serviceEstimateProvider.saveOngoing(directorInfo, serviceRequest,
			LocalDateTime.now(), DEFAULT_WISH_DATE_TIME_1);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, requester);
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);

		// 활동성 푸시 동의 설정 (기본값 false → true)
		entityManager.createQuery("UPDATE Member m SET m.isActivityPushAgreed = true WHERE m.id = :id")
			.setParameter("id", requester.getId())
			.executeUpdate();

		// scheduledAt은 동일, price만 변경
		ServiceEstimateUpdateRequestForDirector request = ServiceEstimateUpdateRequestForDirector.builder()
			.price(99000L)
			.scheduledAt(formatToDateString(DEFAULT_WISH_DATE_TIME_1))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/directors/service-estimates/{serviceEstimateId}",
						estimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		// then: price만 변경되어도 Push가 전송된다
		ArgumentCaptor<FirebasePushEvent> eventCaptor = ArgumentCaptor.forClass(FirebasePushEvent.class);
		verify(eventPublisher, atLeastOnce()).publish(eventCaptor.capture());

		List<FirebasePushEvent> capturedEvents = eventCaptor.getAllValues();
		assertThat(capturedEvents).anyMatch(
			e -> e.getCampaignSpec() == FirebaseCampaignSpec.PUSH_MEMBER_ESTIMATE_SCHEDULE_CHANGED
		);
	}

	@Test
	@DisplayName("제안 수정 API - scheduledAt 변경 시 멤버에게 예약 변경 Push가 전송되며 senderName/receiverName이 정확하게 설정된다")
	void updateEstimate_scheduledAtChanged_sendsPushWithCorrectNames() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now());

		ServiceEstimate estimate = serviceEstimateProvider.saveOngoing(directorInfo, serviceRequest,
			LocalDateTime.now(), DEFAULT_WISH_DATE_TIME_1);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, requester);
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);

		// 활동성 푸시 동의 설정 (기본값 false → true)
		entityManager.createQuery("UPDATE Member m SET m.isActivityPushAgreed = true WHERE m.id = :id")
			.setParameter("id", requester.getId())
			.executeUpdate();

		// price는 동일, scheduledAt만 변경
		ServiceEstimateUpdateRequestForDirector request = ServiceEstimateUpdateRequestForDirector.builder()
			.price(10000L)
			.scheduledAt(formatToDateString(DEFAULT_WISH_DATE_TIME_2))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/directors/service-estimates/{serviceEstimateId}",
						estimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		// then: PUSH_MEMBER_ESTIMATE_SCHEDULE_CHANGED 이벤트가 발행됐으며 senderName, receiverName이 정확하다
		ArgumentCaptor<FirebasePushEvent> eventCaptor = ArgumentCaptor.forClass(FirebasePushEvent.class);
		verify(eventPublisher, atLeastOnce()).publish(eventCaptor.capture());

		List<FirebasePushEvent> capturedEvents = eventCaptor.getAllValues();
		FirebasePushEvent pushEvent = capturedEvents.stream()
			.filter(e -> e.getCampaignSpec() == FirebaseCampaignSpec.PUSH_MEMBER_ESTIMATE_SCHEDULE_CHANGED)
			.findFirst()
			.orElseThrow(() -> new AssertionError("PUSH_MEMBER_ESTIMATE_SCHEDULE_CHANGED 이벤트가 발행되지 않았습니다."));

		assertThat(pushEvent.getVariables().get(SENDER_NAME)).isEqualTo(director.getNickname());
		assertThat(pushEvent.getVariables().get(RECEIVER_NAME)).isEqualTo(requester.getNickname());
		assertThat(pushEvent.getReceiverIds()).contains(requester.getId());
	}

	@Test
	@DisplayName("제안 수정 API - 다른 디렉터의 제안을 수정하면 소유권 검증에 실패한다")
	void updateEstimate_notOwnedEstimate_fails() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		// 다른 디렉터
		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now().plusMonths(1));
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, otherDirectorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);
		directorServiceMappingProvider.save(otherDirectorInfo, directorService);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, requester);

		// 다른 디렉터의 제안
		ServiceEstimate estimate = serviceEstimateProvider.saveWithScheduledAt(otherDirectorInfo,
			serviceRequest, DEFAULT_WISH_DATE_TIME_1);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, otherDirector);
		chatRoomMemberProvider.saveMember(chatRoom, requester);
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);

		ServiceEstimateUpdateRequestForDirector request = ServiceEstimateUpdateRequestForDirector.builder()
			.price(50000L)
			.scheduledAt(formatToDateString(DEFAULT_WISH_DATE_TIME_2))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/directors/service-estimates/{serviceEstimateId}",
						estimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceEstimateException.NOT_OWNED_BY.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(
				ServiceEstimateException.NOT_OWNED_BY.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(
				ServiceEstimateException.NOT_OWNED_BY.getCode()));
	}

}
