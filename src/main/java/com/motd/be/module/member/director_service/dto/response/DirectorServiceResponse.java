package com.motd.be.module.member.director_service.dto.response;

import java.util.List;

import com.motd.be.module.member.director_service.entity.DirectorService;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DirectorServiceResponse {

	private Long id;
	private String name;

	public static DirectorServiceResponse from(DirectorService directorService) {
		return DirectorServiceResponse.builder()
			.id(directorService.getId())
			.name(directorService.getName())
			.build();
	}

	public static List<DirectorServiceResponse> fromList(List<DirectorService> directorServices) {
		return directorServices.stream()
			.map(DirectorServiceResponse::from)
			.toList();
	}
}
