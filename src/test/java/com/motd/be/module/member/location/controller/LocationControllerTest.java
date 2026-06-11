package com.motd.be.module.member.location.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.Constants.ERROR_CODE;
import static com.motd.be.Constants.ERROR_MESSAGE;
import static com.motd.be.Constants.ERROR_STATUS;
import static com.motd.be.Constants.LOCATION_PARENT_ID;
import static com.motd.be.common.constants.Constants.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.LocationException;
import com.motd.be.module.member.location.dto.response.LocationFindAllResponse;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.entity.LocationType;

@ControllerIntegrationTest
public class LocationControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("누구나 전체 지역 조회가 가능하다. (이때 parentId 가 없으면 최상위 지역들만 조회된다.)")
	void findAllWithoutParentId() throws Exception {
		// given
		Location allCityLocation = locationProvider.save(CITY_STR, LocationType.ALL_CITY);
		Location cityLocation = locationProvider.save(CITY_STR, LocationType.CITY);
		Location districtLocation = locationProvider.saveWithParent(DISTRICT_STR, LocationType.DISTRICT,
			cityLocation);
		Location independentCityLocation = locationProvider.save(CITY_STR, LocationType.CITY);
		Location independentDistrictLocation = locationProvider.saveWithParent(DISTRICT_STR, LocationType.DISTRICT,
			independentCityLocation);

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/locations"))
			.andExpect(status().isOk())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		// then
		List<LocationFindAllResponse> response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			new TypeReference<>() {
			}
		);

		assertThat(response.size()).isEqualTo(3);

		assertThat(response.stream().map(LocationFindAllResponse::getId))
			.containsExactlyInAnyOrder(
				allCityLocation.getId(),
				cityLocation.getId(),
				independentCityLocation.getId()
			);
	}

	@Test
	@DisplayName("누구나 전체 지역 조회가 가능하다. (이때 parentId 가 있으면 해당 parentId 를 가진 지역들만 조회된다. 또한 부모 이름을 기반으로한 `전체` 컬럼이 추가된다.)")
	void findAllWithParentId() throws Exception {
		// given
		Location allCityLocation = locationProvider.save(CITY_STR, LocationType.ALL_CITY);
		Location cityLocation = locationProvider.save(CITY_STR, LocationType.CITY);
		Location districtLocation = locationProvider.saveWithParent(DISTRICT_STR, LocationType.DISTRICT,
			cityLocation);
		Location independentCityLocation = locationProvider.save(CITY_STR, LocationType.CITY);
		Location independentDistrictLocation = locationProvider.saveWithParent(DISTRICT_STR, LocationType.DISTRICT,
			independentCityLocation);

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/locations")
					.param(LOCATION_PARENT_ID, cityLocation.getId().toString()))
			.andExpect(status().isOk())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		// then
		List<LocationFindAllResponse> response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			new TypeReference<>() {
			}
		);

		assertThat(response.size()).isEqualTo(2);

		// 서울 전체 컬럼이 잘 들어왔는지 확인
		LocationFindAllResponse allDistrictResponse = response.stream()
			.filter(r -> r.getId().equals(cityLocation.getId()))
			.findFirst()
			.orElseThrow();

		assertThat(allDistrictResponse.getName()).isEqualTo(cityLocation.getName() + DEFAULT_LOCATION_ALL_SUFFIX);
	}

	@Test
	@DisplayName("누구나 전체 지역 조회가 가능하다. (유효하지 않은 ParentId 로 조회시 에러가 발생한다.)")
	void findAllWithInvalidParentId() throws Exception {
		// given
		Location allCityLocation = locationProvider.save(CITY_STR, LocationType.ALL_CITY);
		Location cityLocation = locationProvider.save(CITY_STR, LocationType.CITY);
		Location districtLocation = locationProvider.saveWithParent(DISTRICT_STR, LocationType.DISTRICT,
			cityLocation);
		Location independentCityLocation = locationProvider.save(CITY_STR, LocationType.CITY);
		Location independentDistrictLocation = locationProvider.saveWithParent(DISTRICT_STR, LocationType.DISTRICT,
			independentCityLocation);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/locations")
					.param(LOCATION_PARENT_ID, "99999"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(LocationException.INVALID_PARENT_ID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(LocationException.INVALID_PARENT_ID.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(LocationException.INVALID_PARENT_ID.getCode()));
	}

}
