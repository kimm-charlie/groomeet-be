package com.motd.be.module.member.review.dto.response;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.util.List;
import java.util.Map;

import com.motd.be.module.member.director_service.dto.response.DirectorServiceWithFullNameResponse;
import com.motd.be.module.member.file.dto.response.FileResponse;
import com.motd.be.module.member.member.dto.response.MemberResponse;
import com.motd.be.module.member.review.entity.Review;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewWithReceivedCompletedEstimateCountResponse {

	private Long id;
	private MemberResponse writer;
	private Integer receivedCompletedEstimateCount;
	private String createdAt;
	private String title;
	private String content;
	private DirectorServiceWithFullNameResponse service;
	private List<FileResponse> files;
	private Boolean isEditable;

	public static ReviewWithReceivedCompletedEstimateCountResponse of(Review review,
		int receivedCompletedEstimateCount, Long requestedMemberId) {
		return ReviewWithReceivedCompletedEstimateCountResponse.builder()
			.id(review.getId())
			.writer(MemberResponse.from(review.getWriter()))
			.receivedCompletedEstimateCount(receivedCompletedEstimateCount)
			.createdAt(formatToDateString(review.getCreatedAt()))
			.title(review.getTitle())
			.content(review.getContent())
			.service(DirectorServiceWithFullNameResponse.from(
				review.getServiceEstimate().getServiceRequest().getDirectorService()))
			.files(FileResponse.fromListWithReviewFiles(review.getImages()))
			.isEditable(review.isReviewEditable(requestedMemberId))
			.build();
	}

	public static List<ReviewWithReceivedCompletedEstimateCountResponse> ofList(List<Review> reviews,
		Map<Long, Integer> receivedCompletedEstimateCountMap, Long requestedMemberId) {
		return reviews.stream()
			.map(review -> ReviewWithReceivedCompletedEstimateCountResponse.of(review,
				receivedCompletedEstimateCountMap.getOrDefault(review.getWriter().getId(), 0),
				requestedMemberId))
			.toList();
	}
}
