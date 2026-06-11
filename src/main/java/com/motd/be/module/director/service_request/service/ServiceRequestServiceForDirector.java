package com.motd.be.module.director.service_request.service;

import static com.motd.be.common.constants.PageSizeConstants.*;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.director_service_mapping.entity.DirectorServiceMapping;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member_block.service.MemberBlockQueryService;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.validator.ServiceRequestValidator;
import com.motd.be.redis.domain.repository.RedisDirectorHideRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceRequestServiceForDirector {

	private final ServiceRequestCommandServiceForDirector serviceRequestCommandServiceForDirector;
	private final ServiceRequestQueryServiceForDirector serviceRequestQueryServiceForDirector;
	private final MemberBlockQueryService memberBlockQueryService;
	private final RedisDirectorHideRequestRepository redisDirectorHideRequestRepository;
	private final ServiceRequestValidator serviceRequestValidator;

	public ServiceRequest save(ServiceRequest serviceRequest) {
		serviceRequestValidator.isDuplicateServiceRequestIn24HoursForDirectorAdditionalServiceEstimate(serviceRequest);

		return serviceRequestCommandServiceForDirector.save(serviceRequest);
	}

	public Slice<ServiceRequest> findAllForDirector(Member director, int page, List<Long> targetDirectorServiceIds,
		Boolean showOnlyDirectRequest) {
		// 페이지 정책 적용
		Pageable pageable = PageRequest.of(page, SERVICE_REQUEST_FIND_ALL_SIZE);

		// block 되거나 된 회원 아이디 조회
		List<Long> blockedMemberIds = memberBlockQueryService.findAllBlockRelatedMemberIds(director.getId())
			.stream()
			.distinct()
			.toList();

		// redis 에서 숨김 처리한 요청 조회
		Set<Long> hiddenRequestIds = redisDirectorHideRequestRepository.findAllHiddenRequests(
			director.getDirectorInfo().getId());

		return serviceRequestQueryServiceForDirector.findAllForDirector(director, targetDirectorServiceIds,
			pageable,
			blockedMemberIds,
			showOnlyDirectRequest, hiddenRequestIds);
	}

	public Integer findPendingRequestCountExcludingEstimatedByDirectorInfo(Long directorServiceId,
		DirectorInfo directorInfo, Member director) {

		List<Long> targetDirectorServiceIds = directorServiceId != null ? List.of(directorServiceId) :
			directorInfo.getDirectorServiceMappings()
				.stream()
				.map(DirectorServiceMapping::getDirectorService)
				.map(DirectorService::getId)
				.toList();

		// 숨김 처리한 요청서 목록
		Set<Long> hiddenRequestIds = redisDirectorHideRequestRepository.findAllHiddenRequests(directorInfo.getId());

		List<Long> blockedMemberIds = memberBlockQueryService.findAllBlockRelatedMemberIds(director.getId());

		return serviceRequestQueryServiceForDirector.findCountsByStatusPendingAndDirectorServiceIds(
			targetDirectorServiceIds, director, blockedMemberIds, hiddenRequestIds);
	}

	public Integer findPendingDirectRequestCountExcludingEstimatedByDirectorInfo(Long directorServiceId,
		DirectorInfo directorInfo, Member director) {

		List<Long> targetDirectorServiceIds = directorServiceId != null ? List.of(directorServiceId) :
			directorInfo.getDirectorServiceMappings()
				.stream()
				.map(DirectorServiceMapping::getDirectorService)
				.map(DirectorService::getId)
				.toList();

		// 숨김 처리한 요청서 목록
		Set<Long> hiddenRequestIds = redisDirectorHideRequestRepository.findAllHiddenRequests(directorInfo.getId());

		List<Long> blockedMemberIds = memberBlockQueryService.findAllBlockRelatedMemberIds(director.getId());

		return serviceRequestQueryServiceForDirector.findCountsByStatusPendingAndDirectorServiceIdsAndDirectRequest(
			targetDirectorServiceIds, director, blockedMemberIds, hiddenRequestIds);
	}

	public void cancelByServiceEstimate(ServiceEstimateStatus serviceEstimateStatus, ServiceRequest serviceRequest) {
		if (!serviceEstimateStatus.equals(ServiceEstimateStatus.PENDING)) {
			serviceRequest.cancel();
		}
	}

	public void hideForDirector(Member director, Long serviceRequestId) {
		if (redisDirectorHideRequestRepository.exists(director.getDirectorInfo().getId(), serviceRequestId)) {
			return;
		}

		// 존재하는지 검증
		serviceRequestQueryServiceForDirector.findByIdWithIsDeletedFalse(serviceRequestId);

		redisDirectorHideRequestRepository.hideRequest(director.getDirectorInfo().getId(), serviceRequestId);
	}

	public void increaseReceivedEstimateCount(ServiceRequest serviceRequest, Member director) {
		serviceRequest.increaseReceivedEstimateCount();
	}

	public void decreaseReceivedEstimateCount(ServiceRequest serviceRequest) {
		serviceRequest.decreaseReceivedEstimateCount();
	}
}
