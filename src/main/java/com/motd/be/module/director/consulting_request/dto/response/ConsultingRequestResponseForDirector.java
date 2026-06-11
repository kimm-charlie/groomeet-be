package com.motd.be.module.director.consulting_request.dto.response;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.util.List;

import com.motd.be.module.director.location.dto.response.LocationResponseForDirector;
import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_request_location_mapping.entity.ConsultingRequestLocationMapping;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ConsultingRequestResponseForDirector {

	private Long id;
	private String memberNickname;
	private ConsultingRequestImagesResponseForDirector images;
	private Boolean usesHairProduct;
	private Boolean prefersExposedForehead;
	private String recentProcedure;
	private List<LocationResponseForDirector> locations;
	private String createdAt;

	public static ConsultingRequestResponseForDirector from(ConsultingRequest consultingRequest) {
		List<LocationResponseForDirector> locationResponses = consultingRequest.getLocationMappings().stream()
			.map(ConsultingRequestLocationMapping::getLocation)
			.map(LocationResponseForDirector::from)
			.toList();

		return ConsultingRequestResponseForDirector.builder()
			.id(consultingRequest.getId())
			.memberNickname(consultingRequest.getMember().getNickname())
			.images(ConsultingRequestImagesResponseForDirector.from(consultingRequest))
			.usesHairProduct(consultingRequest.getUsesHairProduct())
			.prefersExposedForehead(consultingRequest.getPrefersExposedForehead())
			.recentProcedure(consultingRequest.getRecentProcedure())
			.locations(locationResponses)
			.createdAt(formatToDateString(consultingRequest.getCreatedAt()))
			.build();
	}

	public static List<ConsultingRequestResponseForDirector> fromList(List<ConsultingRequest> consultingRequests) {
		return consultingRequests.stream()
			.map(ConsultingRequestResponseForDirector::from)
			.toList();
	}
}
