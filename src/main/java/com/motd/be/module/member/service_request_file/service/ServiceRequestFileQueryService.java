package com.motd.be.module.member.service_request_file.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.FileException;
import com.motd.be.module.member.service_request_file.entity.ServiceRequestFile;
import com.motd.be.module.member.service_request_file.repository.ServiceRequestFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceRequestFileQueryService {

	private final ServiceRequestFileRepository serviceRequestFileRepository;

	public List<ServiceRequestFile> findAllByIds(List<Long> ids) {
		return serviceRequestFileRepository.findAllByIds(ids);
	}

	public ServiceRequestFile findByFileKey(String fileKey) {
		return serviceRequestFileRepository.findByFileKey(fileKey)
			.orElseThrow(() -> new CustomRuntimeException(FileException.FILE_NOT_FOUND));
	}
}
