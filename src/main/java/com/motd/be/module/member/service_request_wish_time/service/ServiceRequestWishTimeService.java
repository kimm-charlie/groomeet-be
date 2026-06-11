package com.motd.be.module.member.service_request_wish_time.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request_wish_time.entity.ServiceRequestWishTime;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceRequestWishTimeService {

	private final ServiceRequestWishTimeCommandService serviceRequestWishTimeCommandService;
	private final ServiceRequestWishTimeQueryService serviceRequestWishTimeQueryService;

	public void saveAll(ServiceRequest serviceRequest, List<LocalDateTime> wishTimes) {
		List<ServiceRequestWishTime> entities = wishTimes.stream()
			.map(time -> ServiceRequestWishTime.of(serviceRequest, time))
			.toList();

		serviceRequestWishTimeCommandService.saveAll(entities);
	}

	public void updateConfirmedWishTime(ServiceRequest serviceRequest, LocalDateTime newScheduledAt) {
		List<ServiceRequestWishTime> allWishTimes = serviceRequestWishTimeQueryService.findAllByServiceRequest(
			serviceRequest);
		allWishTimes.stream()
			.filter(wt -> Boolean.TRUE.equals(wt.getIsConfirmed()))
			.findFirst()
			.ifPresent(wt -> wt.updateWishTime(newScheduledAt));
	}

	public void confirmWishTime(ServiceRequest serviceRequest, LocalDateTime scheduledAt) {
		if (scheduledAt == null) {
			return;
		}

		LocalDateTime truncatedScheduledAt = scheduledAt.truncatedTo(ChronoUnit.MINUTES);

		List<ServiceRequestWishTime> allWishTimes = serviceRequestWishTimeQueryService.findAllByServiceRequest(
			serviceRequest);

		// 기존 confirmed 전부 해제 후 새로 설정 (무결성 보장)
		allWishTimes.forEach(ServiceRequestWishTime::unconfirm);

		allWishTimes.stream()
			.filter(wt -> wt.getWishTime().truncatedTo(ChronoUnit.MINUTES).equals(truncatedScheduledAt))
			.findFirst()
			.ifPresent(ServiceRequestWishTime::confirm);
	}
}
