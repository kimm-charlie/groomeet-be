package com.motd.be.module.member.service_request.dto.response;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.util.List;

import com.motd.be.module.member.director_service.dto.response.DirectorServiceWithFullNameResponse;
import com.motd.be.module.member.file.dto.response.FileResponse;
import com.motd.be.module.member.location.dto.response.LocationResponse;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request_wish_time.dto.response.WishTimeResponse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceRequestFindDetailResponseForPublic {

	private Long id;
	private DirectorServiceWithFullNameResponse service;
	private String createdAt;
	private String content;
	private List<LocationResponse> locations;
	private String completedAt;
	private String canceledAt;
	private String expiredAt;
	private List<WishTimeResponse> wishTimes;
	private Boolean isReceivingEstimate;
	private Boolean isDirectRequest;
	private String status;
	private List<FileResponse> files;

	public static ServiceRequestFindDetailResponseForPublic from(ServiceRequest serviceRequest) {
		Boolean isDirectRequest = serviceRequest.getIsDirectRequest();
		String content = Boolean.TRUE.equals(isDirectRequest)
			? serviceRequest.getAdditionalRequest()
			: serviceRequest.getAiContent();

		return ServiceRequestFindDetailResponseForPublic.builder()
			.id(serviceRequest.getId())
			.service(DirectorServiceWithFullNameResponse.from(serviceRequest.getDirectorService()))
			.createdAt(formatToDateString(serviceRequest.getCreatedAt()))
			.content(content)
			.locations(serviceRequest.getRequestLocationMappings()
				.stream()
				.map(mapping -> LocationResponse.from(mapping.getLocation()))
				.toList())
			.completedAt(formatToDateString(serviceRequest.getCompletedAt()))
			.canceledAt(formatToDateString(serviceRequest.getCanceledAt()))
			.expiredAt(formatToDateString(serviceRequest.getExpiredAt()))
			.wishTimes(WishTimeResponse.fromList(serviceRequest.getWishTimes()))
			.isReceivingEstimate(serviceRequest.getIsReceivingEstimate())
			.isDirectRequest(isDirectRequest)
			.status(serviceRequest.getStatus().getDescription())
			.files(FileResponse.fromListWithServiceRequestFiles(serviceRequest.getFiles()))
			.build();
	}
}
