package com.motd.be.module.member.service_request.validator;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ServiceRequestException;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member_block.service.MemberBlockQueryService;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.service.ServiceRequestQueryService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ServiceRequestValidator {

	private final ServiceRequestQueryService serviceRequestQueryService;
	private final MemberBlockQueryService memberBlockQueryService;
	private final Clock clock;

	public void validateOwnership(ServiceRequest serviceRequest, Long memberId) {
		if (!serviceRequest.isOwnedBy(memberId)) {
			throw new CustomRuntimeException(ServiceRequestException.NOT_OWNED_BY);
		}
	}

	public void isOngoingRequestExist(List<ServiceRequest> serviceRequests) {
		if (!serviceRequests.isEmpty()) {
			throw new CustomRuntimeException(ServiceRequestException.ONGOING_REQUEST_EXIST);
		}
	}

	public void validateOngoingRequestWithDirector(boolean hasOngoingRequest) {
		if (hasOngoingRequest) {
			throw new CustomRuntimeException(ServiceRequestException.DIRECT_REQUEST_NOT_ALLOWED_BY_ONGOING);
		}
	}

	public void validateIsPendingOrExpiredStatus(ServiceRequest serviceRequest) {
		if (!(serviceRequest.isPending() || serviceRequest.isExpired())) {
			throw new CustomRuntimeException(ServiceRequestException.INVALID_STATUS_FOR_MEETING_CREATION);
		}
	}

	public void isDuplicateServiceRequestIn24Hours(Member member, DirectorService directorService) {
		if (serviceRequestQueryService.existsByMemberAndDirectorServiceInLast24Hours(member, directorService,
			LocalDateTime.now(clock).minusDays(1))) {
			throw new CustomRuntimeException(ServiceRequestException.DUPLICATE_REQUEST_IN_24_HOURS);
		}
	}

	public void isDuplicateServiceRequestIn24HoursForDirectorAdditionalServiceEstimate(ServiceRequest serviceRequest) {
		if (serviceRequestQueryService.existsByMemberAndDirectorServiceInLast24Hours(serviceRequest.getMember(),
			serviceRequest.getDirectorService(),
			LocalDateTime.now(clock).minusDays(1))) {
			throw new CustomRuntimeException(ServiceRequestException.FAIL_TO_SAVE_BY_ADDITIONAL_ESTIMATE);
		}
	}

	public void validateNotSelfDirectRequest(Member member, Long directRequestedMemberId) {
		if (member.getId().equals(directRequestedMemberId)) {
			throw new CustomRuntimeException(ServiceRequestException.SELF_DIRECT_REQUEST_NOT_ALLOWED);
		}
	}

	public void isMemberBlockedOrBlock(Member director, Member member) {
		if (memberBlockQueryService.existsByBlockerOrBlocked(director, member)) {
			throw new CustomRuntimeException(ServiceRequestException.FAIL_TO_SAVE_BY_BLOCK);
		}
	}
}
