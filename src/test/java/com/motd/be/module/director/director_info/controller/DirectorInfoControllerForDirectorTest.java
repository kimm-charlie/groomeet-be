package com.motd.be.module.director.director_info.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.ValidationMessages.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.ForbiddenWordException;
import com.motd.be.exception.exceptions.HandlerException;
import com.motd.be.module.director.director_info.dto.request.DirectorInfoUpdateIntroduceTextRequestForDirector;
import com.motd.be.module.director.director_info.dto.request.DirectorInfoUpdateStoreAddressRequestForDirector;
import com.motd.be.module.director.director_info.dto.response.DirectorInfoFindProfileBasicInfoResponseForDirector;
import com.motd.be.module.director.director_info.dto.response.DirectorInfoFindProfileCompletenessResponseForDirector;
import com.motd.be.module.director.location.dto.response.LocationResponseForDirector;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.entity.LocationType;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class DirectorInfoControllerForDirectorTest extends BaseIntegrationTest {

	@Test
	@DisplayName("디렉터 소개글을 수정할 수 있다.")
	void updateIntroduceText() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));

		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(member.getId());

		DirectorInfoUpdateIntroduceTextRequestForDirector request = DirectorInfoUpdateIntroduceTextRequestForDirector.builder()
			.introduceText(UPDATED_INTRODUCE_TEXT_STR)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.patch("/api/directors/my/introduce-text")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		DirectorInfo updatedDirector = directorInfoProvider.findById(directorInfo.getId());
		assertThat(updatedDirector.getIntroduceText()).isEqualTo(UPDATED_INTRODUCE_TEXT_STR);
	}

	@Test
	@DisplayName("디렉터 소개글을 수정할 수 있다. (500자 를 초과하는 경우)")
	void updateIntroduceTextWithOver500() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));

		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(member.getId());

		String updatedIntroduceTextOver500 = "A".repeat(600);

		DirectorInfoUpdateIntroduceTextRequestForDirector request = DirectorInfoUpdateIntroduceTextRequestForDirector.builder()
			.introduceText(updatedIntroduceTextOver500)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.patch("/api/directors/my/introduce-text")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(INTRODUCE_TEXT_OUT_OF_BOUND))
			.andExpect(jsonPath("$.code").value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("디렉터 매장주소를 수정할 수 있다.")
	void updateStoreAddress() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));

		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(member.getId());

		DirectorInfoUpdateStoreAddressRequestForDirector request = DirectorInfoUpdateStoreAddressRequestForDirector.builder()
			.storeAddress(UPDATED_STORE_ADDRESS_STR)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.patch("/api/directors/my/store-address")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		DirectorInfo updatedDirector = directorInfoProvider.findById(directorInfo.getId());
		assertThat(updatedDirector.getStoreAddress()).isEqualTo(UPDATED_STORE_ADDRESS_STR);
	}

	@Test
	@DisplayName("디렉터 프로필 완성도 정보를 조회할 수 있다.")
	void findProfileCompleteness() throws Exception {
		// given
		boolean isServiceDetailExist = true;
		boolean isPortfolioExist = true;
		boolean isAccountVerified = false;
		boolean isEstimateTemplateExist = false;

		DirectorInfo directorInfo = directorInfoProvider.saveWithProfileCompleteness(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, isServiceDetailExist,
			isPortfolioExist, isAccountVerified, isEstimateTemplateExist);

		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/me/profile-completeness")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		// then
		DirectorInfoFindProfileCompletenessResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			DirectorInfoFindProfileCompletenessResponseForDirector.class);

		assertThat(response.getIsProfileDetailExist()).isEqualTo(isServiceDetailExist);
		assertThat(response.getIsPortfolioExist()).isEqualTo(isPortfolioExist);
		assertThat(response.getIsFirstCashCharged()).isEqualTo(isAccountVerified);
		assertThat(response.getIsEstimateTemplateExist()).isEqualTo(isEstimateTemplateExist);
	}

	@Test
	@DisplayName("디렉터 기본 프로필 정보를 조회할 수 있다.")
	void findProfileBasicInfo() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member directorMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(directorMember.getId());

		// 서비스 카테고리/서비스 생성 및 매핑 (자식 서비스만 매핑)
		DirectorService rootService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService childService1 = directorCategoryProvider.save(SERVICE_NAME_2_STR, rootService);
		DirectorService childService2 = directorCategoryProvider.save(SERVICE_NAME_3_STR, rootService);

		directorServiceMappingProvider.save(directorInfo, childService1);
		directorServiceMappingProvider.saveWithIsDeletedTrue(directorInfo, childService2);

		// 지역 생성 및 매핑 (도시/구 2개)
		Location city = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location district = locationProvider.save(LOCATION_NAME_2_STR, LocationType.DISTRICT);
		directorLocationMappingProvider.save(directorInfo, city);
		directorLocationMappingProvider.save(directorInfo, district);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/me/profile-basic")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		// then
		DirectorInfoFindProfileBasicInfoResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			DirectorInfoFindProfileBasicInfoResponseForDirector.class);

		assertThat(response.getId()).isEqualTo(directorInfo.getId());
		assertThat(response.getMember()).isNotNull();
		assertThat(response.getMember().getId()).isEqualTo(directorMember.getId());
		assertThat(response.getGender()).isNotNull();
		assertThat(response.getIntroduceText()).isEqualTo(INTRODUCE_TEXT_STR);
		assertThat(response.getStoreAddress()).isEqualTo(STORE_ADDRESS_STR);

		// 서비스/지역 매핑이 응답에 반영되었는지 검증
		assertThat(response.getServices()).isNotNull();
		assertThat(response.getServices()).hasSize(1);
		assertThat(response.getServices().get(0).getId()).isEqualTo(childService1.getId());

		assertThat(response.getLocations()).isNotNull();
		assertThat(response.getLocations()).hasSize(2);
		assertThat(response.getLocations().stream().map(LocationResponseForDirector::getId).toList())
			.containsExactlyInAnyOrder(city.getId(), district.getId());
	}

	// 신규 추가: 권한 검증 - DIRECTOR 권한이 아니면 403
	@Test
	@DisplayName("DIRECTOR 권한이 아니면 디렉터 기본 프로필 정보를 조회할 수 없다.")
	void findProfileBasicInfo_forbiddenWhenNotDirector() throws Exception {
		// given: 일반 회원
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/me/profile-basic")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.status").value(HandlerException.FORBIDDEN.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(HandlerException.FORBIDDEN.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(HandlerException.FORBIDDEN.getCode()));
	}

	@Test
	@DisplayName("디렉터 소개글에 금칙어가 포함되어 있으면 수정할 수 없다.")
	void updateIntroduceTextWithForbiddenWord() throws Exception {
		// given
		forbiddenWordProvider.save(FORBIDDEN_WORD);

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));

		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(member.getId());

		DirectorInfoUpdateIntroduceTextRequestForDirector request = DirectorInfoUpdateIntroduceTextRequestForDirector.builder()
			.introduceText("소개글에 " + FORBIDDEN_WORD + "가 포함되어 있습니다.")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.patch("/api/directors/my/introduce-text")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(
				ForbiddenWordException.CONTAINS_FORBIDDEN_WORD.getHttpStatus().toString()))
			.andExpect(jsonPath("$.code").value(ForbiddenWordException.CONTAINS_FORBIDDEN_WORD.getCode()));
	}

	@Test
	@DisplayName("디렉터 스토어 주소에 금칙어가 포함되어 있으면 수정할 수 없다.")
	void updateStoreAddressWithForbiddenWord() throws Exception {
		// given
		forbiddenWordProvider.save(FORBIDDEN_WORD);

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));

		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(member.getId());

		DirectorInfoUpdateStoreAddressRequestForDirector request = DirectorInfoUpdateStoreAddressRequestForDirector.builder()
			.storeAddress("주소에 " + FORBIDDEN_WORD + "가 포함되어 있습니다.")
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.patch("/api/directors/my/store-address")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(
				ForbiddenWordException.CONTAINS_FORBIDDEN_WORD.getHttpStatus().toString()))
			.andExpect(jsonPath("$.code").value(ForbiddenWordException.CONTAINS_FORBIDDEN_WORD.getCode()));
	}
}
