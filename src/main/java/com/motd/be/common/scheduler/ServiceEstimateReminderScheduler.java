package com.motd.be.common.scheduler;

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
public class ServiceEstimateReminderScheduler {

	private final ServiceEstimateFacade serviceEstimateFacade;

	@Scheduled(cron = "${scheduler.service-estimate-reminder.cron}")
	public void sendRemindersForNextDay() {
		try {
			serviceEstimateFacade.sendOneDayBeforeReminders();
		} catch (Exception e) {
			log.error("[EstimateReminder] ServiceEstimate reminder scheduler failed", e);
		}
	}
}
