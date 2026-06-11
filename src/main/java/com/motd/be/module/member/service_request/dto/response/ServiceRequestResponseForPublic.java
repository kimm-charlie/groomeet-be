package com.motd.be.module.member.service_request.dto.response;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.util.List;
import java.util.Map;

import com.motd.be.module.member.director_service.dto.response.DirectorServiceWithFullNameResponse;
import com.motd.be.module.member.file.dto.response.FileResponse;
import com.motd.be.module.member.location.dto.response.LocationResponse;
import com.motd.be.module.member.member.dto.response.MemberResponse;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.request_location_mapping.entity.RequestLocationMapping;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request_wish_time.dto.response.WishTimeResponse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceRequestResponseForPublic {

	private Long id;
	private DirectorServiceWithFullNameResponse service;
	private List<LocationResponse> locations;
	private String expiredAt;
	private String status;
	private Boolean isReceivingEstimate;
	private Integer receivedEstimateCount;
	private String createdAt;
	private Boolean isDirectRequest;
	private List<WishTimeResponse> wishTimes;
	private List<MemberResponse> directors;
	private List<FileResponse> files;
	private Long hiredServiceEstimateId;

	public static List<ServiceRequestResponseForPublic> fromList(List<ServiceRequest> serviceRequests,
		Map<Long, Integer> receivedEstimateCountByRequestIds, Map<Long, List<Member>> directorsMap) {
		return serviceRequests.stream()
			.map(serviceRequest -> of(serviceRequest,
				receivedEstimateCountByRequestIds.getOrDefault(serviceRequest.getId(), 0),
				directorsMap.getOrDefault(serviceRequest.getId(), List.of())))
			.toList();
	}

	public static ServiceRequestResponseForPublic of(ServiceRequest serviceRequest, Integer receivedEstimateCount,
		List<Member> directors) {
		return ServiceRequestResponseForPublic.builder()
			.id(serviceRequest.getId())
			.service(DirectorServiceWithFullNameResponse.from(serviceRequest.getDirectorService()))
			.locations(LocationResponse.fromList(
				serviceRequest.getRequestLocationMappings().stream().map(RequestLocationMapping::getLocation).toList()))
			.expiredAt(formatToDateString(serviceRequest.getExpiredAt()))
			.status(serviceRequest.getStatus().getDescription())
			.isReceivingEstimate(serviceRequest.getIsReceivingEstimate())
			.receivedEstimateCount(receivedEstimateCount)
			.createdAt(formatToDateString(serviceRequest.getCreatedAt()))
			.isDirectRequest(serviceRequest.getIsDirectRequest())
			.wishTimes(WishTimeResponse.fromList(serviceRequest.getWishTimes()))
			.directors(MemberResponse.fromList(directors))
			.files(FileResponse.fromListWithServiceRequestFiles(serviceRequest.getFiles()))
			.hiredServiceEstimateId(
				serviceRequest.getHiredEstimate() != null ? serviceRequest.getHiredEstimate().getId() : null)
			.build();
	}
}
