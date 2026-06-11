package com.motd.be.module.member.service_request.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;
import com.motd.be.module.member.service_request.repository.ServiceRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceRequestCommandService {

	private final ServiceRequestRepository serviceRequestRepository;

	public ServiceRequest save(ServiceRequest entity) {
		return serviceRequestRepository.save(entity);
	}

	public void updateStatusToExpired(List<ServiceRequest> serviceRequests) {
		serviceRequestRepository.updateStatusToExpired(serviceRequests, ServiceRequestStatus.EXPIRED,
			LocalDateTime.now());
	}

	public void updateStatusToCancel(List<ServiceRequest> serviceRequests) {
		serviceRequestRepository.updateStatusToCancel(serviceRequests, ServiceRequestStatus.CANCELED,
			LocalDateTime.now());
	}

	public void updateStatusToCompleted(List<Long> serviceRequestIds) {
		serviceRequestRepository.updateStatusToCompleted(serviceRequestIds, ServiceRequestStatus.COMPLETED,
			LocalDateTime.now());
	}
}
