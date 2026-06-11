package com.motd.be.module.member.director_info.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.director_info.entity.DirectorInfo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DirectorRankMainViewResponse {

	private List<DirectorRankResponse> directors;

	public static DirectorRankMainViewResponse from(Slice<DirectorInfo> directors) {
		return DirectorRankMainViewResponse.builder()
			.directors(directors.map(DirectorRankResponse::from).toList())
			.build();
	}
}
