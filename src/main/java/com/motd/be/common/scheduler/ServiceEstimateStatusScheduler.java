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
@Profile({"prod-blue", "prod-green", "dev-green", "dev-blue"})
public class ServiceEstimateStatusScheduler {

	private final ServiceEstimateFacade serviceEstimateFacade;

	@Scheduled(cron = "${scheduler.service-estimate-status-update.cron}")
	public void updateServiceStatuses() {
		// 예약일이 하루 지난 시점에 제안(디렉터 완료) 를 완료 처리 한다
		serviceEstimateFacade.completeEstimatesAfterScheduleCompleted();

		// 디렉터 완료 이후, 요청인이 서비스 완료를 누르지 않은 상태에서 3일이 지난 제안, 제안을 모두 사용자 완료 처리 한다
		serviceEstimateFacade.completeEstimatesAfterDirectorCompleted();
	}
}
