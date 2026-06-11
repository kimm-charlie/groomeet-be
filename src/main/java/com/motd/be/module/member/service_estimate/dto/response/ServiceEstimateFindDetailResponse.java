package com.motd.be.module.member.service_estimate.dto.response;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.utils.DateFormatUtils.*;

import java.util.List;
import java.util.Map;

import com.motd.be.module.member.chat_room_service_estimate_mapping.entity.ChatRoomServiceEstimateMapping;
import com.motd.be.module.member.file.dto.response.FileResponse;
import com.motd.be.module.member.member.dto.response.MemberResponseWithCompletedAndReviewCountResponse;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceEstimateFindDetailResponse {

	private Long id;
	private MemberResponseWithCompletedAndReviewCountResponse member;
	private Long price;
	private String createdAt;
	private String title;
	private String content;
	private String scheduledAt;
	private String completedAt;
	private List<FileResponse> files;
	private String status;
	private Long chatRoomId;

	public static ServiceEstimateFindDetailResponse of(ServiceEstimate serviceEstimate,
		Map<Long, ChatRoomServiceEstimateMapping> mappings) {
		return ServiceEstimateFindDetailResponse.builder()
			.id(serviceEstimate.getId())
			.member(
				MemberResponseWithCompletedAndReviewCountResponse.from(serviceEstimate.getDirectorInfo().getMember()))
			.price(serviceEstimate.getPrice())
			.createdAt(formatToDateString(serviceEstimate.getCreatedAt()))
			.title(serviceEstimate.getTitle())
			.content(serviceEstimate.getContent())
			.scheduledAt(formatToDateString(serviceEstimate.getScheduledAt()))
			.completedAt(formatToDateString(serviceEstimate.getDirectorDoneAt()))
			.files(FileResponse.fromListWithEstimateFiles(serviceEstimate.getFiles()))
			.status(serviceEstimate.getStatus().getDescription())
			.chatRoomId(mappings.get(serviceEstimate.getId()).getChatRoom().getId())
			.build();
	}
}
