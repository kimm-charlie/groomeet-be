package com.motd.be.module.member.member.dto.response;

import java.util.List;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_location_mapping.entity.DirectorLocationMapping;
import com.motd.be.module.member.location.dto.response.LocationResponse;
import com.motd.be.module.member.member.entity.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberResponseWithLocation {

	private Long id;
	private String nickname;
	private String profileImageUrl;
	private List<LocationResponse> locations;

	public static MemberResponseWithLocation from(DirectorInfo directorInfo) {
		Member member = directorInfo.getMember();
		return MemberResponseWithLocation.builder()
			.id(member.getId())
			.nickname(member.getNickname())
			.profileImageUrl(member.getCdnProfileImageUrl())
			.locations(
				LocationResponse.fromList(directorInfo
					.getDirectorInfoLocationMappings()
					.stream()
					.map(DirectorLocationMapping::getLocation)
					.toList())
			)
			.build();
	}

}
