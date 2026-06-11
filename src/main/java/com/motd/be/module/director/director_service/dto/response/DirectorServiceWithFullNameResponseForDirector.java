package com.motd.be.module.director.director_service.dto.response;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.utils.Utils.*;

import java.util.List;

import com.motd.be.module.member.director_service.entity.DirectorService;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DirectorServiceWithFullNameResponseForDirector {

	private Long id;
	private String name;
	private String fullName;

	public static DirectorServiceWithFullNameResponseForDirector from(DirectorService directorService) {
		return DirectorServiceWithFullNameResponseForDirector.builder()
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

	public static List<DirectorServiceWithFullNameResponseForDirector> fromList(
		List<DirectorService> directorServices) {
		return directorServices.stream()
			.map(DirectorServiceWithFullNameResponseForDirector::from)
			.toList();
	}

}
