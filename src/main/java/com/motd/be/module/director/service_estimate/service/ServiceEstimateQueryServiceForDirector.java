package com.motd.be.module.director.service_estimate.service;

import static com.motd.be.common.constants.Constants.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ServiceEstimateException;
import com.motd.be.module.director.service_estimate.repository.ServiceEstimateQueryDslRepositoryForDirector;
import com.motd.be.module.director.service_estimate.repository.ServiceEstimateRepositoryForDirector;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceEstimateQueryServiceForDirector {

	private final ServiceEstimateRepositoryForDirector serviceEstimateRepositoryForDirector;
	private final ServiceEstimateQueryDslRepositoryForDirector serviceEstimateQueryDslRepositoryForDirector;

	/**
	 * 모든 요청에 대한 제안 갯수
	 *
	 * @param content
	 * @return
	 */
	public Map<Long, Integer> countEstimatesByServiceRequests(List<ServiceRequest> content) {
		return serviceEstimateRepositoryForDirector.countEstimatesByServiceRequests(content,
				ServiceEstimateStatus.CANCELED)
			.stream()
			.collect(Collectors.toMap(
				row -> (Long)row[0],
				row -> ((Long)row[1]).intValue()
			));
	}

	public Slice<ServiceEstimate> findAll(ServiceEstimateStatus status, DirectorInfo directorInfo,
		Pageable pageable, Long directorServiceId, Boolean showOnlyDirectRequest) {
		return serviceEstimateQueryDslRepositoryForDirector.findAll(status, directorInfo, pageable, directorServiceId,
			showOnlyDirectRequest);
	}

	/**
	 * 특정 요청에 대한 제안 갯수
	 *
	 * @param serviceRequest
	 * @return
	 */
	public Integer countEstimatesByServiceRequest(ServiceRequest serviceRequest) {
		return serviceEstimateRepositoryForDirector.countEstimatesByServiceRequest(serviceRequest,
			ServiceEstimateStatus.CANCELED);
	}

	public ServiceEstimate findByIdWithServiceRequestWithLock(Long serviceEstimateId) {
		return serviceEstimateRepositoryForDirector.findByIdWithServiceRequestWithLock(serviceEstimateId)
			.orElseThrow(() -> new CustomRuntimeException(ServiceEstimateException.NOT_FOUND));
	}

	public Map<ServiceEstimateStatus, Integer> countByDirectorInfo(DirectorInfo directorInfo, Long directorServiceId) {
		return serviceEstimateRepositoryForDirector.countByDirectorInfoGroupByStatus(directorInfo, directorServiceId)
			.stream()
			.collect(Collectors.toMap(
				row -> (ServiceEstimateStatus)row[0],
				row -> ((Long)row[1]).intValue()
			));
	}

	public Map<ServiceEstimateStatus, Integer> countByDirectorInfoAndDirectRequest(DirectorInfo directorInfo,
		Long directorServiceId, Member director) {
		return serviceEstimateRepositoryForDirector.countByDirectorInfoAndDirectRequestGroupByStatus(directorInfo,
				directorServiceId, director)
			.stream()
			.collect(Collectors.toMap(
				row -> (ServiceEstimateStatus)row[0],
				row -> ((Long)row[1]).intValue()
			));
	}

	public ServiceEstimate findByIdWithServiceRequestAndMemberAndDirectorWithLock(
		Long serviceEstimateId) {
		return serviceEstimateRepositoryForDirector.findByIdWithServiceRequestAndMemberAndDirectorWithLock(
				serviceEstimateId)
			.orElseThrow(() -> new CustomRuntimeException(ServiceEstimateException.NOT_FOUND));
	}

	public ServiceEstimate findByIdWithServiceRequestLock(Long serviceEstimateId) {
		return serviceEstimateRepositoryForDirector.findByIdWithServiceRequestLock(serviceEstimateId)
			.orElseThrow(() -> new CustomRuntimeException(ServiceEstimateException.NOT_FOUND));
	}

	public ServiceEstimate findByIdWithIsDeletedFalse(Long serviceEstimateId) {
		return serviceEstimateRepositoryForDirector.findByIdWithIsDeletedFalse(serviceEstimateId)
			.orElseThrow(() -> new CustomRuntimeException(ServiceEstimateException.NOT_FOUND));
	}

	public Slice<ServiceEstimate> findServiceEstimateHistoriesForDirector(DirectorInfo directorInfo,
		Pageable pageable) {
		return serviceEstimateQueryDslRepositoryForDirector.findServiceEstimateHistoriesForDirector(directorInfo,
			pageable,
			COMPLETED_ESTIMATE_STATUSES);
	}
}
