package com.motd.be.module.member.director_service_mapping.controller;

import static com.motd.be.Constants.*;
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
import com.motd.be.exception.exceptions.DirectorInfoException;
import com.motd.be.module.director.director_service_mapping.dto.response.DirectorServiceFindAllResponseForDirector;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;

@ControllerIntegrationTest
public class DirectorServiceMappingControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("특정 디렉터의 서비스 매핑 목록을 정상적으로 조회한다.")
	void findAll() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());

		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		DirectorService service1 = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService service2 = directorServiceProvider.save(SERVICE_NAME_2_STR, service1);
		DirectorService service3 = directorServiceProvider.save(SERVICE_NAME_3_STR, service1);

		directorServiceMappingProvider.save(directorInfo, service2);
		directorServiceMappingProvider.save(directorInfo, service3);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/{targetMemberId}/services", member.getId())
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
	@DisplayName("특정 디렉터의 서비스 매핑 목록을 정상적으로 조회한다. (targetMember가 디렉터가 아닌경우 예외가 발생한다.)")
	void findAllWhenTargetMemberNotDirector() throws Exception {
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/{targetMemberId}/services", member.getId())
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(
				jsonPath("$.status").value(DirectorInfoException.DIRECTOR_INFO_NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(DirectorInfoException.DIRECTOR_INFO_NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(DirectorInfoException.DIRECTOR_INFO_NOT_FOUND.getCode()));
	}
}
