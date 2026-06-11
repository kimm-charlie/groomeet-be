package com.motd.be.common.scheduler;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.service_request.facade.ServiceRequestFacade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"prod-blue", "prod-green", "dev-green", "dev-blue"})
public class ServiceRequestLocationExpandScheduler {

	private final ServiceRequestFacade serviceRequestFacade;

	@Scheduled(fixedRate = 60000 * 5) // 5분마다 실행
	public void expandServiceRequestLocations() {
		serviceRequestFacade.expandServiceRequestLocations();
	}
}
