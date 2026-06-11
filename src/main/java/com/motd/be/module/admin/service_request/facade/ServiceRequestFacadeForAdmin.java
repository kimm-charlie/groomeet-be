package com.motd.be.module.admin.service_request.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.admin.service_request.dto.response.ServiceRequestFindAllResponseForAdmin;
import com.motd.be.module.admin.service_request.dto.response.ServiceRequestFindDetailResponseForAdmin;
import com.motd.be.module.admin.service_request.service.ServiceRequestServiceForAdmin;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceRequestFacadeForAdmin {

	private final ServiceRequestServiceForAdmin serviceRequestServiceForAdmin;

	public ServiceRequestFindAllResponseForAdmin findAll(String search, ServiceRequestStatus status, int page) {
		return serviceRequestServiceForAdmin.findAll(search, status, page);
	}

	public ServiceRequestFindDetailResponseForAdmin findDetail(Long serviceRequestId) {
		return serviceRequestServiceForAdmin.findDetail(serviceRequestId);
	}
}
