package com.motd.be.module.admin.service_request.service;

import static com.motd.be.common.constants.PageSizeConstants.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.module.admin.service_request.dto.response.ServiceRequestFindAllResponseForAdmin;
import com.motd.be.module.admin.service_request.dto.response.ServiceRequestFindDetailResponseForAdmin;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceRequestServiceForAdmin {

	private final ServiceRequestQueryServiceForAdmin serviceRequestQueryServiceForAdmin;

	public ServiceRequestFindAllResponseForAdmin findAll(String search, ServiceRequestStatus status, int page) {
		Pageable pageable = PageRequest.of(page, SERVICE_REQUEST_FIND_ALL_SIZE);

		Slice<ServiceRequest> serviceRequests = serviceRequestQueryServiceForAdmin.findAll(search, status, pageable);
		return ServiceRequestFindAllResponseForAdmin.from(serviceRequests);
	}

	public ServiceRequestFindDetailResponseForAdmin findDetail(Long serviceRequestId) {
		ServiceRequest serviceRequest = serviceRequestQueryServiceForAdmin.findById(serviceRequestId);
		return ServiceRequestFindDetailResponseForAdmin.from(serviceRequest);
	}
}
