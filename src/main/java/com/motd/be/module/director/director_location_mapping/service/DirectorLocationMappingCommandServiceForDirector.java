package com.motd.be.module.director.director_location_mapping.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.director.director_location_mapping.repository.DirectorLocationMappingRepositoryForDirector;
import com.motd.be.module.member.director_location_mapping.entity.DirectorLocationMapping;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectorLocationMappingCommandServiceForDirector {

	private final DirectorLocationMappingRepositoryForDirector directorLocationMappingRepositoryForDirector;

	public void save(DirectorLocationMapping newMapping) {
		directorLocationMappingRepositoryForDirector.save(newMapping);
	}

	public void delete(DirectorLocationMapping mapping) {
		directorLocationMappingRepositoryForDirector.delete(mapping);
	}
}
