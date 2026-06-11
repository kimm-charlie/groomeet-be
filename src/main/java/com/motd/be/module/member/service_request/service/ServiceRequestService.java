package com.motd.be.module.member.service_request.service;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.constants.PageSizeConstants.*;
import static com.motd.be.common.constants.TimePolicy.*;
import static com.motd.be.common.constants.ValidationConstants.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.director_service.dto.response.DirectorServiceResponse;
import com.motd.be.module.member.director_service.validator.DirectorServiceValidator;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.entity.LocationType;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.service.ServiceEstimateQueryService;
import com.motd.be.module.member.service_request.dto.request.ServiceRequestSaveDirectRequest;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;
import com.motd.be.module.member.service_request.entity.StopReceivingEstimateReason;
import com.motd.be.module.member.service_request.repository.ServiceRequestJdbcRepository;
import com.motd.be.module.member.service_request.repository.ServiceRequestJdbcRepository.LocationExpansionUpdate;
import com.motd.be.module.member.service_request.validator.ServiceRequestValidator;
import com.motd.be.redis.domain.repository.RedisServiceRequestLocationExpandRepository;
import com.motd.be.shared.firebase.dto.FirebasePushEvent;
import com.motd.be.shared.firebase.service.FirebaseEventPublisher;
import com.motd.be.shared.firebase.service.FirebasePushFactory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceRequestService {

	private final ServiceRequestCommandService serviceRequestCommandService;
	private final ServiceRequestQueryService serviceRequestQueryService;
	private final ServiceRequestValidator serviceRequestValidator;
	private final MemberQueryService memberQueryService;
	private final DirectorServiceValidator directorServiceValidator;
	private final ServiceEstimateQueryService serviceEstimateQueryService;
	private final ServiceRequestJdbcRepository serviceRequestJdbcRepository;
	private final RedisServiceRequestLocationExpandRepository redisServiceRequestLocationExpandRepository;
	private final FirebaseEventPublisher firebaseEventPublisher;
	private final FirebasePushFactory firebasePushFactory;

	public ServiceRequest save(ServiceRequest serviceRequest) {
		return serviceRequestCommandService.save(serviceRequest);
	}

	public Slice<ServiceRequest> findAllForMember(Member member, Long categoryId, Boolean showOnlyPending, int page) {
		Pageable pageable = PageRequest.of(page, SERVICE_REQUEST_FIND_ALL_SIZE);

		return serviceRequestQueryService.findAllForMember(member, categoryId, showOnlyPending, pageable);
	}

	public ServiceRequest validateAndGetOwnedRequest(Long memberId, Long serviceRequestId) {
		// 1. 요청 조회
		ServiceRequest serviceRequest = serviceRequestQueryService.findByIdWithDirectorService(serviceRequestId);

		// 2. 소유 검증
		serviceRequestValidator.validateOwnership(serviceRequest, memberId);

		return serviceRequest;
	}

	public ServiceRequest findByIdAndValidateOwnership(Long memberId, Long serviceRequestId) {
		ServiceRequest serviceRequest = serviceRequestQueryService.findById(serviceRequestId);
		serviceRequestValidator.validateOwnership(serviceRequest, memberId);
		return serviceRequest;
	}

	public void updateIsReceivingEstimate(Member member, Long serviceRequestId,
		StopReceivingEstimateReason stopReceivingEstimateReason) {
		ServiceRequest serviceRequest = serviceRequestQueryService.findById(serviceRequestId);
		serviceRequestValidator.validateOwnership(serviceRequest, member.getId());
		serviceRequest.updateReceivingEstimateToCancel(stopReceivingEstimateReason);
	}

	public void expireServiceRequests(List<ServiceRequest> serviceRequests) {
		// PENDING 상태로 업데이트
		serviceRequestCommandService.updateStatusToExpired(serviceRequests);
	}

	/**
	 * 유저가 특정 디렉터에게 다이렉트 요청을 할경우 검증 및 업데이트 를 하는 메서드 이다.
	 *
	 * @param serviceRequest
	 * @param request
	 */
	public Member validateDirectRequest(ServiceRequest serviceRequest, ServiceRequestSaveDirectRequest request) {
		// 자기자신한테 요청하는지 확인
		serviceRequestValidator.validateNotSelfDirectRequest(serviceRequest.getMember(),
			request.getDirectRequestedMemberId());

		// 1. 디렉터 조회
		Member director = memberQueryService.findByIdWithDirector(request.getDirectRequestedMemberId());

		// 차단여부 검증
		serviceRequestValidator.isMemberBlockedOrBlock(director, serviceRequest.getMember());

		// 2. 디렉터가 제공하는 서비스 인지 확인
		directorServiceValidator.validateServiceOwnership(director.getDirectorInfo(),
			serviceRequest.getDirectorService().getId());

		// 3. 진행 중인 요청 존재 여부 확인
		serviceRequestValidator.validateOngoingRequestWithDirector(
			serviceEstimateQueryService.existsOngoingEstimateBetween(serviceRequest.getMember(), director));

		// 4. serviceRequest 에 direct 요청 정보 업데이트
		serviceRequest.updateToDirectRequest(director);

		return director;
	}

	public List<ServiceRequest> cancelAllByMember(Member member) {
		Map<ServiceRequestStatus, List<ServiceRequest>> serviceRequestsMap = serviceRequestQueryService.findAllByMemberNotYetEnded(
			member);

		if (serviceRequestsMap.isEmpty()) {
			return List.of();
		}

		// 진행중인 요청서가 있다면 예외 처리
		List<ServiceRequest> ongoingRequests = serviceRequestsMap.getOrDefault(ServiceRequestStatus.ONGOING, List.of());
		serviceRequestValidator.isOngoingRequestExist(ongoingRequests);

		// PENDING 인 요청서들은 취소 처리
		List<ServiceRequest> pendingRequests = serviceRequestsMap.get(ServiceRequestStatus.PENDING);
		serviceRequestCommandService.updateStatusToCancel(pendingRequests);

		return pendingRequests;
	}

	public void completeByServiceEstimateIfNeeded(ServiceEstimate serviceEstimate) {
		serviceEstimate.getServiceRequest().updateToCompleted();
	}

	public List<DirectorServiceResponse> findServicesRelatedToServiceRequest(Member member, Boolean showOnlyPending) {
		List<ServiceRequest> serviceRequests = serviceRequestQueryService.findDirectorServicesByMember(member,
			showOnlyPending);
		return DirectorServiceResponse.fromList(
			serviceRequests.stream().map(ServiceRequest::getDirectorService).distinct().toList());
	}

	public void updateToOngoingStatus(ServiceRequest serviceRequest) {
		serviceRequest.updateToOngoingStatus();
	}

	@Transactional
	public void expandServiceRequestLocations(LocalDateTime now) {
		long nowEpoch = now.atZone(KST).toEpochSecond();

		LocalDateTime expandThreshold = now.minusHours(SERVICE_REQUEST_LOCATION_EXPAND_HOURS);
		List<Long> databaseTargetIds = serviceRequestQueryService.findIdsForLocationExpansionBefore(expandThreshold,
			MAX_RECEIVED_ESTIMATE_COUNT);

		List<Long> targetRequestIds = redisServiceRequestLocationExpandRepository.findExpiredRequestIds(nowEpoch);

		List<Long> targetRequestIdsWithFallback = Stream.concat(targetRequestIds.stream(),
				databaseTargetIds.stream())
			.distinct()
			.toList();

		expandServiceRequestLocationsByIds(targetRequestIdsWithFallback, now);

		redisServiceRequestLocationExpandRepository.removeExpiredRequestsFromRedis(nowEpoch);
	}

	private void expandServiceRequestLocationsByIds(List<Long> targetRequestIds, LocalDateTime now) {
		if (targetRequestIds.isEmpty()) {
			return;
		}

		List<ServiceRequest> serviceRequests = serviceRequestQueryService.findAllForLocationExpansion(
			targetRequestIds, MAX_RECEIVED_ESTIMATE_COUNT);

		List<LocationExpansionUpdate> updates = serviceRequests.stream()
			.map(serviceRequest -> {
				Location location = serviceRequest.getRequestLocationMappings().get(0).getLocation();

				if (location.getType() != LocationType.DISTRICT) {
					return null;
				}

				Location expandedLocation = location.getParent();

				return new LocationExpansionUpdate(serviceRequest.getId(), expandedLocation.getId(), now);
			})
			.filter(Objects::nonNull)
			.toList();

		if (updates.isEmpty()) {
			return;
		}

		serviceRequestJdbcRepository.batchUpdateExpandedLocations(updates);
	}

	public boolean existsNotEndedRequestBetweenMemberAndDirector(Long memberId, Long targetMemberId) {
		if (memberId == null) {
			return false;
		}

		// 다이렉트 요청이 있거나, 진행하고 있는 요청이 있는지 조회
		return serviceRequestQueryService.existsNotEndedRequestBetweenMemberAndDirector(memberId, targetMemberId);
	}

	public List<ServiceRequest> findExpiredRequests(LocalDateTime now, List<Long> expiredRequestIds) {
		List<ServiceRequest> expiredFromRedis = expiredRequestIds.isEmpty()
			? List.of()
			: serviceRequestQueryService.findAllByIdsWithIsDeletedFalseAndStatusPending(expiredRequestIds);

		List<ServiceRequest> expiredFromDatabase = serviceRequestQueryService.findAllExpiredBefore(now,
			ServiceRequestStatus.PENDING);

		if (expiredFromRedis.isEmpty()) {
			return expiredFromDatabase;
		}

		if (expiredFromDatabase.isEmpty()) {
			return expiredFromRedis;
		}

		Map<Long, ServiceRequest> deduplicated = new LinkedHashMap<>();
		Stream.concat(expiredFromRedis.stream(), expiredFromDatabase.stream())
			.forEach(serviceRequest -> deduplicated.put(serviceRequest.getId(), serviceRequest));

		return new ArrayList<>(deduplicated.values());
	}

	public void sendPushForNewDirectRequest(ServiceRequest serviceRequest, Member directRequestedDirector) {
		if (!directRequestedDirector.getIsActivityPushAgreed()) {
			return;
		}

		FirebasePushEvent event = firebasePushFactory.directorDirectRequestReceived(serviceRequest,
			directRequestedDirector);

		firebaseEventPublisher.sendPush(event);
	}
}
