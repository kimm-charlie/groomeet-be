package com.motd.be.module.director.location.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.LocationException;
import com.motd.be.module.director.location.repository.LocationRepositoryForDirector;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.entity.LocationType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocationQueryServiceForDirector {

	private final LocationRepositoryForDirector locationRepositoryForDirector;

	public List<Location> findAllByIds(List<Long> locationIds) {
		return locationRepositoryForDirector.findAllById(locationIds);
	}

	public Location findAllCityLocation() {
		return locationRepositoryForDirector.findAllCityType(LocationType.ALL_CITY)
			.orElseThrow(() -> new CustomRuntimeException(LocationException.ALL_CITY_TYPE_LOCATION_NOT_FOUND));
	}
}
