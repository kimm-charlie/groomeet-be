package com.motd.be.module.director.director_location_mapping.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.ValidationMessages.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.HandlerException;
import com.motd.be.exception.exceptions.LocationException;
import com.motd.be.module.director.director_location_mapping.dto.request.DirectorLocationMappingUpdateRequestForDirector;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_location_mapping.entity.DirectorLocationMapping;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.entity.LocationType;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class DirectorLocationMappingControllerForDirectorTest extends BaseIntegrationTest {

	@Test
	@DisplayName("디렉터 위치를 수정할 수 있다.")
	void updateLocation() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));

		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(member.getId());

		//기존 위치 저장
		Location location = locationProvider.save(서울, LocationType.CITY);
		directorLocationMappingProvider.save(directorInfo, location);

		// 새로운 위치 저장 저장
		Location location1 = locationProvider.save(서울, LocationType.CITY);
		Location location2 = locationProvider.saveWithParent(송파구, LocationType.DISTRICT, location1);

		List<Long> newLocationIds = List.of(location2.getId());
		DirectorLocationMappingUpdateRequestForDirector request = DirectorLocationMappingUpdateRequestForDirector.builder()
			.locationIds(newLocationIds)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.patch("/api/directors/my/location")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		List<DirectorLocationMapping> mappings = directorLocationMappingProvider.findAll();

		List<Long> savedLocationIds = mappings.stream()
			.map(mapping -> mapping.getLocation().getId())
			.collect(Collectors.toList());

		Assertions.assertThat(savedLocationIds).containsExactlyInAnyOrderElementsOf(newLocationIds);
	}

	@Test
	@DisplayName("디렉터 위치를 수정할 수 있다.(최대 갯수인 3개를 넘었을때)")
	void updateLocationExceededMaxLocationCount() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));

		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(member.getId());

		//기존 위치 저장
		Location location = locationProvider.save(서울, LocationType.CITY);
		directorLocationMappingProvider.save(directorInfo, location);

		// 새로운 위치 저장 저장
		Location location1 = locationProvider.save(서울, LocationType.CITY);
		Location location2 = locationProvider.saveWithParent(송파구, LocationType.DISTRICT, location1);
		Location location3 = locationProvider.saveWithParent(송파구, LocationType.DISTRICT, location1);
		Location location4 = locationProvider.saveWithParent(송파구, LocationType.DISTRICT, location1);
		Location location5 = locationProvider.saveWithParent(송파구, LocationType.DISTRICT, location1);

		List<Long> newLocationIds = List.of(location2.getId(), location3.getId(), location4.getId(), location5.getId());
		DirectorLocationMappingUpdateRequestForDirector request = DirectorLocationMappingUpdateRequestForDirector.builder()
			.locationIds(newLocationIds)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.patch("/api/directors/my/location")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath("$.status").value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(LOCATION_EXCEED_MAX_COUNT))
			.andExpect(jsonPath("$.code").value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("디렉터 위치를 수정할 수 있다. (이때 서울, 송파구 둘다 지정시 예외가 발생한다.)")
	void updateLocationWithSeoulAndSongPaGu() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));

		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(member.getId());

		//기존 위치 저장
		Location location = locationProvider.save(서울, LocationType.CITY);
		directorLocationMappingProvider.save(directorInfo, location);

		// 새로운 위치 저장 저장
		Location location1 = locationProvider.save(서울, LocationType.CITY);
		Location location2 = locationProvider.saveWithParent(송파구, LocationType.DISTRICT, location1);

		List<Long> newLocationIds = List.of(location1.getId(), location2.getId());
		DirectorLocationMappingUpdateRequestForDirector request = DirectorLocationMappingUpdateRequestForDirector.builder()
			.locationIds(newLocationIds)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.patch("/api/directors/my/location")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath("$.status").value(LocationException.CITY_WITH_DISTRICT_NOT_ALLOWED.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(LocationException.CITY_WITH_DISTRICT_NOT_ALLOWED.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(LocationException.CITY_WITH_DISTRICT_NOT_ALLOWED.getCode()));
	}

	@Test
	@DisplayName("디렉터 위치를 수정할 수 있다. (이때 전국, 특정 도시 지정시 예외가 발생한다.)")
	void updateLocationWithAllCityAndSeoul() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));

		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(member.getId());

		//기존 위치 저장
		Location location = locationProvider.save(서울, LocationType.CITY);
		directorLocationMappingProvider.save(directorInfo, location);

		// 새로운 위치 저장 저장
		Location location1 = locationProvider.save(전체, LocationType.ALL_CITY);
		Location location2 = locationProvider.saveWithParent(서울, LocationType.CITY, null);

		List<Long> newLocationIds = List.of(location1.getId(), location2.getId());
		DirectorLocationMappingUpdateRequestForDirector request = DirectorLocationMappingUpdateRequestForDirector.builder()
			.locationIds(newLocationIds)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.patch("/api/directors/my/location")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath("$.status").value(LocationException.ALL_CITY_WITH_OTHER_LOCATION.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(LocationException.ALL_CITY_WITH_OTHER_LOCATION.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(LocationException.ALL_CITY_WITH_OTHER_LOCATION.getCode()));
	}

	@Test
	@DisplayName("디렉터 위치를 수정할 수 있다. (이때 전국, 특정 구 지정시 예외가 발생한다.)")
	void updateLocationWithAllCityAndSongPa() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));

		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(member.getId());

		//기존 위치 저장
		Location location = locationProvider.save(서울, LocationType.CITY);
		directorLocationMappingProvider.save(directorInfo, location);

		// 새로운 위치 저장 저장
		Location location1 = locationProvider.save(전체, LocationType.ALL_CITY);
		Location location2 = locationProvider.saveWithParent(서울, LocationType.CITY, null);
		Location location3 = locationProvider.saveWithParent(송파구, LocationType.DISTRICT, location2);

		List<Long> newLocationIds = List.of(location1.getId(), location3.getId());
		DirectorLocationMappingUpdateRequestForDirector request = DirectorLocationMappingUpdateRequestForDirector.builder()
			.locationIds(newLocationIds)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.patch("/api/directors/my/location")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath("$.status").value(LocationException.ALL_CITY_WITH_OTHER_LOCATION.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(LocationException.ALL_CITY_WITH_OTHER_LOCATION.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(LocationException.ALL_CITY_WITH_OTHER_LOCATION.getCode()));
	}

}
