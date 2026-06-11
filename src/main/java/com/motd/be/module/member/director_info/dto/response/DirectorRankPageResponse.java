package com.motd.be.module.member.director_info.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.director_info.entity.DirectorInfo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DirectorRankPageResponse {

	private int page;
	private Boolean hasNext;
	private List<DirectorRankResponse> directors;

	public static DirectorRankPageResponse from(Slice<DirectorInfo> directors) {
		return DirectorRankPageResponse.builder()
			.page(directors.getNumber())
			.hasNext(directors.hasNext())
			.directors(directors.map(DirectorRankResponse::from).toList())
			.build();
	}
}
