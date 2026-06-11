package com.motd.be.module.member.director_location_mapping.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_location_mapping.entity.DirectorLocationMapping;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.service.LocationQueryService;
import com.motd.be.module.member.location.validator.LocationValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectorLocationMappingService {

	private final DirectorLocationMappingCommandService directorLocationMappingCommandService;
	private final LocationQueryService locationQueryService;
	private final LocationValidator locationValidator;

	public void createMappings(DirectorInfo directorInfo, List<Long> locationIds) {
		List<Location> locations = locationQueryService.findAllByIds(locationIds);
		locationValidator.validateCombinationAndSize(locations, locationIds);
		locations.forEach(location ->
			directorLocationMappingCommandService.save(DirectorLocationMapping.of(directorInfo, location))
		);
	}
}
