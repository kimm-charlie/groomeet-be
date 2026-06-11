package com.motd.be.module.director.request_location_mapping.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.request_location_mapping.repository.RequestLocationMappingRepositoryForDirector;
import com.motd.be.module.member.request_location_mapping.entity.RequestLocationMapping;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RequestLocationMappingCommandServiceForDirector {

	private final RequestLocationMappingRepositoryForDirector requestLocationMappingRepositoryForDirector;

	public void save(RequestLocationMapping entity) {
		requestLocationMappingRepositoryForDirector.save(entity);
	}
}
