package com.motd.be.module.member.consulting_request_location_mapping.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.consulting_request_location_mapping.entity.ConsultingRequestLocationMapping;
import com.motd.be.module.member.consulting_request_location_mapping.repository.ConsultingRequestLocationMappingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsultingRequestLocationMappingCommandService {

	private final ConsultingRequestLocationMappingRepository consultingRequestLocationMappingRepository;

	public void saveAll(List<ConsultingRequestLocationMapping> mappings) {
		consultingRequestLocationMappingRepository.saveAll(mappings);
	}
}
