package com.motd.be.module.director.director_service_mapping.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.DirectorInfoDirectorServiceMappingException;
import com.motd.be.module.director.director_service.service.DirectorServiceQueryServiceForDirector;
import com.motd.be.module.director.director_service_mapping.dto.request.DirectorServiceMappingUpdateServiceRequestForDirector;
import com.motd.be.module.director.director_service_mapping.dto.response.DirectorServiceFindActivationProgressResponseForDirector;
import com.motd.be.module.director.director_service_mapping.dto.response.DirectorServiceFindAllResponseForDirector;
import com.motd.be.module.director.director_service_mapping.service.result.DirectorServiceMappingUpdateResult;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.director_service.validator.DirectorServiceValidator;
import com.motd.be.module.member.director_service_mapping.entity.DirectorServiceMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectorServiceMappingServiceForDirector {

	private final DirectorServiceMappingCommandServiceForDirector directorServiceMappingCommandServiceForDirector;
	private final DirectorServiceQueryServiceForDirector directorServiceQueryServiceForDirector;
	private final DirectorServiceValidator directorServiceValidator;
	private final DirectorServiceMappingQueryServiceForDirector directorServiceMappingQueryServiceForDirector;

	public DirectorServiceMappingUpdateResult update(DirectorInfo directorInfo,
		DirectorServiceMappingUpdateServiceRequestForDirector request) {
		// 1. 현재 매핑 전체 조회 (삭제 포함)
		List<DirectorServiceMapping> allMappings =
			directorServiceMappingQueryServiceForDirector.findAllByDirectorInfoIncludingIsDeletedTrue(directorInfo);

		List<DirectorServiceMapping> deletedMappings = allMappings.stream()
			.filter(DirectorServiceMapping::getIsDeleted)
			.toList();

		Set<DirectorService> deletedServices = deletedMappings.stream()
			.map(DirectorServiceMapping::getDirectorService)
			.collect(Collectors.toSet());

		List<DirectorServiceMapping> activeMappings = allMappings.stream()
			.filter(mapping -> !mapping.getIsDeleted())
			.toList();

		// 2. 요청한 서비스 목록 조회 및 검증
		List<DirectorService> requestedServices = directorServiceQueryServiceForDirector.findAllByIds(
			request.getServiceIds());
		directorServiceValidator.validateRequestedServices(requestedServices, request.getServiceIds());

		// 3. 추가/삭제/복구 목록 계산
		Set<DirectorServiceMapping> toBeDeleted = activeMappings.stream()
			.filter(mapping -> !requestedServices.contains(mapping.getDirectorService()))
			.collect(Collectors.toSet());

		Set<DirectorServiceMapping> toBeAdded = requestedServices.stream()
			.filter(service ->
				activeMappings.stream().noneMatch(m -> m.getDirectorService().equals(service)) &&
					!deletedServices.contains(service)
			)
			.map(service -> DirectorServiceMapping.of(directorInfo, service))
			.collect(Collectors.toSet());

		Set<DirectorServiceMapping> toBeRestored = deletedMappings.stream()
			.filter(mapping -> requestedServices.contains(mapping.getDirectorService()))
			.collect(Collectors.toSet());

		// 4. 삭제
		directorServiceMappingCommandServiceForDirector.deleteAllByIds(
			toBeDeleted.stream().map(DirectorServiceMapping::getId).toList());

		// 5. 추가
		try {
			toBeAdded.forEach(directorServiceMappingCommandServiceForDirector::save);
		} catch (DataIntegrityViolationException e) {
			log.error("서비스 업데이트 중 무결성 위반 발생, directorInfoId={}", directorInfo.getId());
			throw new CustomRuntimeException(DirectorInfoDirectorServiceMappingException.FAIL_TO_UPDATE);
		}

		// 6. 복구
		directorServiceMappingCommandServiceForDirector.restoreAllByIds(
			toBeRestored.stream().map(DirectorServiceMapping::getId).toList());

		return DirectorServiceMappingUpdateResult.of(toBeDeleted, toBeRestored);
	}

	public List<DirectorServiceFindAllResponseForDirector> findAll(DirectorInfo directorInfo) {
		List<DirectorServiceMapping> mappings =
			directorServiceMappingQueryServiceForDirector.findAllByDirectorInfo(directorInfo);
		return DirectorServiceFindAllResponseForDirector.fromList(mappings);
	}

	public List<DirectorServiceFindAllResponseForDirector> findAllForEstimateTemplate(DirectorInfo directorInfo) {
		List<DirectorServiceMapping> mappings =
			directorServiceMappingQueryServiceForDirector.findAllByDirectorInfoForEstimateTemplate(directorInfo);
		return DirectorServiceFindAllResponseForDirector.fromList(mappings);
	}

	public DirectorServiceFindActivationProgressResponseForDirector findActivationProgress(DirectorInfo directorInfo) {
		// 디렉터의 모든 서비스 매핑 조회 및 각 서비스별 활성화 여부 확인
		List<DirectorService> allServices = directorServiceMappingQueryServiceForDirector.findAllByDirectorInfo(
				directorInfo)
			.stream()
			.map(DirectorServiceMapping::getDirectorService)
			.toList();

		boolean hasActiveService =
			allServices.stream()
				.anyMatch(DirectorService::getIsActive);

		// 하나라도 활성화된 서비스가 있으면 100%로 간주
		if (hasActiveService) {
			return DirectorServiceFindActivationProgressResponseForDirector.builder().progressPercentage(100).build();
		}

		// 그외 가장 많이 모집된 서비스의 활성화 퍼센트 반환
		Map<DirectorService, Integer> serviceActivationStatusMap =
			directorServiceMappingQueryServiceForDirector.findServiceActivationStatusMapByDirectorServices(allServices);

		int maxActivationCount = serviceActivationStatusMap.values().stream()
			.max(Integer::compareTo)
			.orElse(0);

		return DirectorServiceFindActivationProgressResponseForDirector.from(maxActivationCount);
	}
}
