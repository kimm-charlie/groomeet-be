package com.motd.be.module.member.review.dto.response;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.review.entity.Review;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewFindAllForDirectorResponse {

	private int page;
	private boolean hasNext;
	private Integer reviewCount;
	private List<ReviewWithReceivedCompletedEstimateCountResponse> reviews;

	public static ReviewFindAllForDirectorResponse of(Slice<Review> reviews, int reviewCount,
		Map<Long, Integer> receivedCompletedEstimateCountMap, Long requestedMemberId) {
		return ReviewFindAllForDirectorResponse.builder()
			.page(reviews.getNumber())
			.hasNext(reviews.hasNext())
			.reviewCount(reviewCount)
			.reviews(ReviewWithReceivedCompletedEstimateCountResponse.ofList(reviews.getContent(),
				receivedCompletedEstimateCountMap, requestedMemberId))
			.build();
	}
}
