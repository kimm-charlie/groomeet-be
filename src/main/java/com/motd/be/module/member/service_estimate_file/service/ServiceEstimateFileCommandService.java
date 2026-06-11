package com.motd.be.module.member.service_estimate_file.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.service_estimate_file.entity.ServiceEstimateFile;
import com.motd.be.module.member.service_estimate_file.repository.ServiceEstimateFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceEstimateFileCommandService {

	private final ServiceEstimateFileRepository serviceEstimateFileRepository;

	public ServiceEstimateFile save(ServiceEstimateFile serviceEstimateFile) {
		return serviceEstimateFileRepository.save(serviceEstimateFile);
	}
}
