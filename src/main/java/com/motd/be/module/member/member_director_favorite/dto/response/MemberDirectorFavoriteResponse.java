package com.motd.be.module.member.member_director_favorite.dto.response;

import java.util.List;

import com.motd.be.module.member.director_service.dto.response.DirectorServiceResponse;
import com.motd.be.module.member.member.dto.response.MemberResponseWithCompletedAndReviewCountResponse;
import com.motd.be.module.member.member.entity.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberDirectorFavoriteResponse {

	private MemberResponseWithCompletedAndReviewCountResponse director;
	private List<DirectorServiceResponse> services;

	public static MemberDirectorFavoriteResponse from(Member targetMember) {
		return MemberDirectorFavoriteResponse.builder()
			.director(MemberResponseWithCompletedAndReviewCountResponse.from(targetMember))
			.services(targetMember.getDirectorInfo().getDirectorServiceMappings().stream()
				.map(mapping -> DirectorServiceResponse.from(mapping.getDirectorService()))
				.toList())
			.build();
	}
}
