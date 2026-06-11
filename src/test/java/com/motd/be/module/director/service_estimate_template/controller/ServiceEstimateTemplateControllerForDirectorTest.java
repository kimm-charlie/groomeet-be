package com.motd.be.module.director.service_estimate_template.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.ValidationMessages.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.DirectorServiceException;
import com.motd.be.exception.exceptions.HandlerException;
import com.motd.be.exception.exceptions.ServiceEstimateFileException;
import com.motd.be.exception.exceptions.ServiceEstimateTemplateException;
import com.motd.be.module.director.service_estimate_template.dto.request.ServiceEstimateTemplateSaveAndUpdateRequestForDirector;
import com.motd.be.module.director.service_estimate_template.dto.response.ServiceEstimateTemplateFindAllResponseForDirector;
import com.motd.be.module.director.service_estimate_template.dto.response.ServiceEstimateTemplateFindDetailResponseForDirector;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.director_service_mapping.entity.DirectorServiceMapping;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.service_estimate_file.entity.ServiceEstimateFile;
import com.motd.be.module.member.service_estimate_template.entity.ServiceEstimateTemplate;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class ServiceEstimateTemplateControllerForDirectorTest extends BaseIntegrationTest {

	@Test
	@DisplayName("디렉터는 자신의 모든 제안 템플릿을 조회할 수 있다.")
	void findAll_returnsAllMyTemplates() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceEstimateTemplate template1 = serviceEstimateTemplateProvider.save(directorInfo, directorService1);
		ServiceEstimateTemplate template2 = serviceEstimateTemplateProvider.save(directorInfo, directorService2);
		ServiceEstimateTemplate template3 = serviceEstimateTemplateProvider.saveWithIsDeletedTrue(directorInfo,
			directorService3);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/my/services/estimate-templates")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		List<ServiceEstimateTemplateFindAllResponseForDirector> response = objectMapper.readValue(
			responseJson, new TypeReference<List<ServiceEstimateTemplateFindAllResponseForDirector>>() {
			});

		// then
		assertThat(response).hasSize(2);
	}

	@Test
	@DisplayName("디렉터는 자신의 모든 제안 템플릿을 조회할 수 있다. (특정 서비스로 필터링 할때)")
	void findAll_returnsAllMyTemplatesFilteredByService() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceEstimateTemplate template1 = serviceEstimateTemplateProvider.save(directorInfo, directorService1);
		ServiceEstimateTemplate template2 = serviceEstimateTemplateProvider.save(directorInfo, directorService2);
		ServiceEstimateTemplate template3 = serviceEstimateTemplateProvider.saveWithIsDeletedTrue(directorInfo,
			directorService3);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/my/services/estimate-templates")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.param(SERVICE_ID_STR, directorService2.getId().toString())
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		List<ServiceEstimateTemplateFindAllResponseForDirector> response = objectMapper.readValue(
			responseJson, new TypeReference<List<ServiceEstimateTemplateFindAllResponseForDirector>>() {
			});

		// then
		assertThat(response).hasSize(1);
	}

	@Test
	@DisplayName("디렉터는 템플릿 아이디를 통해 자신의 제안 템플릿 상세를 조회할 수 있다.")
	void findDetailByTemplateId_whenOwned_returnsDetail() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceEstimateTemplate template = serviceEstimateTemplateProvider.save(directorInfo, directorService);

		entityManager.flush();
		entityManager.clear();

		// when
		String responseJson = mockMvc.perform(
				MockMvcRequestBuilders.get(
						"/api/directors/my/services/estimate-templates/{templateId}", template.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		ServiceEstimateTemplateFindDetailResponseForDirector response = objectMapper.readValue(
			responseJson, ServiceEstimateTemplateFindDetailResponseForDirector.class);

		// then: id만 일치 검증 (세부 필드는 DTO 정의에 따름)
		assertThat(response.getId()).isEqualTo(template.getId());
	}

	@Test
	@DisplayName("디렉터는 템플릿 아이디를 통해 자신의 제안 템플릿 상세를 조회할 수 있다. (삭제한 템플릿 조회시)")
	void findDetailByTemplateIdWhenDeleted() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceEstimateTemplate template = serviceEstimateTemplateProvider.saveWithIsDeletedTrue(directorInfo,
			directorService);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.get(
						"/api/directors/my/services/estimate-templates/{templateId}", template.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(
				jsonPath(ERROR_STATUS).value(ServiceEstimateTemplateException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceEstimateTemplateException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateTemplateException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("디렉터는 템플릿 아이디를 통해 자신의 제안 템플릿 상세를 조회할 수 있다. (본인의 템플릿이 아닐시)")
	void findDetailByTemplateIdWhenNotOwnedBy() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, directorInfo1);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(otherDirector.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceEstimateTemplate template = serviceEstimateTemplateProvider.save(directorInfo,
			directorService);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.get(
						"/api/directors/my/services/estimate-templates/{templateId}", template.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isForbidden())
			.andExpect(
				jsonPath(ERROR_STATUS).value(ServiceEstimateTemplateException.NOT_OWNED_BY.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceEstimateTemplateException.NOT_OWNED_BY.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateTemplateException.NOT_OWNED_BY.getCode()));
	}

	@Test
	@DisplayName("디렉터는 템플릿 아이디를 통해 자신의 제안 템플릿 상세를 조회할 수 있다. (특정 서비스의 템플릿이 아닐시)")
	void findDetailByTemplateIdWithInvalidServiceId() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, directorInfo1);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(otherDirector.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);
		DirectorService invalidDirectorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceEstimateTemplate template = serviceEstimateTemplateProvider.saveWithIsDeletedTrue(directorInfo,
			directorService);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.get(
						"/api/directors/my/services/estimate-templates/{templateId}", template.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(
				jsonPath(ERROR_STATUS).value(ServiceEstimateTemplateException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceEstimateTemplateException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateTemplateException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("디렉터는 제안 템플릿을 저장이 가능하다.(이미지가 없는경우)")
	void save_whenValidWithoutImages_returnsCreated() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		// 서비스 매핑
		DirectorServiceMapping directorServiceMapping = directorServiceMappingProvider.save(directorInfo,
			directorService);

		ServiceEstimateTemplateSaveAndUpdateRequestForDirector request = ServiceEstimateTemplateSaveAndUpdateRequestForDirector.builder()
			.serviceId(directorService.getId())
			.price(10000L)
			.title("간단 템플릿")
			.content("설명")
			.fileIds(List.of())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/my/services/estimate-templates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		entityManager.flush();
		entityManager.clear();

		// then
		List<ServiceEstimateTemplate> savedTemplates = serviceEstimateTemplateProvider.findAll();
		assertThat(savedTemplates).hasSize(1);

		ServiceEstimateTemplate savedTemplate = savedTemplates.get(0);
		assertThat(savedTemplate.getDirectorInfo().getId()).isEqualTo(directorInfo.getId());
		assertThat(savedTemplate.getDirectorService().getId()).isEqualTo(directorService.getId());

		// 디렉터의 자주쓰는 제안 저장 여부 컬럼 업데이트 되었는지 검증
		DirectorInfo updatedDirector = memberProvider.findById(director.getId()).getDirectorInfo();
		assertThat(updatedDirector.getIsEstimateTemplateExist()).isTrue();
	}

	@Test
	@DisplayName("디렉터는 제안 템플릿을 저장이 가능하다.(이미지가 존재하는 경우)")
	void save_whenValidWithImages_returnsCreated() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		// 이미지 저장
		ServiceEstimateFile serviceEstimateTemplateImage1 = serviceEstimateFileProvider.saveWithEstimateType(
			director);
		ServiceEstimateFile serviceEstimateTemplateImage2 = serviceEstimateFileProvider.saveWithEstimateType(
			director);
		ServiceEstimateFile serviceEstimateTemplateImage3 = serviceEstimateFileProvider.saveWithEstimateType(
			director);

		// 서비스 매핑
		DirectorServiceMapping directorServiceMapping = directorServiceMappingProvider.save(directorInfo,
			directorService);

		ServiceEstimateTemplateSaveAndUpdateRequestForDirector request = ServiceEstimateTemplateSaveAndUpdateRequestForDirector.builder()
			.serviceId(directorService.getId())
			.price(10000L)
			.title("간단 템플릿")
			.content("설명")
			.fileIds(List.of(serviceEstimateTemplateImage1.getId(), serviceEstimateTemplateImage2.getId(),
				serviceEstimateTemplateImage3.getId()))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/my/services/estimate-templates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		entityManager.flush();
		entityManager.clear();

		// then
		List<ServiceEstimateTemplate> savedTemplates = serviceEstimateTemplateProvider.findAll();
		assertThat(savedTemplates).hasSize(1);

		ServiceEstimateTemplate savedTemplate = savedTemplates.get(0);
		assertThat(savedTemplate.getDirectorInfo().getId()).isEqualTo(directorInfo.getId());
		assertThat(savedTemplate.getDirectorService().getId()).isEqualTo(directorService.getId());

		// 이미지 연관관계 검증
		List<ServiceEstimateFile> savedImages = savedTemplate.getImages();
		assertThat(savedImages).hasSize(3);
		assertThat(savedImages).extracting("id").containsExactly(
			serviceEstimateTemplateImage1.getId(),
			serviceEstimateTemplateImage2.getId(),
			serviceEstimateTemplateImage3.getId()
		);

		// 이미지 sortOrder 검증
		assertThat(savedImages.get(0).getSortOrder()).isEqualTo(0);
		assertThat(savedImages.get(0).getId()).isEqualTo(serviceEstimateTemplateImage1.getId());
		assertThat(savedImages.get(1).getSortOrder()).isEqualTo(1);
		assertThat(savedImages.get(1).getId()).isEqualTo(serviceEstimateTemplateImage2.getId());
		assertThat(savedImages.get(2).getSortOrder()).isEqualTo(2);
		assertThat(savedImages.get(2).getId()).isEqualTo(serviceEstimateTemplateImage3.getId());
	}

	@Test
	@DisplayName("디렉터는 제안 템플릿을 저장이 가능하다.(title 이 없는 경우)")
	void save_whenInvalidRequest_returnsBadRequest() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		// 서비스 매핑
		DirectorServiceMapping directorServiceMapping = directorServiceMappingProvider.save(directorInfo,
			directorService);

		// title 누락
		ServiceEstimateTemplateSaveAndUpdateRequestForDirector request = ServiceEstimateTemplateSaveAndUpdateRequestForDirector.builder()
			.serviceId(directorService.getId())
			.price(10000L)
			.content("설명")
			.fileIds(List.of())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when / then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/my/services/estimate-templates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(TITLE_REQUIRED))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("디렉터는 제안 템플릿을 저장이 가능하다.(해당 서비스를 제공하지 않는 경우)")
	void save_whenServiceNotOwned_returnsForbidden() throws Exception {
		// given: A 디렉터와 그의 서비스
		DirectorInfo directorInfoA = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member directorA = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfoA);
		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService serviceOfA = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		// 서비스 매핑
		DirectorServiceMapping directorServiceMapping = directorServiceMappingProvider.save(directorInfoA, serviceOfA);

		// given: B 디렉터로 요청 시도
		DirectorInfo directorInfoB = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member directorB = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, directorInfoB);
		Jwt jwtB = generateTokenWithMemberIdRoleDirector(directorB.getId());

		ServiceEstimateTemplateSaveAndUpdateRequestForDirector request = ServiceEstimateTemplateSaveAndUpdateRequestForDirector.builder()
			.serviceId(serviceOfA.getId())
			.price(10000L)
			.title("간단 템플릿")
			.content("설명")
			.fileIds(List.of())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when / then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/my/services/estimate-templates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwtB.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwtB.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				DirectorServiceException.DIRECTOR_SERVICE_NOT_FOUND.getHttpStatus().toString()))
			.andExpect(
				jsonPath(ERROR_MESSAGE).value(DirectorServiceException.DIRECTOR_SERVICE_NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(DirectorServiceException.DIRECTOR_SERVICE_NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("디렉터는 제안 템플릿을 저장이 가능하다.(이미 해당 서비스에 대한 템플릿이 존재하는 경우)")
	void save_whenDuplicateTemplateExist() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		// 서비스 매핑
		DirectorServiceMapping directorServiceMapping = directorServiceMappingProvider.save(directorInfo,
			directorService);

		// 이미 존재하는 템플릿 저장
		serviceEstimateTemplateProvider.save(directorInfo, directorService);

		ServiceEstimateTemplateSaveAndUpdateRequestForDirector request = ServiceEstimateTemplateSaveAndUpdateRequestForDirector.builder()
			.serviceId(directorService.getId())
			.price(10000L)
			.title("간단 템플릿")
			.content("설명")
			.fileIds(List.of())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when / then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/my/services/estimate-templates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		// then
		List<ServiceEstimateTemplate> savedTemplates = serviceEstimateTemplateProvider.findAll();
		assertThat(savedTemplates).hasSize(2);
	}

	@Test
	@DisplayName("디렉터는 제안 템플릿을 저장이 가능하다.(이미 해당 서비스에 대한 템플릿이 3개 존재하는 경우)")
	void save_whenDuplicateTemplate3Exist() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		// 서비스 매핑
		DirectorServiceMapping directorServiceMapping = directorServiceMappingProvider.save(directorInfo,
			directorService);

		// 이미 존재하는 템플릿 저장
		serviceEstimateTemplateProvider.save(directorInfo, directorService);
		serviceEstimateTemplateProvider.save(directorInfo, directorService);
		serviceEstimateTemplateProvider.save(directorInfo, directorService);

		ServiceEstimateTemplateSaveAndUpdateRequestForDirector request = ServiceEstimateTemplateSaveAndUpdateRequestForDirector.builder()
			.serviceId(directorService.getId())
			.price(10000L)
			.title("간단 템플릿")
			.content("설명")
			.fileIds(List.of())
			.build();

		entityManager.flush();
		entityManager.clear();

		// when / then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/my/services/estimate-templates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceEstimateTemplateException.EXCEEDED_LIMIT_COUNT.getHttpStatus().toString()))
			.andExpect(
				jsonPath(ERROR_MESSAGE).value(ServiceEstimateTemplateException.EXCEEDED_LIMIT_COUNT.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateTemplateException.EXCEEDED_LIMIT_COUNT.getCode()));
	}

	@Test
	@DisplayName("디렉터는 제안 템플릿을 저장이 가능하다.(다른 디렉터의 이미지를 사용하는 경우)")
	void saveWithNotOwnedImage() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now().plusMonths(1));
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, otherDirectorInfo);

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceEstimateFile serviceEstimateTemplateImage2 = serviceEstimateFileProvider.saveWithEstimateType(
			director);
		ServiceEstimateFile serviceEstimateTemplateImage3 = serviceEstimateFileProvider.saveWithEstimateType(
			otherDirector);

		// 서비스 매핑
		DirectorServiceMapping directorServiceMapping = directorServiceMappingProvider.save(directorInfo,
			directorService);

		// 이미 존재하는 템플릿 저장
		serviceEstimateTemplateProvider.save(directorInfo, directorService);

		ServiceEstimateTemplateSaveAndUpdateRequestForDirector request = ServiceEstimateTemplateSaveAndUpdateRequestForDirector.builder()
			.serviceId(directorService.getId())
			.price(10000L)
			.title("간단 템플릿")
			.content("설명")
			.fileIds(List.of(serviceEstimateTemplateImage2.getId(), serviceEstimateTemplateImage3.getId()))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when / then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/directors/my/services/estimate-templates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceEstimateFileException.NOT_OWNED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceEstimateFileException.NOT_OWNED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateFileException.NOT_OWNED.getCode()));
	}

	@Test
	@DisplayName("디렉터는 제안 템플릿을 업데이트 할 수 있다. (이미지가 존재하며, 새로운 이미지로 바꿀떄)")
	void updateWhenImagesExistAndNewImageExist() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		// 서비스 매핑
		DirectorServiceMapping directorServiceMapping = directorServiceMappingProvider.save(directorInfo,
			directorService);

		// 템플릿 생성
		ServiceEstimateTemplate template = serviceEstimateTemplateProvider.save(directorInfo, directorService);

		// 초기 이미지 3장
		ServiceEstimateFile img1 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, template, 0);
		ServiceEstimateFile img2 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, template, 1);
		ServiceEstimateFile img3 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, template, 2);

		// 새 이미지 1장 추가
		ServiceEstimateFile img4 = serviceEstimateFileProvider.saveWithEstimateType(director);

		ServiceEstimateTemplateSaveAndUpdateRequestForDirector request = ServiceEstimateTemplateSaveAndUpdateRequestForDirector.builder()
			.serviceId(directorService.getId())
			.price(20000L)
			.title(UPDATED_TITLE_STR)
			.content(UPDATED_CONTENT_STR)
			.fileIds(List.of(img3.getId(), img4.getId()))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.put("/api/directors/my/services/estimate-templates/{templateId}",
						template.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		ServiceEstimateTemplate updatedTemplate = serviceEstimateTemplateProvider.findById(template.getId());
		List<ServiceEstimateFile> updatedImages = updatedTemplate.getImages();

		// 삭제/추가 반영 확인
		assertThat(updatedImages).hasSize(2);
		assertThat(updatedImages).extracting("id").containsExactlyInAnyOrder(img3.getId(), img4.getId());

		// img 3,4 의 순서가 1,2 인지 확인한다.
		assertThat(updatedImages.get(0).getSortOrder()).isEqualTo(0);
		assertThat(updatedImages.get(0).getId()).isEqualTo(img3.getId());
		assertThat(updatedImages.get(1).getSortOrder()).isEqualTo(1);
		assertThat(updatedImages.get(1).getId()).isEqualTo(img4.getId());

		// 내용이 바꼇는지 확인
		assertThat(updatedTemplate.getTitle()).isEqualTo(UPDATED_TITLE_STR);
		assertThat(updatedTemplate.getContent()).isEqualTo(UPDATED_CONTENT_STR);
	}

	@Test
	@DisplayName("디렉터는 제안 템플릿을 업데이트 할 수 있다. (이미지가 존재하며, 모든 이미지를 삭제할때)")
	void updateWhenImagesExistAndNewImageNotExist() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		// 서비스 매핑
		DirectorServiceMapping directorServiceMapping = directorServiceMappingProvider.save(directorInfo,
			directorService);

		// 템플릿 생성
		ServiceEstimateTemplate template = serviceEstimateTemplateProvider.save(directorInfo, directorService);

		// 초기 이미지 3장
		ServiceEstimateFile img1 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, template, 0);
		ServiceEstimateFile img2 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, template, 1);
		ServiceEstimateFile img3 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, template, 2);

		ServiceEstimateTemplateSaveAndUpdateRequestForDirector request = ServiceEstimateTemplateSaveAndUpdateRequestForDirector.builder()
			.serviceId(directorService.getId())
			.price(20000L)
			.title(UPDATED_TITLE_STR)
			.content(UPDATED_CONTENT_STR)
			.fileIds(null)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.put("/api/directors/my/services/estimate-templates/{templateId}",
						template.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		ServiceEstimateTemplate updatedTemplate = serviceEstimateTemplateProvider.findById(template.getId());
		List<ServiceEstimateFile> updatedImages = updatedTemplate.getImages();

		// 삭제/추가 반영 확인
		assertThat(updatedImages).hasSize(0);

		// 내용이 바꼇는지 확인
		assertThat(updatedTemplate.getTitle()).isEqualTo(UPDATED_TITLE_STR);
		assertThat(updatedTemplate.getContent()).isEqualTo(UPDATED_CONTENT_STR);
	}

	@Test
	@DisplayName("디렉터는 제안 템플릿을 업데이트 할 수 있다. (이미지가 존재하지않으며 , 새로운 이미지로 바꿀떄)")
	void updateWhenImagesNotExistAndNewImageExist() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		// 서비스 매핑
		DirectorServiceMapping directorServiceMapping = directorServiceMappingProvider.save(directorInfo,
			directorService);

		// 템플릿 생성
		ServiceEstimateTemplate template = serviceEstimateTemplateProvider.save(directorInfo, directorService);

		// 새 이미지 1장 추가
		ServiceEstimateFile img1 = serviceEstimateFileProvider.saveWithEstimateType(director);
		ServiceEstimateFile img2 = serviceEstimateFileProvider.saveWithEstimateType(director);

		ServiceEstimateTemplateSaveAndUpdateRequestForDirector request = ServiceEstimateTemplateSaveAndUpdateRequestForDirector.builder()
			.serviceId(directorService.getId())
			.price(20000L)
			.title(UPDATED_TITLE_STR)
			.content(UPDATED_CONTENT_STR)
			.fileIds(List.of(img1.getId(), img2.getId()))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.put("/api/directors/my/services/estimate-templates/{templateId}",
						template.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		ServiceEstimateTemplate updatedTemplate = serviceEstimateTemplateProvider.findById(template.getId());
		List<ServiceEstimateFile> updatedImages = updatedTemplate.getImages();

		// 삭제/추가 반영 확인
		assertThat(updatedImages).hasSize(2);
		assertThat(updatedImages).extracting("id").containsExactlyInAnyOrder(img1.getId(), img2.getId());

		// img 3,4 의 순서가 1,2 인지 확인한다.
		assertThat(updatedImages.get(0).getSortOrder()).isEqualTo(0);
		assertThat(updatedImages.get(0).getId()).isEqualTo(img1.getId());
		assertThat(updatedImages.get(1).getSortOrder()).isEqualTo(1);
		assertThat(updatedImages.get(1).getId()).isEqualTo(img2.getId());

		// 내용이 바꼇는지 확인
		assertThat(updatedTemplate.getTitle()).isEqualTo(UPDATED_TITLE_STR);
		assertThat(updatedTemplate.getContent()).isEqualTo(UPDATED_CONTENT_STR);
	}

	@Test
	@DisplayName("디렉터는 제안 템플릿을 업데이트 할 수 있다. (다른 디렉터가 올린 사진으로 업데이트 하려는 경우)")
	void updateWhenImagesNotOwned() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now().plusMonths(1));
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, otherDirectorInfo);

		// 서비스 매핑
		DirectorServiceMapping directorServiceMapping = directorServiceMappingProvider.save(directorInfo,
			directorService);

		// 템플릿 생성
		ServiceEstimateTemplate template = serviceEstimateTemplateProvider.save(directorInfo, directorService);

		// 초기 이미지 3장
		ServiceEstimateFile img1 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, template, 0);
		ServiceEstimateFile img2 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, template, 1);
		ServiceEstimateFile img3 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, template, 2);

		// 새 이미지 1장 추가
		ServiceEstimateFile img4 = serviceEstimateFileProvider.saveWithEstimateType(director);
		ServiceEstimateFile img5 = serviceEstimateFileProvider.saveWithEstimateType(otherDirector);

		ServiceEstimateTemplateSaveAndUpdateRequestForDirector request = ServiceEstimateTemplateSaveAndUpdateRequestForDirector.builder()
			.serviceId(directorService.getId())
			.price(20000L)
			.title(UPDATED_TITLE_STR)
			.content(UPDATED_CONTENT_STR)
			.fileIds(List.of(img3.getId(), img4.getId(), img5.getId()))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when && then
		mockMvc.perform(
				MockMvcRequestBuilders.put("/api/directors/my/services/estimate-templates/{templateId}",
						directorService.getId(),
						template.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceEstimateFileException.NOT_OWNED.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceEstimateFileException.NOT_OWNED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateFileException.NOT_OWNED.getCode()));
	}

	@Test
	@DisplayName("디렉터는 자신의 제안 템플릿을 삭제할 수 있다.")
	void delete_whenOwned_returnsNoContent() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceEstimateTemplate template = serviceEstimateTemplateProvider.save(directorInfo, directorService);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.delete("/api/directors/my/services/estimate-templates/{templateId}",
						template.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then
		ServiceEstimateTemplate deletedTemplate = serviceEstimateTemplateProvider.findById(template.getId());
		assertThat(deletedTemplate.getIsDeleted()).isTrue();
	}

	@Test
	@DisplayName("디렉터는 자신의 제안 템플릿을 삭제할 수 있다. (이미 삭제된 템플릿을 다시 삭제할때)")
	void delete_whenAlreadyDeleted_returnsNotFound() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceEstimateTemplate template = serviceEstimateTemplateProvider.saveWithIsDeletedTrue(directorInfo,
			directorService);

		entityManager.flush();
		entityManager.clear();

		// when / then
		mockMvc.perform(
				MockMvcRequestBuilders.delete("/api/directors/my/services/estimate-templates/{templateId}",
						template.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceEstimateTemplateException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceEstimateTemplateException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateTemplateException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("디렉터는 자신의 제안 템플릿을 삭제할 수 있다. (본인의 템플릿이 아닐시)")
	void delete_whenNotOwnedBy_returnsForbidden() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now().plusMonths(1));
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, otherDirectorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(otherDirector.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceEstimateTemplate template = serviceEstimateTemplateProvider.save(directorInfo, directorService);

		entityManager.flush();
		entityManager.clear();

		// when / then
		mockMvc.perform(
				MockMvcRequestBuilders.delete("/api/directors/my/services/estimate-templates/{templateId}",
						template.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceEstimateTemplateException.NOT_OWNED_BY.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceEstimateTemplateException.NOT_OWNED_BY.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateTemplateException.NOT_OWNED_BY.getCode()));
	}

	@Test
	@DisplayName("디렉터는 자신의 제안 템플릿을 삭제할 수 있다. (존재하지 않는 템플릿 삭제시)")
	void delete_whenTemplateNotExist_returnsNotFound() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		long nonExistentTemplateId = 99999L;

		entityManager.flush();
		entityManager.clear();

		// when / then
		mockMvc.perform(
				MockMvcRequestBuilders.delete("/api/directors/my/services/estimate-templates/{templateId}",
						nonExistentTemplateId)
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath(ERROR_STATUS).value(
				ServiceEstimateTemplateException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(ServiceEstimateTemplateException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(ServiceEstimateTemplateException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("디렉터는 자신의 제안 템플릿을 삭제할 수 있다. (템플릿 이미지도 함께 삭제되어야함)")
	void delete_whenTemplateHasImages_deletesImagesAlso() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService parent = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent);

		ServiceEstimateTemplate template = serviceEstimateTemplateProvider.save(directorInfo, directorService);

		// 이미지 3장 저장
		ServiceEstimateFile img1 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, template, 0);
		ServiceEstimateFile img2 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, template, 1);
		ServiceEstimateFile img3 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			director, template, 2);

		entityManager.flush();
		entityManager.clear();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.delete("/api/directors/my/services/estimate-templates/{templateId}",
						template.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		// then: 템플릿 삭제 확인
		ServiceEstimateTemplate deletedTemplate = serviceEstimateTemplateProvider.findById(template.getId());
		assertThat(deletedTemplate.getIsDeleted()).isTrue();

		// then: 이미지도 삭제되었는지 확인
		List<ServiceEstimateFile> deletedImage = serviceEstimateFileProvider.findAll();
		deletedImage.forEach(serviceEstimateFile -> {
			assertThat(serviceEstimateFile.getIsDeleted()).isTrue();
		});
	}
}
