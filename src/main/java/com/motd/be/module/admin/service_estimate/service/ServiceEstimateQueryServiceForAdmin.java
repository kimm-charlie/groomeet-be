package com.motd.be.module.admin.service_estimate.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ServiceEstimateException;
import com.motd.be.module.admin.service_estimate.repository.ServiceEstimateQueryDslRepositoryForAdmin;
import com.motd.be.module.admin.service_estimate.repository.ServiceEstimateRepositoryForAdmin;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceEstimateQueryServiceForAdmin {

	private final ServiceEstimateRepositoryForAdmin serviceEstimateRepositoryForAdmin;
	private final ServiceEstimateQueryDslRepositoryForAdmin serviceEstimateQueryDslRepositoryForAdmin;

	public Slice<ServiceEstimate> findAll(String search, ServiceEstimateStatus status, Pageable pageable) {
		return serviceEstimateQueryDslRepositoryForAdmin.findAll(search, status, pageable);
	}

	public ServiceEstimate findById(Long id) {
		return serviceEstimateRepositoryForAdmin.findByIdWithFetch(id)
			.orElseThrow(() -> new CustomRuntimeException(ServiceEstimateException.NOT_FOUND));
	}
}
