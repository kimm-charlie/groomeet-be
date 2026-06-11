package com.motd.be.module.member.review.dto.request;

import static com.motd.be.common.constants.ValidationConstants.*;
import static com.motd.be.common.constants.ValidationMessages.*;

import java.util.List;

import org.hibernate.validator.constraints.Length;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.review.entity.Review;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSaveAndUpdateRequest {

	@NotBlank(message = REVIEW_CONTENT_REQUIRED)
	@Length(max = REVIEW_MAX_CONTENT_LENGTH, message = REVIEW_CONTENT_MAX_LENGTH_MSG)
	private String content;
	@Size(max = REVIEW_MAX_IMAGE_COUNT, message = REVIEW_FILE_MAX_COUNT_MSG)
	private List<Long> fileIds;

	public Review toEntity(ServiceEstimate serviceEstimate, Member writer) {
		return Review.builder()
			.serviceEstimate(serviceEstimate)
			.writer(writer)
			.title(serviceEstimate.getContent())
			.content(this.content)
			.build();
	}
}
