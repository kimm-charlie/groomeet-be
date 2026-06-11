package com.motd.be.module.member.director_service.dto.response;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.utils.Utils.*;

import java.util.List;

import com.motd.be.module.member.director_service.entity.DirectorService;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DirectorServiceWithFullNameResponse {

	private Long id;
	private String name;
	private String fullName;

	public static DirectorServiceWithFullNameResponse from(DirectorService directorService) {
		return DirectorServiceWithFullNameResponse.builder()
			.id(directorService.getId())
			.name(directorService.getName())
			.fullName(buildFullName(
				directorService,
				DirectorService::getName,
				DirectorService::getParent,
				PATH_SEPARATOR
			))
			.build();
	}

	public static List<DirectorServiceWithFullNameResponse> fromList(List<DirectorService> directorServices) {
		return directorServices.stream()
			.map(DirectorServiceWithFullNameResponse::from)
			.toList();
	}

}
