package com.motd.be.module.member.review.validator;

import static com.motd.be.common.constants.TimePolicy.*;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ReviewException;
import com.motd.be.module.member.review.entity.Review;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewValidator {

	public void validateReviewableStatus(ServiceEstimate serviceEstimate) {
		if (!serviceEstimate.getStatus().equals(ServiceEstimateStatus.COMPLETED_BY_MEMBER)) {
			throw new CustomRuntimeException(ReviewException.CANNOT_SAVE_REVIEW);
		}
	}

	public void validateReviewPeriodNotExpired(ServiceEstimate serviceEstimate) {
		// 7일 초과 시 예외
		if (serviceEstimate.getMemberCompletedAt().plusDays(REVIEW_WRITE_EXPIRE_DAYS).isBefore(LocalDateTime.now())) {
			throw new CustomRuntimeException(ReviewException.REVIEW_PERIOD_EXPIRED);
		}
	}

	public void validateOwnership(Review review, Long memberId) {
		if (!review.getWriter().getId().equals(memberId)) {
			throw new CustomRuntimeException(ReviewException.NOT_OWNED);
		}
	}
}
