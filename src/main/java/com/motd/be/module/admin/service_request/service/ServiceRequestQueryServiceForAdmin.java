package com.motd.be.module.admin.service_request.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ServiceRequestException;
import com.motd.be.module.admin.service_request.repository.ServiceRequestQueryDslRepositoryForAdmin;
import com.motd.be.module.admin.service_request.repository.ServiceRequestRepositoryForAdmin;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceRequestQueryServiceForAdmin {

	private final ServiceRequestRepositoryForAdmin serviceRequestRepositoryForAdmin;
	private final ServiceRequestQueryDslRepositoryForAdmin serviceRequestQueryDslRepositoryForAdmin;

	public long countTodayOngoingServiceRequests(ServiceRequestStatus status, LocalDateTime startOfDay,
		LocalDateTime endOfDay) {
		return serviceRequestRepositoryForAdmin.countByStatusAndOngoingAtBetween(status, startOfDay, endOfDay);
	}

	public long countServiceRequestsWithoutEstimate(ServiceRequestStatus status) {
		return serviceRequestRepositoryForAdmin.countByStatusAndNoEstimate(status);
	}

	public Slice<ServiceRequest> findAll(String search, ServiceRequestStatus status, Pageable pageable) {
		return serviceRequestQueryDslRepositoryForAdmin.findAll(search, status, pageable);
	}

	public ServiceRequest findById(Long id) {
		return serviceRequestRepositoryForAdmin.findByIdWithFetch(id)
			.orElseThrow(() -> new CustomRuntimeException(ServiceRequestException.NOT_FOUND));
	}
}
