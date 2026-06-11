package com.motd.be.common.scheduler;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.service_request.facade.ServiceRequestFacade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"prod-blue", "prod-green", "dev-green", "dev-blue"})
public class ServiceRequestExpireScheduler {

	private final ServiceRequestFacade serviceRequestFacade;

	@Scheduled(fixedRate = 60000) // 1분 간격 실행
	@Transactional
	public void expireServiceRequests() {
		serviceRequestFacade.expireServiceRequests();
	}
}
