package com.motd.be.module.member.request_location_mapping.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.request_location_mapping.entity.RequestLocationMapping;
import com.motd.be.module.member.request_location_mapping.repository.RequestLocationMappingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RequestLocationMappingCommandService {

	private final RequestLocationMappingRepository requestLocationMappingRepository;

	public void saveAll(List<RequestLocationMapping> entity) {
		requestLocationMappingRepository.saveAll(entity);
	}

	public void save(RequestLocationMapping entity) {
		requestLocationMappingRepository.save(entity);
	}
}
