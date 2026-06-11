package com.motd.be.common.scheduler;

import java.time.LocalDateTime;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.service_estimate.facade.ServiceEstimateFacade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"prod-blue", "prod-green", "dev-blue", "dev-green"})
public class ReviewReminderScheduler {

	private final ServiceEstimateFacade serviceEstimateFacade;

	/**
	 * 리뷰 작성 장려 Push 발송 스케줄러
	 * 매시 00분에 실행하여, 해당 시간대 제안 수락의 리뷰 장려 Push 발송
	 */
	@Scheduled(cron = "${scheduler.review-reminder.cron}")
	public void sendReviewReminders() {
		int currentHour = LocalDateTime.now().getHour();

		try {
			serviceEstimateFacade.sendReviewReminders(currentHour);
		} catch (Exception e) {
			log.error("[ReviewReminder] Review reminder scheduler failed for hour={}", currentHour, e);
		}
	}
}
