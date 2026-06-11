package com.motd.be.common.scheduler;

import static com.motd.be.Constants.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateReminderStatus;
import com.motd.be.module.member.service_estimate.facade.ServiceEstimateFacade;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.shared.hackle.dto.request.HackleKakaoRequest;
import com.motd.be.shared.hackle.entity.HackleCampaignSpec;

@ControllerIntegrationTest
class ServiceEstimateReminderSchedulerTest extends BaseIntegrationTest {

	@Autowired
	private ServiceEstimateFacade serviceEstimateFacade;

	@Test
	@DisplayName("하루 전 리마인더 발송 대상이 있으면 알림톡 발송 후 SENT 상태로 변경한다.")
	void sendOneDayBeforeReminders_success() {
		// given
		LocalDateTime now = LocalDate.now().atTime(10, 0);
		Instant fixedInstant = now.atZone(ZoneId.systemDefault()).toInstant();
		when(clock.instant()).thenReturn(fixedInstant);
		when(clock.getZone()).thenReturn(ZoneId.systemDefault());

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		DirectorService directorService = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester, now);

		ServiceEstimate targetEstimate = serviceEstimateProvider.saveOngoingWithPendingReminder(
			directorInfo, serviceRequest, now, now.plusDays(1), now.minusMinutes(1));

		entityManager.flush();
		entityManager.clear();

		// when
		serviceEstimateFacade.sendOneDayBeforeReminders();

		entityManager.flush();
		entityManager.clear();

		// then
		ArgumentCaptor<List<HackleKakaoRequest>> requestsCaptor = ArgumentCaptor.forClass(List.class);
		verify(hackleEventPublisher).sendKakaoInBatch(requestsCaptor.capture());

		List<HackleKakaoRequest> capturedRequests = requestsCaptor.getValue();
		assertThat(capturedRequests).hasSize(1);
		assertThat(capturedRequests.get(0).getCampaignSpec())
			.isEqualTo(HackleCampaignSpec.KAKAO_USER_ESTIMATE_ONE_DAY_BEFORE_REMINDER);
		assertThat(capturedRequests.get(0).getReferenceId()).isEqualTo(targetEstimate.getId());

		ServiceEstimate updatedEstimate = serviceEstimateProvider.findById(targetEstimate.getId());
		assertThat(updatedEstimate.getReminderStatus()).isEqualTo(ServiceEstimateReminderStatus.SENT);
		assertThat(updatedEstimate.getReminderSentAt()).isNotNull();
	}

	@Test
	@DisplayName("이미 SENT 상태인 경우 리마인더를 중복 발송하지 않는다.")
	void sendOneDayBeforeReminders_alreadySent_noAction() {
		// given
		LocalDateTime now = LocalDate.now().atTime(10, 0);
		Instant fixedInstant = now.atZone(ZoneId.systemDefault()).toInstant();
		when(clock.instant()).thenReturn(fixedInstant);
		when(clock.getZone()).thenReturn(ZoneId.systemDefault());

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		DirectorService directorService = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester, now);

		ServiceEstimate alreadySentEstimate = serviceEstimateProvider.saveOngoingWithSentReminder(
			directorInfo, serviceRequest, now, now.plusDays(1), now.minusMinutes(1), now.minusMinutes(1));

		entityManager.flush();
		entityManager.clear();

		// when
		serviceEstimateFacade.sendOneDayBeforeReminders();

		entityManager.flush();
		entityManager.clear();

		// then
		verify(hackleEventPublisher, never()).sendKakaoInBatch(anyList());

		ServiceEstimate updatedEstimate = serviceEstimateProvider.findById(alreadySentEstimate.getId());
		assertThat(updatedEstimate.getReminderStatus()).isEqualTo(ServiceEstimateReminderStatus.SENT);
		assertThat(updatedEstimate.getReminderSentAt()).isNotNull();
	}

	@Test
	@DisplayName("발송 조건을 만족하지 않으면 리마인더를 발송하지 않는다.")
	void sendOneDayBeforeReminders_outsideWindow_noAction() {
		// given
		LocalDateTime now = LocalDate.now().atTime(10, 0);
		Instant fixedInstant = now.atZone(ZoneId.systemDefault()).toInstant();
		when(clock.instant()).thenReturn(fixedInstant);
		when(clock.getZone()).thenReturn(ZoneId.systemDefault());

		Member requester = memberProvider.saveMember(SignInPlatform.KAKAO);
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		DirectorService directorService = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, requester, now);

		ServiceEstimate outsideWindowEstimate = serviceEstimateProvider.saveOngoingWithPendingReminder(
			directorInfo, serviceRequest, now, now.plusHours(2), now.minusMinutes(1));

		entityManager.flush();
		entityManager.clear();

		// when
		serviceEstimateFacade.sendOneDayBeforeReminders();

		entityManager.flush();
		entityManager.clear();

		// then
		verify(hackleEventPublisher, never()).sendKakaoInBatch(anyList());

		ServiceEstimate updatedEstimate = serviceEstimateProvider.findById(outsideWindowEstimate.getId());
		assertThat(updatedEstimate.getReminderStatus()).isEqualTo(ServiceEstimateReminderStatus.PENDING);
		assertThat(updatedEstimate.getReminderSentAt()).isNull();
	}
}
