package com.motd.be.module.member.service_request_wish_time.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request_wish_time.entity.ServiceRequestWishTime;
import com.motd.be.module.member.service_request_wish_time.repository.ServiceRequestWishTimeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceRequestWishTimeQueryService {

	private final ServiceRequestWishTimeRepository serviceRequestWishTimeRepository;

	public List<ServiceRequestWishTime> findAllByServiceRequest(ServiceRequest serviceRequest) {
		return serviceRequestWishTimeRepository.findAllByServiceRequest(serviceRequest);
	}
}
