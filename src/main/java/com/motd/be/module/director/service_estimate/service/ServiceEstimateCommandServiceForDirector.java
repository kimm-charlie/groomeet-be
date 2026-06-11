package com.motd.be.module.director.service_estimate.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.service_estimate.repository.ServiceEstimateRepositoryForDirector;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceEstimateCommandServiceForDirector {

	private final ServiceEstimateRepositoryForDirector serviceEstimateRepositoryForDirector;

	public ServiceEstimate save(ServiceEstimate entity) {
		return serviceEstimateRepositoryForDirector.save(entity);
	}
}
