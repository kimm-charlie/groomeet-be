package com.motd.be.module.member.director_service_mapping.service.result;

import java.util.Set;

import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.director_service_mapping.entity.DirectorServiceMapping;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DirectorServiceMappingUpdateResult {

	private final Set<DirectorService> deleted;
	private final Set<DirectorService> restored;

	public static DirectorServiceMappingUpdateResult of(Set<DirectorServiceMapping> deleted,
		Set<DirectorServiceMapping> restored) {

		return DirectorServiceMappingUpdateResult.builder()
			.deleted(deleted.stream()
				.map(DirectorServiceMapping::getDirectorService)
				.collect(java.util.stream.Collectors.toSet()))

			.restored(restored.stream()
				.map(DirectorServiceMapping::getDirectorService)
				.collect(java.util.stream.Collectors.toSet()))
			.build();
	}
}
