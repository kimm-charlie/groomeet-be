package com.motd.be.module.director.service_estimate.dto.response;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.util.List;
import java.util.Map;

import com.motd.be.module.director.file.dto.response.FileResponseForDirector;
import com.motd.be.module.member.chat_room_service_estimate_mapping.entity.ChatRoomServiceEstimateMapping;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.Builder;
import lombok.Getter;

/**
 * 제안 상세조회에 사용하는 dto
 */
@Getter
@Builder
public class ServiceEstimateFindDetailResponseForDirector {

	private Long id;
	private String status;
	private String title;
	private Long price;
	private String content;
	private String createdAt;
	private String scheduledAt;
	private String completedAt;
	private String canceledAt;
	private String expiredAt;
	private List<FileResponseForDirector> files;
	private Long chatRoomId;

	public static ServiceEstimateFindDetailResponseForDirector of(ServiceEstimate serviceEstimate,
		Map<Long, ChatRoomServiceEstimateMapping> mappings) {
		return ServiceEstimateFindDetailResponseForDirector.builder()
			.id(serviceEstimate.getId())
			.status(serviceEstimate.getStatus().getDescription())
			.title(serviceEstimate.getTitle())
			.price(serviceEstimate.getPrice())
			.content(serviceEstimate.getContent())
			.createdAt(formatToDateString(serviceEstimate.getCreatedAt()))
			.scheduledAt(formatToDateString(serviceEstimate.getScheduledAt()))
			.completedAt(formatToDateString(serviceEstimate.getDirectorDoneAt()))
			.canceledAt(formatToDateString(serviceEstimate.getCanceledAt()))
			.expiredAt(formatToDateString(serviceEstimate.getExpiredAt()))
			.files(FileResponseForDirector.fromListWithEstimateFiles(serviceEstimate.getFiles()))
			.chatRoomId(mappings.get(serviceEstimate.getId()).getChatRoom().getId())
			.build();
	}
}
