package com.motd.be.module.member.location.dto.response;

import java.util.List;

import com.motd.be.module.member.location.entity.Location;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LocationFindAllResponse {

	private Long id;
	private String name;
	private String locationType;
	private Long parentId;

	public static LocationFindAllResponse from(Location location) {
		return LocationFindAllResponse.builder()
			.id(location.getId())
			.name(location.getName())
			.locationType(location.getType().name())
			.parentId(location.getParent() != null ? location.getParent().getId() : null)
			.build();
	}

	public static List<LocationFindAllResponse> fromList(List<Location> locations) {
		return locations.stream()
			.map(LocationFindAllResponse::from)
			.toList();
	}
}
