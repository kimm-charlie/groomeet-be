package com.motd.be.module.director.director_service_mapping.controller;

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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.DirectorServiceException;
import com.motd.be.exception.exceptions.HandlerException;
import com.motd.be.module.director.director_service_mapping.dto.request.DirectorServiceMappingUpdateServiceRequestForDirector;
import com.motd.be.module.director.director_service_mapping.dto.response.DirectorServiceFindActivationProgressResponseForDirector;
import com.motd.be.module.director.director_service_mapping.dto.response.DirectorServiceFindAllResponseForDirector;
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
public class DirectorServiceMappingControllerForDirectorTest extends BaseIntegrationTest {

	@Test
	@DisplayName("디렉터는 서비스 매핑을 정상적으로 수정할 수 있다.")
	void updateServiceMappingSuccess() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		DirectorService service1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService service2 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);
		DirectorService service3 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);
		DirectorService service4 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);

		// 기존 매핑 1개 생성
		directorServiceMappingProvider.save(directorInfo, service2);
		directorServiceMappingProvider.save(directorInfo, service4);

		entityManager.flush();
		entityManager.clear();

		// 요청: service3 , 4만 남기기
		List<Long> serviceIds = List.of(service3.getId(), service4.getId());
		var request = DirectorServiceMappingUpdateServiceRequestForDirector.builder()
			.serviceIds(serviceIds)
			.build();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.put("/api/directors/my/services")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		//then
		List<DirectorServiceMapping> mappings = directorServiceMappingProvider.findAll();

		assertThat(mappings).hasSize(3);

		mappings.forEach(
			mapping -> {
				if (serviceIds.contains(mapping.getDirectorService().getId())) {
					assertThat(mapping.getDirectorInfo().getId()).isEqualTo(directorInfo.getId());
					assertThat(serviceIds).contains(mapping.getDirectorService().getId());
					assertThat(mapping.getIsDeleted()).isFalse();
				} else {
					assertThat(mapping.getDirectorService().getId()).isEqualTo(service2.getId());
					assertThat(mapping.getIsDeleted()).isTrue();
				}
			}
		);

	}

	@Test
	@DisplayName("디렉터는 서비스 매핑을 정상적으로 수정할 수 있다. (서비스가 7개 초과일때)")
	void updateServiceMappingWithMoreThan7Services() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		DirectorService service1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService service2 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);
		DirectorService service3 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);
		DirectorService service4 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);
		DirectorService service5 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);
		DirectorService service6 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);
		DirectorService service7 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);
		DirectorService service8 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);
		DirectorService service9 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);

		// 기존 매핑 1개 생성
		directorServiceMappingProvider.save(directorInfo, service2);
		directorServiceMappingProvider.save(directorInfo, service4);

		entityManager.flush();
		entityManager.clear();

		// 요청: service3 , 4만 남기기
		List<Long> serviceIds = List.of(service2.getId(), service3.getId(), service4.getId(), service5.getId(),
			service6.getId(), service7.getId(), service8.getId(), service9.getId());
		var request = DirectorServiceMappingUpdateServiceRequestForDirector.builder()
			.serviceIds(serviceIds)
			.build();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.put("/api/directors/my/services")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(DIRECTOR_SERVICE_SELECTION_OUT_OF_BOUNDS))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("디렉터는 서비스 매핑을 정상적으로 수정할 수 있다. (이때 추가된 서비스에 대해선 자주쓰는 제안 복구, 삭제된 서비스에 대해선 자주쓰는 제안 삭제가 이루어 진다.)")
	void updateServiceMappingSuccessWithEstimateTemplate() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		DirectorService service1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService service2 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);
		DirectorService service3 = directorServiceProvider.save(SERVICE_NAME_3_STR, service1);
		DirectorService service4 = directorServiceProvider.save(SERVICE_NAME_4_STR, service1);
		DirectorService service5 = directorServiceProvider.save(SERVICE_NAME_5_STR, service1);
		DirectorService service6 = directorServiceProvider.save(SERVICE_NAME_6_STR, service1);

		// 기존 매핑 2개 생성
		directorServiceMappingProvider.save(directorInfo, service2);
		directorServiceMappingProvider.save(directorInfo, service3);
		directorServiceMappingProvider.save(directorInfo, service5);

		directorServiceMappingProvider.saveWithIsDeletedTrue(directorInfo, service4);

		// 기존 2개의 매핑에 대해서 ServiceEstimateTemplate 생성
		ServiceEstimateTemplate serviceEstimateTemplate1 = serviceEstimateTemplateProvider.save(directorInfo, service2);
		ServiceEstimateTemplate serviceEstimateTemplate2 = serviceEstimateTemplateProvider.save(directorInfo, service3);

		// service4 에 대해서는 isDeleted = true 인 템플릿 생성
		ServiceEstimateTemplate serviceEstimateTemplate3 = serviceEstimateTemplateProvider.saveWithIsDeletedTrue(
			directorInfo, service4);

		// 서비스 템플릿 이미지 생성
		ServiceEstimateFile serviceEstimateFile1 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			member, serviceEstimateTemplate1, 0);
		ServiceEstimateFile serviceEstimateFile2 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			member, serviceEstimateTemplate1, 1);
		ServiceEstimateFile serviceEstimateFile3 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			member, serviceEstimateTemplate1, 2);

		ServiceEstimateFile serviceEstimateFile4 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			member, serviceEstimateTemplate2, 0);
		ServiceEstimateFile serviceEstimateFile5 = serviceEstimateFileProvider.saveWithServiceEstimateTemplate(
			member, serviceEstimateTemplate2, 1);

		ServiceEstimateFile serviceEstimateFile6 = serviceEstimateFileProvider.saveWithServiceEstimateTemplateWithIsDeletedTrue(
			member, serviceEstimateTemplate3, 0);
		ServiceEstimateFile serviceEstimateFile7 = serviceEstimateFileProvider.saveWithServiceEstimateTemplateWithIsDeletedTrue(
			member, serviceEstimateTemplate3, 1);

		entityManager.flush();
		entityManager.clear();

		// 요청: service2,4,6만 남기기
		List<Long> serviceIds = List.of(service2.getId(), service4.getId(), service6.getId());
		List<Long> deletedServiceIds = List.of(service3.getId(), service5.getId());
		var request = DirectorServiceMappingUpdateServiceRequestForDirector.builder()
			.serviceIds(serviceIds)
			.build();

		// when
		mockMvc.perform(
				MockMvcRequestBuilders.put("/api/directors/my/services")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		//then
		List<DirectorServiceMapping> mappings = directorServiceMappingProvider.findAll();

		assertThat(mappings).hasSize(5);

		mappings.forEach(
			mapping -> {
				if (serviceIds.contains(mapping.getDirectorService().getId())) {
					assertThat(mapping.getDirectorInfo().getId()).isEqualTo(directorInfo.getId());
					assertThat(serviceIds).contains(mapping.getDirectorService().getId());
					assertThat(mapping.getIsDeleted()).isFalse();
				} else {
					assertThat(deletedServiceIds).contains(mapping.getDirectorService().getId());
					assertThat(mapping.getIsDeleted()).isTrue();
				}
			}
		);

		// 템플릿 업데이트 여부 확인
		List<ServiceEstimateTemplate> templates = serviceEstimateTemplateProvider.findAll();

		assertThat(templates).hasSize(3);

		// service 2 에 대한 템플릿은 그대로 유지되었는지 확인 한다.
		ServiceEstimateTemplate updatedTemplate1 = templates.stream()
			.filter(template -> template.getDirectorService().getId().equals(service2.getId()))
			.findFirst()
			.orElseThrow();

		assertThat(updatedTemplate1.getIsDeleted()).isFalse();
		assertThat(updatedTemplate1.getImages().size()).isEqualTo(3);

		// service 3 에 대한 템플릿은 isDeleted 가 true 로 변경 되었는지 확인 한다.
		ServiceEstimateTemplate updatedTemplate2 = templates.stream()
			.filter(template -> template.getDirectorService().getId().equals(service3.getId()))
			.findFirst()
			.orElseThrow();

		assertThat(updatedTemplate2.getIsDeleted()).isTrue();
		assertThat(updatedTemplate2.getImages().size()).isEqualTo(0);

		// service 4 에 대한 템플릿은 isDeleted 가 false 로 변경 되었는지 확인 한다.
		ServiceEstimateTemplate updatedTemplate3 = templates.stream()
			.filter(template -> template.getDirectorService().getId().equals(service4.getId()))
			.findFirst()
			.orElseThrow();

		assertThat(updatedTemplate3.getIsDeleted()).isFalse();
		assertThat(updatedTemplate3.getImages().size()).isEqualTo(2);
	}

	@Test
	@DisplayName("디렉터는 서비스 매핑을 정상적으로 수정할 수 있다. (요청 reuqest 의 serviceIds 가 빈 리스트일 경우 예외가 발생한다.)")
	void updateServiceMappingValidationFail() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		var request = DirectorServiceMappingUpdateServiceRequestForDirector.builder()
			.serviceIds(List.of())
			.build();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.put("/api/directors/my/services")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(DIRECTOR_SERVICE_MUST_BE_SELECTED))
			.andExpect(jsonPath("$.code").value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("디렉터는 서비스 매핑을 정상적으로 수정할 수 있다. (권한이 없는 사용자 일 경우.)")
	void updateServiceMappingForbidden() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member directorMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Member normalMember = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(normalMember.getId());

		DirectorService service1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService service2 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);

		List<Long> serviceIds = List.of(service2.getId());
		var request = DirectorServiceMappingUpdateServiceRequestForDirector.builder()
			.serviceIds(serviceIds)
			.build();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.put("/api/directors/my/services")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.status").value(HandlerException.FORBIDDEN.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(HandlerException.FORBIDDEN.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(HandlerException.FORBIDDEN.getCode()));
	}

	@Test
	@DisplayName("디렉터는 서비스 매핑을 정상적으로 수정할 수 있다. (유효하지 않은 카테고리가 들어있을때.)")
	void updateServiceMappingWithInvalidCategory() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		DirectorService service1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService service2 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);

		List<Long> serviceIds = List.of(service1.getId(), service2.getId());
		var request = DirectorServiceMappingUpdateServiceRequestForDirector.builder()
			.serviceIds(serviceIds)
			.build();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.put("/api/directors/my/services")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(DirectorServiceException.INVALID_SERVICE.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(DirectorServiceException.INVALID_SERVICE.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(DirectorServiceException.INVALID_SERVICE.getCode()));
	}

	@Test
	@DisplayName("특정 디렉터의 서비스 매핑 목록을 정상적으로 조회한다.")
	void findAll() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));

		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		DirectorService service1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService service2 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);
		DirectorService service3 = directorServiceProvider.save(SERVICE_NAME_3_STR, service1);

		DirectorServiceMapping mapping1 = directorServiceMappingProvider.save(directorInfo, service2);
		DirectorServiceMapping mapping2 = directorServiceMappingProvider.save(directorInfo, service3);

		entityManager.flush();
		entityManager.clear();

		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/my/services", member.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		// then
		List<DirectorServiceFindAllResponseForDirector> response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			objectMapper.getTypeFactory()
				.constructCollectionType(List.class, DirectorServiceFindAllResponseForDirector.class));

		assertThat(response).hasSize(2);
		assertThat(response).extracting("id").containsExactlyInAnyOrder(service2.getId(), service3.getId());
		assertThat(response).extracting("name").containsExactlyInAnyOrder(SERVICE_NAME_2_STR, SERVICE_NAME_3_STR);
		assertThat(response).extracting("parentName").containsExactlyInAnyOrder(SERVICE_NAME_1_STR, SERVICE_NAME_1_STR);
	}

	@Test
	@DisplayName("특정 디렉터의 서비스 매핑 목록을 정상적으로 조회한다. (targetMember가 디렉터가 아닌경우 권한 예외가 발생한다.)")
	void findAllWhenTargetMemberNotDirector() throws Exception {
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/my/services", member.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isForbidden())
			.andExpect(
				jsonPath("$.status").value(HandlerException.FORBIDDEN.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(HandlerException.FORBIDDEN.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(HandlerException.FORBIDDEN.getCode()));
	}

	@Test
	@DisplayName("제안 템플릿이 있는 서비스 매핑 목록을 정상적으로 조회한다.")
	void findAllForEstimateTemplateSuccess() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		DirectorService service1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService service2 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);
		DirectorService service3 = directorServiceProvider.save(SERVICE_NAME_3_STR, service1);
		DirectorService service4 = directorServiceProvider.save(SERVICE_NAME_4_STR, service1);

		// 서비스 매핑 생성
		directorServiceMappingProvider.save(directorInfo, service2);
		directorServiceMappingProvider.save(directorInfo, service3);
		directorServiceMappingProvider.save(directorInfo, service4);

		// service2, service3에만 템플릿 생성 (service4는 템플릿 없음)
		serviceEstimateTemplateProvider.save(directorInfo, service2);
		serviceEstimateTemplateProvider.save(directorInfo, service3);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/my/services/with-estimate-templates")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		// then
		List<DirectorServiceFindAllResponseForDirector> response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			objectMapper.getTypeFactory()
				.constructCollectionType(List.class, DirectorServiceFindAllResponseForDirector.class));

		assertThat(response).hasSize(2);
		assertThat(response).extracting("id").containsExactlyInAnyOrder(service2.getId(), service3.getId());
		assertThat(response).extracting("name").containsExactlyInAnyOrder(SERVICE_NAME_2_STR, SERVICE_NAME_3_STR);
		assertThat(response).extracting("parentName").containsOnly(SERVICE_NAME_1_STR);
	}

	@Test
	@DisplayName("제안 템플릿이 있는 서비스 매핑 목록 조회 시 삭제된 템플릿은 제외된다.")
	void findAllForEstimateTemplateExcludeDeletedTemplate() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		DirectorService service1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService service2 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);
		DirectorService service3 = directorServiceProvider.save(SERVICE_NAME_3_STR, service1);

		// 서비스 매핑 생성
		directorServiceMappingProvider.save(directorInfo, service2);
		directorServiceMappingProvider.save(directorInfo, service3);

		// service2는 정상 템플릿, service3는 삭제된 템플릿
		serviceEstimateTemplateProvider.save(directorInfo, service2);
		serviceEstimateTemplateProvider.saveWithIsDeletedTrue(directorInfo, service3);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/my/services/with-estimate-templates")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		// then
		List<DirectorServiceFindAllResponseForDirector> response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			objectMapper.getTypeFactory()
				.constructCollectionType(List.class, DirectorServiceFindAllResponseForDirector.class));

		assertThat(response).hasSize(1);
		assertThat(response).extracting("id").containsOnly(service2.getId());
		assertThat(response).extracting("name").containsOnly(SERVICE_NAME_2_STR);
	}

	@Test
	@DisplayName("제안 템플릿이 있는 서비스 매핑 목록 조회 시 템플릿이 없으면 빈 리스트를 반환한다.")
	void findAllForEstimateTemplateWhenNoTemplate() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		DirectorService service1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService service2 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);

		// 서비스 매핑만 생성하고 템플릿은 생성하지 않음
		directorServiceMappingProvider.save(directorInfo, service2);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/my/services/with-estimate-templates")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		// then
		List<DirectorServiceFindAllResponseForDirector> response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			objectMapper.getTypeFactory()
				.constructCollectionType(List.class, DirectorServiceFindAllResponseForDirector.class));

		assertThat(response).isEmpty();
	}

	@Test
	@DisplayName("제안 템플릿이 있는 서비스 매핑 목록 조회 시 권한이 없는 사용자인 경우 예외가 발생한다.")
	void findAllForEstimateTemplateForbidden() throws Exception {
		// given
		Member normalMember = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(normalMember.getId());

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/my/services/with-estimate-templates")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.status").value(HandlerException.FORBIDDEN.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(HandlerException.FORBIDDEN.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(HandlerException.FORBIDDEN.getCode()));
	}

	@Test
	@DisplayName("제안 템플릿이 있는 서비스 매핑 목록 조회 시 삭제된 서비스 매핑은 제외된다.")
	void findAllForEstimateTemplateExcludeDeletedMapping() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		DirectorService service1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService service2 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);
		DirectorService service3 = directorServiceProvider.save(SERVICE_NAME_3_STR, service1);

		// service2는 정상 매핑, service3는 삭제된 매핑
		directorServiceMappingProvider.save(directorInfo, service2);
		directorServiceMappingProvider.saveWithIsDeletedTrue(directorInfo, service3);

		// 둘 다 템플릿은 존재
		serviceEstimateTemplateProvider.save(directorInfo, service2);
		serviceEstimateTemplateProvider.save(directorInfo, service3);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/my/services/with-estimate-templates")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		// then
		List<DirectorServiceFindAllResponseForDirector> response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			objectMapper.getTypeFactory()
				.constructCollectionType(List.class, DirectorServiceFindAllResponseForDirector.class));

		assertThat(response).hasSize(1);
		assertThat(response).extracting("id").containsOnly(service2.getId());
	}

	@Test
	@DisplayName("디렉터는 서비스 활성화 진행률을 정상적으로 조회할 수 있다. (모든 서비스가 비활성화 상태)")
	void findActivationProgressWhenAllServicesInactive() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member member1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member member2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo2);

		DirectorService service1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService service2 = directorServiceProvider.saveWithIsActiveFalse(SERVICE_NAME_2_STR, service1);
		DirectorService service3 = directorServiceProvider.saveWithIsActiveFalse(SERVICE_NAME_3_STR, service1);

		directorServiceMappingProvider.save(directorInfo, service2);
		directorServiceMappingProvider.save(directorInfo, service3);

		directorServiceMappingProvider.save(directorInfo1, service2);
		directorServiceMappingProvider.save(directorInfo1, service3);

		directorServiceMappingProvider.save(directorInfo2, service2);
		directorServiceMappingProvider.save(directorInfo2, service3);

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/services/activation/progress")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		// then
		DirectorServiceFindActivationProgressResponseForDirector response =
			objectMapper.readValue(
				result.getResponse().getContentAsString(),
				DirectorServiceFindActivationProgressResponseForDirector.class
			);

		assertThat(response.getProgressPercentage()).isEqualTo(3 * 100 / 20);
	}

	@Test
	@DisplayName("디렉터는 서비스 활성화 진행률을 정상적으로 조회할 수 있다. (일부 서비스만 활성화 상태)")
	void findActivationProgressWhenSomeServicesActive() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member member1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member member2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo2);

		DirectorService service1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService service2 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);
		DirectorService service3 = directorServiceProvider.saveWithIsActiveFalse(SERVICE_NAME_3_STR, service1);

		directorServiceMappingProvider.save(directorInfo, service2);
		directorServiceMappingProvider.save(directorInfo, service3);

		directorServiceMappingProvider.save(directorInfo1, service2);
		directorServiceMappingProvider.save(directorInfo1, service3);

		directorServiceMappingProvider.save(directorInfo2, service2);
		directorServiceMappingProvider.save(directorInfo2, service3);
		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/services/activation/progress")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		// then
		DirectorServiceFindActivationProgressResponseForDirector response =
			objectMapper.readValue(
				result.getResponse().getContentAsString(),
				DirectorServiceFindActivationProgressResponseForDirector.class
			);

		assertThat(response.getProgressPercentage()).isEqualTo(100);
	}

	@Test
	@DisplayName("디렉터는 서비스 활성화 진행률을 정상적으로 조회할 수 있다. (모든 서비스가 활성화 상태)")
	void findActivationProgressWhenAllServicesActive() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		DirectorService service1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService service2 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);
		DirectorService service3 = directorServiceProvider.save(SERVICE_NAME_3_STR, service1);

		directorServiceMappingProvider.save(directorInfo, service2);
		directorServiceMappingProvider.save(directorInfo, service3);

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/services/activation/progress")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		// then
		DirectorServiceFindActivationProgressResponseForDirector response =
			objectMapper.readValue(
				result.getResponse().getContentAsString(),
				DirectorServiceFindActivationProgressResponseForDirector.class
			);

		assertThat(response.getProgressPercentage()).isEqualTo(100);
	}

}
