package com.motd.be.module.director.service_request.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.service_request.repository.ServiceRequestRepositoryForDirector;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceRequestCommandServiceForDirector {

	private final ServiceRequestRepositoryForDirector serviceRequestRepositoryForDirector;

	public ServiceRequest save(ServiceRequest entity) {
		return serviceRequestRepositoryForDirector.save(entity);
	}
}
