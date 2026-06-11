package com.motd.be.module.member.member_location_mapping.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.LocationException;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.location.dto.response.LocationResponse;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.entity.LocationType;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.member_location_mapping.dto.request.MemberLocationMappingUpdateRequest;
import com.motd.be.module.member.member_location_mapping.entity.MemberLocationMapping;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class MemberLocationMappingControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("회원의 지역 목록을 조회할 수 있다.")
	void findAll_success() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Location location1 = locationProvider.save("서울시", LocationType.CITY);
		Location location2 = locationProvider.saveWithParent("강남구", LocationType.DISTRICT, location1);
		Location location3 = locationProvider.saveWithParent("광진구", LocationType.DISTRICT, location1);

		memberLocationMappingProvider.save(member, location1);
		memberLocationMappingProvider.save(member, location2);

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/my/location")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		List<LocationResponse> response = objectMapper.readValue(responseJson,
			objectMapper.getTypeFactory().constructCollectionType(List.class, LocationResponse.class));

		// then
		assertThat(response).hasSize(2);
		assertThat(response)
			.extracting("id")
			.containsExactlyInAnyOrder(location1.getId(), location2.getId());
	}

	@Test
	@DisplayName("회원의 지역 목록을 조회할 수 있다. (지역이 없는 경우)")
	void findAll_emptyList() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/members/my/location")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		List<LocationResponse> response = objectMapper.readValue(responseJson,
			objectMapper.getTypeFactory().constructCollectionType(List.class, LocationResponse.class));

		// then
		assertThat(response).isEmpty();
	}

	@Test
	@DisplayName("회원의 지역을 등록할 수 있다.")
	void saveOrUpdate_save_success() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Location location1 = locationProvider.save("서울시", LocationType.CITY);
		Location location2 = locationProvider.saveWithParent("강남구", LocationType.DISTRICT, location1);
		Location location3 = locationProvider.saveWithParent("광진구", LocationType.DISTRICT, location1);

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		MemberLocationMappingUpdateRequest request = MemberLocationMappingUpdateRequest.builder()
			.locationIds(List.of(location2.getId(), location3.getId()))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.patch("/api/members/my/location")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		// then
		List<MemberLocationMapping> mappings = memberLocationMappingProvider.findAll();
		assertThat(mappings).hasSize(2);
		assertThat(mappings)
			.extracting(mapping -> mapping.getLocation().getId())
			.containsExactlyInAnyOrder(location2.getId(), location3.getId());
	}

	@Test
	@DisplayName("회원의 지역을 수정할 수 있다.")
	void saveOrUpdate_update_success() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Location location1 = locationProvider.save("서울시", LocationType.CITY);
		Location location2 = locationProvider.saveWithParent("강남구", LocationType.DISTRICT, location1);
		Location location3 = locationProvider.saveWithParent("광진구", LocationType.DISTRICT, location1);

		memberLocationMappingProvider.save(member, location1);

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		MemberLocationMappingUpdateRequest request = MemberLocationMappingUpdateRequest.builder()
			.locationIds(List.of(location2.getId(), location3.getId()))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.patch("/api/members/my/location")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		// then
		List<MemberLocationMapping> mappings = memberLocationMappingProvider.findAll();
		assertThat(mappings).hasSize(2);
		assertThat(mappings)
			.extracting(mapping -> mapping.getLocation().getId())
			.containsExactlyInAnyOrder(location2.getId(), location3.getId());
	}

	@Test
	@DisplayName("회원의 지역을 모두 삭제할 수 있다.")
	void saveOrUpdate_deleteAll_success() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Location location1 = locationProvider.save("서울시", LocationType.CITY);
		Location location2 = locationProvider.saveWithParent("강남구", LocationType.DISTRICT, location1);
		Location location3 = locationProvider.saveWithParent("광진구", LocationType.DISTRICT, location1);

		memberLocationMappingProvider.save(member, location1);

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		MemberLocationMappingUpdateRequest request = MemberLocationMappingUpdateRequest.builder()
			.locationIds(List.of())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.patch("/api/members/my/location")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		// then
		List<MemberLocationMapping> mappings = memberLocationMappingProvider.findAll();
		assertThat(mappings).isEmpty();
	}

	@Test
	@DisplayName("회원의 지역을 등록할 수 있다. (존재하지 않는 지역 ID가 포함된 경우)")
	void saveOrUpdate_withNotFoundLocation() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Location location1 = locationProvider.save("서울시", LocationType.CITY);
		Location location2 = locationProvider.saveWithParent("강남구", LocationType.DISTRICT, location1);
		Location location3 = locationProvider.saveWithParent("광진구", LocationType.DISTRICT, location1);

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		MemberLocationMappingUpdateRequest request = MemberLocationMappingUpdateRequest.builder()
			.locationIds(List.of(location1.getId(), 99999999L))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.patch("/api/members/my/location")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(LocationException.INVALID_LOCATION_EXIST.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(LocationException.INVALID_LOCATION_EXIST.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(LocationException.INVALID_LOCATION_EXIST.getCode()));
	}
}
