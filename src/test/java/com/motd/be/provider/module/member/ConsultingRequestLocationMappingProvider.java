package com.motd.be.provider.module.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_request_location_mapping.entity.ConsultingRequestLocationMapping;
import com.motd.be.module.member.consulting_request_location_mapping.repository.ConsultingRequestLocationMappingRepository;
import com.motd.be.module.member.location.entity.Location;

@Component
public class ConsultingRequestLocationMappingProvider {

	@Autowired
	private ConsultingRequestLocationMappingRepository consultingRequestLocationMappingRepository;

	public ConsultingRequestLocationMapping save(Location location, ConsultingRequest consultingRequest) {
		return consultingRequestLocationMappingRepository.save(
			ConsultingRequestLocationMapping.of(location, consultingRequest));
	}
}
