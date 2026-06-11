package com.motd.be.module.director.request_location_mapping.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.location.service.LocationQueryServiceForDirector;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.request_location_mapping.entity.RequestLocationMapping;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RequestLocationMappingServiceForDirector {

	private final LocationQueryServiceForDirector locationQueryServiceForDirector;
	private final RequestLocationMappingCommandServiceForDirector requestLocationMappingCommandServiceForDirector;

	public void saveRequestLocationForAllCity(ServiceRequest serviceRequest) {
		// ALL CITY 타입 조회
		Location location = locationQueryServiceForDirector.findAllCityLocation();

		requestLocationMappingCommandServiceForDirector.save(RequestLocationMapping.of(location, serviceRequest));
	}
}
