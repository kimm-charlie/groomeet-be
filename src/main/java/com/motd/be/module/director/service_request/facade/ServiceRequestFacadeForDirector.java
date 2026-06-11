package com.motd.be.module.director.service_request.facade;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.director.director_service.service.DirectorServiceServiceForDirector;
import com.motd.be.module.director.member.service.MemberQueryServiceForDirector;
import com.motd.be.module.director.service_estimate.service.ServiceEstimateQueryServiceForDirector;
import com.motd.be.module.director.service_request.dto.response.ServiceRequestFindAllResponseForDirector;
import com.motd.be.module.director.service_request.dto.response.ServiceRequestFindDetailResponseForDirector;
import com.motd.be.module.director.service_request.service.ServiceRequestQueryServiceForDirector;
import com.motd.be.module.director.service_request.service.ServiceRequestServiceForDirector;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceRequestFacadeForDirector {

	private final MemberQueryServiceForDirector memberQueryServiceForDirector;
	private final ServiceRequestServiceForDirector serviceRequestServiceForDirector;
	private final ServiceRequestQueryServiceForDirector serviceRequestQueryServiceForDirector;
	private final ServiceEstimateQueryServiceForDirector serviceEstimateQueryServiceForDirector;
	private final DirectorServiceServiceForDirector directorServiceServiceForDirector;

	// 디렉터용 요청 상세 조회
	public ServiceRequestFindDetailResponseForDirector findDetailForDirector(Long memberId, Long serviceRequestId) {
		// 1. 요청 조회 및 상태 검증
		ServiceRequest serviceRequest = serviceRequestQueryServiceForDirector.findByIdWithDirectorService(
			serviceRequestId);

		// 2. 제안 개수 조회
		Integer estimateCount = serviceEstimateQueryServiceForDirector.countEstimatesByServiceRequest(serviceRequest);

		// 3. 응답 조립
		return ServiceRequestFindDetailResponseForDirector.of(serviceRequest, estimateCount);
	}

	// 디렉터용 요청 목록 조회
	public ServiceRequestFindAllResponseForDirector findAllForDirector(Long memberId, Long directorServiceId, int page,
		Boolean showOnlyDirectRequest) {
		// 1. 디렉터 회원 조회
		Member director = memberQueryServiceForDirector.findByIdWithDirector(memberId);

		// 2. 디렉터 서비스 유효성 검증 및 조회
		List<Long> targetDirectorServiceIds = directorServiceServiceForDirector.resolveTargetServiceIds(
			director.getDirectorInfo(), directorServiceId);

		// 3. 서비스 요청 조회
		Slice<ServiceRequest> serviceRequests = serviceRequestServiceForDirector.findAllForDirector(director, page,
			targetDirectorServiceIds, showOnlyDirectRequest);

		// 4. 제안 개수 조회
		Map<Long, Integer> receivedEstimateCountByRequestId = serviceEstimateQueryServiceForDirector.countEstimatesByServiceRequests(
			serviceRequests.getContent());

		// 5. 응답 조립
		return ServiceRequestFindAllResponseForDirector.of(serviceRequests, receivedEstimateCountByRequestId);
	}

	@Transactional
	public void hideForDirector(Long memberId, Long serviceRequestId) {
		Member director = memberQueryServiceForDirector.findByIdWithDirector(memberId);

		serviceRequestServiceForDirector.hideForDirector(director, serviceRequestId);
	}
}

