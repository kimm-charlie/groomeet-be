package com.motd.be.module.director.service_request.dto.response;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.utils.DateFormatUtils.*;

import java.util.List;

import com.motd.be.module.director.director_service.dto.response.DirectorServiceWithFullNameResponseForDirector;
import com.motd.be.module.director.file.dto.response.FileResponseForDirector;
import com.motd.be.module.director.location.dto.response.LocationResponseForDirector;
import com.motd.be.module.director.member.dto.response.MemberResponseWithReceivedEstimateCountForDirector;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request_wish_time.dto.response.WishTimeResponse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceRequestFindDetailResponseForDirector {

	private Long id;
	private DirectorServiceWithFullNameResponseForDirector service;
	private String createdAt;
	private String expiredAt;
	private MemberResponseWithReceivedEstimateCountForDirector member;
	private List<LocationResponseForDirector> locations;
	private List<WishTimeResponse> wishTimes;
	private Boolean isDirectRequest;
	private String status;
	private String content;
	private List<FileResponseForDirector> files;

	public static ServiceRequestFindDetailResponseForDirector of(ServiceRequest serviceRequest,
		Integer estimateCount) {
		Boolean isDirectRequest = serviceRequest.getIsDirectRequest();
		String content = Boolean.TRUE.equals(isDirectRequest)
			? serviceRequest.getAdditionalRequest()
			: serviceRequest.getAiContent();

		return ServiceRequestFindDetailResponseForDirector.builder()
			.id(serviceRequest.getId())
			.service(DirectorServiceWithFullNameResponseForDirector.from(serviceRequest.getDirectorService()))
			.createdAt(formatToDateString(serviceRequest.getCreatedAt()))
			.expiredAt(formatToDateString(serviceRequest.getExpiredAt()))
			.locations(serviceRequest.getRequestLocationMappings()
				.stream()
				.map(mapping -> LocationResponseForDirector.from(mapping.getLocation()))
				.toList())
			.wishTimes(WishTimeResponse.fromList(serviceRequest.getWishTimes()))
			.member(MemberResponseWithReceivedEstimateCountForDirector.of(serviceRequest.getMember(), estimateCount))
			.isDirectRequest(isDirectRequest)
			.status(serviceRequest.getStatus().getDescription())
			.content(content)
			.files(FileResponseForDirector.fromListWithServiceRequestFiles(serviceRequest.getFiles()))
			.build();
	}
}
