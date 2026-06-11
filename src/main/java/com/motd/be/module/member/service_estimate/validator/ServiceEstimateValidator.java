package com.motd.be.module.member.service_estimate.validator;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.MemberBlockException;
import com.motd.be.exception.exceptions.ServiceEstimateException;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member_block.service.MemberBlockQueryService;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.motd.be.module.member.service_estimate.service.ServiceEstimateQueryService;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request_wish_time.entity.ServiceRequestWishTime;

import static com.motd.be.common.constants.ValidationConstants.*;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ServiceEstimateValidator {

	private final ServiceEstimateQueryService serviceEstimateQueryService;
	private final MemberBlockQueryService memberBlockQueryService;

	public void validateOwnership(ServiceEstimate serviceEstimate, Long directorInfoId) {
		if (!serviceEstimate.isOwnedBy(directorInfoId)) {
			throw new CustomRuntimeException(ServiceEstimateException.NOT_OWNED_BY);
		}
	}

	public void validateCancellable(ServiceEstimate serviceEstimate) {
		if (serviceEstimate.getStatus() == ServiceEstimateStatus.CANCELED) {
			throw new CustomRuntimeException(ServiceEstimateException.ALREADY_CANCELED);
		}
		if (!serviceEstimate.isCancellableForPublic()) {
			throw new CustomRuntimeException(ServiceEstimateException.NOT_CANCELLABLE_STATUS);
		}
	}

	public void validateIsOngoingStatus(ServiceEstimate serviceEstimate) {
		if (!serviceEstimate.isOngoing()) {
			throw new CustomRuntimeException(ServiceEstimateException.NOT_ONGOING_STATUS);
		}
	}

	public void validateIsDirectorCompletedStatus(ServiceEstimate serviceEstimate) {
		if (!serviceEstimate.getStatus().equals(ServiceEstimateStatus.DIRECTOR_DONE)) {
			throw new CustomRuntimeException(ServiceEstimateException.NOT_DIRECTOR_COMPLETED_STATUS);
		}
	}

	public void validateIsMemberNotCompletedStatus(ServiceEstimate serviceEstimate) {
		if (serviceEstimate.getStatus().equals(ServiceEstimateStatus.COMPLETED_BY_MEMBER)) {
			throw new CustomRuntimeException(ServiceEstimateException.ALREADY_MEMBER_COMPLETED_STATUS);
		}
	}

	public void validateIsPendingStatus(ServiceEstimate serviceEstimate) {
		if (!serviceEstimate.isPending()) {
			throw new CustomRuntimeException(ServiceEstimateException.NOT_PENDING_STATUS);
		}
	}

	public void validateMemberOwnership(ServiceEstimate serviceEstimate, Long memberId) {
		if (!serviceEstimate.getServiceRequest().isOwnedBy(memberId)) {
			throw new CustomRuntimeException(ServiceEstimateException.NOT_OWNED_BY_MEMBER);
		}
	}

	public void validateNotSelfEstimate(Member director, Member member) {
		if (director.getId().equals(member.getId())) {
			throw new CustomRuntimeException(ServiceEstimateException.SELF_ESTIMATE_NOT_ALLOWED);
		}
	}

	public void validateExistsNotEndedEstimateByMemberAndDirector(Member member, Long directorInfoId) {
		if (serviceEstimateQueryService.validateExistsNotEndedEstimateByMemberAndDirector(member,
			directorInfoId)) {
			throw new CustomRuntimeException(ServiceEstimateException.ALREADY_EXISTS_ONGOING_ESTIMATE_BY_DIRECTOR);
		}
	}

	public void validateNoOngoingEstimatesForBlock(Member member, Member target) {
		if (serviceEstimateQueryService.existsOngoingEstimateBetween(member, target)) {
			throw new CustomRuntimeException(MemberBlockException.CANNOT_BLOCK_DURING_ONGOING_ESTIMATE);
		}
	}

	public void isMemberBlockedOrBlock(Member director, Member member) throws CustomRuntimeException {
		if (memberBlockQueryService.existsByBlockerOrBlocked(director, member)) {
			throw new CustomRuntimeException(ServiceEstimateException.FAIL_TO_SAVE_BY_BLOCK);
		}
	}

	public void validateScheduledAtInWishTimes(LocalDateTime scheduledAt, ServiceRequest serviceRequest) {
		List<LocalDateTime> wishTimes = serviceRequest.getWishTimes().stream()
			.map(ServiceRequestWishTime::getWishTime)
			.toList();
		if (!wishTimes.contains(scheduledAt)) {
			throw new CustomRuntimeException(ServiceEstimateException.SCHEDULED_AT_NOT_IN_WISH_TIMES);
		}
	}

	public void validateReceivedEstimateCountNotExceeded(ServiceRequest serviceRequest) {
		if (serviceRequest.getReceivedEstimateCount() >= MAX_RECEIVED_ESTIMATE_COUNT) {
			throw new CustomRuntimeException(ServiceEstimateException.EXCEEDED_MAX_RECEIVED_ESTIMATE_COUNT);
		}
	}

	public void validateNoExistingBookingAtScheduledAt(LocalDateTime scheduledAt, Member director) {
		List<LocalDateTime> bookedTimes = serviceEstimateQueryService.findScheduledAtByDirectorMemberIdAndDate(
			director.getId(), scheduledAt.toLocalDate());
		if (bookedTimes.contains(scheduledAt)) {
			throw new CustomRuntimeException(ServiceEstimateException.DIRECTOR_ALREADY_BOOKED_AT_TIME);
		}
	}

	public void validateNoExistingBookingAtScheduledAt(LocalDateTime scheduledAt, Member director,
		Long excludeEstimateId) {
		List<LocalDateTime> bookedTimes = serviceEstimateQueryService.findScheduledAtByDirectorMemberIdAndDateExcluding(
			director.getId(), scheduledAt.toLocalDate(), excludeEstimateId);
		if (bookedTimes.contains(scheduledAt)) {
			throw new CustomRuntimeException(ServiceEstimateException.DIRECTOR_ALREADY_BOOKED_AT_TIME);
		}
	}

}
