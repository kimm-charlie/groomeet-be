package com.motd.be.module.director.director_info.dto.response;

import java.util.List;

import com.motd.be.module.director.director_service.dto.response.DirectorServiceWithFullNameResponseForDirector;
import com.motd.be.module.director.location.dto.response.LocationResponseForDirector;
import com.motd.be.module.director.member.dto.response.MemberResponseForDirector;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_location_mapping.entity.DirectorLocationMapping;
import com.motd.be.module.member.director_service_mapping.entity.DirectorServiceMapping;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DirectorInfoFindProfileBasicInfoResponseForDirector {

	private Long id;
	private MemberResponseForDirector member;
	private String gender;
	private List<DirectorServiceWithFullNameResponseForDirector> services;
	private String introduceText;
	private List<LocationResponseForDirector> locations;
	private String storeAddress;

	public static DirectorInfoFindProfileBasicInfoResponseForDirector from(DirectorInfo directorInfo) {
		return DirectorInfoFindProfileBasicInfoResponseForDirector.builder()
			.id(directorInfo.getId())
			.member(MemberResponseForDirector.from(directorInfo.getMember()))
			.gender(directorInfo.getGender().name())
			.services(
				DirectorServiceWithFullNameResponseForDirector.fromList(
					directorInfo.getDirectorServiceMappings().stream().map(
						DirectorServiceMapping::getDirectorService).toList()))
			.introduceText(directorInfo.getIntroduceText())
			.locations(LocationResponseForDirector.fromList(directorInfo.getDirectorInfoLocationMappings().stream().map(
				DirectorLocationMapping::getLocation).toList()))
			.storeAddress(directorInfo.getStoreAddress())
			.build();
	}
}
