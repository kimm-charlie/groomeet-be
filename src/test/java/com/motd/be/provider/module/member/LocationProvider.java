package com.motd.be.provider.module.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.entity.LocationType;
import com.motd.be.module.member.location.repository.LocationRepository;

@Component
public class LocationProvider {

	@Autowired
	private LocationRepository locationRepository;

	private static Location locationDummy(String name, LocationType type) {
		return Location.builder()
			.name(name)
			.type(type)
			.build();
	}

	private static Location locationDummyWithParentId(String name, LocationType type, Location parent) {
		return Location.builder()
			.name(name)
			.parent(parent)
			.type(type)
			.build();
	}

	public Location save(String name, LocationType type) {
		return locationRepository.save(locationDummy(name, type));
	}

	public Location saveWithParent(String name, LocationType type, Location parent) {
		return locationRepository.save(locationDummyWithParentId(name, type, parent));
	}
}
