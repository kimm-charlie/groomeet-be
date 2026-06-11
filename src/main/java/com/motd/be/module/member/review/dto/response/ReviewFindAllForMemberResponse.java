package com.motd.be.module.member.review.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.review.entity.Review;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewFindAllForMemberResponse {

	private int page;
	private boolean hasNext;
	private Integer reviewCount;
	private List<ReviewWithDirectorResponse> reviews;

	public static ReviewFindAllForMemberResponse of(Slice<Review> allByWriter, int writtenReviewCount,
		Long requestedMemberId) {
		return ReviewFindAllForMemberResponse.builder()
			.page(allByWriter.getNumber())
			.hasNext(allByWriter.hasNext())
			.reviewCount(writtenReviewCount)
			.reviews(ReviewWithDirectorResponse.ofList(allByWriter.getContent(), requestedMemberId))
			.build();
	}
}
