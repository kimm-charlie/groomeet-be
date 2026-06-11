package com.motd.be.module.member.service_request.service;

import static com.motd.be.common.constants.Constants.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ServiceRequestException;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;
import com.motd.be.module.member.service_request.repository.ServiceRequestQueryDslRepository;
import com.motd.be.module.member.service_request.repository.ServiceRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceRequestQueryService {

	private final ServiceRequestRepository serviceRequestRepository;
	private final ServiceRequestQueryDslRepository serviceRequestQueryDslRepository;

	public Slice<ServiceRequest> findAllForMember(Member member, Long directorServiceId, Boolean showOnlyPending,
		Pageable pageable) {
		return serviceRequestQueryDslRepository.findAllForMember(member, directorServiceId, showOnlyPending, pageable);
	}

	public ServiceRequest findByIdWithDirectorService(Long serviceRequestId) {
		return serviceRequestRepository.findByIdWithDirectorService(serviceRequestId)
			.orElseThrow(() -> new CustomRuntimeException(ServiceRequestException.NOT_FOUND));
	}

	public ServiceRequest findById(Long serviceRequestId) {
		return serviceRequestRepository.findById(serviceRequestId)
			.orElseThrow(() -> new CustomRuntimeException(ServiceRequestException.NOT_FOUND));
	}

	public List<ServiceRequest> findAllByIdsWithIsDeletedFalseAndStatusPending(List<Long> expiredRequestIds) {
		return serviceRequestRepository.findAllByIdWithIsDeletedFalse(expiredRequestIds, ServiceRequestStatus.PENDING);
	}

	public Map<ServiceRequestStatus, List<ServiceRequest>> findAllByMemberNotYetEnded(Member member) {
		return serviceRequestRepository.findAllByMemberNotYetEnded(member, ENDED_SERVICE_REQUEST_STATUSES)
			.stream()
			.collect(Collectors.groupingBy(ServiceRequest::getStatus));
	}

	public Map<ServiceRequestStatus, Integer> countByMemberId(Long memberId) {
		return serviceRequestRepository.countByDirectorInfoGroupByStatus(memberId)
			.stream()
			.collect(Collectors.toMap(row -> (ServiceRequestStatus)row[0], row -> ((Long)row[1]).intValue()));
	}

	/**
	 * 요청가 존재하는 카테고리만 보여주는 용도다
	 *
	 * @param member
	 * @param showOnlyPending
	 * @return
	 */
	public List<ServiceRequest> findDirectorServicesByMember(Member member, Boolean showOnlyPending) {
		return serviceRequestQueryDslRepository.findDirectorServicesByMember(member, showOnlyPending);
	}

	public boolean existsByMemberAndDirectorServiceInLast24Hours(Member member, DirectorService directorService,
		LocalDateTime oneDaysAgo) {
		return serviceRequestRepository.existsByMemberAndDirectorServiceInLast24Hours(member, directorService,
			oneDaysAgo);
	}

	public List<ServiceRequest> findAllForLocationExpansion(List<Long> serviceRequestIds, int maxEstimateCount) {
		return serviceRequestRepository.findAllForLocationExpansion(serviceRequestIds, ServiceRequestStatus.PENDING,
			maxEstimateCount);
	}

	public List<Long> findIdsForLocationExpansionBefore(LocalDateTime expandThreshold, int maxEstimateCount) {
		return serviceRequestRepository.findIdsForLocationExpansionBefore(expandThreshold,
			ServiceRequestStatus.PENDING, maxEstimateCount);
	}

	public boolean existsNotEndedRequestBetweenMemberAndDirector(Long memberId, Long targetMemberId) {
		return serviceRequestRepository.existsNotEndedRequestBetweenMemberAndDirector(memberId, targetMemberId,
			ENDED_SERVICE_REQUEST_STATUSES, ServiceEstimateStatus.CANCELED);
	}

	public List<ServiceRequest> findAllExpiredBefore(LocalDateTime now, ServiceRequestStatus serviceRequestStatus) {
		return serviceRequestRepository.findAllExpiredBefore(now, serviceRequestStatus);
	}
}
