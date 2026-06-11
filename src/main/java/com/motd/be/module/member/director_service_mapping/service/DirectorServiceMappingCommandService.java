package com.motd.be.module.member.director_service_mapping.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.director_service_mapping.entity.DirectorServiceMapping;
import com.motd.be.module.member.director_service_mapping.repository.DirectorServiceMappingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectorServiceMappingCommandService {

	private final DirectorServiceMappingRepository directorInfoDirectorServiceMappingRepository;
	
	public void saveAll(List<DirectorServiceMapping> mappings) {
		directorInfoDirectorServiceMappingRepository.saveAll(mappings);
	}
}
