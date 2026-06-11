package com.motd.be.module.member.review.service;

import static com.motd.be.common.constants.PageSizeConstants.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ReviewException;
import com.motd.be.module.member.chat_message.entity.ChatMessage;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.validator.DirectorServiceValidator;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.review.entity.Review;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.shared.firebase.dto.FirebasePushEvent;
import com.motd.be.shared.firebase.policy.ActivityAgreedPolicy;
import com.motd.be.shared.firebase.policy.ChatVisibilityPolicy;
import com.motd.be.shared.firebase.policy.CompositePushSendPolicy;
import com.motd.be.shared.firebase.policy.PushContext;
import com.motd.be.shared.firebase.policy.PushSendPolicy;
import com.motd.be.shared.firebase.policy.ReceiverOfflinePolicy;
import com.motd.be.shared.firebase.service.FirebaseEventPublisher;
import com.motd.be.shared.firebase.service.FirebasePushFactory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private final DirectorServiceValidator directorServiceValidator;
	private final ReviewQueryService reviewQueryService;
	private final ReviewCommandService reviewCommandService;
	private final FirebaseEventPublisher firebaseEventPublisher;
	private final FirebasePushFactory firebasePushFactory;

	public Review save(Review review) {
		try {
			return reviewCommandService.save(review);
		} catch (DataIntegrityViolationException e) {
			throw new CustomRuntimeException(ReviewException.ALREADY_WRITTEN);
		}

	}

	public Optional<Review> findByServiceEstimateIdWithOptional(Long serviceEstimateId) {
		return reviewQueryService.findByServiceEstimateId(serviceEstimateId);
	}

	public Slice<Review> findAllByDirectorAndService(DirectorInfo directorInfo, int page, Long directorServiceId) {
		// 디렉터가 해당 서비스 소유 여부 확인
		if (directorServiceId != null) {
			directorServiceValidator.validateServiceOwnership(directorInfo, directorServiceId);
		}

		// 리뷰 조회
		Pageable pageable = PageRequest.of(page, REVIEW_FIND_ALL_SIZE);
		return reviewQueryService.findAllByDirectorAndService(directorInfo, directorServiceId, pageable);
	}

	public Review findByIdWithWriterAndServiceEstimate(Long reviewId) {
		return reviewQueryService.findByIdWithWriterAndServiceEstimate(reviewId);
	}

	public void sendPushWhenReviewWrittenToDirector(Member writer, ServiceEstimate serviceEstimate, Review review,
		Set<Long> onlineMemberIds, ChatMessage chatMessage) {
		Member receiver = serviceEstimate.getDirectorInfo().getMember();

		// 정책 검사
		PushContext pushContext = PushContext.of(writer, receiver, onlineMemberIds, chatMessage);

		PushSendPolicy pushSendPolicy =
			CompositePushSendPolicy.of(List.of(
				new ChatVisibilityPolicy(),
				new ActivityAgreedPolicy(),
				new ReceiverOfflinePolicy()
			));

		if (!pushSendPolicy.canSend(pushContext)) {
			return;
		}

		FirebasePushEvent event = firebasePushFactory.reviewCreated(writer, receiver, review,
			serviceEstimate.getServiceRequest());

		firebaseEventPublisher.sendPush(event);
	}
}
