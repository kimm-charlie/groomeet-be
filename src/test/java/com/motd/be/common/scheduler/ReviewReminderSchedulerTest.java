package com.motd.be.common.scheduler;

import static com.motd.be.Constants.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.module.member.chat_room.entity.ChatRoom;
import com.motd.be.module.member.chat_room_member.entity.ChatRoomMember;
import com.motd.be.module.member.chat_room_service_estimate_mapping.entity.ChatRoomServiceEstimateMapping;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.facade.ServiceEstimateFacade;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.shared.firebase.dto.FirebasePushEvent;
import com.motd.be.shared.firebase.entity.FirebaseCampaignSpec;

@ControllerIntegrationTest
class ReviewReminderSchedulerTest extends BaseIntegrationTest {

	@Autowired
	private ServiceEstimateFacade serviceEstimateFacade;

	@Test
	@DisplayName("리뷰 장려 스케줄러 - 조건 충족 시 정상 발송")
	void sendReviewReminders_success() {
		// given
		int targetHour = 14;
		LocalDateTime baseTime = LocalDateTime.now().toLocalDate().atTime(targetHour, 0);
		Instant fixedInstant = baseTime.atZone(ZoneId.systemDefault()).toInstant();
		when(clock.instant()).thenReturn(fixedInstant);
		when(clock.getZone()).thenReturn(ZoneId.systemDefault());

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			baseTime.toLocalDate());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		DirectorService directorService = directorServiceProvider.save(SERVICE_NAME_1_STR, null);

		// 리마인더 대상 - COMPLETED_BY_MEMBER, 1일 이상 경과, 리뷰 미작성, 시간대 일치
		ServiceRequest targetRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester, baseTime);
		ServiceEstimate targetEstimate = serviceEstimateProvider.saveMemberCompleted(
			directorInfo, targetRequest, baseTime.minusDays(2), baseTime.minusDays(1).withHour(targetHour));

		// ChatRoom 설정
		ChatRoom targetChatRoom = chatRoomProvider.save();
		ChatRoomMember targetChatRoomMemberDirector = chatRoomMemberProvider.saveDirector(targetChatRoom, director);
		ChatRoomMember targetChatRoomMemberRequester = chatRoomMemberProvider.saveMember(targetChatRoom, requester);
		ChatRoomServiceEstimateMapping targetMapping = chatRoomServiceEstimateMappingProvider.save(
			targetChatRoom, targetEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		serviceEstimateFacade.sendReviewReminders(targetHour);

		entityManager.flush();
		entityManager.clear();

		// then
		ArgumentCaptor<FirebasePushEvent> eventCaptor = ArgumentCaptor.forClass(FirebasePushEvent.class);
		verify(eventPublisher, atLeast(1)).publish(eventCaptor.capture());

		List<FirebasePushEvent> capturedEvents = eventCaptor.getAllValues();
		assertThat(capturedEvents).isNotEmpty();
		assertThat(capturedEvents).anyMatch(event ->
			event.getCampaignSpec() == FirebaseCampaignSpec.PUSH_MEMBER_REVIEW_REMINDER
		);

		// reviewReminderSentAt 업데이트 확인
		ServiceEstimate updatedEstimate = serviceEstimateProvider.findById(targetEstimate.getId());
		assertThat(updatedEstimate.getReviewReminderSentAt()).isNotNull();
	}

	@Test
	@DisplayName("리뷰 장려 스케줄러 - 이미 발송된 경우 중복 발송 안함")
	void sendReviewReminders_alreadySent_noAction() {
		// given
		int targetHour = 14;
		LocalDateTime baseTime = LocalDateTime.now().toLocalDate().atTime(targetHour, 0);
		Instant fixedInstant = baseTime.atZone(ZoneId.systemDefault()).toInstant();
		when(clock.instant()).thenReturn(fixedInstant);
		when(clock.getZone()).thenReturn(ZoneId.systemDefault());

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			baseTime.toLocalDate());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		DirectorService directorService = directorServiceProvider.save(SERVICE_NAME_1_STR, null);

		// 이미 리마인더 발송된 제안
		ServiceRequest alreadySentRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester, baseTime);
		ServiceEstimate alreadySentEstimate = serviceEstimateProvider.saveMemberCompletedWithReviewReminderSent(
			directorInfo, alreadySentRequest, baseTime.minusDays(2), baseTime.minusDays(1).withHour(targetHour));

		// ChatRoom 설정
		ChatRoom alreadySentChatRoom = chatRoomProvider.save();
		ChatRoomMember alreadySentChatRoomMember1 = chatRoomMemberProvider.saveDirector(alreadySentChatRoom, director);
		ChatRoomMember alreadySentChatRoomMember2 = chatRoomMemberProvider.saveMember(alreadySentChatRoom, requester);
		ChatRoomServiceEstimateMapping alreadySentMapping = chatRoomServiceEstimateMappingProvider.save(
			alreadySentChatRoom, alreadySentEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		serviceEstimateFacade.sendReviewReminders(targetHour);

		entityManager.flush();
		entityManager.clear();

		// then - PUSH_MEMBER_REVIEW_REMINDER 이벤트가 발행되지 않아야 함
		verify(eventPublisher, never()).publish(argThat(event ->
			event instanceof FirebasePushEvent &&
				((FirebasePushEvent) event).getCampaignSpec() == FirebaseCampaignSpec.PUSH_MEMBER_REVIEW_REMINDER
		));
	}

	@Test
	@DisplayName("리뷰 장려 스케줄러 - 리뷰 작성 완료된 경우 발송 안함")
	void sendReviewReminders_reviewCompleted_noAction() {
		// given
		int targetHour = 14;
		LocalDateTime baseTime = LocalDateTime.now().toLocalDate().atTime(targetHour, 0);
		Instant fixedInstant = baseTime.atZone(ZoneId.systemDefault()).toInstant();
		when(clock.instant()).thenReturn(fixedInstant);
		when(clock.getZone()).thenReturn(ZoneId.systemDefault());

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			baseTime.toLocalDate());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		DirectorService directorService = directorServiceProvider.save(SERVICE_NAME_1_STR, null);

		// 리뷰 작성 완료된 제안 (REVIEW_COMPLETED 상태)
		ServiceRequest reviewCompletedRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester, baseTime);
		ServiceEstimate reviewCompletedEstimate = serviceEstimateProvider.saveReviewCompleted(
			directorInfo, reviewCompletedRequest, baseTime.minusDays(2), baseTime.minusDays(1).withHour(targetHour));

		// ChatRoom 설정
		ChatRoom reviewCompletedChatRoom = chatRoomProvider.save();
		ChatRoomMember reviewCompletedChatRoomMember1 = chatRoomMemberProvider.saveDirector(reviewCompletedChatRoom, director);
		ChatRoomMember reviewCompletedChatRoomMember2 = chatRoomMemberProvider.saveMember(reviewCompletedChatRoom, requester);
		ChatRoomServiceEstimateMapping reviewCompletedMapping = chatRoomServiceEstimateMappingProvider.save(
			reviewCompletedChatRoom, reviewCompletedEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		serviceEstimateFacade.sendReviewReminders(targetHour);

		entityManager.flush();
		entityManager.clear();

		// then - PUSH_MEMBER_REVIEW_REMINDER 이벤트가 발행되지 않아야 함
		verify(eventPublisher, never()).publish(argThat(event ->
			event instanceof FirebasePushEvent &&
				((FirebasePushEvent) event).getCampaignSpec() == FirebaseCampaignSpec.PUSH_MEMBER_REVIEW_REMINDER
		));
	}

	@Test
	@DisplayName("리뷰 장려 스케줄러 - 시간대 불일치 시 발송 안함")
	void sendReviewReminders_outsideTimeWindow_noAction() {
		// given
		int currentHour = 14;
		int meetingHour = 10; // 4시간 차이 (tolerance 1시간 초과)
		LocalDateTime baseTime = LocalDateTime.now().toLocalDate().atTime(currentHour, 0);
		Instant fixedInstant = baseTime.atZone(ZoneId.systemDefault()).toInstant();
		when(clock.instant()).thenReturn(fixedInstant);
		when(clock.getZone()).thenReturn(ZoneId.systemDefault());

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			baseTime.toLocalDate());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		DirectorService directorService = directorServiceProvider.save(SERVICE_NAME_1_STR, null);

		// 시간대 불일치 제안
		ServiceRequest outsideWindowRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester, baseTime);
		ServiceEstimate outsideWindowEstimate = serviceEstimateProvider.saveMemberCompleted(
			directorInfo, outsideWindowRequest, baseTime.minusDays(2), baseTime.minusDays(1).withHour(meetingHour));

		// ChatRoom 설정
		ChatRoom outsideWindowChatRoom = chatRoomProvider.save();
		ChatRoomMember outsideWindowChatRoomMember1 = chatRoomMemberProvider.saveDirector(outsideWindowChatRoom, director);
		ChatRoomMember outsideWindowChatRoomMember2 = chatRoomMemberProvider.saveMember(outsideWindowChatRoom, requester);
		ChatRoomServiceEstimateMapping outsideWindowMapping = chatRoomServiceEstimateMappingProvider.save(
			outsideWindowChatRoom, outsideWindowEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		serviceEstimateFacade.sendReviewReminders(currentHour);

		entityManager.flush();
		entityManager.clear();

		// then - PUSH_MEMBER_REVIEW_REMINDER 이벤트가 발행되지 않아야 함
		verify(eventPublisher, never()).publish(argThat(event ->
			event instanceof FirebasePushEvent &&
				((FirebasePushEvent) event).getCampaignSpec() == FirebaseCampaignSpec.PUSH_MEMBER_REVIEW_REMINDER
		));
	}

	@Test
	@DisplayName("리뷰 장려 스케줄러 - 자정을 걸치는 경우 (현재 0시, 제안 수락 23시) 정상 발송")
	void sendReviewReminders_midnightCrossing_currentHour0_meetingHour23_success() {
		// given
		// 현재 시간: 0시, tolerance ±1시간 → 범위: 23시 ~ 1시
		// 제안 수락 시간: 23시 → 범위 내 → 발송 대상
		int currentHour = 0;
		int meetingHour = 23;
		LocalDateTime baseTime = LocalDateTime.now().toLocalDate().atTime(currentHour, 0);
		Instant fixedInstant = baseTime.atZone(ZoneId.systemDefault()).toInstant();
		when(clock.instant()).thenReturn(fixedInstant);
		when(clock.getZone()).thenReturn(ZoneId.systemDefault());

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			baseTime.toLocalDate());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		DirectorService directorService = directorServiceProvider.save(SERVICE_NAME_1_STR, null);

		ServiceRequest targetRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester, baseTime);
		ServiceEstimate targetEstimate = serviceEstimateProvider.saveMemberCompleted(
			directorInfo, targetRequest, baseTime.minusDays(2), baseTime.minusDays(1).withHour(meetingHour));

		ChatRoom targetChatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(targetChatRoom, director);
		chatRoomMemberProvider.saveMember(targetChatRoom, requester);
		chatRoomServiceEstimateMappingProvider.save(targetChatRoom, targetEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		serviceEstimateFacade.sendReviewReminders(currentHour);

		entityManager.flush();
		entityManager.clear();

		// then
		ArgumentCaptor<FirebasePushEvent> eventCaptor = ArgumentCaptor.forClass(FirebasePushEvent.class);
		verify(eventPublisher, atLeast(1)).publish(eventCaptor.capture());

		List<FirebasePushEvent> capturedEvents = eventCaptor.getAllValues();
		assertThat(capturedEvents).anyMatch(event ->
			event.getCampaignSpec() == FirebaseCampaignSpec.PUSH_MEMBER_REVIEW_REMINDER
		);

		ServiceEstimate updatedEstimate = serviceEstimateProvider.findById(targetEstimate.getId());
		assertThat(updatedEstimate.getReviewReminderSentAt()).isNotNull();
	}

	@Test
	@DisplayName("리뷰 장려 스케줄러 - 자정을 걸치는 경우 (현재 23시, 제안 수락 0시) 정상 발송")
	void sendReviewReminders_midnightCrossing_currentHour23_meetingHour0_success() {
		// given
		// 현재 시간: 23시, tolerance ±1시간 → 범위: 22시 ~ 0시
		// 제안 수락 시간: 0시 → 범위 내 → 발송 대상
		int currentHour = 23;
		int meetingHour = 0;
		LocalDateTime baseTime = LocalDateTime.now().toLocalDate().atTime(currentHour, 0);
		Instant fixedInstant = baseTime.atZone(ZoneId.systemDefault()).toInstant();
		when(clock.instant()).thenReturn(fixedInstant);
		when(clock.getZone()).thenReturn(ZoneId.systemDefault());

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			baseTime.toLocalDate());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		DirectorService directorService = directorServiceProvider.save(SERVICE_NAME_1_STR, null);

		ServiceRequest targetRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester, baseTime);
		ServiceEstimate targetEstimate = serviceEstimateProvider.saveMemberCompleted(
			directorInfo, targetRequest, baseTime.minusDays(2), baseTime.minusDays(1).withHour(meetingHour));

		ChatRoom targetChatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(targetChatRoom, director);
		chatRoomMemberProvider.saveMember(targetChatRoom, requester);
		chatRoomServiceEstimateMappingProvider.save(targetChatRoom, targetEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		serviceEstimateFacade.sendReviewReminders(currentHour);

		entityManager.flush();
		entityManager.clear();

		// then
		ArgumentCaptor<FirebasePushEvent> eventCaptor = ArgumentCaptor.forClass(FirebasePushEvent.class);
		verify(eventPublisher, atLeast(1)).publish(eventCaptor.capture());

		List<FirebasePushEvent> capturedEvents = eventCaptor.getAllValues();
		assertThat(capturedEvents).anyMatch(event ->
			event.getCampaignSpec() == FirebaseCampaignSpec.PUSH_MEMBER_REVIEW_REMINDER
		);

		ServiceEstimate updatedEstimate = serviceEstimateProvider.findById(targetEstimate.getId());
		assertThat(updatedEstimate.getReviewReminderSentAt()).isNotNull();
	}

	@Test
	@DisplayName("리뷰 장려 스케줄러 - 정확히 1일 경과 (경계 조건) 발송 대상")
	void sendReviewReminders_exactly1DayElapsed_success() {
		// given
		// 현재 시간: 14:00, memberCompletedAt: 어제 13:59 → 1일 1분 경과 → 대상
		int targetHour = 14;
		LocalDateTime baseTime = LocalDateTime.now().toLocalDate().atTime(targetHour, 0);
		Instant fixedInstant = baseTime.atZone(ZoneId.systemDefault()).toInstant();
		when(clock.instant()).thenReturn(fixedInstant);
		when(clock.getZone()).thenReturn(ZoneId.systemDefault());

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			baseTime.toLocalDate());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		DirectorService directorService = directorServiceProvider.save(SERVICE_NAME_1_STR, null);

		// 정확히 1일 1분 전에 완료됨 → 대상
		LocalDateTime memberCompletedAt = baseTime.minusDays(1).minusMinutes(1);
		ServiceRequest targetRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester, baseTime);
		ServiceEstimate targetEstimate = serviceEstimateProvider.saveMemberCompleted(
			directorInfo, targetRequest, memberCompletedAt, baseTime.minusDays(1).withHour(targetHour));

		ChatRoom targetChatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(targetChatRoom, director);
		chatRoomMemberProvider.saveMember(targetChatRoom, requester);
		chatRoomServiceEstimateMappingProvider.save(targetChatRoom, targetEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		serviceEstimateFacade.sendReviewReminders(targetHour);

		entityManager.flush();
		entityManager.clear();

		// then
		ArgumentCaptor<FirebasePushEvent> eventCaptor = ArgumentCaptor.forClass(FirebasePushEvent.class);
		verify(eventPublisher, atLeast(1)).publish(eventCaptor.capture());

		List<FirebasePushEvent> capturedEvents = eventCaptor.getAllValues();
		assertThat(capturedEvents).anyMatch(event ->
			event.getCampaignSpec() == FirebaseCampaignSpec.PUSH_MEMBER_REVIEW_REMINDER
		);
	}

	@Test
	@DisplayName("리뷰 장려 스케줄러 - 아직 1일 안됨 (경계 조건) 발송 안함")
	void sendReviewReminders_lessThan1DayElapsed_noAction() {
		// given
		// 현재 시간: 14:00, memberCompletedAt: 어제 14:01 → 23시간 59분 경과 → 대상 아님
		int targetHour = 14;
		LocalDateTime baseTime = LocalDateTime.now().toLocalDate().atTime(targetHour, 0);
		Instant fixedInstant = baseTime.atZone(ZoneId.systemDefault()).toInstant();
		when(clock.instant()).thenReturn(fixedInstant);
		when(clock.getZone()).thenReturn(ZoneId.systemDefault());

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			baseTime.toLocalDate());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		DirectorService directorService = directorServiceProvider.save(SERVICE_NAME_1_STR, null);

		// 아직 1일 안됨 (23시간 59분 전에 완료) → 대상 아님
		LocalDateTime memberCompletedAt = baseTime.minusDays(1).plusMinutes(1);
		ServiceRequest targetRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester, baseTime);
		ServiceEstimate targetEstimate = serviceEstimateProvider.saveMemberCompleted(
			directorInfo, targetRequest, memberCompletedAt, baseTime.minusDays(1).withHour(targetHour));

		ChatRoom targetChatRoom = chatRoomProvider.save();
		chatRoomMemberProvider.saveDirector(targetChatRoom, director);
		chatRoomMemberProvider.saveMember(targetChatRoom, requester);
		chatRoomServiceEstimateMappingProvider.save(targetChatRoom, targetEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		serviceEstimateFacade.sendReviewReminders(targetHour);

		entityManager.flush();
		entityManager.clear();

		// then - 발송 안됨
		verify(eventPublisher, never()).publish(argThat(event ->
			event instanceof FirebasePushEvent &&
				((FirebasePushEvent) event).getCampaignSpec() == FirebaseCampaignSpec.PUSH_MEMBER_REVIEW_REMINDER
		));
	}
}
