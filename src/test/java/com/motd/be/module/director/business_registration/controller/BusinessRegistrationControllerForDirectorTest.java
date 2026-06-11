package com.motd.be.module.director.business_registration.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.BusinessRegistrationException;
import com.motd.be.exception.exceptions.BusinessRegistrationFileException;
import com.motd.be.exception.exceptions.HandlerException;
import com.motd.be.module.director.business_registration.dto.request.BusinessRegistrationCreateRequestForDirector;
import com.motd.be.module.member.business_registration.entity.BusinessRegistration;
import com.motd.be.module.member.business_registration_file.entity.BusinessRegistrationFile;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
class BusinessRegistrationControllerForDirectorTest extends BaseIntegrationTest {
	@Test
	@DisplayName("디렉터는 사업자등록을 등록할 수 있다.")
	void registerBusinessRegistration() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		BusinessRegistrationFile file1 = businessRegistrationFileProvider.save(director);
		BusinessRegistrationFile file2 = businessRegistrationFileProvider.save(director);
		BusinessRegistrationFile file3 = businessRegistrationFileProvider.save(director);

		List<Long> fileIds = Arrays.asList(file1.getId(), file2.getId(), file3.getId());

		BusinessRegistrationCreateRequestForDirector request = BusinessRegistrationCreateRequestForDirector.builder()
			.businessRegistrationNumber(BUSINESS_REGISTRATION_NUMBER_STR)
			.residentRegistrationNumber(RESIDENT_NUMBER_STR)
			.fileIds(fileIds)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/my/business-registrations")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		entityManager.flush();
		entityManager.clear();

		// 실제 저장된 사업자등록 검증
		List<BusinessRegistration> registrations = businessRegistrationProvider.findAll();
		assertThat(registrations).hasSize(1);

		BusinessRegistration savedRegistration = registrations.get(0);
		assertThat(savedRegistration.getBusinessRegistrationNumber()).isEqualTo(BUSINESS_REGISTRATION_NUMBER_STR);
		assertThat(savedRegistration.getMember().getId()).isEqualTo(director.getId());
		assertThat(stringEncryptor.decrypt(savedRegistration.getResidentRegistrationNumber())).isEqualTo(
			RESIDENT_NUMBER_STR);
	}

	@Test
	@DisplayName("디렉터는 사업자등록을 등록할 수 있다. (파일 갯수 불일치시)")
	void registerBusinessRegistration_invalidFileCount() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		BusinessRegistrationFile file1 = businessRegistrationFileProvider.save(director);
		BusinessRegistrationFile file2 = businessRegistrationFileProvider.save(director);

		List<Long> fileIds = Arrays.asList(file1.getId(), file2.getId(), 999L);

		BusinessRegistrationCreateRequestForDirector request = BusinessRegistrationCreateRequestForDirector.builder()
			.businessRegistrationNumber(BUSINESS_REGISTRATION_NUMBER_STR)
			.residentRegistrationNumber(RESIDENT_NUMBER_STR)
			.fileIds(fileIds)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/my/business-registrations")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(
					BusinessRegistrationFileException.INVALID_IMAGE_EXIST.getHttpStatus().toString()))
			.andExpect(
				jsonPath(ERROR_MESSAGE).value(BusinessRegistrationFileException.INVALID_IMAGE_EXIST.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(BusinessRegistrationFileException.INVALID_IMAGE_EXIST.getCode()));
	}

	@Test
	@DisplayName("디렉터는 사업자등록을 등록할 수 있다. (파일 소유권 불일치시)")
	void registerBusinessRegistration_invalidFileOwner() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Member otherMember = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		BusinessRegistrationFile file1 = businessRegistrationFileProvider.save(director);
		BusinessRegistrationFile file2 = businessRegistrationFileProvider.save(director);
		BusinessRegistrationFile otherFile = businessRegistrationFileProvider.save(otherMember);

		List<Long> fileIds = Arrays.asList(file1.getId(), file2.getId(), otherFile.getId());

		BusinessRegistrationCreateRequestForDirector request = BusinessRegistrationCreateRequestForDirector.builder()
			.businessRegistrationNumber(BUSINESS_REGISTRATION_NUMBER_STR)
			.residentRegistrationNumber(RESIDENT_NUMBER_STR)
			.fileIds(fileIds)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/my/business-registrations")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(
				jsonPath(ERROR_STATUS).value(BusinessRegistrationFileException.NOT_OWNED_BY.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(BusinessRegistrationFileException.NOT_OWNED_BY.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(BusinessRegistrationFileException.NOT_OWNED_BY.getCode()));
	}

	@Test
	@DisplayName("디렉터는 사업자등록을 등록할 수 있다. (한명의 디렉터가 중복해서 사업자 등록증을 업로드 하는 경우)")
	void registerBusinessRegistration_duplicated() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		BusinessRegistrationFile file1 = businessRegistrationFileProvider.save(director);
		BusinessRegistrationFile file2 = businessRegistrationFileProvider.save(director);
		BusinessRegistrationFile file3 = businessRegistrationFileProvider.save(director);

		// 기존 사업자등록 저장
		businessRegistrationProvider.save(director, BUSINESS_REGISTRATION_NUMBER_STR, RESIDENT_NUMBER_STR);

		List<Long> fileIds = Arrays.asList(file1.getId(), file2.getId(), file3.getId());

		BusinessRegistrationCreateRequestForDirector request = BusinessRegistrationCreateRequestForDirector.builder()
			.businessRegistrationNumber(BUSINESS_REGISTRATION_NUMBER_STR)
			.residentRegistrationNumber(RESIDENT_NUMBER_STR)
			.fileIds(fileIds)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/my/business-registrations")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(
					BusinessRegistrationException.DUPLICATED_BUSINESS_REGISTRATION.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(
				BusinessRegistrationException.DUPLICATED_BUSINESS_REGISTRATION.getErrorMessage()))
			.andExpect(
				jsonPath(ERROR_CODE).value(BusinessRegistrationException.DUPLICATED_BUSINESS_REGISTRATION.getCode()));
	}

	@Test
	@DisplayName("디렉터는 사업자등록을 등록할 수 있다. (일반회원이 요청했을때)")
	void registerBusinessRegistration_memberCannotAccess() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		List<Long> fileIds = Arrays.asList(1L, 2L, 3L);

		BusinessRegistrationCreateRequestForDirector request = BusinessRegistrationCreateRequestForDirector.builder()
			.businessRegistrationNumber(BUSINESS_REGISTRATION_NUMBER_STR)
			.residentRegistrationNumber(RESIDENT_NUMBER_STR)
			.fileIds(fileIds)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/my/business-registrations")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.FORBIDDEN.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(HandlerException.FORBIDDEN.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.FORBIDDEN.getCode()));
	}

	@Test
	@DisplayName("디렉터는 사업자등록을 등록할 수 있다. (필수 값 누락)")
	void registerBusinessRegistration_missingRequiredParams() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		List<Long> fileIds = Arrays.asList(1L, 2L, 3L);

		BusinessRegistrationCreateRequestForDirector request = BusinessRegistrationCreateRequestForDirector.builder()
			.businessRegistrationNumber(null) // businessRegistrationNumber 누락
			.fileIds(fileIds)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/my/business-registrations")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("디렉터는 사업자등록을 조회할 수 있다.")
	void findBusinessRegistration() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		BusinessRegistration registration = businessRegistrationProvider.save(director,
			BUSINESS_REGISTRATION_NUMBER_STR, stringEncryptor.encrypt(RESIDENT_NUMBER_STR));

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/my/business-registrations")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(registration.getId()))
			.andExpect(jsonPath("$.businessRegistrationNumber").value(BUSINESS_REGISTRATION_NUMBER_STR))
			.andExpect(jsonPath("$.residentRegistrationNumber").value(RESIDENT_NUMBER_STR))
			.andExpect(jsonPath("$.files").isArray());
	}

	@Test
	@DisplayName("디렉터는 사업자등록을 조회할 수 있다. (아직 사업자 등록증을 등록하지 않은 경우)")
	void findBusinessRegistration_notRegistered() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/my/business-registrations")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").doesNotExist())
			.andExpect(jsonPath("$.businessRegistrationNumber").doesNotExist())
			.andExpect(jsonPath("$.residentRegistrationNumber").doesNotExist());
	}

	@Test
	@DisplayName("디렉터는 사업자등록을 조회할 수 있다. (회원이 요청하는 경우)")
	void findBusinessRegistration_memberCannotAccess() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.get("/api/directors/my/business-registrations")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken())))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.FORBIDDEN.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.FORBIDDEN.getCode()));
	}

}