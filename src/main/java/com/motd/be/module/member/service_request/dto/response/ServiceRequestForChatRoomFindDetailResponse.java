package com.motd.be.module.member.service_request.dto.response;

import java.util.List;

import com.motd.be.module.member.director_service.dto.response.DirectorServiceWithFullNameResponse;
import com.motd.be.module.member.location.dto.response.LocationResponse;
import com.motd.be.module.member.request_location_mapping.entity.RequestLocationMapping;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request_wish_time.dto.response.WishTimeResponse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceRequestForChatRoomFindDetailResponse {

	private Long id;
	private DirectorServiceWithFullNameResponse service;
	private List<WishTimeResponse> wishTimes;
	private List<LocationResponse> location;

	public static ServiceRequestForChatRoomFindDetailResponse from(ServiceRequest serviceRequest) {
		return ServiceRequestForChatRoomFindDetailResponse.builder()
			.id(serviceRequest.getId())
			.service(DirectorServiceWithFullNameResponse.from(serviceRequest.getDirectorService()))
			.wishTimes(WishTimeResponse.fromList(serviceRequest.getWishTimes()))
			.location(LocationResponse.fromList(
				serviceRequest.getRequestLocationMappings().stream()
					.map(RequestLocationMapping::getLocation)
					.toList()
			))
			.build();
	}

}
