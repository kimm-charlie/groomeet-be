package com.motd.be.module.member.location.service;

import static com.motd.be.common.constants.Constants.*;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.location.dto.response.LocationFindAllResponse;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.entity.LocationType;
import com.motd.be.module.member.location.validator.LocationValidator;
import com.motd.be.module.member.request_location_mapping.entity.RequestLocationMapping;
import com.motd.be.module.member.request_location_mapping.service.RequestLocationMappingCommandService;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocationService {

	private final LocationQueryService locationQueryService;
	private final LocationValidator locationValidator;
	private final RequestLocationMappingCommandService requestLocationMappingCommandService;

	public List<LocationFindAllResponse> findAll(Long locationParentId) {
		List<Location> locations = locationQueryService.findAllByParentId(locationParentId);

		locationValidator.validateNotEmpty(locations);

		// parent 가 존재한다면 = 지금 DISTRICT 조회중
		if (locationParentId != null) {
			Location parent = locations.get(0).getParent();
			Location allDistrict = Location.builder()
				.id(locationParentId) // parent city id 를 그대로 사용
				.name(parent.getName() + DEFAULT_LOCATION_ALL_SUFFIX) // "서울 전체"
				.type(LocationType.CITY)
				.parent(parent)
				.build();

			// 리스트에 추가
			locations.add(0, allDistrict); // 앞에 추가하면 UI에서 "전체" 먼저 보임
		}
		return LocationFindAllResponse.fromList(locations);
	}

	public LocationType saveRequestLocations(ServiceRequest serviceRequest, List<Long> locationIds) {
		List<Location> locations = locationQueryService.findAllByIds(locationIds);

		locationValidator.validateCombinationAndSize(locations, locationIds);
		locationValidator.validateForMixedType(locations);
		LocationType baseLocationType = locations.get(0).getType();
		List<RequestLocationMapping> mappings = locations.stream()
			.map(location -> RequestLocationMapping.of(location, serviceRequest))
			.toList();

		requestLocationMappingCommandService.saveAll(mappings);

		return baseLocationType;
	}

	public void saveRequestLocationForAllCity(ServiceRequest serviceRequest) {
		// ALL CITY 타입 조회
		Location location = locationQueryService.findAllCityLocation();

		requestLocationMappingCommandService.save(RequestLocationMapping.of(location, serviceRequest));
	}
}
