package com.motd.be.module.member.member.dto.response;

import java.util.List;

import com.motd.be.module.member.director_location_mapping.entity.DirectorLocationMapping;
import com.motd.be.module.member.location.dto.response.LocationResponse;
import com.motd.be.module.member.member.entity.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberProfileResponse {

	private Long id;
	private MemberResponseWithCompletedAndReviewCountResponse member;
	private List<LocationResponse> locations;
	private String introduceText;
	private Boolean isFavorited;
	private Boolean hasNotEndedRequest;
	private String storeAddress;

	public static MemberProfileResponse of(Member targetMember, Boolean isFavorited, Boolean hasNotEndedRequest) {
		return MemberProfileResponse.builder()
			.id(targetMember.getId())
			.member(MemberResponseWithCompletedAndReviewCountResponse.from(targetMember))
			.locations(
				LocationResponse.fromList(targetMember.getDirectorInfo().getDirectorInfoLocationMappings().stream().map(
					DirectorLocationMapping::getLocation).toList())
			)
			.introduceText(targetMember.getDirectorInfo().getIntroduceText())
			.isFavorited(isFavorited)
			.hasNotEndedRequest(hasNotEndedRequest)
			.storeAddress(targetMember.getDirectorInfo().getStoreAddress())
			.build();
	}
}
