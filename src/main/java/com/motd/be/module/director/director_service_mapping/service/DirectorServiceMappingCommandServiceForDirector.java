package com.motd.be.module.director.director_service_mapping.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.director_service_mapping.repository.DirectorServiceMappingRepositoryForDirector;
import com.motd.be.module.member.director_service_mapping.entity.DirectorServiceMapping;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectorServiceMappingCommandServiceForDirector {

	private final DirectorServiceMappingRepositoryForDirector directorInfoDirectorServiceMappingRepositoryForDirector;

	public void save(DirectorServiceMapping mapping) {
		directorInfoDirectorServiceMappingRepositoryForDirector.save(mapping);
	}

	public void deleteAllByIds(List<Long> ids) {
		directorInfoDirectorServiceMappingRepositoryForDirector.deleteAllByIds(ids);
	}

	public void restoreAllByIds(List<Long> ids) {
		directorInfoDirectorServiceMappingRepositoryForDirector.restoreAllByIds(ids);
	}
}
