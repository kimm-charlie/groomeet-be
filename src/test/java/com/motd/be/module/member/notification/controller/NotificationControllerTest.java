package com.motd.be.module.member.notification.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.notification.dto.response.NotificationExistResponse;
import com.motd.be.module.member.notification.dto.response.NotificationFindAllResponse;
import com.motd.be.module.member.notification.entity.Notification;
import com.motd.be.module.member.notification.entity.NotificationCategoryType;
import com.motd.be.module.member.notification.entity.NotificationReceiverType;
import com.motd.be.module.member.notification.entity.NotificationType;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class NotificationControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("회원은 자신의 알림 목록을 조회할 수 있다. (정상 케이스)")
	void findAllNotifications() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		// 여러 알림 생성
		notificationProvider.save(member, NotificationType.TRANSACTION_CONFIRMED,
			1L,
			NotificationReceiverType.MEMBER);
		notificationProvider.save(member, NotificationType.TRANSACTION_CONFIRMED,
			1L,
			NotificationReceiverType.MEMBER);
		notificationProvider.save(member, NotificationType.TRANSACTION_CONFIRMED,
			1L,
			NotificationReceiverType.MEMBER);

		notificationProvider.save(member, NotificationType.TRANSACTION_CONFIRMED,
			1L,
			NotificationReceiverType.DIRECTOR);

		entityManager.flush();
		entityManager.clear();

		// when
		String jsonResponse = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/notifications")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		entityManager.flush();
		entityManager.clear();

		// then
		NotificationFindAllResponse findAllResponse = objectMapper.readValue(jsonResponse,
			NotificationFindAllResponse.class);

		assertThat(findAllResponse.getNotifications()).hasSize(3);

		// 알림이 모두 읽음처리 되었는지 확인한다.
		List<Notification> updatedNotifications = notificationProvider.findAll();

		updatedNotifications.forEach(notification -> {
			if (notification.getReceiverType().equals(NotificationReceiverType.DIRECTOR)) {
				assertThat(notification.getIsRead()).isFalse();
			} else {
				assertThat(notification.getIsRead()).isTrue();
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
			1L, NotificationReceiverType.MEMBER);
		notificationProvider.save(member, NotificationType.TRANSACTION_CONFIRMED,
			2L, NotificationReceiverType.MEMBER);
		notificationProvider.save(member, NotificationType.REVIEW_WRITTEN,
			3L, NotificationReceiverType.MEMBER);
		notificationProvider.save(member, NotificationType.TRANSACTION_CONFIRMED,
			4L, NotificationReceiverType.MEMBER);

		entityManager.flush();
		entityManager.clear();

		// when - TRANSACTION 카테고리만 조회
		String jsonResponse = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/notifications")
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
	@DisplayName("회원은 자신의 알림 목록을 조회할 수 있다. (알림이 없는 경우)")
	void findAllNotificationsWhenEmpty() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		String jsonResponse = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/notifications")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		// then
		NotificationFindAllResponse findAllResponse = objectMapper.readValue(jsonResponse,
			NotificationFindAllResponse.class);

		assertThat(findAllResponse.getNotifications()).hasSize(0);
	}

	@Test
	@DisplayName("회원은 자신이 읽지않은 알림이 존재하는지 여부를 판단할 수 있다. (모든 알림이 읽음처리 되지 않은 경우)")
	void hasUnreadNotification() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		// 여러 알림 생성
		notificationProvider.save(member, NotificationType.TRANSACTION_CONFIRMED,
			1L,
			NotificationReceiverType.MEMBER);
		notificationProvider.save(member, NotificationType.TRANSACTION_CONFIRMED,
			1L,
			NotificationReceiverType.MEMBER);
		notificationProvider.save(member, NotificationType.TRANSACTION_CONFIRMED,
			1L,
			NotificationReceiverType.MEMBER);

		notificationProvider.save(member, NotificationType.TRANSACTION_CONFIRMED,
			1L,
			NotificationReceiverType.DIRECTOR);

		entityManager.flush();
		entityManager.clear();

		// when
		String jsonResponse = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/notifications/exists")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		// then
		NotificationExistResponse response = objectMapper.readValue(jsonResponse,
			NotificationExistResponse.class);

		assertThat(response.getHasUnreadNotification()).isTrue();
	}

	@Test
	@DisplayName("회원은 자신이 읽지않은 알림이 존재하는지 여부를 판단할 수 있다. (일반회원이지만 디렉터로 받은 알림만 읽지 않은 경우)")
	void hasUnreadNotificationWhenOnlyUnreadDirectorNotificationExist() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		// 여러 알림 생성
		notificationProvider.saveWithIsReadTrue(member, NotificationType.TRANSACTION_CONFIRMED,
			1L,
			NotificationReceiverType.MEMBER);
		notificationProvider.saveWithIsReadTrue(member, NotificationType.TRANSACTION_CONFIRMED,
			1L,
			NotificationReceiverType.MEMBER);
		notificationProvider.saveWithIsReadTrue(member, NotificationType.TRANSACTION_CONFIRMED,
			1L,
			NotificationReceiverType.MEMBER);

		notificationProvider.save(member, NotificationType.TRANSACTION_CONFIRMED,
			1L,
			NotificationReceiverType.DIRECTOR);

		entityManager.flush();
		entityManager.clear();

		// when
		String jsonResponse = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/notifications/exists")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		// then
		NotificationExistResponse response = objectMapper.readValue(jsonResponse,
			NotificationExistResponse.class);

		assertThat(response.getHasUnreadNotification()).isFalse();
	}
}
