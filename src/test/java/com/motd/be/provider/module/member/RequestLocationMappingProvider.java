package com.motd.be.provider.module.member;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.request_location_mapping.entity.RequestLocationMapping;
import com.motd.be.module.member.request_location_mapping.repository.RequestLocationMappingRepository;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

@Component
public class RequestLocationMappingProvider {

	@Autowired
	private RequestLocationMappingRepository requestLocationMappingRepository;

	public List<RequestLocationMapping> findAll() {
		return requestLocationMappingRepository.findAll();
	}

	public RequestLocationMapping save(Location location, ServiceRequest serviceRequest) {
		return requestLocationMappingRepository.save(RequestLocationMapping.builder()
			.location(location)
			.serviceRequest(serviceRequest)
			.build());
	}
}
