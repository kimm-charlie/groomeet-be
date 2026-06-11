package com.motd.be.module.director.service_estimate_file.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.service_estimate_file.repository.ServiceEstimateFileRepositoryForDirector;
import com.motd.be.module.member.service_estimate_file.entity.ServiceEstimateFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceEstimateFileQueryServiceForDirector {

	private final ServiceEstimateFileRepositoryForDirector serviceEstimateTemplateFileRepository;

	public List<ServiceEstimateFile> findAllByIds(List<Long> fileIds) {
		return serviceEstimateTemplateFileRepository.findAllByIds(fileIds);
	}
}
