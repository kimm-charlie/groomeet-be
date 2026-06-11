package com.motd.be.module.member.review.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.TimePolicy.*;
import static com.motd.be.common.constants.ValidationConstants.*;
import static com.motd.be.common.constants.ValidationMessages.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.HandlerException;
import com.motd.be.exception.exceptions.ReviewException;
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
import com.motd.be.module.member.review.dto.request.ReviewSaveAndUpdateRequest;
import com.motd.be.module.member.review.dto.request.ReviewUpdateRequest;
import com.motd.be.module.member.review.dto.response.ReviewFindAllForDirectorResponse;
import com.motd.be.module.member.review.dto.response.ReviewFindAllForMemberResponse;
import com.motd.be.module.member.review.dto.response.ReviewWithReceivedCompletedEstimateCountResponse;
import com.motd.be.module.member.review.entity.Review;
import com.motd.be.module.member.review_file.entity.ReviewFile;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.sse.SseEventType;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class ReviewControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("일반 회원이 리뷰를 작성할 수 있다. (이미지가 존재하지 않는 경우)")
	void saveReview_success() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, member,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveMemberCompleted(directorInfo, serviceRequest,
			LocalDateTime.now());

		// 채팅방 저장
		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, member);

		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		ReviewSaveAndUpdateRequest request = ReviewSaveAndUpdateRequest.builder()
			.content(CONTENT_STR)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/reviews/service-estimates/{serviceEstimateId}",
						serviceEstimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		entityManager.flush();
		entityManager.clear();

		// then
		List<Review> reviews = reviewProvider.findAll();
		assertThat(reviews).hasSize(1);

		Review savedReview = reviews.get(0);
		assertThat(savedReview.getContent()).isEqualTo(CONTENT_STR);
		assertThat(savedReview.getServiceEstimate().getId()).isEqualTo(serviceEstimate.getId());
		assertThat(savedReview.getWriter().getId()).isEqualTo(member.getId());

		// 제안상태 업데이트 여부 검증
		ServiceEstimate updatedEstimate = serviceEstimateProvider.findById(serviceEstimate.getId());
		assertThat(updatedEstimate.getStatus()).isEqualTo(ServiceEstimateStatus.REVIEW_COMPLETED);

		// 디렉터 리뷰갯수 증가 여부 검증
		DirectorInfo updatedDirectorInfo = directorInfoProvider.findById(directorInfo.getId());
		assertThat(updatedDirectorInfo.getReviewCount()).isEqualTo(1);

		// notification 이 저장되었는지 검증
		List<Notification> notifications = notificationProvider.findAll();

		assertThat(notifications).hasSize(1);
		assertThat(notifications.get(0).getReceiverType()).isEqualTo(NotificationReceiverType.DIRECTOR);
		assertThat(notifications.get(0).getType()).isEqualTo(NotificationType.REVIEW_WRITTEN);

		// 리뷰 메세지가 생성되었는지 검증
		List<ChatMessage> chatMessages = chatMessageProvider.findAll();
		assertThat(chatMessages).hasSize(1);
		assertThat(chatMessages.get(0).getMessageType()).isEqualTo(ChatMessageType.REVIEW_COMPLETED);

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
	@DisplayName("일반 회원이 리뷰를 작성할 수 있다. (이미지 가능 갯수 초과일 경우)")
	void saveReview_WithFileLimitExceeded() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, member,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveMemberCompleted(directorInfo, serviceRequest,
			LocalDateTime.now());

		// 채팅방 저장
		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, member);

		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		List<ReviewFile> reviewFiles = new ArrayList<>();
		for (int i = 0; i < REVIEW_MAX_IMAGE_COUNT + 1; i++) {
			ReviewFile reviewFile = reviewFileProvider.save(member);
			reviewFiles.add(reviewFile);
		}

		ReviewSaveAndUpdateRequest request = ReviewSaveAndUpdateRequest.builder()
			.content(CONTENT_STR)
			.fileIds(reviewFiles.stream().map(ReviewFile::getId).toList())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/reviews/service-estimates/{serviceEstimateId}",
						serviceEstimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(REVIEW_FILE_MAX_COUNT_MSG))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("일반 회원이 리뷰를 작성할 수 있다. (이미지가 존재하는 경우)")
	void saveReview_successWithImage() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, member,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveMemberCompleted(directorInfo, serviceRequest,
			LocalDateTime.now());

		ReviewFile reviewFile1 = reviewFileProvider.save(member);
		ReviewFile reviewFile2 = reviewFileProvider.save(member);

		// 채팅방 저장
		ChatRoom chatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(chatRoom, director);
		chatRoomMemberProvider.saveMember(chatRoom, member);

		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		ReviewSaveAndUpdateRequest request = ReviewSaveAndUpdateRequest.builder()
			.content(CONTENT_STR)
			.fileIds(List.of(reviewFile1.getId(), reviewFile2.getId()))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/reviews/service-estimates/{serviceEstimateId}",
						serviceEstimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		entityManager.flush();
		entityManager.clear();

		// then
		List<Review> reviews = reviewProvider.findAll();
		assertThat(reviews).hasSize(1);

		Review savedReview = reviews.get(0);
		assertThat(savedReview.getContent()).isEqualTo(CONTENT_STR);
		assertThat(savedReview.getServiceEstimate().getId()).isEqualTo(serviceEstimate.getId());
		assertThat(savedReview.getWriter().getId()).isEqualTo(member.getId());

		// 제안상태 업데이트 여부 검증
		ServiceEstimate updatedEstimate = serviceEstimateProvider.findById(serviceEstimate.getId());
		assertThat(updatedEstimate.getStatus()).isEqualTo(ServiceEstimateStatus.REVIEW_COMPLETED);

		// 디렉터 리뷰갯수 증가 여부 검증
		DirectorInfo updatedDirectorInfo = directorInfoProvider.findById(directorInfo.getId());
		assertThat(updatedDirectorInfo.getReviewCount()).isEqualTo(1);

		// 이미지 매핑여부 검증
		List<ReviewFile> mappedImages = reviewFileProvider.findAll();
		assertThat(mappedImages).hasSize(2);

		for (ReviewFile image : mappedImages) {
			assertThat(image.getReview().getId()).isEqualTo(savedReview.getId());
		}
	}

	@Test
	@DisplayName("일반 회원이 리뷰를 작성할 수 있다. (제안서가 존재하지 않는 경우)")
	void saveReview_estimateNotFound() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		ReviewSaveAndUpdateRequest request = ReviewSaveAndUpdateRequest.builder()
			.content(CONTENT_STR)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/reviews/service-estimates/{serviceEstimateId}", 99999999L)
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath(ERROR_STATUS).value(ServiceEstimateException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceEstimateException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("일반 회원이 리뷰를 작성할 수 있다. (본인의 제안서가 아닌 경우)")
	void saveReview_notOwnedEstimate() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Member otherMember = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, directorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, otherMember,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveMemberCompleted(directorInfo, serviceRequest,
			LocalDateTime.now());

		ReviewSaveAndUpdateRequest request = ReviewSaveAndUpdateRequest.builder()
			.content(CONTENT_STR)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/reviews/service-estimates/{serviceEstimateId}",
						serviceEstimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(ServiceRequestException.NOT_OWNED_BY.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceRequestException.NOT_OWNED_BY.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceRequestException.NOT_OWNED_BY.getCode()));
	}

	@Test
	@DisplayName("일반 회원이 리뷰를 작성할 수 있다. (제안서가 완료 상태가 아닌 경우)")
	void saveReview_estimateNotCompleted() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, member);
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveDirectorDone(directorInfo, serviceRequest,
			LocalDateTime.now());

		ReviewSaveAndUpdateRequest request = ReviewSaveAndUpdateRequest.builder()
			.content(CONTENT_STR)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/reviews/service-estimates/{serviceEstimateId}",
						serviceEstimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(ReviewException.CANNOT_SAVE_REVIEW.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ReviewException.CANNOT_SAVE_REVIEW.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ReviewException.CANNOT_SAVE_REVIEW.getCode()));
	}

	@Test
	@DisplayName("일반 회원이 리뷰를 작성할 수 있다. (삭제된 리뷰가 존재하는 경우)")
	void saveReview_alreadyReviewedWithDeletedStatusExist() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, member);
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveMemberCompleted(directorInfo, serviceRequest,
			LocalDateTime.now());

		// 기존 리뷰 생성
		reviewProvider.saveWithIsDeletedTrue(member, serviceEstimate);

		ReviewSaveAndUpdateRequest request = ReviewSaveAndUpdateRequest.builder()
			.content(CONTENT_STR)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/reviews/service-estimates/{serviceEstimateId}",
						serviceEstimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(ReviewException.ALREADY_WRITTEN.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ReviewException.ALREADY_WRITTEN.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ReviewException.ALREADY_WRITTEN.getCode()));
	}

	@Test
	@DisplayName("일반 회원이 리뷰를 작성할 수 있다. (이미 작업을 완료한지 7일 이후인 경우 )")
	void saveReview_WhenCompletedAfter7daysLater() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, member);
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveMemberCompleted(directorInfo, serviceRequest,
			LocalDateTime.now().minusDays(REVIEW_WRITE_EXPIRE_DAYS).minusMinutes(1));

		ReviewSaveAndUpdateRequest request = ReviewSaveAndUpdateRequest.builder()
			.content(CONTENT_STR)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/reviews/service-estimates/{serviceEstimateId}",
						serviceEstimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(ReviewException.REVIEW_PERIOD_EXPIRED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ReviewException.REVIEW_PERIOD_EXPIRED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ReviewException.REVIEW_PERIOD_EXPIRED.getCode()));
	}

	@Test
	@DisplayName("일반 회원이 리뷰를 작성할 수 있다. (이미 리뷰가 작성된 경우)")
	void saveReview_alreadyReviewed() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, member,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest,
			LocalDateTime.now());

		// 기존 리뷰 생성
		reviewProvider.save(member, serviceEstimate);

		ReviewSaveAndUpdateRequest request = ReviewSaveAndUpdateRequest.builder()
			.content(CONTENT_STR)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/reviews/service-estimates/{serviceEstimateId}",
						serviceEstimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(ReviewException.CANNOT_SAVE_REVIEW.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ReviewException.CANNOT_SAVE_REVIEW.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ReviewException.CANNOT_SAVE_REVIEW.getCode()));
	}

	@Test
	@DisplayName("일반 회원이 리뷰를 작성할 수 있다. (리뷰 내용이 비어있는 경우)")
	void saveReview_emptyContent() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, member,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest,
			LocalDateTime.now());

		ReviewSaveAndUpdateRequest request = ReviewSaveAndUpdateRequest.builder()
			.content("")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/reviews/service-estimates/{serviceEstimateId}",
						serviceEstimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("일반 회원이 리뷰를 수정할 수 있다. (이미지 변경 없음)")
	void updateReview_successWithoutImageChange() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, member,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest,
			LocalDateTime.now());

		Review review = reviewProvider.save(member, serviceEstimate);
		ReviewFile originalImage = reviewFileProvider.saveWithReview(member, review);

		ReviewUpdateRequest request = ReviewUpdateRequest.builder()
			.content(UPDATED_CONTENT_STR)
			.fileIds(Collections.singletonList(originalImage.getId()))
			.build();

		Long reviewId = review.getId();
		Long originalImageId = originalImage.getId();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/reviews/{reviewId}", reviewId)
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		Review updatedReview = reviewProvider.findAll().stream()
			.filter(it -> it.getId().equals(reviewId))
			.findFirst()
			.orElseThrow();
		assertThat(updatedReview.getContent()).isEqualTo(UPDATED_CONTENT_STR);

		ReviewFile persistedOriginalImage = reviewFileRepository.findById(originalImageId).orElseThrow();
		assertThat(persistedOriginalImage.getIsDeleted()).isFalse();
		assertThat(persistedOriginalImage.getReview().getId()).isEqualTo(reviewId);
	}

	@Test
	@DisplayName("일반 회원이 리뷰를 수정할 수 있다. (이미지 변경)")
	void updateReview_successWithImageChange() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, member,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest,
			LocalDateTime.now());

		Review review = reviewProvider.save(member, serviceEstimate);
		ReviewFile oldImage1 = reviewFileProvider.saveWithReview(member, review);
		ReviewFile oldImage2 = reviewFileProvider.saveWithReview(member, review);

		ReviewFile newImage1 = reviewFileProvider.save(member);
		ReviewFile newImage2 = reviewFileProvider.save(member);

		ReviewUpdateRequest request = ReviewUpdateRequest.builder()
			.content(UPDATED_CONTENT_STR)
			.fileIds(List.of(newImage1.getId(), newImage2.getId()))
			.build();

		Long reviewId = review.getId();
		Long oldImageId1 = oldImage1.getId();
		Long oldImageId2 = oldImage2.getId();
		Long newImageId1 = newImage1.getId();
		Long newImageId2 = newImage2.getId();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/reviews/{reviewId}", reviewId)
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		Review updatedReview = reviewProvider.findAll().stream()
			.filter(it -> it.getId().equals(reviewId))
			.findFirst()
			.orElseThrow();
		assertThat(updatedReview.getContent()).isEqualTo(UPDATED_CONTENT_STR);

		ReviewFile deletedImage1 = reviewFileRepository.findById(oldImageId1).orElseThrow();
		ReviewFile deletedImage2 = reviewFileRepository.findById(oldImageId2).orElseThrow();
		assertThat(deletedImage1.getIsDeleted()).isTrue();
		assertThat(deletedImage2.getIsDeleted()).isTrue();

		ReviewFile mappedImage1 = reviewFileRepository.findById(newImageId1).orElseThrow();
		ReviewFile mappedImage2 = reviewFileRepository.findById(newImageId2).orElseThrow();
		assertThat(mappedImage1.getIsDeleted()).isFalse();
		assertThat(mappedImage2.getIsDeleted()).isFalse();
		assertThat(mappedImage1.getReview().getId()).isEqualTo(reviewId);
		assertThat(mappedImage2.getReview().getId()).isEqualTo(reviewId);
	}

	@Test
	@DisplayName("일반 회원이 리뷰를 수정할 수 있다. (본인이 작성하지 않았을때)")
	void updateReview_notOwned() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Member otherMember = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, directorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, otherMember,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest,
			LocalDateTime.now());

		Review review = reviewProvider.save(otherMember, serviceEstimate);

		ReviewUpdateRequest request = ReviewUpdateRequest.builder()
			.content(UPDATED_CONTENT_STR)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/reviews/{reviewId}", review.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(ReviewException.NOT_OWNED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ReviewException.NOT_OWNED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ReviewException.NOT_OWNED.getCode()));
	}

	@Test
	@DisplayName("일반 회원이 리뷰를 수정할 수 있다. (작업을 완료한지 7일이 지났을때)")
	void updateReview_WhenCompletedAfter7daysLater() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, directorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, member,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest,
			LocalDateTime.now().minusDays(REVIEW_WRITE_EXPIRE_DAYS).minusMinutes(1));

		Review review = reviewProvider.save(member, serviceEstimate);

		ReviewUpdateRequest request = ReviewUpdateRequest.builder()
			.content(UPDATED_CONTENT_STR)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/reviews/{reviewId}", review.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(ReviewException.REVIEW_PERIOD_EXPIRED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ReviewException.REVIEW_PERIOD_EXPIRED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ReviewException.REVIEW_PERIOD_EXPIRED.getCode()));
	}

	@Test
	@DisplayName("일반 회원이 리뷰를 삭제할 수 있다.")
	void deleteReview_success() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, member,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest,
			LocalDateTime.now());

		Review review = reviewProvider.save(member, serviceEstimate);
		ReviewFile oldImage1 = reviewFileProvider.saveWithReview(member, review);
		ReviewFile oldImage2 = reviewFileProvider.saveWithReview(member, review);

		Long reviewId = review.getId();
		Long oldImageId1 = oldImage1.getId();
		Long oldImageId2 = oldImage2.getId();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.delete("/api/reviews/{reviewId}", reviewId)
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		Review deletedReview = reviewProvider.findAll().stream()
			.filter(it -> it.getId().equals(reviewId))
			.findFirst()
			.orElseThrow();
		assertThat(deletedReview.getIsDeleted()).isTrue();

		ReviewFile deletedImage1 = reviewFileRepository.findById(oldImageId1).orElseThrow();
		ReviewFile deletedImage2 = reviewFileRepository.findById(oldImageId2).orElseThrow();
		assertThat(deletedImage1.getIsDeleted()).isTrue();
		assertThat(deletedImage2.getIsDeleted()).isTrue();
	}

	@Test
	@DisplayName("일반 회원이 리뷰를 삭제할 수 있다.(리뷰 작성자가 아닌경우)")
	void deleteReview_notOwned() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Member otherMember = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, directorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, otherMember,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest,
			LocalDateTime.now());

		Review review = reviewProvider.save(otherMember, serviceEstimate);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.delete("/api/reviews/{reviewId}", review.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(ReviewException.NOT_OWNED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ReviewException.NOT_OWNED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ReviewException.NOT_OWNED.getCode()));
	}

	@Test
	@DisplayName("회원은 특정 제안서에 대한 리뷰를 조회할 수 있다.")
	void findByServiceEstimate_success() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE,
			directorInfo);
		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest serviceRequest1 = serviceRequestProvider.saveWithIsCompletedTrue(directorService, requester,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate1 = serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest1,
			LocalDateTime.now());

		ServiceRequest serviceRequest2 = serviceRequestProvider.saveWithIsCompletedTrue(directorService, requester,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate2 = serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest2,
			LocalDateTime.now());

		ServiceRequest serviceRequest3 = serviceRequestProvider.saveWithIsCompletedTrue(directorService, requester,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate3 = serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest3,
			LocalDateTime.now());

		Review review1 = reviewProvider.save(requester, serviceEstimate1);
		Review review2 = reviewProvider.save(requester, serviceEstimate2);
		Review review3 = reviewProvider.save(requester, serviceEstimate3);

		// 리뷰 이미지 저장
		ReviewFile reviewImageForReview1 = reviewFileProvider.saveWithReview(requester, review1);
		ReviewFile reviewImageForReview2 = reviewFileProvider.saveWithIsDeletedTrue(requester, review1);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/service-estimates/{serviceEstimateId}/reviews",
						serviceEstimate1.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ReviewWithReceivedCompletedEstimateCountResponse response = objectMapper.readValue(responseJson,
			ReviewWithReceivedCompletedEstimateCountResponse.class);

		// then
		assertThat(response.getId()).isEqualTo(review1.getId());
		assertThat(response.getContent()).isEqualTo(CONTENT_STR);
		assertThat(response.getWriter().getId()).isEqualTo(requester.getId());
		assertThat(response.getReceivedCompletedEstimateCount()).isEqualTo(3);

		// 이미지 검증
		assertThat(response.getFiles()).hasSize(1);
		assertThat(response.getFiles().get(0).getId()).isEqualTo(reviewImageForReview1.getId());
	}

	@Test
	@DisplayName("회원은 특정 제안서에 대한 리뷰를 조회할 수 있다. (리뷰가 없는 경우)")
	void findByServiceEstimate_notFound() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, requester,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest,
			LocalDateTime.now());

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/service-estimates/{serviceEstimateId}/review",
						serviceEstimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andReturn()
			.getResponse()
			.getContentAsString();

		// then

		// 리뷰가 없다
		ReviewWithReceivedCompletedEstimateCountResponse response = objectMapper.readValue(responseJson,
			ReviewWithReceivedCompletedEstimateCountResponse.class);

		assertThat(response.getId()).isNull();
	}

	@Test
	@DisplayName("회원은 특정 제안서에 대한 리뷰를 조회할 수 있다. (작성자가 본인인 경우 isEditable이 true로 반환된다.)")
	void findByServiceEstimate_isEditableTrue_whenWriter() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, member,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest,
			LocalDateTime.now());

		Review review = reviewProvider.save(member, serviceEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/service-estimates/{serviceEstimateId}/reviews",
						serviceEstimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ReviewWithReceivedCompletedEstimateCountResponse response = objectMapper.readValue(responseJson,
			ReviewWithReceivedCompletedEstimateCountResponse.class);

		// then
		assertThat(response.getId()).isEqualTo(review.getId());
		assertThat(response.getIsEditable()).isTrue();
	}

	@Test
	@DisplayName("회원은 특정 제안서에 대한 리뷰를 조회할 수 있다. (작성자가 본인인 이지만 작업 완료일로부터 7일이 지난 경우 isEditable이 false로 반환된다.)")
	void findByServiceEstimate_isEditableTrue_whenWriter_After7DaysFronCompletedAt() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, member,
			LocalDateTime.now());
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest,
			LocalDateTime.now().minusDays(8));

		Review review = reviewProvider.save(member, serviceEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/service-estimates/{serviceEstimateId}/reviews",
						serviceEstimate.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ReviewWithReceivedCompletedEstimateCountResponse response = objectMapper.readValue(responseJson,
			ReviewWithReceivedCompletedEstimateCountResponse.class);

		// then
		assertThat(response.getId()).isEqualTo(review.getId());
		assertThat(response.getIsEditable()).isFalse();
	}

	@Test
	@DisplayName("회원은 자신이 작성한 리뷰 목록을 조회할 수 있다.")
	void findMyReviews_success() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE,
			directorInfo);
		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		// 3개의 리뷰 생성
		for (int i = 0; i < 3; i++) {
			ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, requester,
				LocalDateTime.now());
			ServiceEstimate serviceEstimate = serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest,
				LocalDateTime.now());
			reviewProvider.save(requester, serviceEstimate);
		}
		// 작성한지 8일 지난 리뷰
		ServiceRequest oldServiceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, requester,
			LocalDateTime.now().minusDays(8));
		ServiceEstimate oldServiceEstimate = serviceEstimateProvider.saveReviewCompleted(directorInfo,
			oldServiceRequest,
			LocalDateTime.now().minusDays(8));
		Review oldReview = reviewProvider.save(requester, oldServiceEstimate);

		// 다른 회원의 리뷰
		Member otherMember = memberProvider.saveMember(SignInPlatform.GOOGLE);
		ServiceRequest otherServiceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService,
			otherMember,
			LocalDateTime.now());
		ServiceEstimate otherServiceEstimate = serviceEstimateProvider.saveReviewCompleted(directorInfo,
			otherServiceRequest,
			LocalDateTime.now());
		reviewProvider.save(otherMember, otherServiceEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/members/my/reviews")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(PAGE_STR, ZERO_STR))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ReviewFindAllForMemberResponse response = objectMapper.readValue(responseJson,
			ReviewFindAllForMemberResponse.class);

		// then
		assertThat(response.getReviewCount()).isEqualTo(4);
		assertThat(response.getReviews()).hasSize(4);
		assertThat(response.getReviews())
			.allMatch(review -> review.getWriter().getId().equals(requester.getId()));

		response.getReviews().forEach(review -> {
			if (review.getId().equals(oldReview.getId())) {
				assertThat(review.getIsEditable()).isFalse();
			} else {
				assertThat(review.getIsEditable()).isTrue();
			}
		});
	}

	@Test
	@DisplayName("회원은 자신이 작성한 리뷰 목록을 조회할 수 있다. (작성한 리뷰가 없는 경우)")
	void findMyReviews_emptyList() throws Exception {
		// given
		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(requester.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/members/my/reviews")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(PAGE_STR, ZERO_STR))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ReviewFindAllForMemberResponse response = objectMapper.readValue(responseJson,
			ReviewFindAllForMemberResponse.class);

		// then
		assertThat(response.getReviewCount()).isZero();
		assertThat(response.getReviews()).isEmpty();
	}

	@Test
	@DisplayName("누구나 디렉터에게 작성된 리뷰 목록을 조회할 수 있다.(디렉터 서비스 아이디로 필터링)")
	void findAllForDirector_successWithDirectorServiceId() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithReviewCount(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			0);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE,
			directorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService1);
		directorServiceMappingProvider.save(directorInfo, directorService2);

		// 3명의 다른 회원이 리뷰 작성
		for (int i = 0; i < 3; i++) {
			Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
			ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService1, requester,
				LocalDateTime.now());
			ServiceEstimate serviceEstimate = serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest,
				LocalDateTime.now());
			reviewProvider.save(requester, serviceEstimate);
			directorInfo.incrementReviewCount();
		}

		// 2명의 directorService2에 대한 리뷰 작성
		for (int i = 0; i < 2; i++) {
			Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
			ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService2, requester,
				LocalDateTime.now());
			ServiceEstimate serviceEstimate = serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest,
				LocalDateTime.now());
			reviewProvider.save(requester, serviceEstimate);
			directorInfo.incrementReviewCount();
		}

		// 다른 디렉터에 대한 리뷰
		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE,
			otherDirectorInfo);
		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		ServiceRequest otherServiceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService1, requester,
			LocalDateTime.now());
		ServiceEstimate otherServiceEstimate = serviceEstimateProvider.saveReviewCompleted(otherDirectorInfo,
			otherServiceRequest,
			LocalDateTime.now());
		reviewProvider.save(requester, otherServiceEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/{targetMemberId}/reviews", director.getId())
					.param(PAGE_STR, ZERO_STR)
					.param(DIRECTOR_SERVICE_ID_STR, directorService1.getId().toString()))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ReviewFindAllForDirectorResponse response = objectMapper.readValue(responseJson,
			ReviewFindAllForDirectorResponse.class);

		// then
		assertThat(response.getReviewCount()).isEqualTo(5);
		assertThat(response.getReviews()).hasSize(3);
		assertThat(response.getReviews())
			.allMatch(review -> review.getReceivedCompletedEstimateCount() >= 0);
	}

	@Test
	@DisplayName("누구나 디렉터에게 작성된 리뷰 목록을 조회할 수 있다.(전체 조회)")
	void findAllForDirector_success() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithReviewCount(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			0);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE,
			directorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		directorServiceMappingProvider.save(directorInfo, directorService1);

		// 3명의 다른 회원이 리뷰 작성
		for (int i = 0; i < 3; i++) {
			Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
			ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService1, requester,
				LocalDateTime.now());
			ServiceEstimate serviceEstimate = serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest,
				LocalDateTime.now());
			reviewProvider.save(requester, serviceEstimate);
			directorInfo.incrementReviewCount();
		}

		// 2명의 directorService2에 대한 리뷰 작성
		for (int i = 0; i < 2; i++) {
			Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
			ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService2, requester,
				LocalDateTime.now());
			ServiceEstimate serviceEstimate = serviceEstimateProvider.saveReviewCompleted(directorInfo, serviceRequest,
				LocalDateTime.now());
			reviewProvider.save(requester, serviceEstimate);
			directorInfo.incrementReviewCount();
		}

		// 다른 디렉터에 대한 리뷰
		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE,
			otherDirectorInfo);
		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		ServiceRequest otherServiceRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService1, requester,
			LocalDateTime.now());
		ServiceEstimate otherServiceEstimate = serviceEstimateProvider.saveReviewCompleted(otherDirectorInfo,
			otherServiceRequest,
			LocalDateTime.now());
		reviewProvider.save(requester, otherServiceEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/{targetMemberId}/reviews", director.getId())
					.param(PAGE_STR, ZERO_STR))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ReviewFindAllForDirectorResponse response = objectMapper.readValue(responseJson,
			ReviewFindAllForDirectorResponse.class);

		// then
		assertThat(response.getReviewCount()).isEqualTo(5);
		assertThat(response.getReviews()).hasSize(5);
		assertThat(response.getReviews())
			.allMatch(review -> review.getReceivedCompletedEstimateCount() >= 0);
	}

	@Test
	@DisplayName("누구나 디렉터에게 작성된 리뷰 목록을 조회할 수 있다 (작성자가 로그인한 경우 본인 리뷰는 isEditable true)")
	void findAllForDirector_withAuthenticatedWriter_isEditableTrue() throws Exception {
		// given
		Member writer = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(writer.getId());

		DirectorInfo directorInfo = directorInfoProvider.saveWithReviewCount(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR, 0);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		directorServiceMappingProvider.save(directorInfo, directorService);

		// 로그인한 회원이 작성한 리뷰 (편집 가능)
		ServiceRequest writerRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, writer,
			LocalDateTime.now());
		ServiceEstimate writerEstimate = serviceEstimateProvider.saveReviewCompleted(directorInfo, writerRequest,
			LocalDateTime.now());
		Review writerReview = reviewProvider.save(writer, writerEstimate);
		directorInfo.incrementReviewCount();

		// 다른 회원이 작성한 리뷰 (편집 불가)
		Member otherMember = memberProvider.saveMember(SignInPlatform.GOOGLE);
		ServiceRequest otherRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService, otherMember,
			LocalDateTime.now());
		ServiceEstimate otherEstimate = serviceEstimateProvider.saveReviewCompleted(directorInfo, otherRequest,
			LocalDateTime.now());
		Review otherReview = reviewProvider.save(otherMember, otherEstimate);
		directorInfo.incrementReviewCount();

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/{targetMemberId}/reviews", director.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(PAGE_STR, ZERO_STR))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ReviewFindAllForDirectorResponse response = objectMapper.readValue(responseJson,
			ReviewFindAllForDirectorResponse.class);

		// then
		assertThat(response.getReviews()).hasSize(2);

		ReviewWithReceivedCompletedEstimateCountResponse writerReviewResponse = response.getReviews().stream()
			.filter(r -> r.getId().equals(writerReview.getId()))
			.findFirst()
			.orElseThrow();
		assertThat(writerReviewResponse.getIsEditable()).isTrue();

		ReviewWithReceivedCompletedEstimateCountResponse otherReviewResponse = response.getReviews().stream()
			.filter(r -> r.getId().equals(otherReview.getId()))
			.findFirst()
			.orElseThrow();
		assertThat(otherReviewResponse.getIsEditable()).isFalse();
	}

	@Test
	@DisplayName("디렉터는 자신에게 작성된 리뷰 목록을 조회할 수 있다. (받은 리뷰가 없는 경우)")
	void findAllForDirector_emptyList() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/{targetMemberId}/reviews", director.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(PAGE_STR, ZERO_STR))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ReviewFindAllForDirectorResponse response = objectMapper.readValue(responseJson,
			ReviewFindAllForDirectorResponse.class);

		// then
		assertThat(response.getReviewCount()).isZero();
		assertThat(response.getReviews()).isEmpty();
	}
}
