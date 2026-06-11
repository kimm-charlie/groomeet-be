package com.motd.be.module.member.location.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.LocationException;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.entity.LocationType;
import com.motd.be.module.member.location.repository.LocationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocationQueryService {

	private final LocationRepository locationRepository;

	public List<Location> findAllByIds(List<Long> locationIds) {
		return locationRepository.findAllById(locationIds);
	}

	public Location findById(Long id) {
		return locationRepository.findById(id)
			.orElseThrow(() -> new CustomRuntimeException(LocationException.NOT_FOUND));
	}

	public List<Location> findAllByParentId(Long locationParentId) {
		return locationRepository.findAllByParentId(locationParentId);
	}

	public Location findAllCityLocation() {
		return locationRepository.findAllCityType(LocationType.ALL_CITY)
			.orElseThrow(() -> new CustomRuntimeException(LocationException.ALL_CITY_TYPE_LOCATION_NOT_FOUND));
	}
}
