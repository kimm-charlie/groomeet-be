package com.motd.be.module.member.review.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.review.dto.request.ReviewSaveAndUpdateRequest;
import com.motd.be.module.member.review.dto.response.ReviewFindAllForDirectorResponse;
import com.motd.be.module.member.review.dto.response.ReviewFindAllForMemberResponse;
import com.motd.be.module.member.review.dto.response.ReviewWithReceivedCompletedEstimateCountResponse;
import com.motd.be.module.member.review.facade.ReviewFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReviewController {

	private final ReviewFacade reviewFacade;

	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@PostMapping("/reviews/service-estimates/{serviceEstimateId}")
	public ResponseEntity<Void> save(@AuthenticationPrincipal Long memberId,
		@PathVariable(SERVICE_ESTIMATE_ID) Long serviceEstimateId,
		@RequestBody @Validated ReviewSaveAndUpdateRequest request) {
		reviewFacade.saveByMember(memberId, serviceEstimateId, request);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@PatchMapping("/reviews/{reviewId}")
	public ResponseEntity<Void> update(@AuthenticationPrincipal Long memberId,
		@PathVariable(REVIEW_ID) Long reviewId,
		@RequestBody @Validated ReviewSaveAndUpdateRequest request) {
		reviewFacade.updateByMember(memberId, reviewId, request);
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@DeleteMapping("/reviews/{reviewId}")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal Long memberId,
		@PathVariable(REVIEW_ID) Long reviewId) {
		reviewFacade.deleteByMember(memberId, reviewId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * 특정 제안에 있는 리뷰 조회
	 *
	 * @param memberId
	 * @param serviceEstimateId
	 * @return
	 */
	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@GetMapping("/service-estimates/{serviceEstimateId}/reviews")
	public ResponseEntity<ReviewWithReceivedCompletedEstimateCountResponse> findByServiceEstimate(
		@AuthenticationPrincipal Long memberId,
		@PathVariable(SERVICE_ESTIMATE_ID) Long serviceEstimateId) {
		return ResponseEntity.ok(reviewFacade.findByServiceEstimate(memberId, serviceEstimateId));
	}

	/**
	 * 일반 회원이 작성한 리뷰 조회
	 *
	 * @param memberId
	 * @param page
	 * @return
	 */
	@PreAuthorize("hasAnyRole('MEMBER','DIRECTOR')")
	@GetMapping("/members/my/reviews")
	public ResponseEntity<ReviewFindAllForMemberResponse> findMyReviews(@AuthenticationPrincipal Long memberId,
		@RequestParam(name = PAGE, required = false, defaultValue = ZERO) int page) {
		return ResponseEntity.ok(reviewFacade.findAllByMember(memberId, page));
	}

	/**
	 * 디렉터가 받은 모든 리뷰 조회 (디렉터 마이페이지, 디렉터 메인페이지)
	 *
	 * @param targetMemberId
	 * @param page
	 * @param directorServiceId
	 * @return
	 */
	@GetMapping("/directors/{targetMemberId}/reviews")
	public ResponseEntity<ReviewFindAllForDirectorResponse> findAllForDirector(
		@AuthenticationPrincipal Long memberId,
		@PathVariable(TARGET_MEMBER_ID) Long targetMemberId,
		@RequestParam(name = PAGE, required = false, defaultValue = ZERO) int page,
		@RequestParam(name = DIRECTOR_SERVICE_ID, required = false) Long directorServiceId) {
		return ResponseEntity.ok(
			reviewFacade.findAllByDirectorAndService(targetMemberId, page, directorServiceId, memberId));
	}
}
