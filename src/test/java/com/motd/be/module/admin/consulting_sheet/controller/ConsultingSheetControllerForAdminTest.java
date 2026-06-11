package com.motd.be.module.admin.consulting_sheet.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.admin.AdminTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.dreamsecurity.json.JSONObject;
import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.module.admin.admin.entity.Admin;
import com.motd.be.exception.exceptions.ConsultingSheetException;
import com.motd.be.module.member.banner.entity.Banner;
import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_request.enums.ConsultingRequestStatus;
import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
import com.motd.be.module.member.consulting_sheet.enums.ConsultingSheetStatus;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.Role;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.notification.entity.Notification;
import com.motd.be.module.member.notification.entity.NotificationReceiverType;
import com.motd.be.module.member.notification.entity.NotificationType;
import com.motd.be.module.member.sse.SseEventType;
import com.motd.be.shared.firebase.dto.FirebasePushEvent;
import com.motd.be.shared.firebase.entity.FirebaseCampaignSpec;

@ControllerIntegrationTest
public class ConsultingSheetControllerForAdminTest extends BaseIntegrationTest {

	@Test
	@DisplayName("관리자 컨설팅지 승인이 가능하다")
	void 관리자_컨설팅지_승인이_가능하다() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		Member directorMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		ConsultingRequest consultingRequest = consultingRequestProvider.save(member);
		ConsultingSheet consultingSheet = consultingSheetProvider.savePendingApproval(consultingRequest, directorInfo);

		Banner banner = bannerProvider.saveWithTitle("컨설팅 요청 배너",
			LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), 1);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/admin/consulting-sheets/{consultingSheetId}/approve",
					consultingSheet.getId())
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		MvcResult detailResult = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/consulting-requests/{consultingRequestId}",
					consultingRequest.getId())
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		String responseBody = detailResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);
		JSONObject sheetObj = jsonObject.getJSONObject("consultingSheet");

		assertThat(sheetObj.getString("status")).isEqualTo(ConsultingSheetStatus.APPROVED.name());
		assertThat(sheetObj.isNull("approvedAt")).isFalse();

		// notification 저장 검증
		List<Notification> notifications = notificationProvider.findAll();
		assertThat(notifications).hasSize(1);
		assertThat(notifications.get(0).getType()).isEqualTo(NotificationType.CONSULTING_SHEET_APPROVED);
		assertThat(notifications.get(0).getReceiverType()).isEqualTo(NotificationReceiverType.MEMBER);
		assertThat(notifications.get(0).getReceiver().getId()).isEqualTo(member.getId());
		assertThat(notifications.get(0).getSenderId()).isEqualTo(directorMember.getId());

		// SSE refresh event 발행 검증
		verify(sseService, atLeastOnce()).refreshNotificationCount(
			argThat(payload ->
				payload.getEventName() == SseEventType.REFRESH_NOTIFICATION_COUNT &&
					Objects.equals(payload.getReceiverId(), member.getId()) &&
					Objects.equals(payload.getReceiverRole(), Role.MEMBER)));

		// Firebase push event 발행 검증
		ArgumentCaptor<FirebasePushEvent> eventCaptor = ArgumentCaptor.forClass(FirebasePushEvent.class);
		verify(eventPublisher, atLeast(1)).publish(eventCaptor.capture());

		List<FirebasePushEvent> capturedEvents = eventCaptor.getAllValues();
		assertThat(capturedEvents).anyMatch(event ->
			event.getCampaignSpec() == FirebaseCampaignSpec.PUSH_MEMBER_CONSULTING_SHEET_APPROVED
				&& event.getReferenceId().equals(banner.getId()));
	}

	@Test
	@DisplayName("관리자 컨설팅지 승인 시 유효한 배너가 없으면 push만 skip하고 알림/SSE는 발행한다")
	void 관리자_컨설팅지_승인_시_유효한_배너가_없으면_push만_skip하고_알림_SSE는_발행한다() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		Member directorMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		ConsultingRequest consultingRequest = consultingRequestProvider.save(member);
		ConsultingSheet consultingSheet = consultingSheetProvider.savePendingApproval(consultingRequest, directorInfo);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/admin/consulting-sheets/{consultingSheetId}/approve",
					consultingSheet.getId())
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		// notification 저장 검증
		List<Notification> notifications = notificationProvider.findAll();
		assertThat(notifications).hasSize(1);
		assertThat(notifications.get(0).getType()).isEqualTo(NotificationType.CONSULTING_SHEET_APPROVED);
		assertThat(notifications.get(0).getReceiverType()).isEqualTo(NotificationReceiverType.MEMBER);

		// SSE refresh event 발행 검증
		verify(sseService, atLeastOnce()).refreshNotificationCount(
			argThat(payload ->
				payload.getEventName() == SseEventType.REFRESH_NOTIFICATION_COUNT &&
					Objects.equals(payload.getReceiverId(), member.getId()) &&
					Objects.equals(payload.getReceiverRole(), Role.MEMBER)));

		// Firebase push event 미발행 검증
		verify(eventPublisher, never()).publish(argThat(event ->
			event instanceof FirebasePushEvent &&
				((FirebasePushEvent)event).getCampaignSpec() == FirebaseCampaignSpec.PUSH_MEMBER_CONSULTING_SHEET_APPROVED
		));
	}

	@Test
	@DisplayName("관리자 컨설팅지 승인 시 삭제된 배너만 있으면 push만 skip하고 알림/SSE는 발행한다")
	void 관리자_컨설팅지_승인_시_삭제된_배너만_있으면_push만_skip하고_알림_SSE는_발행한다() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		Member directorMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		ConsultingRequest consultingRequest = consultingRequestProvider.save(member);
		ConsultingSheet consultingSheet = consultingSheetProvider.savePendingApproval(consultingRequest, directorInfo);

		Banner banner = bannerProvider.saveWithTitle("컨설팅 요청 배너",
			LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), 1);
		banner.delete();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/admin/consulting-sheets/{consultingSheetId}/approve",
					consultingSheet.getId())
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		List<Notification> notifications = notificationProvider.findAll();
		assertThat(notifications).hasSize(1);
		assertThat(notifications.get(0).getType()).isEqualTo(NotificationType.CONSULTING_SHEET_APPROVED);

		verify(sseService, atLeastOnce()).refreshNotificationCount(
			argThat(payload ->
				payload.getEventName() == SseEventType.REFRESH_NOTIFICATION_COUNT &&
					Objects.equals(payload.getReceiverId(), member.getId()) &&
					Objects.equals(payload.getReceiverRole(), Role.MEMBER)));

		verify(eventPublisher, never()).publish(argThat(event ->
			event instanceof FirebasePushEvent &&
				((FirebasePushEvent)event).getCampaignSpec() == FirebaseCampaignSpec.PUSH_MEMBER_CONSULTING_SHEET_APPROVED
		));
	}

	@Test
	@DisplayName("관리자 컨설팅지 승인 시 만료된 배너만 있으면 push만 skip하고 알림/SSE는 발행한다")
	void 관리자_컨설팅지_승인_시_만료된_배너만_있으면_push만_skip하고_알림_SSE는_발행한다() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		Member directorMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		ConsultingRequest consultingRequest = consultingRequestProvider.save(member);
		ConsultingSheet consultingSheet = consultingSheetProvider.savePendingApproval(consultingRequest, directorInfo);

		bannerProvider.saveWithTitle("컨설팅 요청 배너",
			LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(1), 1);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/admin/consulting-sheets/{consultingSheetId}/approve",
					consultingSheet.getId())
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		List<Notification> notifications = notificationProvider.findAll();
		assertThat(notifications).hasSize(1);
		assertThat(notifications.get(0).getType()).isEqualTo(NotificationType.CONSULTING_SHEET_APPROVED);

		verify(sseService, atLeastOnce()).refreshNotificationCount(
			argThat(payload ->
				payload.getEventName() == SseEventType.REFRESH_NOTIFICATION_COUNT &&
					Objects.equals(payload.getReceiverId(), member.getId()) &&
					Objects.equals(payload.getReceiverRole(), Role.MEMBER)));

		verify(eventPublisher, never()).publish(argThat(event ->
			event instanceof FirebasePushEvent &&
				((FirebasePushEvent)event).getCampaignSpec() == FirebaseCampaignSpec.PUSH_MEMBER_CONSULTING_SHEET_APPROVED
		));
	}

	@Test
	@DisplayName("관리자 컨설팅지 승인이 가능하다 (이미 승인된 상태면 400)")
	void 관리자_컨설팅지_승인이_가능하다_이미_승인된_상태면_400() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		ConsultingRequest consultingRequest = consultingRequestProvider.save(member);
		ConsultingSheet consultingSheet = consultingSheetProvider.saveApproved(consultingRequest, directorInfo);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/admin/consulting-sheets/{consultingSheetId}/approve",
					consultingSheet.getId())
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_MESSAGE).value(ConsultingSheetException.NOT_PENDING_APPROVAL.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ConsultingSheetException.NOT_PENDING_APPROVAL.getCode()));
	}

	@Test
	@DisplayName("관리자 컨설팅지 승인이 가능하다 (존재하지 않으면 404)")
	void 관리자_컨설팅지_승인이_가능하다_존재하지_않으면_404() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		Long invalidId = 99999L;

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/admin/consulting-sheets/{consultingSheetId}/approve", invalidId)
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("관리자 컨설팅지 반려가 가능하다")
	void 관리자_컨설팅지_반려가_가능하다() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		ConsultingRequest consultingRequest = consultingRequestProvider.saveCompleted(member);
		ConsultingSheet consultingSheet = consultingSheetProvider.savePendingApproval(consultingRequest, directorInfo);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/admin/consulting-sheets/{consultingSheetId}/reject",
					consultingSheet.getId())
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		MvcResult detailResult = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/consulting-requests/{consultingRequestId}",
					consultingRequest.getId())
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		String responseBody = detailResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);
		JSONObject sheetObj = jsonObject.getJSONObject("consultingSheet");

		assertThat(sheetObj.getString("status")).isEqualTo(ConsultingSheetStatus.REJECTED.name());

		ConsultingRequest updatedRequest = consultingRequestProvider.findAll().stream()
			.filter(cr -> cr.getId().equals(consultingRequest.getId()))
			.findFirst().orElseThrow();
		assertThat(updatedRequest.getStatus()).isEqualTo(ConsultingRequestStatus.PENDING);
		assertThat(updatedRequest.getReservedBy()).isNull();
		assertThat(updatedRequest.getReservedAt()).isNull();
	}

	@Test
	@DisplayName("관리자 컨설팅지 반려가 가능하다 (이미 반려된 상태면 400)")
	void 관리자_컨설팅지_반려가_가능하다_이미_반려된_상태면_400() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		ConsultingRequest consultingRequest = consultingRequestProvider.save(member);
		ConsultingSheet consultingSheet = consultingSheetProvider.saveRejected(consultingRequest, directorInfo);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/admin/consulting-sheets/{consultingSheetId}/reject",
					consultingSheet.getId())
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_MESSAGE).value(ConsultingSheetException.NOT_PENDING_APPROVAL.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ConsultingSheetException.NOT_PENDING_APPROVAL.getCode()));
	}

	@Test
	@DisplayName("관리자 컨설팅지 반려가 가능하다 (존재하지 않으면 404)")
	void 관리자_컨설팅지_반려가_가능하다_존재하지_않으면_404() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		Long invalidId = 99999L;

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/admin/consulting-sheets/{consultingSheetId}/reject", invalidId)
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("관리자 컨설팅지 승인이 가능하다 (권한 없으면 401)")
	void 관리자_컨설팅지_승인이_가능하다_권한_없으면_401() throws Exception {
		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/admin/consulting-sheets/{consultingSheetId}/approve", 1L)
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("관리자 컨설팅지 반려가 가능하다 (권한 없으면 401)")
	void 관리자_컨설팅지_반려가_가능하다_권한_없으면_401() throws Exception {
		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/admin/consulting-sheets/{consultingSheetId}/reject", 1L)
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized());
	}
}
