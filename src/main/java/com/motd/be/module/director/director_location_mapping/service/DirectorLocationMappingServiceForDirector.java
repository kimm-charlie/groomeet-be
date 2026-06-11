package com.motd.be.module.director.director_location_mapping.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.LocationException;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_location_mapping.entity.DirectorLocationMapping;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.service.LocationQueryService;
import com.motd.be.module.member.location.validator.LocationValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectorLocationMappingServiceForDirector {

	private final DirectorLocationMappingCommandServiceForDirector directorLocationMappingCommandServiceForDirector;
	private final DirectorLocationMappingQueryServiceForDirector directorLocationMappingQueryServiceForDirector;
	private final LocationQueryService locationQueryService;
	private final LocationValidator locationValidator;

	public void updateMappings(DirectorInfo directorInfo, List<Long> locationsIds) {
		// 선택된 지역 목록 조회
		List<Location> locationsFromDb = locationQueryService.findAllByIds(locationsIds);

		// 지역 조합 및 개수 유효성 검증
		locationValidator.validateCombinationAndSize(locationsFromDb, locationsIds);

		List<DirectorLocationMapping> currentMappings =
			directorLocationMappingQueryServiceForDirector.findAllByDirectorIdWithLocation(directorInfo.getId());

		Set<Long> selectedIds = locationsFromDb.stream()
			.map(Location::getId)
			.collect(Collectors.toSet());

		// 삭제 대상
		List<DirectorLocationMapping> toDelete = currentMappings.stream()
			.filter(mapping -> !selectedIds.contains(mapping.getLocation().getId()))
			.toList();

		// 추가 대상
		List<Location> toAdd = locationsFromDb.stream()
			.filter(location -> currentMappings.stream()
				.noneMatch(mapping -> mapping.getLocation().getId().equals(location.getId())))
			.toList();

		// 실제 반영
		try {
			toDelete.forEach(directorLocationMappingCommandServiceForDirector::delete);
			toAdd.forEach(location ->
				directorLocationMappingCommandServiceForDirector.save(
					DirectorLocationMapping.of(directorInfo, location)));
		} catch (DataIntegrityViolationException e) {
			log.error("지역 업데이트 중 데이터 무결성 위반 발생 - directorId: {}", directorInfo.getId());
			throw new CustomRuntimeException(LocationException.FAIL_TO_SAVE_OR_UPDATE);
		}
	}
}
