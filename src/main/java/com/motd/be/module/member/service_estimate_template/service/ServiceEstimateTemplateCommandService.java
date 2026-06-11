package com.motd.be.module.member.service_estimate_template.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.service_estimate_template.repository.ServiceEstimateTemplateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceEstimateTemplateCommandService {

	private final ServiceEstimateTemplateRepository serviceEstimateTemplateRepository;

	public void deleteAllByDirectorInfo(DirectorInfo directorInfo) {
		serviceEstimateTemplateRepository.deleteAllByDirectorInfo(directorInfo);
	}

}
