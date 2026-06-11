package com.motd.be.module.admin.revice.dto.response;

import java.util.List;

import com.motd.be.common.utils.DateFormatUtils;
import com.motd.be.module.admin.member.dto.response.MemberSummaryForAdmin;
import com.motd.be.module.member.file.dto.response.FileResponse;
import com.motd.be.module.member.review.entity.Review;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewSummaryForAdmin {

	private Long id;
	private String title;
	private String content;
	private String createdAt;
	private Boolean isDeleted;
	private MemberSummaryForAdmin writer;
	private List<FileResponse> images;

	public static ReviewSummaryForAdmin from(Review review) {
		if (review == null) {
			return null;
		}
		return ReviewSummaryForAdmin.builder()
			.id(review.getId())
			.title(review.getTitle())
			.content(review.getContent())
			.createdAt(DateFormatUtils.formatToDateString(review.getCreatedAt()))
			.isDeleted(review.getIsDeleted())
			.writer(MemberSummaryForAdmin.from(review.getWriter()))
			.images(FileResponse.fromListWithReviewFiles(review.getImages()))
			.build();
	}
}
