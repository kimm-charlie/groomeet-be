package com.motd.be.module.member.service_estimate_file.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.FileException;
import com.motd.be.module.member.service_estimate_file.entity.ServiceEstimateFile;
import com.motd.be.module.member.service_estimate_file.repository.ServiceEstimateFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceEstimateFileQueryService {

	private final ServiceEstimateFileRepository serviceEstimateTemplateFileRepository;

	public List<ServiceEstimateFile> findAllByIds(List<Long> fileIds) {
		return serviceEstimateTemplateFileRepository.findAllByIds(fileIds);
	}

	public ServiceEstimateFile findByFileKey(String fileKey) {
		return serviceEstimateTemplateFileRepository.findByFileKey(fileKey)
			.orElseThrow(() -> new CustomRuntimeException(FileException.FILE_NOT_FOUND));
	}
}
