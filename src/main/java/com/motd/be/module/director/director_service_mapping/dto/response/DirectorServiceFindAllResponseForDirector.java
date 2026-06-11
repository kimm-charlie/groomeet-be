package com.motd.be.module.director.director_service_mapping.dto.response;

import java.util.List;

import com.motd.be.module.member.director_service_mapping.entity.DirectorServiceMapping;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DirectorServiceFindAllResponseForDirector {

	private Long id;
	private String name;
	private String parentName;

	public static List<DirectorServiceFindAllResponseForDirector> fromList(List<DirectorServiceMapping> mappings) {
		return mappings.stream()
			.map(DirectorServiceFindAllResponseForDirector::from)
			.toList();
	}

	private static DirectorServiceFindAllResponseForDirector from(DirectorServiceMapping directorServiceMapping) {
		return DirectorServiceFindAllResponseForDirector.builder()
			.id(directorServiceMapping.getDirectorService().getId())
			.name(directorServiceMapping.getDirectorService().getName())
			.parentName(directorServiceMapping.getDirectorService().getParent().getName())
			.build();
	}
}
