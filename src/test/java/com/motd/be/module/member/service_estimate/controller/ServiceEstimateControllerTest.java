package com.motd.be.module.member.service_estimate.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.Constants.ERROR_CODE;
import static com.motd.be.Constants.ERROR_MESSAGE;
import static com.motd.be.Constants.ERROR_STATUS;
import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.ServiceEstimateException;
import com.motd.be.exception.exceptions.ServiceRequestException;
import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.chat_message.entity.ChatMessageType;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.Role;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.notification.entity.Notification;
import com.motd.be.module.member.notification.entity.NotificationReceiverType;
import com.motd.be.module.member.notification.entity.NotificationType;
import com.motd.be.module.member.service_estimate.dto.response.ServiceEstimateFindAllResponse;
import com.motd.be.module.member.service_estimate.dto.response.ServiceEstimateFindDetailResponse;
import com.motd.be.module.member.service_estimate.dto.response.ServiceEstimateHistoriesResponse;
import com.motd.be.module.member.service_estimate.dto.response.ServiceEstimateResponseWithStatusAndMember;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request_wish_time.entity.ServiceRequestWishTime;
import com.motd.be.module.member.sse.SseEventType;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class ServiceEstimateControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("회원은 자신의 요청에 대한 모든 제안을 조회할 수 있다. (요청가 pending일 때 취소되지 않은 모든 제안 반환)")
	void findAllByRequestId_whenRequestIsPending_returnsAllNonCanceledEstimates() throws Exception {
		// given
		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo2);

		DirectorInfo directorInfo3 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director3 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, directorInfo3);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest pendingServiceRequest = serviceRequestProvider.savePending(directorService, requester);
		ServiceRequest ongoingServiceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now());
		ServiceRequest completedServiceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService,
			requester, LocalDateTime.now());
		ServiceRequest canceledServiceRequest = serviceRequestProvider.saveWithIsCanceledTrue(directorService,
			requester, LocalDateTime.now());

		// estimates: one pending, one ongoing, one completed, one canceled
		ServiceEstimate pending1 = serviceEstimateProvider.save(director1.getDirectorInfo(), pendingServiceRequest);
		ServiceEstimate pending2 = serviceEstimateProvider.save(director2.getDirectorInfo(), pendingServiceRequest);
		ServiceEstimate canceled = serviceEstimateProvider.saveCanceled(director3.getDirectorInfo(),
			pendingServiceRequest, LocalDateTime.now());

		// chatRoom 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		// chatRoomServiceEstimateMapping 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom, pending1);
		chatRoomServiceEstimateMappingProvider.save(chatRoom, pending2);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(SERVICE_REQUEST_ID, String.valueOf(pendingServiceRequest.getId()))
				.param(PAGE_STR, ZERO_STR)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ServiceEstimateFindAllResponse response = objectMapper.readValue(responseJson,
			ServiceEstimateFindAllResponse.class);

		// then: canceled 제외한 3개가 반환되어야 함
		List<Long> ids = response.getServiceEstimates()
			.stream()
			.map(ServiceEstimateResponseWithStatusAndMember::getId)
			.collect(Collectors.toList());

		assertThat(response.getServiceEstimates()).hasSize(2);
		assertThat(ids).containsExactly(pending2.getId(), pending1.getId());
	}

	@Test
	@DisplayName("회원은 자신의 요청에 대한 진행중 제안만 조회할 수 있다. (요청가 ongoing일 때)")
	void findAllByRequestId_whenRequestIsOngoing_returnsOnlyOngoingEstimates() throws Exception {
		// given
		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo2);

		DirectorInfo directorInfo3 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director3 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, directorInfo3);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest pendingServiceRequest = serviceRequestProvider.savePending(directorService, requester);
		ServiceRequest ongoingServiceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now());
		ServiceRequest completedServiceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService,
			requester, LocalDateTime.now());
		ServiceRequest canceledServiceRequest = serviceRequestProvider.saveWithIsCanceledTrue(directorService,
			requester, LocalDateTime.now());

		// estimates: one pending, one ongoing, one completed, one canceled
		ServiceEstimate pending1 = serviceEstimateProvider.save(director1.getDirectorInfo(), ongoingServiceRequest);
		ServiceEstimate ongoing = serviceEstimateProvider.saveOngoing(director2.getDirectorInfo(),
			ongoingServiceRequest, LocalDateTime.now());
		ServiceEstimate canceled = serviceEstimateProvider.save(director3.getDirectorInfo(), ongoingServiceRequest);

		// chatRoom 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		// chatRoomServiceEstimateMapping 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom, ongoing);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(SERVICE_REQUEST_ID, String.valueOf(ongoingServiceRequest.getId()))
				.param(PAGE_STR, ZERO_STR)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ServiceEstimateFindAllResponse response = objectMapper.readValue(responseJson,
			ServiceEstimateFindAllResponse.class);

		// then: ongoing만 반환 (repo method returns only isOngoing true)
		List<Long> ids = response.getServiceEstimates()
			.stream()
			.map(ServiceEstimateResponseWithStatusAndMember::getId)
			.collect(Collectors.toList());

		assertThat(response.getServiceEstimates()).hasSize(1);
		assertThat(ids).containsExactly(ongoing.getId());
	}

	@Test
	@DisplayName("회원은 자신의 요청에 대한 진행중 제안만 조회할 수 있다. (요청가 expired일 때)")
	void findAllByRequestId_whenRequestIsExpired() throws Exception {
		// given
		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo2);

		DirectorInfo directorInfo3 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director3 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, directorInfo3);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest pendingServiceRequest = serviceRequestProvider.savePending(directorService, requester);
		ServiceRequest ongoingServiceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now());
		ServiceRequest completedServiceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService,
			requester, LocalDateTime.now());
		ServiceRequest canceledServiceRequest = serviceRequestProvider.saveWithIsCanceledTrue(directorService,
			requester, LocalDateTime.now());
		ServiceRequest expiredServiceRequest = serviceRequestProvider.saveWithIsExpiredTrue(directorService,
			requester, LocalDateTime.now());

		// estimates: one pending, one ongoing, one completed, one canceled
		ServiceEstimate expiredEstimate = serviceEstimateProvider.saveExpired(director1.getDirectorInfo(),
			expiredServiceRequest, LocalDateTime.now());
		ServiceEstimate pending2 = serviceEstimateProvider.save(director2.getDirectorInfo(), expiredServiceRequest);
		ServiceEstimate pending3 = serviceEstimateProvider.save(director3.getDirectorInfo(), expiredServiceRequest);

		// chatRoom 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		// chatRoomServiceEstimateMapping 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom, expiredEstimate);
		chatRoomServiceEstimateMappingProvider.save(chatRoom, pending2);
		chatRoomServiceEstimateMappingProvider.save(chatRoom, pending3);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(SERVICE_REQUEST_ID, String.valueOf(expiredServiceRequest.getId()))
				.param(PAGE_STR, ZERO_STR)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ServiceEstimateFindAllResponse response = objectMapper.readValue(responseJson,
			ServiceEstimateFindAllResponse.class);

		// then: expired만 반환
		List<Long> ids = response.getServiceEstimates()
			.stream()
			.map(ServiceEstimateResponseWithStatusAndMember::getId)
			.toList();

		assertThat(ids).containsExactly(pending3.getId(), pending2.getId(), expiredEstimate.getId());

		assertThat(response.getServiceEstimates()).hasSize(3);
	}

	@Test
	@DisplayName("회원은 자신의 요청에 대한 진행중 제안만 조회할 수 있다. (요청가 ongoing이며, 제안이 디렉터에 의하여 완료되었을 때)")
	void findAllByRequestId_whenRequestIsCompletedByDirector() throws Exception {
		// given
		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo2);

		DirectorInfo directorInfo3 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director3 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, directorInfo3);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest pendingServiceRequest = serviceRequestProvider.savePending(directorService, requester);
		ServiceRequest ongoingServiceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now());
		ServiceRequest ongoingServiceRequest1 = serviceRequestProvider.saveWithIsOngoingTrue(directorService,
			requester, LocalDateTime.now());
		ServiceRequest canceledServiceRequest = serviceRequestProvider.saveWithIsCanceledTrue(directorService,
			requester, LocalDateTime.now());
		ServiceRequest expiredServiceRequest = serviceRequestProvider.saveWithIsExpiredTrue(directorService,
			requester, LocalDateTime.now());

		// estimates: one pending, one ongoing, one completed, one canceled
		ServiceEstimate pending1 = serviceEstimateProvider.saveCanceled(director1.getDirectorInfo(),
			ongoingServiceRequest1, LocalDateTime.now());
		ServiceEstimate pending2 = serviceEstimateProvider.saveCanceled(director2.getDirectorInfo(),
			ongoingServiceRequest1, LocalDateTime.now());
		ServiceEstimate directorDoneEstimate = serviceEstimateProvider.saveDirectorDone(director3.getDirectorInfo(),
			ongoingServiceRequest1, LocalDateTime.now());

		// chatRoom 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		// chatRoomServiceEstimateMapping 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom, directorDoneEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(SERVICE_REQUEST_ID, String.valueOf(ongoingServiceRequest1.getId()))
				.param(PAGE_STR, ZERO_STR)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ServiceEstimateFindAllResponse response = objectMapper.readValue(responseJson,
			ServiceEstimateFindAllResponse.class);

		// then: ongoing 반환
		List<Long> ids = response.getServiceEstimates()
			.stream()
			.map(ServiceEstimateResponseWithStatusAndMember::getId)
			.toList();

		assertThat(ids.contains(directorDoneEstimate.getId())).isTrue();

		assertThat(response.getServiceEstimates()).hasSize(1);
	}

	@Test
	@DisplayName("회원은 자신의 요청에 대한 진행중 제안만 조회할 수 있다. (요청가 completed이며, 제안이 회원 의하여 완료되었을 때)")
	void findAllByRequestId_whenRequestIsCompletedByMember() throws Exception {
		// given
		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo2);

		DirectorInfo directorInfo3 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director3 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, directorInfo3);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest pendingServiceRequest = serviceRequestProvider.savePending(directorService, requester);
		ServiceRequest ongoingServiceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now());
		ServiceRequest completedServiceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService,
			requester, LocalDateTime.now());
		ServiceRequest canceledServiceRequest = serviceRequestProvider.saveWithIsCanceledTrue(directorService,
			requester, LocalDateTime.now());
		ServiceRequest expiredServiceRequest = serviceRequestProvider.saveWithIsExpiredTrue(directorService,
			requester, LocalDateTime.now());

		// estimates: one pending, one ongoing, one completed, one canceled
		ServiceEstimate pending1 = serviceEstimateProvider.saveCanceled(director1.getDirectorInfo(),
			completedServiceRequest, LocalDateTime.now());
		ServiceEstimate pending2 = serviceEstimateProvider.saveCanceled(director2.getDirectorInfo(),
			completedServiceRequest, LocalDateTime.now());
		ServiceEstimate completedEstimate = serviceEstimateProvider.saveMemberCompleted(director3.getDirectorInfo(),
			completedServiceRequest, LocalDateTime.now());

		// chatRoom 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		// chatRoomServiceEstimateMapping 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom, completedEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(SERVICE_REQUEST_ID, String.valueOf(completedServiceRequest.getId()))
				.param(PAGE_STR, ZERO_STR)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ServiceEstimateFindAllResponse response = objectMapper.readValue(responseJson,
			ServiceEstimateFindAllResponse.class);

		// then: completed 반환
		List<Long> ids = response.getServiceEstimates()
			.stream()
			.map(ServiceEstimateResponseWithStatusAndMember::getId)
			.toList();

		assertThat(ids.contains(completedEstimate.getId())).isTrue();

		assertThat(response.getServiceEstimates()).hasSize(1);
	}

	@Test
	@DisplayName("회원은 자신의 요청에 대한 진행중 제안만 조회할 수 있다. (요청가 completed이며, 제안이 리뷰에 의하여 완료되었을 때)")
	void findAllByRequestId_whenRequestWithReviewCompletedStatus() throws Exception {
		// given
		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo2);

		DirectorInfo directorInfo3 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director3 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, directorInfo3);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest pendingServiceRequest = serviceRequestProvider.savePending(directorService, requester);
		ServiceRequest ongoingServiceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester,
			LocalDateTime.now());
		ServiceRequest completedServiceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService,
			requester, LocalDateTime.now());
		ServiceRequest canceledServiceRequest = serviceRequestProvider.saveWithIsCanceledTrue(directorService,
			requester, LocalDateTime.now());
		ServiceRequest expiredServiceRequest = serviceRequestProvider.saveWithIsExpiredTrue(directorService,
			requester, LocalDateTime.now());

		// estimates: one pending, one ongoing, one completed, one canceled
		ServiceEstimate pending1 = serviceEstimateProvider.saveCanceled(director1.getDirectorInfo(),
			completedServiceRequest, LocalDateTime.now());
		ServiceEstimate pending2 = serviceEstimateProvider.saveCanceled(director2.getDirectorInfo(),
			completedServiceRequest, LocalDateTime.now());
		ServiceEstimate reviewCompletedEstimate = serviceEstimateProvider.saveReviewCompleted(
			director3.getDirectorInfo(),
			completedServiceRequest, LocalDateTime.now());

		// chatRoom 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		// chatRoomServiceEstimateMapping 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom, reviewCompletedEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(SERVICE_REQUEST_ID, String.valueOf(completedServiceRequest.getId()))
				.param(PAGE_STR, ZERO_STR)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ServiceEstimateFindAllResponse response = objectMapper.readValue(responseJson,
			ServiceEstimateFindAllResponse.class);

		// then: completed 반환
		List<Long> ids = response.getServiceEstimates()
			.stream()
			.map(ServiceEstimateResponseWithStatusAndMember::getId)
			.toList();

		assertThat(ids.contains(reviewCompletedEstimate.getId())).isTrue();

		assertThat(response.getServiceEstimates()).hasSize(1);
	}

	@Test
	@DisplayName("회원은 자신의 요청에 대한 진행중 제안만 조회할 수 있다. (자기 자신의 요청로 조회하는 경우가 아닌경우)")
	void findAllByRequestId_notOwnedBy() throws Exception {
		// given
		Member owner = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member other = memberProvider.saveMember(SignInPlatform.APPLE);

		Jwt jwt = generateTokenWithMemberIdRoleMember(other.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, owner);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.get("/api/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(SERVICE_REQUEST_ID, String.valueOf(serviceRequest.getId()))
				.param(PAGE_STR, ZERO_STR)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(ServiceRequestException.NOT_OWNED_BY.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceRequestException.NOT_OWNED_BY.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceRequestException.NOT_OWNED_BY.getCode()));
	}

	@Test
	@DisplayName("존재하지 않는 요청에 대한 조회시 예외가 발생한다.")
	void findAllByRequestId_notFoundServiceRequest() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.get("/api/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(SERVICE_REQUEST_ID, String.valueOf(99999999L))
				.param(PAGE_STR, ZERO_STR)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath(ERROR_STATUS).value(ServiceRequestException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceRequestException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceRequestException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("회원은 자신의 제안 상세를 조회할 수 있다.")
	void findDetail_whenOwned_returnsDetail() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithCompletedCount(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			0);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, requester);
		ServiceRequest completedRequest1 = serviceRequestProvider.saveWithIsCompletedTrue(directorService, requester,
			LocalDateTime.now());
		ServiceRequest completedRequest2 = serviceRequestProvider.saveWithIsCompletedTrue(directorService, requester,
			LocalDateTime.now());
		ServiceRequest completedRequest3 = serviceRequestProvider.saveWithIsCompletedTrue(directorService, requester,
			LocalDateTime.now());

		ServiceEstimate estimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		// completed estimates for counting
		serviceEstimateProvider.saveMemberCompleted(directorInfo, completedRequest1, LocalDateTime.now());
		serviceEstimateProvider.saveDirectorDone(directorInfo, completedRequest2, LocalDateTime.now());
		serviceEstimateProvider.saveReviewCompleted(directorInfo, completedRequest3, LocalDateTime.now());

		director.getDirectorInfo().incrementCompletedEstimateCount();
		director.getDirectorInfo().incrementCompletedEstimateCount();
		director.getDirectorInfo().incrementCompletedEstimateCount();

		// chatRoom 저장
		ChatRoom chatRoom = chatRoomProvider.save();

		// chatRoomServiceEstimateMapping 저장
		chatRoomServiceEstimateMappingProvider.save(chatRoom, estimate);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/service-estimates/{serviceEstimateId}", estimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ServiceEstimateFindDetailResponse response = objectMapper.readValue(responseJson,
			ServiceEstimateFindDetailResponse.class);

		// then
		assertThat(response.getId()).isEqualTo(estimate.getId());
		assertThat(response.getPrice()).isEqualTo(estimate.getPrice());
		assertThat(response.getMember().getId()).isEqualTo(director.getId());
		assertThat(response.getMember().getCompletedEstimateCount()).isEqualTo(3);
	}

	@Test
	@DisplayName("회원은 소유하지 않은 제안 상세를 조회할 수 없다. (FORBIDDEN)")
	void findDetail_notOwnedBy() throws Exception {
		// given
		Member owner = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member other = memberProvider.saveMember(SignInPlatform.APPLE);

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Jwt jwt = generateTokenWithMemberIdRoleMember(other.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, owner);

		ServiceEstimate estimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.get("/api/service-estimates/{serviceEstimateId}", estimate.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(ServiceRequestException.NOT_OWNED_BY.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceRequestException.NOT_OWNED_BY.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceRequestException.NOT_OWNED_BY.getCode()));
	}

	@Test
	@DisplayName("존재하지 않는 제안 상세 조회 시 예외 발생 (NOT_FOUND)")
	void findDetail_notFoundEstimate() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.get("/api/service-estimates/{serviceEstimateId}", 99999999L)
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath(ERROR_STATUS).value(ServiceEstimateException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceEstimateException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("회원은 디렉터가 작업완료한 제안을 완료 처리할 수 있다.")
	void complete_whenOngoing_success() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest completedRequest = serviceRequestProvider.saveWithIsCompletedTrue(
			directorService, requester, LocalDateTime.now());

		ServiceEstimate directorCompletedEstimate = serviceEstimateProvider.saveDirectorDone(
			directorInfo, completedRequest, LocalDateTime.now());

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, requester);

		chatRoomServiceEstimateMappingProvider.save(chatRoom, directorCompletedEstimate);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.patch(
					"/api/service-estimates/{serviceEstimateId}/complete", directorCompletedEstimate.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

		// verify
		ServiceEstimate updatedEstimate = serviceEstimateProvider.findById(directorCompletedEstimate.getId());
		assertThat(updatedEstimate.getMemberCompletedAt()).isNotNull();

		// 채팅 메세지 저장되었는지 확인
		List<ChatMessage> chatMessages = chatMessageProvider.findAll();
		assertThat(chatMessages).hasSize(1);

		assertThat(chatMessages.get(0).getMessageType()).isEqualTo(ChatMessageType.ESTIMATE_COMPLETED_BY_MEMBER);

		// notification 이 저장되었는지 확인
		List<Notification> notifications = notificationProvider.findAll();

		assertThat(notifications).hasSize(1);
		assertThat(notifications.get(0).getReceiverType()).isEqualTo(NotificationReceiverType.DIRECTOR);
		assertThat(notifications.get(0).getType()).isEqualTo(NotificationType.TRANSACTION_CONFIRMED);

		// SSE 발행 검증 (REFRESH_CHAT_ROOM_LIST)
		verify(sseService, atLeastOnce()).refreshChatRoomList(
			argThat(payload ->
				payload.getEventName() == SseEventType.REFRESH_CHAT_ROOM_LIST &&
					Objects.equals(payload.getReceiverId(), director.getId()) &&
					Objects.equals(payload.getReceiverRole(), Role.DIRECTOR)));

		// SSE 발행 검증 (REFRESH_NAV_CHAT_COUNT)
		verify(sseService, atLeastOnce()).refreshNavChatCount(
			argThat(payload ->
				payload.getEventName() == SseEventType.REFRESH_NAV_CHAT_COUNT &&
					payload.getReceiverId().equals(director.getId()) &&
					Objects.equals(payload.getReceiverRole(), Role.DIRECTOR)));

		// SSE 발행 검증 (REFRESH_NOTIFICATION_COUNT)
		verify(sseService, atLeastOnce()).refreshNotificationCount(
			argThat(payload ->
				payload.getEventName() == SseEventType.REFRESH_NOTIFICATION_COUNT &&
					Objects.equals(payload.getReceiverId(), director.getId()) &&
					Objects.equals(payload.getReceiverRole(), Role.DIRECTOR)));
	}

	@Test
	@DisplayName("디렉터가 작업완료 하지 않은 제안은 완료 처리할 수 없다. (PENDING 상태)")
	void complete_whenNotOngoing_throwsException() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest pendingServiceRequest = serviceRequestProvider.savePending(directorService, requester);

		ServiceEstimate pendingEstimate = serviceEstimateProvider.save(directorInfo, pendingServiceRequest);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.patch(
					"/api/service-estimates/{serviceEstimateId}/complete", pendingEstimate.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(
					ServiceEstimateException.NOT_DIRECTOR_COMPLETED_STATUS.getHttpStatus().toString()))
			.andExpect(
				jsonPath(ERROR_MESSAGE).value(ServiceEstimateException.NOT_DIRECTOR_COMPLETED_STATUS.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateException.NOT_DIRECTOR_COMPLETED_STATUS.getCode()));
	}

	@Test
	@DisplayName("소유하지 않은 제안은 완료 처리할 수 없다.")
	void complete_notOwnedBy_throwsException() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Member owner = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member other = memberProvider.saveMember(SignInPlatform.GOOGLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(other.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest ongoingServiceRequest = serviceRequestProvider.saveWithIsOngoingTrue(
			directorService, owner, LocalDateTime.now());

		ServiceEstimate directorCompletedEstimate = serviceEstimateProvider.saveDirectorDone(
			directorInfo, ongoingServiceRequest, LocalDateTime.now());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.patch(
					"/api/service-estimates/{serviceEstimateId}/complete", directorCompletedEstimate.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(ServiceRequestException.NOT_OWNED_BY.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceRequestException.NOT_OWNED_BY.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceRequestException.NOT_OWNED_BY.getCode()));
	}

	@Test
	@DisplayName("존재하지 않는 제안은 완료 처리할 수 없다.")
	void complete_notFoundEstimate_throwsException() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.patch(
					"/api/service-estimates/{serviceEstimateId}/complete", 99999999L)
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath(ERROR_STATUS).value(ServiceEstimateException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceEstimateException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("완료된 제안은 다시 완료 처리할 수 없다.")
	void complete_alreadyCompleted_throwsException() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest completedServiceRequest = serviceRequestProvider.saveWithIsCompletedTrue(
			directorService, requester, LocalDateTime.now());

		ServiceEstimate completedEstimate = serviceEstimateProvider.saveMemberCompleted(
			directorInfo, completedServiceRequest, LocalDateTime.now());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.patch(
					"/api/service-estimates/{serviceEstimateId}/complete", completedEstimate.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(
					ServiceEstimateException.ALREADY_MEMBER_COMPLETED_STATUS.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(
				ServiceEstimateException.ALREADY_MEMBER_COMPLETED_STATUS.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateException.ALREADY_MEMBER_COMPLETED_STATUS.getCode()));
	}

	@Test
	@DisplayName("일반 회원은 서비스 진행 내역을 조회할 수 있다. (정상)")
	void findServiceEstimateHistoriesWithPaging() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest1 = serviceRequestProvider.saveWithIsCompletedTrue(directorService, member,
			LocalDateTime.now().minusDays(1));
		ServiceEstimate serviceEstimate1 = serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest1,
			LocalDateTime.now().minusDays(1));

		ServiceRequest serviceRequest2 = serviceRequestProvider.saveWithIsOngoingTrue(directorService, member,
			LocalDateTime.now().minusDays(2));
		ServiceEstimate serviceEstimate2 = serviceEstimateProvider.saveDirectorDone(directorInfo, serviceRequest2,
			LocalDateTime.now().minusDays(2));

		// 15개의 완료된 제안 생성
		for (int i = 0; i < 15; i++) {
			ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, member,
				LocalDateTime.now().minusDays(i));
			ServiceEstimate serviceEstimate = serviceEstimateProvider.saveMemberCompleted(directorInfo, serviceRequest,
				LocalDateTime.now().minusDays(i));
		}

		entityManager.flush();
		entityManager.clear();

		// when & then
		String firstPageJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/members/service-estimate/histories")
					.param("page", "0")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ServiceEstimateHistoriesResponse firstPage = objectMapper.readValue(firstPageJson,
			ServiceEstimateHistoriesResponse.class);

		assertThat(firstPage.getPage()).isEqualTo(0);
		assertThat(firstPage.getHasNext()).isFalse();
		assertThat(firstPage.getHistories()).hasSize(16);
	}

	@Test
	@DisplayName("일반 회원은 서비스 진행 내역을 조회할 수 있다. (내역이 없는 경우)")
	void findServiceEstimateHistoriesWhenEmpty() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/members/service-estimate/histories")
					.param("page", "0")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ServiceEstimateHistoriesResponse response = objectMapper.readValue(responseJson,
			ServiceEstimateHistoriesResponse.class);

		// then
		assertThat(response.getPage()).isEqualTo(0);
		assertThat(response.getHasNext()).isFalse();
		assertThat(response.getHistories()).isEmpty();
	}

	@Test
	@DisplayName("제안 수락 시 scheduledAt에 해당하는 wishTime만 isConfirmed가 true로 변경된다")
	void accept_wishTimeIsConfirmed() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, requester);

		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveWithScheduledAt(
			directorInfo, serviceRequest,
			com.motd.be.provider.module.member.ServiceRequestProvider.DEFAULT_WISH_DATE_TIME_1);

		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, requester);
		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post(
					"/api/service-estimates/{serviceEstimateId}/accept", serviceEstimate.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		List<ServiceRequestWishTime> wishTimes = serviceRequestWishTimeProvider.findAllByServiceRequest(
			serviceRequestProvider.findById(serviceRequest.getId()));

		List<ServiceRequestWishTime> confirmed = wishTimes.stream()
			.filter(wt -> Boolean.TRUE.equals(wt.getIsConfirmed()))
			.toList();

		assertThat(confirmed).hasSize(1);
		assertThat(confirmed.get(0).getWishTime()).isEqualTo(
			com.motd.be.provider.module.member.ServiceRequestProvider.DEFAULT_WISH_DATE_TIME_1);

		List<ServiceRequestWishTime> unconfirmed = wishTimes.stream()
			.filter(wt -> !Boolean.TRUE.equals(wt.getIsConfirmed()))
			.toList();

		assertThat(unconfirmed).hasSize(1);
	}

}
