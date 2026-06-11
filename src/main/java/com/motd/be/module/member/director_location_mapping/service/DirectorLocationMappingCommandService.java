package com.motd.be.module.member.director_location_mapping.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.director_location_mapping.entity.DirectorLocationMapping;
import com.motd.be.module.member.director_location_mapping.repository.DirectorLocationMappingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectorLocationMappingCommandService {

	private final DirectorLocationMappingRepository directorLocationMappingRepository;

	public void save(DirectorLocationMapping newMapping) {
		directorLocationMappingRepository.save(newMapping);
	}
}
