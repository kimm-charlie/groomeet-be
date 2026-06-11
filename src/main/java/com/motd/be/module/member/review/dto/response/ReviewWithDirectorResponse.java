package com.motd.be.module.member.review.dto.response;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.util.List;

import com.motd.be.module.member.director_service.dto.response.DirectorServiceWithFullNameResponse;
import com.motd.be.module.member.file.dto.response.FileResponse;
import com.motd.be.module.member.member.dto.response.MemberResponse;
import com.motd.be.module.member.review.entity.Review;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewWithDirectorResponse {

	private Long id;
	private String title;
	private MemberResponse writer;
	private MemberResponse director;
	private String createdAt;
	private String content;
	private DirectorServiceWithFullNameResponse service;
	private List<FileResponse> files;
	private Boolean isEditable;

	public static List<ReviewWithDirectorResponse> ofList(List<Review> reviews, Long requestedMemberId) {
		return reviews.stream()
			.map(review -> of(review, requestedMemberId))
			.toList();
	}

	private static ReviewWithDirectorResponse of(Review review, Long requestedMemberId) {
		return ReviewWithDirectorResponse.builder()
			.id(review.getId())
			.title(review.getTitle())
			.writer(MemberResponse.from(review.getWriter()))
			.director(MemberResponse.from(review.getServiceEstimate().getDirectorInfo().getMember()))
			.createdAt(formatToDateString(review.getCreatedAt()))
			.content(review.getContent())
			.service(DirectorServiceWithFullNameResponse.from(
				review.getServiceEstimate().getServiceRequest().getDirectorService()))
			.files(FileResponse.fromListWithReviewFiles(review.getImages()))
			.isEditable(review.isReviewEditable(requestedMemberId))
			.build();
	}
}
