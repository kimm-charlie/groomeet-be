package com.motd.be.module.member.member_location_mapping.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.LocationException;
import com.motd.be.module.member.location.dto.response.LocationResponse;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.service.LocationQueryService;
import com.motd.be.module.member.location.validator.LocationValidator;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member_location_mapping.entity.MemberLocationMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberLocationMappingService {

	private final MemberLocationMappingCommandService memberLocationMappingCommandService;
	private final MemberLocationMappingQueryService memberLocationMappingQueryService;
	private final LocationQueryService locationQueryService;
	private final LocationValidator locationValidator;

	public List<LocationResponse> findAll(Member member) {
		List<MemberLocationMapping> mappings = memberLocationMappingQueryService.findAllByMemberIdWithLocation(
			member.getId());

		return LocationResponse.fromList(mappings.stream()
			.map(MemberLocationMapping::getLocation)
			.toList());
	}

	public void updateMappings(Member member, List<Long> locationsIds) {
		if (locationsIds == null || locationsIds.isEmpty()) {
			// 모든 지역 삭제
			memberLocationMappingCommandService.deleteAllByMember(member);
			return;
		}

		List<Location> locationsFromDb = locationQueryService.findAllByIds(locationsIds);
		locationValidator.validateCombinationAndSize(locationsFromDb, locationsIds);

		List<MemberLocationMapping> currentMappings =
			memberLocationMappingQueryService.findAllByMemberIdWithLocation(member.getId());

		Set<Long> selectedIds = locationsFromDb.stream()
			.map(Location::getId)
			.collect(Collectors.toSet());

		List<MemberLocationMapping> toDelete = currentMappings.stream()
			.filter(mapping -> !selectedIds.contains(mapping.getLocation().getId()))
			.toList();

		List<Location> toAdd = locationsFromDb.stream()
			.filter(location -> currentMappings.stream()
				.noneMatch(mapping -> mapping.getLocation().getId().equals(location.getId())))
			.toList();

		try {
			memberLocationMappingCommandService.deleteAllByMappings(toDelete);
			toAdd.forEach(location -> memberLocationMappingCommandService
				.save(MemberLocationMapping.of(member, location)));
		} catch (DataIntegrityViolationException e) {
			log.error("지역 업데이트 중 데이터 무결성 위반 발생 - memberId: {}", member.getId());
			throw new CustomRuntimeException(LocationException.FAIL_TO_SAVE_OR_UPDATE);
		}
	}
}
