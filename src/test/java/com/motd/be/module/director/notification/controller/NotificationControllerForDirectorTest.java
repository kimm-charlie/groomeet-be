package com.motd.be.module.director.notification.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.HandlerException;
import com.motd.be.module.director.notification.dto.response.NotificationExistResponseForDirector;
import com.motd.be.module.director.notification.dto.response.NotificationFindAllResponseForDirector;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.notification.dto.response.NotificationFindAllResponse;
import com.motd.be.module.member.notification.entity.Notification;
import com.motd.be.module.member.notification.entity.NotificationCategoryType;
import com.motd.be.module.member.notification.entity.NotificationReceiverType;
import com.motd.be.module.member.notification.entity.NotificationType;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class NotificationControllerForDirectorTest extends BaseIntegrationTest {

	@Test
	@DisplayName("디렉터는 자신의 알림 목록을 조회할 수 있다. (정상 케이스)")
	void findAllNotificationsForDirector() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, requester);
		ServiceEstimate serviceEstimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		ChatRoom chatRoom = chatRoomProvider.save();

		// 여러 알림 생성
		notificationProvider.save(director, NotificationType.TRANSACTION_CONFIRMED,
			chatRoom.getId(),
			NotificationReceiverType.DIRECTOR);
		notificationProvider.save(director, NotificationType.TRANSACTION_CONFIRMED,
			chatRoom.getId(),
			NotificationReceiverType.DIRECTOR);
		notificationProvider.save(director, NotificationType.TRANSACTION_CONFIRMED,
			chatRoom.getId(),
			NotificationReceiverType.DIRECTOR);
		notificationProvider.save(director, NotificationType.TRANSACTION_CONFIRMED,
			chatRoom.getId(),
			NotificationReceiverType.MEMBER);

		entityManager.flush();
		entityManager.clear();

		// when
		String jsonResponse = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/notifications")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		entityManager.flush();
		entityManager.clear();

		// then
		NotificationFindAllResponseForDirector findAllResponse = objectMapper.readValue(jsonResponse,
			NotificationFindAllResponseForDirector.class);

		assertThat(findAllResponse.getNotifications()).hasSize(3);

		// 알림이 모두 읽음처리 되었는지 확인한다.
		List<Notification> updatedNotifications = notificationProvider.findAll();

		updatedNotifications.forEach(notification -> {
			if (notification.getReceiverType().equals(NotificationReceiverType.DIRECTOR)) {
				assertThat(notification.getIsRead()).isTrue();
			} else {
				assertThat(notification.getIsRead()).isFalse();
			}
		});
	}

	@Test
	@DisplayName("회원은 자신의 알림 목록을 조회할 수 있다. (카테고리별 조회)")
	void findAllNotificationsByCategory() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		// 다양한 카테고리의 알림 생성
		notificationProvider.save(member, NotificationType.ESTIMATE_ARRIVED,
			1L, NotificationReceiverType.DIRECTOR);
		notificationProvider.save(member, NotificationType.TRANSACTION_CONFIRMED,
			2L, NotificationReceiverType.DIRECTOR);
		notificationProvider.save(member, NotificationType.REVIEW_WRITTEN,
			3L, NotificationReceiverType.DIRECTOR);
		notificationProvider.save(member, NotificationType.TRANSACTION_CONFIRMED,
			4L, NotificationReceiverType.DIRECTOR);

		entityManager.flush();
		entityManager.clear();

		// when - TRANSACTION 카테고리만 조회
		String jsonResponse = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/notifications")
					.param("notificationCategoryType", NotificationCategoryType.TRANSACTION.name())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		// then
		NotificationFindAllResponse response = objectMapper.readValue(jsonResponse,
			NotificationFindAllResponse.class);

		assertThat(response.getNotifications()).hasSize(2);
		assertThat(response.getNotifications())
			.allMatch(notification -> notification.getType().equals(NotificationType.TRANSACTION_CONFIRMED.name()));
	}

	@Test
	@DisplayName("디렉터는 자신의 알림 목록을 조회할 수 있다. (알림이 없는 경우)")
	void findAllNotificationsForDirectorWhenEmpty() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		String jsonResponse = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/notifications")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		// then
		NotificationFindAllResponseForDirector findAllResponse = objectMapper.readValue(jsonResponse,
			NotificationFindAllResponseForDirector.class);

		assertThat(findAllResponse.getNotifications()).hasSize(0);
	}

	@Test
	@DisplayName("디렉터는 자신의 알림 목록을 조회할 수 있다. (일반 회원이 요청하는 경우)")
	void findAllNotificationsForDirectorWithMember() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/notifications")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.status").value(HandlerException.FORBIDDEN.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(HandlerException.FORBIDDEN.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(HandlerException.FORBIDDEN.getCode()));
	}

	@Test
	@DisplayName("디렉터는 자신이 읽지않은 알림이 존재하는지 여부를 판단할 수 있다. (모든 알림이 읽음처리 되지 않은 경우)")
	void hasUnreadNotification() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, requester);
		ServiceEstimate serviceEstimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		ChatRoom chatRoom = chatRoomProvider.save();

		// 여러 알림 생성
		notificationProvider.save(director, NotificationType.TRANSACTION_CONFIRMED,
			chatRoom.getId(),
			NotificationReceiverType.DIRECTOR);
		notificationProvider.save(director, NotificationType.TRANSACTION_CONFIRMED,
			chatRoom.getId(),
			NotificationReceiverType.DIRECTOR);
		notificationProvider.saveWithIsReadTrue(director, NotificationType.TRANSACTION_CONFIRMED,
			chatRoom.getId(),
			NotificationReceiverType.DIRECTOR);
		notificationProvider.save(director, NotificationType.TRANSACTION_CONFIRMED,
			chatRoom.getId(), NotificationReceiverType.MEMBER);

		entityManager.flush();
		entityManager.clear();

		// when
		String jsonResponse = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/notifications/exists")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		// then
		NotificationExistResponseForDirector response = objectMapper.readValue(jsonResponse,
			NotificationExistResponseForDirector.class);

		assertThat(response.getHasUnreadNotification()).isTrue();
	}

	@Test
	@DisplayName("디렉터는 자신이 읽지않은 알림이 존재하는지 여부를 판단할 수 있다. (디렉터지만 일반회원으로 받은 알림만 읽지 않은 경우)")
	void hasUnreadNotificationWhenOnlyUnreadMemberNotificationExist() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(directorService, requester);
		ServiceEstimate serviceEstimate = serviceEstimateProvider.save(directorInfo, serviceRequest);

		ChatRoom chatRoom = chatRoomProvider.save();

		// 여러 알림 생성
		notificationProvider.saveWithIsReadTrue(director, NotificationType.TRANSACTION_CONFIRMED,
			chatRoom.getId(),
			NotificationReceiverType.DIRECTOR);
		notificationProvider.saveWithIsReadTrue(director, NotificationType.TRANSACTION_CONFIRMED,
			chatRoom.getId(),
			NotificationReceiverType.DIRECTOR);
		notificationProvider.saveWithIsReadTrue(director, NotificationType.TRANSACTION_CONFIRMED,
			chatRoom.getId(),
			NotificationReceiverType.DIRECTOR);

		notificationProvider.save(director, NotificationType.TRANSACTION_CONFIRMED,
			chatRoom.getId(),
			NotificationReceiverType.MEMBER);

		entityManager.flush();
		entityManager.clear();

		// when
		String jsonResponse = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/notifications/exists")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		// then
		NotificationExistResponseForDirector response = objectMapper.readValue(jsonResponse,
			NotificationExistResponseForDirector.class);

		assertThat(response.getHasUnreadNotification()).isFalse();
	}

}
