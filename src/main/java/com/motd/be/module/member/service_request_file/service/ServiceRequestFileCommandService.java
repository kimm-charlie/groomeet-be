package com.motd.be.module.member.service_request_file.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request_file.entity.ServiceRequestFile;
import com.motd.be.module.member.service_request_file.repository.ServiceRequestFileJdbcTemplateRepository;
import com.motd.be.module.member.service_request_file.repository.ServiceRequestFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceRequestFileCommandService {

	private final ServiceRequestFileRepository serviceRequestFileRepository;
	private final ServiceRequestFileJdbcTemplateRepository serviceRequestFileJdbcTemplateRepository;

	public ServiceRequestFile save(ServiceRequestFile image) {
		return serviceRequestFileRepository.save(image);
	}

	public void updateSortOrder(Map<Long, Integer> sortOrderMap) {
		serviceRequestFileJdbcTemplateRepository.updateSortOrder(sortOrderMap);
	}

	public void mapServiceRequest(List<ServiceRequestFile> serviceRequestFiles, ServiceRequest serviceRequest) {
		serviceRequestFileRepository.mapServiceRequest(serviceRequestFiles, serviceRequest);
	}
}
