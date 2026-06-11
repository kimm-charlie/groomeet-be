package com.motd.be.module.member.review.dto.request;

import static com.motd.be.common.constants.ValidationMessages.*;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewUpdateRequest {
	
	@NotBlank(message = REVIEW_CONTENT_REQUIRED)
	private String content;
	private List<Long> fileIds;
}
