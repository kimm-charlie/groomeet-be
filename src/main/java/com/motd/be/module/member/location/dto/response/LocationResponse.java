package com.motd.be.module.member.location.dto.response;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.utils.Utils.*;

import java.util.List;

import com.motd.be.module.member.location.entity.Location;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LocationResponse {

	private Long id;
	private String name;
	private String fullName;

	public static LocationResponse from(Location location) {
		return LocationResponse.builder()
			.id(location.getId())
			.name(location.getName())
			.fullName(buildFullName(
				location,
				Location::getName,
				Location::getParent,
				SPACE
			))
			.build();
	}

	public static List<LocationResponse> fromList(List<Location> locations) {
		return locations.stream()
			.map(LocationResponse::from)
			.toList();
	}
}
