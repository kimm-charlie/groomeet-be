package com.motd.be.module.director.service_request.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ServiceRequestException;
import com.motd.be.module.director.service_request.repository.ServiceRequestQueryDslRepositoryForDirector;
import com.motd.be.module.director.service_request.repository.ServiceRequestRepositoryForDirector;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceRequestQueryServiceForDirector {

	private final ServiceRequestRepositoryForDirector serviceRequestRepositoryForDirector;
	private final ServiceRequestQueryDslRepositoryForDirector serviceRequestQueryDslRepositoryForDirector;

	public Slice<ServiceRequest> findAllForDirector(Member director, List<Long> directorServiceIds, Pageable pageable,
		List<Long> blockedMemberIds, Boolean showOnlyDirectRequest, Set<Long> hiddenRequestIds) {
		return serviceRequestQueryDslRepositoryForDirector.findAllForDirector(director, directorServiceIds, pageable,
			blockedMemberIds, showOnlyDirectRequest, hiddenRequestIds);
	}

	public ServiceRequest findByIdWithDirectorServiceAndMemberByStatusPendingAndReceivingEstimateTrue(
		Long serviceRequestId) {
		return serviceRequestRepositoryForDirector.findByIdWithDirectorServiceAndStatusPendingAndReceivingEstimateTrue(
				serviceRequestId, LocalDateTime.now(), ServiceRequestStatus.PENDING)
			.orElseThrow(() -> new CustomRuntimeException(ServiceRequestException.NOT_FOUND));
	}

	public ServiceRequest findByIdWithDirectorService(Long serviceRequestId) {
		return serviceRequestRepositoryForDirector.findByIdWithDirectorService(serviceRequestId)
			.orElseThrow(() -> new CustomRuntimeException(ServiceRequestException.NOT_FOUND));
	}

	public Integer findCountsByStatusPendingAndDirectorServiceIds(List<Long> directorServiceIds, Member director,
		List<Long> blockedMemberIds, Set<Long> hiddenRequestIds) {
		return serviceRequestQueryDslRepositoryForDirector.findCountsByStatusPendingAndDirectorServiceIds(
			directorServiceIds, director, blockedMemberIds, hiddenRequestIds);
	}

	public Integer findCountsByStatusPendingAndDirectorServiceIdsAndDirectRequest(List<Long> directorServiceIds,
		Member director, List<Long> blockedMemberIds, Set<Long> hiddenRequestIds) {
		return serviceRequestQueryDslRepositoryForDirector.findCountsByStatusPendingAndDirectorServiceIdsAndDirectRequest(
			directorServiceIds, director, blockedMemberIds, hiddenRequestIds);
	}

	public Map<ServiceRequestStatus, Integer> countByMemberId(Long memberId) {
		return serviceRequestRepositoryForDirector.countByDirectorInfoGroupByStatus(memberId)
			.stream()
			.collect(Collectors.toMap(
				row -> (ServiceRequestStatus)row[0],
				row -> ((Long)row[1]).intValue()
			));
	}

	public ServiceRequest findByIdWithIsDeletedFalse(Long serviceRequestId) {
		return serviceRequestRepositoryForDirector.findByIdWithIsDeletedFalse(serviceRequestId)
			.orElseThrow(() -> new CustomRuntimeException(ServiceRequestException.NOT_FOUND));
	}
}
