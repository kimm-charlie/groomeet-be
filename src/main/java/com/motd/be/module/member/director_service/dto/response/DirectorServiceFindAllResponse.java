package com.motd.be.module.member.director_service.dto.response;

import java.util.List;

import com.motd.be.module.member.director_service.entity.DirectorService;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DirectorServiceFindAllResponse {

	private Long id;
	private String name;
	private Long parentId;
	private Boolean isActive;

	public static List<DirectorServiceFindAllResponse> fromList(List<DirectorService> directorServices) {
		return directorServices.stream()
			.map(DirectorServiceFindAllResponse::from)
			.toList();
	}

	public static DirectorServiceFindAllResponse from(DirectorService directorService) {
		return DirectorServiceFindAllResponse.builder()
			.id(directorService.getId())
			.name(directorService.getName())
			.parentId(directorService.getParent() != null ? directorService.getParent().getId() : null)
			.isActive(directorService.getIsActive())
			.build();
	}
}
