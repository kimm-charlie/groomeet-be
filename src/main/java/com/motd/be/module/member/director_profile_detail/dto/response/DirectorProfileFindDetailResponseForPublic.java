package com.motd.be.module.member.director_profile_detail.dto.response;

import com.motd.be.module.member.director_profile_detail.entity.DirectorProfileDetail;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DirectorProfileFindDetailResponseForPublic {

	private Long id;
	private String contentJson;

	public static DirectorProfileFindDetailResponseForPublic from(DirectorProfileDetail directorProfileDetail) {
		return DirectorProfileFindDetailResponseForPublic.builder()
			.id(directorProfileDetail.getId())
			.contentJson(directorProfileDetail.getContentJson())
			.build();
	}
}
