package com.motd.be.module.director.service_request.dto.response;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.util.List;
import java.util.Map;

import com.motd.be.module.director.director_service.dto.response.DirectorServiceWithFullNameResponseForDirector;
import com.motd.be.module.director.file.dto.response.FileResponseForDirector;
import com.motd.be.module.director.location.dto.response.LocationResponseForDirector;
import com.motd.be.module.director.member.dto.response.MemberResponseWithReceivedEstimateCountForDirector;
import com.motd.be.module.member.request_location_mapping.entity.RequestLocationMapping;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request_wish_time.dto.response.WishTimeResponse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceRequestResponseForDirector {

	private Long id;
	private String createdAt;
	private String expiredAt;
	private MemberResponseWithReceivedEstimateCountForDirector member;
	private DirectorServiceWithFullNameResponseForDirector service;
	private List<LocationResponseForDirector> locations;
	private String status;
	private List<WishTimeResponse> wishTimes;
	private Boolean isDirectRequest;
	private List<FileResponseForDirector> files;

	public static List<ServiceRequestResponseForDirector> ofList(List<ServiceRequest> serviceRequests,
		Map<Long, Integer> estimateCountByRequestId) {
		return serviceRequests.stream().map(serviceRequest -> {
			Integer estimateCount = estimateCountByRequestId.getOrDefault(serviceRequest.getId(), 0);
			return ServiceRequestResponseForDirector.of(serviceRequest, estimateCount);
		}).toList();
	}

	public static ServiceRequestResponseForDirector of(ServiceRequest serviceRequest, Integer receivedEstimateCount) {
		return ServiceRequestResponseForDirector.builder()
			.id(serviceRequest.getId())
			.createdAt(formatToDateString(serviceRequest.getCreatedAt()))
			.expiredAt(formatToDateString(serviceRequest.getExpiredAt()))
			.member(MemberResponseWithReceivedEstimateCountForDirector.of(serviceRequest.getMember(),
				receivedEstimateCount))
			.service(DirectorServiceWithFullNameResponseForDirector.from(serviceRequest.getDirectorService()))
			.locations(LocationResponseForDirector.fromList(
				serviceRequest.getRequestLocationMappings().stream().map(RequestLocationMapping::getLocation).toList()))
			.status(serviceRequest.getStatus().getDescription())
			.wishTimes(WishTimeResponse.fromList(serviceRequest.getWishTimes()))
			.isDirectRequest(serviceRequest.getIsDirectRequest())
			.files(FileResponseForDirector.fromListWithServiceRequestFiles(serviceRequest.getFiles()))
			.build();
	}

}
