package com.motd.be.module.member.director_service_mapping.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.director_service_mapping.entity.DirectorServiceMapping;
import com.motd.be.module.member.director_service_mapping.repository.DirectorServiceMappingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectorServiceMappingQueryService {

	private final DirectorServiceMappingRepository directorInfoDirectorServiceMappingRepository;

	public List<DirectorServiceMapping> findAllByDirectorInfo(DirectorInfo directorInfo) {
		return directorInfoDirectorServiceMappingRepository.findAllByDirectorInfo(directorInfo);
	}

	public boolean existsByDirectorInfoAndDirectorService(DirectorInfo directorInfo, DirectorService directorService) {
		return directorInfoDirectorServiceMappingRepository.existsByDirectorInfoAndDirectorService(directorInfo,
			directorService);
	}
}
