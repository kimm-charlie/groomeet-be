package com.motd.be.module.member.director_info.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.ValidationMessages.*;
import static com.motd.be.module.member.jwt.JwtProvider.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.DirectorInfoException;
import com.motd.be.exception.exceptions.DirectorServiceException;
import com.motd.be.exception.exceptions.HandlerException;
import com.motd.be.exception.exceptions.MemberException;
import com.motd.be.module.member.director_info.dto.request.DirectorInfoRegisterRequest;
import com.motd.be.module.member.director_info.dto.response.DirectorRankMainViewResponse;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.entity.LocationType;
import com.motd.be.module.member.member.entity.Gender;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.Role;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.member_nickname_history.entity.MemberNicknameHistory;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class DirectorInfoControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("디렉터로 전환이 가능하다.")
	void registerDirectorInfoSuccess() throws Exception {
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		DirectorService directorService1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorServiceProvider.save(SERVICE_NAME_1_STR, directorService1);
		DirectorService directorService3 = directorServiceProvider.save(SERVICE_NAME_2_STR, directorService1);

		DirectorInfoRegisterRequest request = DirectorInfoRegisterRequest.builder()
			.nickname(NICKNAME_STR)
			.locationIds(Arrays.asList(location1.getId(), location2.getId()))
			.directorServiceIds(Arrays.asList(directorService2.getId(), directorService3.getId()))
			.gender(Gender.MAN.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/members/me/director")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		//member 조회시 directorInfo 매핑 및 설정값 변경 확인
		Member director = memberProvider.findById(member.getId());

		assertThat(director.getDirectorInfo()).isNotNull();
		assertThat(director.getRole()).isEqualTo(Role.DIRECTOR);
		assertThat(director.getIsDirector()).isTrue();

		// response 에 cookie 가 2개 포함되며, 해당 jwt 의 role 이 DIRECTOR 인지 확인
		// Set-Cookie 헤더 검증
		String[] setCookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE).toArray(new String[0]);

		// refreshToken 쿠키가 두 개 있는지 확인
		assertThat(setCookies).hasSize(2);

		// accessToken 쿠키 존재 여부 확인
		assertThat(Arrays.stream(setCookies)
			.anyMatch(cookie -> cookie.contains(ACCESS_TOKEN_STR))).isTrue();

		// refreshToken 쿠키 존재 여부 확인
		assertThat(Arrays.stream(setCookies)
			.anyMatch(cookie -> cookie.contains(REFRESH_TOKEN_STR))).isTrue();

		// 쿠키에서 accessToken 값 추출
		String accessToken = Arrays.stream(setCookies)
			.filter(cookie -> cookie.startsWith(ACCESS_TOKEN_STR + "="))
			.findFirst()
			.map(cookie -> cookie.split(";", 2)[0])  // "accessToken=..." 중 앞부분만
			.map(cookie -> cookie.substring(ACCESS_TOKEN_STR.length() + 1)) // "accessToken=" 제외
			.orElse(null);

		Claims accessTokenClaims = getClaimsFromAccessToken(accessToken);
		assertThat(accessTokenClaims.get(ROLE_STR)).isEqualTo(Role.DIRECTOR.getRoleType());

		// 쿠키에서 refreshToken 값 추출
		String refreshTokenCookie = Arrays.stream(setCookies)
			.filter(cookie -> cookie.startsWith(REFRESH_TOKEN_STR + "="))
			.findFirst()
			.map(cookie -> cookie.split(";", 2)[0])  // "accessToken=..." 중 앞부분만
			.map(cookie -> cookie.substring(REFRESH_TOKEN_STR.length() + 1)) // "accessToken=" 제외
			.orElse(null);
		assertThat(refreshTokenCookie).isNotNull();

		Claims refreshTokenClaims = getClaimsFromRefreshToken(refreshTokenCookie);
		assertThat(refreshTokenClaims.get(ROLE_STR)).isEqualTo(Role.DIRECTOR.getRoleType());

		// 저장된 directorInfo 에 directorProfileDetail 도 함께 생성되어 있는지 확인
		DirectorInfo savedDirectorInfo = directorInfoProvider.findById(director.getDirectorInfo().getId());

		assertThat(savedDirectorInfo.getDirectorProfileDetail()).isNotNull();
	}

	@Test
	@DisplayName("디렉터로 전환이 가능하다. (닉네임이 변경된 경우)")
	void registerDirectorInfoWhenNicknameChanged() throws Exception {
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		DirectorService directorService1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorServiceProvider.save(SERVICE_NAME_1_STR, directorService1);
		DirectorService directorService3 = directorServiceProvider.save(SERVICE_NAME_2_STR, directorService1);

		DirectorInfoRegisterRequest request = DirectorInfoRegisterRequest.builder()
			.nickname(UPDATED_NICKNAME_STR)
			.locationIds(Arrays.asList(location1.getId(), location2.getId()))
			.directorServiceIds(Arrays.asList(directorService2.getId(), directorService3.getId()))
			.gender(Gender.MAN.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/members/me/director")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		//member 조회시 directorInfo 매핑 및 설정값 변경 확인
		Member director = memberProvider.findById(member.getId());

		assertThat(director.getDirectorInfo()).isNotNull();
		assertThat(director.getRole()).isEqualTo(Role.DIRECTOR);
		assertThat(director.getIsDirector()).isTrue();

		// response 에 cookie 가 2개 포함되며, 해당 jwt 의 role 이 DIRECTOR 인지 확인
		// Set-Cookie 헤더 검증
		String[] setCookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE).toArray(new String[0]);

		// refreshToken 쿠키가 두 개 있는지 확인
		assertThat(setCookies).hasSize(2);

		// accessToken 쿠키 존재 여부 확인
		assertThat(Arrays.stream(setCookies)
			.anyMatch(cookie -> cookie.contains(ACCESS_TOKEN_STR))).isTrue();

		// refreshToken 쿠키 존재 여부 확인
		assertThat(Arrays.stream(setCookies)
			.anyMatch(cookie -> cookie.contains(REFRESH_TOKEN_STR))).isTrue();

		// 쿠키에서 accessToken 값 추출
		String accessToken = Arrays.stream(setCookies)
			.filter(cookie -> cookie.startsWith(ACCESS_TOKEN_STR + "="))
			.findFirst()
			.map(cookie -> cookie.split(";", 2)[0])  // "accessToken=..." 중 앞부분만
			.map(cookie -> cookie.substring(ACCESS_TOKEN_STR.length() + 1)) // "accessToken=" 제외
			.orElse(null);

		Claims accessTokenClaims = getClaimsFromAccessToken(accessToken);
		assertThat(accessTokenClaims.get(ROLE_STR)).isEqualTo(Role.DIRECTOR.getRoleType());

		// 쿠키에서 refreshToken 값 추출
		String refreshTokenCookie = Arrays.stream(setCookies)
			.filter(cookie -> cookie.startsWith(REFRESH_TOKEN_STR + "="))
			.findFirst()
			.map(cookie -> cookie.split(";", 2)[0])  // "accessToken=..." 중 앞부분만
			.map(cookie -> cookie.substring(REFRESH_TOKEN_STR.length() + 1)) // "accessToken=" 제외
			.orElse(null);
		assertThat(refreshTokenCookie).isNotNull();

		Claims refreshTokenClaims = getClaimsFromRefreshToken(refreshTokenCookie);
		assertThat(refreshTokenClaims.get(ROLE_STR)).isEqualTo(Role.DIRECTOR.getRoleType());

		// 저장된 directorInfo 에 directorProfileDetail 도 함께 생성되어 있는지 확인
		DirectorInfo savedDirectorInfo = directorInfoProvider.findById(director.getDirectorInfo().getId());

		assertThat(savedDirectorInfo.getDirectorProfileDetail()).isNotNull();

		// 닉네임 변경이력 저장 여부 확인
		List<MemberNicknameHistory> histories = memberNicknameHistoryProvider.findAll().stream()
			.filter(history -> history.getMember().getId().equals(member.getId()))
			.toList();

		assertThat(histories).hasSize(1);
		assertThat(histories.get(0).getFromNickname()).isEqualTo(NICKNAME_STR);
		assertThat(histories.get(0).getToNickname()).isEqualTo(UPDATED_NICKNAME_STR);
	}

	@Test
	@DisplayName("디렉터로 전환이 가능하다. (닉네임이 변경되지 않은 경우)")
	void registerDirectorInfoWhenNicknameNotChanged() throws Exception {
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		DirectorService directorService1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorServiceProvider.save(SERVICE_NAME_1_STR, directorService1);
		DirectorService directorService3 = directorServiceProvider.save(SERVICE_NAME_2_STR, directorService1);

		DirectorInfoRegisterRequest request = DirectorInfoRegisterRequest.builder()
			.nickname(member.getNickname())
			.locationIds(Arrays.asList(location1.getId(), location2.getId()))
			.directorServiceIds(Arrays.asList(directorService2.getId(), directorService3.getId()))
			.gender(Gender.MAN.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/members/me/director")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		//member 조회시 directorInfo 매핑 및 설정값 변경 확인
		Member director = memberProvider.findById(member.getId());

		assertThat(director.getDirectorInfo()).isNotNull();
		assertThat(director.getRole()).isEqualTo(Role.DIRECTOR);
		assertThat(director.getIsDirector()).isTrue();

		// response 에 cookie 가 2개 포함되며, 해당 jwt 의 role 이 DIRECTOR 인지 확인
		// Set-Cookie 헤더 검증
		String[] setCookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE).toArray(new String[0]);

		// refreshToken 쿠키가 두 개 있는지 확인
		assertThat(setCookies).hasSize(2);

		// accessToken 쿠키 존재 여부 확인
		assertThat(Arrays.stream(setCookies)
			.anyMatch(cookie -> cookie.contains(ACCESS_TOKEN_STR))).isTrue();

		// refreshToken 쿠키 존재 여부 확인
		assertThat(Arrays.stream(setCookies)
			.anyMatch(cookie -> cookie.contains(REFRESH_TOKEN_STR))).isTrue();

		// 쿠키에서 accessToken 값 추출
		String accessToken = Arrays.stream(setCookies)
			.filter(cookie -> cookie.startsWith(ACCESS_TOKEN_STR + "="))
			.findFirst()
			.map(cookie -> cookie.split(";", 2)[0])  // "accessToken=..." 중 앞부분만
			.map(cookie -> cookie.substring(ACCESS_TOKEN_STR.length() + 1)) // "accessToken=" 제외
			.orElse(null);

		Claims accessTokenClaims = getClaimsFromAccessToken(accessToken);
		assertThat(accessTokenClaims.get(ROLE_STR)).isEqualTo(Role.DIRECTOR.getRoleType());

		// 쿠키에서 refreshToken 값 추출
		String refreshTokenCookie = Arrays.stream(setCookies)
			.filter(cookie -> cookie.startsWith(REFRESH_TOKEN_STR + "="))
			.findFirst()
			.map(cookie -> cookie.split(";", 2)[0])  // "accessToken=..." 중 앞부분만
			.map(cookie -> cookie.substring(REFRESH_TOKEN_STR.length() + 1)) // "accessToken=" 제외
			.orElse(null);
		assertThat(refreshTokenCookie).isNotNull();

		Claims refreshTokenClaims = getClaimsFromRefreshToken(refreshTokenCookie);
		assertThat(refreshTokenClaims.get(ROLE_STR)).isEqualTo(Role.DIRECTOR.getRoleType());

		// 저장된 directorInfo 에 directorProfileDetail 도 함께 생성되어 있는지 확인
		DirectorInfo savedDirectorInfo = directorInfoProvider.findById(director.getDirectorInfo().getId());

		assertThat(savedDirectorInfo.getDirectorProfileDetail()).isNotNull();

		// 닉네임 변경이력 저장 여부 확인
		List<MemberNicknameHistory> histories = memberNicknameHistoryProvider.findAll().stream()
			.filter(history -> history.getMember().getId().equals(member.getId()))
			.toList();

		assertThat(histories).hasSize(0);
	}

	@Test
	@DisplayName("디렉터로 전환이 가능하다.(서비스가 7개 초과인 경우)")
	void registerDirectorInfoWithMoreThan7Services() throws Exception {
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_STR, ADDRESS_STR,
			LocalDate.now());

		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(member.getId());

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		DirectorService directorService1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorServiceProvider.save(SERVICE_NAME_1_STR, directorService1);
		DirectorService directorService3 = directorServiceProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService4 = directorServiceProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService5 = directorServiceProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService6 = directorServiceProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService7 = directorServiceProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService8 = directorServiceProvider.save(SERVICE_NAME_2_STR, directorService1);
		DirectorService directorService9 = directorServiceProvider.save(SERVICE_NAME_2_STR, directorService1);

		DirectorInfoRegisterRequest request = DirectorInfoRegisterRequest.builder()
			.nickname(NICKNAME_STR)
			.locationIds(Arrays.asList(location1.getId(), location2.getId()))
			.directorServiceIds(
				Arrays.asList(directorService2.getId(), directorService3.getId(), directorService4.getId(),
					directorService5.getId(), directorService6.getId(), directorService7.getId(),
					directorService8.getId(), directorService9.getId()))
			.gender(Gender.MAN.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/me/director")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(DIRECTOR_SERVICE_SELECTION_OUT_OF_BOUNDS))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("디렉터로 전환이 가능하다.(이미 디렉터인 회원이면서, 이때 토큰이 디렉터 기반인 경우)")
	void registerDirectorInfoAlreadyDirectorExceptionWithDirectorToken() throws Exception {
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_STR, ADDRESS_STR,
			LocalDate.now());

		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleDirector(member.getId());

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		DirectorService directorService1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorServiceProvider.save(SERVICE_NAME_1_STR, directorService1);
		DirectorService directorService3 = directorServiceProvider.save(SERVICE_NAME_2_STR, directorService1);

		DirectorInfoRegisterRequest request = DirectorInfoRegisterRequest.builder()
			.nickname(NICKNAME_STR)
			.locationIds(Arrays.asList(location1.getId(), location2.getId()))
			.directorServiceIds(Arrays.asList(directorService2.getId(), directorService3.getId()))
			.gender(Gender.MAN.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/me/director")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(
				jsonPath(ERROR_STATUS).value(HandlerException.FORBIDDEN.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(HandlerException.FORBIDDEN.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.FORBIDDEN.getCode()));
	}

	@Test
	@DisplayName("디렉터로 전환이 가능하다.(중복된 닉네임이 존재하는 경우)")
	void registerDirectorInfoDuplicateNicknameExist() throws Exception {
		Member existingMember = memberProvider.saveMember(SignInPlatform.KAKAO);

		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		DirectorService directorService1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorServiceProvider.save(SERVICE_NAME_1_STR, directorService1);
		DirectorService directorService3 = directorServiceProvider.save(SERVICE_NAME_2_STR, directorService1);

		DirectorInfoRegisterRequest request = DirectorInfoRegisterRequest.builder()
			.nickname(NICKNAME_STR)
			.locationIds(Arrays.asList(location1.getId(), location2.getId()))
			.directorServiceIds(Arrays.asList(directorService2.getId(), directorService3.getId()))
			.gender(Gender.MAN.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/me/director")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(MemberException.DUPLICATE_NICKNAME.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(MemberException.DUPLICATE_NICKNAME.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(MemberException.DUPLICATE_NICKNAME.getCode()));
	}

	@Test
	@DisplayName("디렉터로 전환이 가능하다.(이미 디렉터인 회원이면서, 이때 토큰이 일반인 기반인 경우)")
	void registerDirectorInfoAlreadyDirectorExceptionWithMemberToken() throws Exception {
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_STR, ADDRESS_STR,
			LocalDate.now());

		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		DirectorService directorService1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorServiceProvider.save(SERVICE_NAME_1_STR, directorService1);
		DirectorService directorService3 = directorServiceProvider.save(SERVICE_NAME_2_STR, directorService1);

		DirectorInfoRegisterRequest request = DirectorInfoRegisterRequest.builder()
			.nickname(NICKNAME_STR)
			.locationIds(Arrays.asList(location1.getId(), location2.getId()))
			.directorServiceIds(Arrays.asList(directorService2.getId(), directorService3.getId()))
			.gender(Gender.MAN.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/me/director")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(DirectorInfoException.ALREADY_DIRECTOR.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(DirectorInfoException.ALREADY_DIRECTOR.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(DirectorInfoException.ALREADY_DIRECTOR.getCode()));
	}

	@Test
	@DisplayName("디렉터로 전환이 가능하다.(카테고리가 0 개인 경우.)")
	void registerDirectorInfoCategoryCountException() throws Exception {
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		DirectorService directorService1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorServiceProvider.save(SERVICE_NAME_2_STR, directorService1);

		DirectorInfoRegisterRequest request = DirectorInfoRegisterRequest.builder()
			.nickname(NICKNAME_STR)
			.locationIds(Arrays.asList(location1.getId(), location2.getId()))
			.directorServiceIds(null)
			.gender(Gender.MAN.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/me/director")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(DIRECTOR_SERVICE_MUST_BE_SELECTED))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("디렉터로 전환이 가능하다.(지역이 0 개인 경우.)")
	void registerDirectorInfoLocationCountException() throws Exception {
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService directorService1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorServiceProvider.save(SERVICE_NAME_2_STR, directorService1);

		DirectorInfoRegisterRequest request = DirectorInfoRegisterRequest.builder()
			.nickname(NICKNAME_STR)
			.locationIds(null)
			.directorServiceIds(Arrays.asList(directorService1.getId(), directorService2.getId()))
			.gender(Gender.MAN.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/me/director")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(LOCATION_MUST_BE_SELECTED))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("디렉터로 전환이 가능하다.(요청에 포함된 카테고리 id의 갯수와 실제 조회된 category 개수가 다르면 예외가 발생한다.)")
	void registerDirectorInfoCategoryIdMismatchException() throws Exception {
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwtCreatedBySavedMember = generateTokenWithMemberIdRoleMember(member.getId());

		Location location1 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location location2 = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		DirectorService directorService1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorServiceProvider.save(SERVICE_NAME_2_STR, directorService1);

		DirectorInfoRegisterRequest request = DirectorInfoRegisterRequest.builder()
			.nickname(NICKNAME_STR)
			.locationIds(Arrays.asList(location1.getId(), location2.getId()))
			.directorServiceIds(Arrays.asList(99999L, 100000L)) // 존재하지 않는 idw
			.gender(Gender.MAN.name())
			.build();

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/members/me/director")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedMember.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtCreatedBySavedMember.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(DirectorServiceException.INVALID_SERVICE.getHttpStatus().toString()))
			.andExpect(
				jsonPath(ERROR_MESSAGE).value(DirectorServiceException.INVALID_SERVICE.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(DirectorServiceException.INVALID_SERVICE.getCode()));
	}

	@Test
	@DisplayName("메인 화면에서 디렉터 랭킹을 조회할 수 있다.")
	void getDirectorRankForMainSuccess() throws Exception {
		// given
		DirectorInfo directorInfo1 = directorInfoProvider.saveWithCompletedCount(INTRODUCE_STR, ADDRESS_STR, 10);
		DirectorInfo directorInfo2 = directorInfoProvider.saveWithCompletedCount(INTRODUCE_STR, ADDRESS_STR, 50);
		DirectorInfo directorInfo3 = directorInfoProvider.saveWithCompletedCount(INTRODUCE_STR, ADDRESS_STR, 5);
		DirectorInfo directorInfo4 = directorInfoProvider.saveWithCompletedCount(INTRODUCE_STR, ADDRESS_STR, 5);

		Member member1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo1);
		Member member2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo2);
		Member member3 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo3);
		Member withDrawalMember = memberProvider.saveMemberWithDirectorAndWithdrawalTrue(SignInPlatform.KAKAO,
			directorInfo4);

		entityManager.flush();
		entityManager.clear();

		// when
		String body = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/rank")
				.param("viewType", "mainView"))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		DirectorRankMainViewResponse response = objectMapper.readValue(body, DirectorRankMainViewResponse.class);

		// then
		assertThat(response.getDirectors().size()).isEqualTo(3);

		assertThat(response.getDirectors().get(0).getDirector().getId()).isEqualTo(member2.getId());
		assertThat(response.getDirectors().get(0).getCompletedEstimateCount()).isEqualTo(50);

		assertThat(response.getDirectors().get(1).getDirector().getId()).isEqualTo(member1.getId());
		assertThat(response.getDirectors().get(1).getCompletedEstimateCount()).isEqualTo(10);

		assertThat(response.getDirectors().get(2).getDirector().getId()).isEqualTo(member3.getId());
		assertThat(response.getDirectors().get(2).getCompletedEstimateCount()).isEqualTo(5);
	}

	@Test
	@DisplayName("랭킹 페이지에서 디렉터 랭킹을 조회할 수 있다.")
	void getDirectorRankForRankViewSuccess() throws Exception {
		// given
		DirectorInfo directorInfo1 = directorInfoProvider.saveWithCompletedCount(INTRODUCE_STR, ADDRESS_STR, 10);
		DirectorInfo directorInfo2 = directorInfoProvider.saveWithCompletedCount(INTRODUCE_STR, ADDRESS_STR, 50);

		Member member1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo1);
		Member member2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo2);

		entityManager.flush();
		entityManager.clear();

		// when & then
		String body = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/rank")
				.param("viewType", "rankView")
				.param("page", "0"))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		DirectorRankMainViewResponse response = objectMapper.readValue(body, DirectorRankMainViewResponse.class);

		// then
		assertThat(response.getDirectors().size()).isEqualTo(2);

		assertThat(response.getDirectors().get(0).getDirector().getId()).isEqualTo(member2.getId());
		assertThat(response.getDirectors().get(0).getCompletedEstimateCount()).isEqualTo(50);

		assertThat(response.getDirectors().get(1).getDirector().getId()).isEqualTo(member1.getId());
		assertThat(response.getDirectors().get(1).getCompletedEstimateCount()).isEqualTo(10);
	}

	@Test
	@DisplayName("랭킹 페이지에서 디렉터 랭킹을 조회할 수 있다. (페이지네이션)")
	void getDirectorRankForRankViewWithPageSuccess() throws Exception {
		// given
		for (int i = 0; i < 30; i++) {
			DirectorInfo directorInfo = directorInfoProvider.saveWithCompletedCount(INTRODUCE_STR, ADDRESS_STR, i);

			Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		}

		entityManager.flush();
		entityManager.clear();

		// when & then
		String body = mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/rank")
				.param("viewType", "rankView")
				.param("page", "1"))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		DirectorRankMainViewResponse response = objectMapper.readValue(body, DirectorRankMainViewResponse.class);

		// then
		assertThat(response.getDirectors().size()).isEqualTo(10);
	}
}
