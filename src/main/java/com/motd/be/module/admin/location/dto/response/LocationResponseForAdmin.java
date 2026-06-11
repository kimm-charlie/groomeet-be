package com.motd.be.module.admin.location.dto.response;

import static com.motd.be.common.constants.Constants.*;

import com.motd.be.module.member.location.entity.Location;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LocationResponseForAdmin {

	private Long id;
	private String name;
	private String fullName;

	public static LocationResponseForAdmin from(Location location) {
		String fullName = location.getParent() != null
			? location.getParent().getName() + SPACE + location.getName()
			: location.getName();
		return LocationResponseForAdmin.builder()
			.id(location.getId())
			.name(location.getName())
			.fullName(fullName)
			.build();
	}
}
