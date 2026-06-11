package com.motd.be.module.director.director_service_mapping.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.director_service_mapping.repository.DirectorServiceMappingRepositoryForDirector;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.director_service_mapping.entity.DirectorServiceMapping;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectorServiceMappingQueryServiceForDirector {

	private final DirectorServiceMappingRepositoryForDirector directorInfoDirectorServiceMappingRepositoryForDirector;

	public List<DirectorServiceMapping> findAllByDirectorInfoIncludingIsDeletedTrue(
		DirectorInfo directorInfo) {
		return directorInfoDirectorServiceMappingRepositoryForDirector.findAllByDirectorInfoIncludingIsDeletedTrue(
			directorInfo);
	}

	public List<DirectorServiceMapping> findAllByDirectorInfo(DirectorInfo directorInfo) {
		return directorInfoDirectorServiceMappingRepositoryForDirector.findAllByDirectorInfo(directorInfo);
	}

	public List<DirectorServiceMapping> findAllByDirectorInfoForEstimateTemplate(DirectorInfo directorInfo) {
		return directorInfoDirectorServiceMappingRepositoryForDirector.findAllByDirectorInfoForEstimateTemplate(
			directorInfo);
	}

	public Map<DirectorService, Integer> findServiceActivationStatusMapByDirectorServices(
		List<DirectorService> directorServices) {
		return directorInfoDirectorServiceMappingRepositoryForDirector.findDirectorCountByDirectorServices(
				directorServices).stream()
			.collect(Collectors.toMap(
				r -> (DirectorService)r[0],
				r -> ((Number)r[1]).intValue()
			));
	}
}
