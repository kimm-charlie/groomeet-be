package com.motd.be.module.member.service_request_wish_time.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.service_request_wish_time.entity.ServiceRequestWishTime;
import com.motd.be.module.member.service_request_wish_time.repository.ServiceRequestWishTimeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceRequestWishTimeCommandService {

	private final ServiceRequestWishTimeRepository serviceRequestWishTimeRepository;

	public void saveAll(List<ServiceRequestWishTime> entities) {
		serviceRequestWishTimeRepository.saveAll(entities);
	}
}
