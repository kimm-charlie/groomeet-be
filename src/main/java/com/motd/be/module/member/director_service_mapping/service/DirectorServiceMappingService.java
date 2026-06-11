package com.motd.be.module.member.director_service_mapping.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.director_service_mapping.dto.response.DirectorServiceFindAllResponseForDirector;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.director_service.service.DirectorServiceQueryService;
import com.motd.be.module.member.director_service.validator.DirectorServiceValidator;
import com.motd.be.module.member.director_service_mapping.entity.DirectorServiceMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectorServiceMappingService {

	private final DirectorServiceMappingCommandService directorServiceMappingCommandService;
	private final DirectorServiceQueryService directorServiceQueryService;
	private final DirectorServiceValidator directorServiceValidator;
	private final DirectorServiceMappingQueryService directorServiceMappingQueryService;

	public List<DirectorServiceFindAllResponseForDirector> findAll(DirectorInfo directorInfo) {
		List<DirectorServiceMapping> mappings =
			directorServiceMappingQueryService.findAllByDirectorInfo(directorInfo);
		return DirectorServiceFindAllResponseForDirector.fromList(mappings);
	}

	public void createMappings(DirectorInfo directorInfo, List<Long> serviceIds) {
		// 1. 유효성 검증
		List<DirectorService> directorServices = directorServiceQueryService.findAllByIds(serviceIds);
		directorServiceValidator.validateRequestedServices(directorServices, serviceIds);

		// 2. 매핑 엔티티 생성
		List<DirectorServiceMapping> mappings = directorServices.stream()
			.map(service -> DirectorServiceMapping.of(directorInfo, service))
			.toList();

		// 3. 일괄 저장
		directorServiceMappingCommandService.saveAll(mappings);
	}
}
