package com.motd.be.module.member.director_info.dto.response;

import java.util.List;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.dto.response.DirectorServiceResponse;
import com.motd.be.module.member.member.dto.response.MemberResponse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DirectorRankResponse {

	private MemberResponse director;
	private List<DirectorServiceResponse> services;
	private Integer completedEstimateCount;

	public static DirectorRankResponse from(DirectorInfo directorInfo) {
		return DirectorRankResponse.builder()
			.director(MemberResponse.from(directorInfo.getMember()))
			.services(directorInfo.getDirectorServiceMappings().stream()
				.map(mapping -> DirectorServiceResponse.from(mapping.getDirectorService()))
				.toList())
			.completedEstimateCount(directorInfo.getCompletedEstimateCount())
			.build();
	}
}
