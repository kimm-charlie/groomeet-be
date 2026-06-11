package com.motd.be.module.admin.service_estimate.dto.response;

import java.util.List;
import java.util.Map;

import com.motd.be.common.utils.DateFormatUtils;
import com.motd.be.module.admin.member.dto.response.MemberSummaryForAdmin;
import com.motd.be.module.admin.revice.dto.response.ReviewSummaryForAdmin;
import com.motd.be.module.member.chat_room_service_estimate_mapping.entity.ChatRoomServiceEstimateMapping;
import com.motd.be.module.member.file.dto.response.FileResponse;
import com.motd.be.module.member.review.entity.Review;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceEstimateFindDetailResponseForAdmin {

	private Long serviceEstimateId;
	private String title;
	private String content;
	private Long price;
	private String status;
	private String createdAt;
	private String ongoingAt;
	private String canceledAt;
	private String expiredAt;
	private String directorDoneAt;
	private String memberCompletedAt;
	private Boolean isDeleted;
	private Boolean isHired;
	private MemberSummaryForAdmin director;
	private MemberSummaryForAdmin member;
	private ServiceRequestSummaryForServiceEstimate serviceRequest;
	private List<FileResponse> files;
	private ReviewSummaryForAdmin review;
	private Long chatRoomId;

	public static ServiceEstimateFindDetailResponseForAdmin of(ServiceEstimate serviceEstimate, Review review,
		Map<Long, ChatRoomServiceEstimateMapping> mappings) {
		ChatRoomServiceEstimateMapping mapping = mappings.get(serviceEstimate.getId());
		Long chatRoomId = mapping != null ? mapping.getChatRoom().getId() : null;

		return ServiceEstimateFindDetailResponseForAdmin.builder()
			.serviceEstimateId(serviceEstimate.getId())
			.title(serviceEstimate.getTitle())
			.content(serviceEstimate.getContent())
			.price(serviceEstimate.getPrice())
			.status(serviceEstimate.getStatus().getDescription())
			.createdAt(DateFormatUtils.formatToDateString(serviceEstimate.getCreatedAt()))
			.ongoingAt(DateFormatUtils.formatToDateString(serviceEstimate.getOngoingAt()))
			.canceledAt(DateFormatUtils.formatToDateString(serviceEstimate.getCanceledAt()))
			.expiredAt(DateFormatUtils.formatToDateString(serviceEstimate.getExpiredAt()))
			.directorDoneAt(DateFormatUtils.formatToDateString(serviceEstimate.getDirectorDoneAt()))
			.memberCompletedAt(DateFormatUtils.formatToDateString(serviceEstimate.getMemberCompletedAt()))
			.isDeleted(serviceEstimate.getIsDeleted())
			.isHired(serviceEstimate.getIsHired())
			.director(MemberSummaryForAdmin.from(serviceEstimate.getDirectorInfo().getMember()))
			.member(MemberSummaryForAdmin.from(serviceEstimate.getServiceRequest().getMember()))
			.serviceRequest(ServiceRequestSummaryForServiceEstimate.from(serviceEstimate.getServiceRequest()))
			.files(FileResponse.fromListWithEstimateFiles(serviceEstimate.getFiles()))
			.review(ReviewSummaryForAdmin.from(review))
			.chatRoomId(chatRoomId)
			.build();
	}
}
