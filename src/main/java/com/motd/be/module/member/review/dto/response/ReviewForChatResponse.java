package com.motd.be.module.member.review.dto.response;

import java.util.List;

import com.motd.be.module.member.director_service.dto.response.DirectorServiceWithFullNameResponse;
import com.motd.be.module.member.file.dto.response.FileResponse;
import com.motd.be.module.member.review.entity.Review;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewForChatResponse {

	private Long id;
	private String title;
	private String content;
	private DirectorServiceWithFullNameResponse service;
	private List<FileResponse> files;

	public static ReviewForChatResponse from(Review review) {
		return ReviewForChatResponse.builder()
			.id(review.getId())
			.title(review.getTitle())
			.content(review.getContent())
			.service(DirectorServiceWithFullNameResponse.from(review.getServiceEstimate().getServiceRequest()
				.getDirectorService()))
			.files(FileResponse.fromListWithReviewFiles(review.getImages()))
			.build();
	}
}
