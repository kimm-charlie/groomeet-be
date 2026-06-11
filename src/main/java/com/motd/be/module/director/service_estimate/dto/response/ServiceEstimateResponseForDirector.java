package com.motd.be.module.director.service_estimate.dto.response;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.util.List;
import java.util.Map;

import com.motd.be.module.director.director_service.dto.response.DirectorServiceWithFullNameResponseForDirector;
import com.motd.be.module.director.file.dto.response.FileResponseForDirector;
import com.motd.be.module.director.location.dto.response.LocationResponseForDirector;
import com.motd.be.module.director.member.dto.response.MemberResponseForDirector;
import com.motd.be.module.member.chat_room_service_estimate_mapping.entity.ChatRoomServiceEstimateMapping;
import com.motd.be.module.member.request_location_mapping.entity.RequestLocationMapping;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;

import lombok.Builder;
import lombok.Getter;

/**
 * 제안 전체 조회에 사용하는 dto
 */
@Getter
@Builder
public class ServiceEstimateResponseForDirector {

	private Long id;
	private Long serviceRequestId;
	private String createdAt;
	private MemberResponseForDirector requester;
	private DirectorServiceWithFullNameResponseForDirector service;
	private List<LocationResponseForDirector> locations;
	private String status;
	private String scheduledAt;
	private Long price;
	private String completedAt;
	private String canceledAt;
	private String expiredAt;
	private Boolean isDirectRequest;
	private Long chatRoomId;
	// 이때 제안서가 아닌 요청에 대한 이미지가 들어간다!
	private List<FileResponseForDirector> files;

	public static List<ServiceEstimateResponseForDirector> ofList(List<ServiceEstimate> serviceEstimates,
		Map<Long, ChatRoomServiceEstimateMapping> mappings) {
		return serviceEstimates.stream()
			.map(estimate -> from(estimate, mappings.get(estimate.getId())))
			.toList();
	}

	public static ServiceEstimateResponseForDirector from(ServiceEstimate serviceEstimate,
		ChatRoomServiceEstimateMapping mapping) {
		return ServiceEstimateResponseForDirector.builder()
			.id(serviceEstimate.getId())
			.serviceRequestId(serviceEstimate.getServiceRequest().getId())
			.createdAt(formatToDateString(serviceEstimate.getCreatedAt()))
			.requester(MemberResponseForDirector.from(serviceEstimate.getServiceRequest().getMember()))
			.service(DirectorServiceWithFullNameResponseForDirector.from(
				serviceEstimate.getServiceRequest().getDirectorService()))
			.locations(
				LocationResponseForDirector.fromList(
					serviceEstimate.getServiceRequest().getRequestLocationMappings().stream()
						.map(RequestLocationMapping::getLocation)
						.toList()
				)
			)
			.status(serviceEstimate.getStatus().getDescription())
			.scheduledAt(formatToDateString(serviceEstimate.getScheduledAt()))
			.price(serviceEstimate.getPrice())
			.completedAt(formatToDateString(serviceEstimate.getDirectorDoneAt()))
			.expiredAt(formatToDateString(serviceEstimate.getExpiredAt()))
			.canceledAt(formatToDateString(serviceEstimate.getCanceledAt()))
			.isDirectRequest(serviceEstimate.getServiceRequest().getIsDirectRequest())
			.chatRoomId(mapping.getChatRoom().getId())
			.files(FileResponseForDirector.fromListWithServiceRequestFiles(serviceEstimate.getServiceRequest().getFiles()))
			.build();
	}
}
