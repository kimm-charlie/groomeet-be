package com.motd.be.module.member.service_request.facade;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.utils.DateFormatUtils.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.common.utils.DateFormatUtils;
import com.motd.be.module.member.director_service.dto.response.DirectorServiceResponse;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.director_service.service.DirectorServiceService;
import com.motd.be.module.member.location.entity.LocationType;
import com.motd.be.module.member.location.service.LocationService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;
import com.motd.be.module.member.member.validator.MemberValidator;
import com.motd.be.module.member.service_estimate.service.ServiceEstimateQueryService;
import com.motd.be.module.member.service_estimate.service.ServiceEstimateService;
import com.motd.be.module.member.service_request.dto.request.ServiceRequestSaveDirectRequest;
import com.motd.be.module.member.service_request.dto.request.ServiceRequestSaveRequest;
import com.motd.be.module.member.service_request.dto.request.ServiceRequestUpdateReceivingEstimateRequest;
import com.motd.be.module.member.service_request.dto.response.ServiceRequestFindAllResponseForPublic;
import com.motd.be.module.member.service_request.dto.response.ServiceRequestFindCountResponseForPublic;
import com.motd.be.module.member.service_request.dto.response.ServiceRequestFindDetailResponseForPublic;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;
import com.motd.be.module.member.service_request.service.ServiceRequestQueryService;
import com.motd.be.module.member.service_request.service.ServiceRequestService;
import com.motd.be.module.member.service_request.validator.ServiceRequestValidator;
import com.motd.be.module.member.service_request_file.service.ServiceRequestFileService;
import com.motd.be.module.member.service_request_wish_time.service.ServiceRequestWishTimeService;
import com.motd.be.module.member.time_slot.validator.TimeSlotValidator;
import com.motd.be.redis.domain.repository.RedisServiceRequestExpireRepository;
import com.motd.be.redis.domain.repository.RedisServiceRequestLocationExpandRepository;
import com.motd.be.shared.forbidden_word.validator.ForbiddenWordValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceRequestFacade {

	private final Clock clock;
	private final ServiceRequestService serviceRequestService;
	private final MemberQueryService memberQueryService;
	private final LocationService locationService;
	private final DirectorServiceService directorServiceService;
	private final ServiceEstimateQueryService serviceEstimateQueryService;
	private final RedisServiceRequestExpireRepository redisServiceRequestExpireRepository;
	private final ServiceRequestQueryService serviceRequestQueryService;
	private final ServiceEstimateService serviceEstimateService;
	private final ServiceRequestFileService serviceRequestFileService;
	private final ServiceRequestValidator serviceRequestValidator;
	private final RedisServiceRequestLocationExpandRepository redisServiceRequestLocationExpandRepository;
	private final MemberValidator memberValidator;
	private final ForbiddenWordValidator forbiddenWordValidator;
	private final ServiceRequestWishTimeService serviceRequestWishTimeService;
	private final TimeSlotValidator timeSlotValidator;

	@Transactional
	public void save(Long memberId, ServiceRequestSaveRequest request) {
		// 1. 회원 조회 (락 걸기)
		Member member = memberQueryService.findByIdWithLock(memberId);

		memberValidator.isAuthenticatedMember(member);

		// 2. 디렉터 서비스 조회 + 카테고리 유효성 검증
		DirectorService directorService = directorServiceService.validateAndFindForRequest(
			request.getDirectorServiceId());

		// 24시간내 같은 서비스 요청를 보낸 기록이 있는지 조회
		serviceRequestValidator.isDuplicateServiceRequestIn24Hours(member, directorService);

		// 금칙어 검증
		forbiddenWordValidator.validate(request.getAiContent());

		// 3. 서비스 요청 생성
		ServiceRequest serviceRequest = serviceRequestService.save(request.toEntity(member, directorService));

		// 4. 희망시간 저장 (LocalDateTime 문자열 파싱)
		List<LocalDateTime> wishTimes = request.getWishTimes().stream()
			.map(DateFormatUtils::parseToLocalDateTime)
			.toList();
		timeSlotValidator.validateWishTimes(wishTimes);
		serviceRequestWishTimeService.saveAll(serviceRequest, wishTimes);

		// 5. 지역(Location) 매핑 저장
		LocationType baseLocationType = locationService.saveRequestLocations(serviceRequest, request.getLocationIds());

		// 6. 이미지 매핑
		serviceRequestFileService.mapServiceRequest(serviceRequest, request.getFileIds(), member);

		// 7. Redis 에 만료 스케줄링 등록
		redisServiceRequestExpireRepository.save(serviceRequest);

		// 8. Redis 에 지역 자동 확장 스케줄링 등록 (구 단위 선택시에만)
		if (baseLocationType == LocationType.DISTRICT) {
			redisServiceRequestLocationExpandRepository.save(serviceRequest);
		}
	}

	@Transactional
	public void saveDirectRequest(Long memberId, ServiceRequestSaveDirectRequest request) {
		// 1. 회원 조회 (락 걸기)
		Member member = memberQueryService.findByIdWithLock(memberId);

		memberValidator.isAuthenticatedMember(member);

		// 2. 디렉터 서비스 조회 + 카테고리 유효성 검증
		DirectorService directorService = directorServiceService.validateAndFindForRequest(
			request.getDirectorServiceId());

		// 24시간내 같은 서비스 요청를 보낸 기록이 있는지 조회
		serviceRequestValidator.isDuplicateServiceRequestIn24Hours(member, directorService);

		// 금칙어 검증
		forbiddenWordValidator.validate(request.getAdditionalRequest());

		// 3. 서비스 요청 생성
		ServiceRequest serviceRequest = serviceRequestService.save(request.toEntity(member, directorService));

		// 4. 희망시간 저장 (LocalDateTime 문자열 파싱)
		List<LocalDateTime> wishTimes = request.getWishTimes().stream()
			.map(DateFormatUtils::parseToLocalDateTime)
			.toList();
		timeSlotValidator.validateWishTimes(wishTimes);
		serviceRequestWishTimeService.saveAll(serviceRequest, wishTimes);

		// 5. 지역(Location) 매핑 저장 (전국으로 저장)
		locationService.saveRequestLocationForAllCity(serviceRequest);

		// 6. 다이렉트 요청 여부 검증, block 조회
		Member director = serviceRequestService.validateDirectRequest(serviceRequest, request);

		// 7. push 발송
		serviceRequestService.sendPushForNewDirectRequest(serviceRequest, director);
	}

	// 회원용 요청 목록 조회
	public ServiceRequestFindAllResponseForPublic findAllForMember(Long memberId, Boolean showOnlyPending, int page,
		Long directorServiceId) {

		// 1. 회원 조회
		Member member = memberQueryService.findById(memberId);

		// 2. 요청 목록 조회
		Slice<ServiceRequest> serviceRequests = serviceRequestService.findAllForMember(member, directorServiceId,
			showOnlyPending, page);

		// 3. 요청별 받은 제안갯수 조회
		Map<Long, Integer> receivedEstimateCountByRequestIds = serviceEstimateQueryService.countEstimatesByServiceRequests(
			serviceRequests.getContent());

		// 4. 요청별 받은 제안의 디렉터 회원 조회
		Map<Long, List<Member>> directorsByRequestIds = serviceEstimateQueryService.findDirectorsByServiceRequests(
			serviceRequests);

		// 3. 응답 조립
		return ServiceRequestFindAllResponseForPublic.of(serviceRequests, receivedEstimateCountByRequestIds,
			directorsByRequestIds);
	}

	// PENDING 상태의 서비스 조회
	public List<DirectorServiceResponse> findServicesRelatedToServiceRequest(Long memberId, Boolean showOnlyPending) {
		Member member = memberQueryService.findById(memberId);
		return serviceRequestService.findServicesRelatedToServiceRequest(member, showOnlyPending);
	}

	// 회원용 요청 상세 조회
	public ServiceRequestFindDetailResponseForPublic findDetailForPublic(Long memberId, Long serviceRequestId) {
		// 1. 요청 조회 및 검증
		ServiceRequest serviceRequest = serviceRequestService.validateAndGetOwnedRequest(memberId, serviceRequestId);

		// 2. 응답 조립
		return ServiceRequestFindDetailResponseForPublic.from(serviceRequest);
	}

	@Transactional
	public void updateIsReceivingEstimate(Long memberId, Long serviceRequestId,
		ServiceRequestUpdateReceivingEstimateRequest request) {
		// 1. 회원 조회
		Member member = memberQueryService.findById(memberId);

		// 2. 요청 상태 업데이트
		serviceRequestService.updateIsReceivingEstimate(member, serviceRequestId, request.getReason());
	}

	// 오래된 서비스 요청 만료 처리 (스케줄러에서 호출)
	public void expireServiceRequests() {
		//todo bulk 업데이트 사용

		// 1. 현재 시각을 epoch second 으로 구함
		LocalDateTime now = LocalDateTime.now(clock);
		long nowEpoch = now.atZone(KST).toEpochSecond();

		// 2. 만료된 서비스 요청 ID 조회
		List<Long> expiredRequestIds = redisServiceRequestExpireRepository.findExpiredRequestIds(nowEpoch);

		// 3. 서비스 요청 조회 (삭제되지 않았으면서 상태가 'PENDING'인 것만)
		List<ServiceRequest> serviceRequests = serviceRequestService.findExpiredRequests(now, expiredRequestIds);

		// 4. 서비스와 관련된 제안 상태 EXPIRED 로 업데이트
		serviceEstimateService.updateToExpiredStatus(serviceRequests);

		// 5. 서비스 요청 상태를 'EXPIRED'로 업데이트
		serviceRequestService.expireServiceRequests(serviceRequests);

		// 6. Redis 에서 만료된 요청 ID 제거
		// todo 이거 시간 지나지 않은것들도 다 지운다.
		redisServiceRequestExpireRepository.removeExpiredRequestsFromRedis(nowEpoch);
	}

	@Transactional
	public void expandServiceRequestLocations() {
		LocalDateTime now = LocalDateTime.now(clock);

		serviceRequestService.expandServiceRequestLocations(now);
	}

	public ServiceRequestFindCountResponseForPublic findCounts(Long memberId) {
		// 1. 회원 조회
		Member member = memberQueryService.findById(memberId);

		// 2. 상태별 요청 갯수 조회
		Map<ServiceRequestStatus, Integer> serviceRequestCountMap = serviceRequestQueryService.countByMemberId(
			memberId);

		// 3. 응답 조립
		return ServiceRequestFindCountResponseForPublic.from(serviceRequestCountMap);
	}
}
