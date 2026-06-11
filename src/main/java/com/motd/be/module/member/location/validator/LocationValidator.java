package com.motd.be.module.member.location.validator;

import java.util.List;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.LocationException;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.entity.LocationType;

@Component
public class LocationValidator {

	public void validateCombinationAndSize(
		List<Location> locations,
		List<Long> requestedLocationIds
	) {
		// 1. 요청 개수 != 조회 개수
		if (locations.size() != requestedLocationIds.size()) {
			throw new CustomRuntimeException(LocationException.INVALID_LOCATION_EXIST);
		}

		boolean hasAllCity = locations.stream()
			.anyMatch(l -> l.getType() == LocationType.ALL_CITY);

		// 2. 전국은 단독 선택만 가능
		if (hasAllCity && locations.size() > 1) {
			throw new CustomRuntimeException(LocationException.ALL_CITY_WITH_OTHER_LOCATION);
		}

		// 3. 같은 시의 CITY + DISTRICT 동시 선택만 불가
		List<Location> cities = locations.stream()
			.filter(l -> l.getType() == LocationType.CITY)
			.toList();

		List<Location> districts = locations.stream()
			.filter(l -> l.getType() == LocationType.DISTRICT)
			.toList();

		for (Location city : cities) {
			boolean hasChildDistrict = districts.stream()
				.anyMatch(d ->
					d.getParent() != null &&
						d.getParent().getId().equals(city.getId())
				);

			if (hasChildDistrict) {
				throw new CustomRuntimeException(LocationException.CITY_WITH_DISTRICT_NOT_ALLOWED);
			}
		}
	}

	/**
	 * 요청 저장시 사용하는 메서드
	 *
	 * @param locations
	 */
	public void validateForMixedType(List<Location> locations) {
		LocationType baseType = locations.get(0).getType();

		// 1. 타입 혼합 불가
		boolean hasMixedType = locations.stream()
			.anyMatch(l -> l.getType() != baseType);

		if (hasMixedType) {
			throw new CustomRuntimeException(LocationException.LOCATION_TYPE_MIXED);
		}

		// 2. CITY: 서로 다른 시 선택 불가
		if (baseType == LocationType.CITY) {
			Long cityId = locations.get(0).getId();

			boolean hasDifferentCity = locations.stream()
				.anyMatch(l -> !l.getId().equals(cityId));

			if (hasDifferentCity) {
				throw new CustomRuntimeException(LocationException.CITY_MIXED);
			}
		}

		// 3. DISTRICT: 다른 시의 하위 지역 혼합 불가
		if (baseType == LocationType.DISTRICT) {
			Long parentCityId = locations.get(0).getParent().getId();

			boolean hasDifferentParentCity = locations.stream()
				.anyMatch(l ->
					l.getParent() == null ||
						!l.getParent().getId().equals(parentCityId)
				);

			if (hasDifferentParentCity) {
				throw new CustomRuntimeException(LocationException.PARENT_CITY_DIFFERENT);
			}
		}
	}

	public void validateNotEmpty(List<Location> locations) {
		if (locations.isEmpty()) {
			throw new CustomRuntimeException(LocationException.INVALID_PARENT_ID);
		}
	}

}
