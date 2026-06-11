package com.motd.be.module.member.director_service.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.Constants.DIRECTOR_SERVICE_PARENT_ID;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.module.member.director_service.dto.response.DirectorServiceFindAllResponse;
import com.motd.be.module.member.director_service.entity.DirectorService;

@ControllerIntegrationTest
public class DirectorServiceControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("디렉터 서비스 전체 조회 (parentId 가 없을떄)")
	void findAllWithoutParentId() throws Exception {

		// given: 부모 없는 서비스 2개, 부모 있는 서비스 1개 생성
		DirectorService parent1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService childService1 = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent1);

		entityManager.flush();
		entityManager.clear();

		// when & then: parentId 없이 전체 조회
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/director-services")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		List<DirectorServiceFindAllResponse> response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			objectMapper.getTypeFactory().constructCollectionType(List.class, DirectorServiceFindAllResponse.class));

		// 부모만 조회됨 (parentId == null)
		assertThat(response).hasSize(1);

		List<Long> responseIds = response.stream()
			.map(DirectorServiceFindAllResponse::getId)
			.toList();

		assertThat(responseIds).containsExactlyInAnyOrder(parent1.getId());
	}

	@Test
	@DisplayName("디렉터 서비스 하위 조회 (parentId 있을 때)")
	void findAllWithParentId() throws Exception {
		// given: 부모/자식 서비스 생성
		DirectorService parent1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService parent2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService childService1 = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent1);
		DirectorService childService2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent1);
		DirectorService childService3 = directorCategoryProvider.save(SERVICE_NAME_2_STR, parent2);

		entityManager.flush();
		entityManager.clear();

		// when & then: parentId로 하위 서비스 조회
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/director-services")
				.param(DIRECTOR_SERVICE_PARENT_ID, parent1.getId().toString())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		List<DirectorServiceFindAllResponse> response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			objectMapper.getTypeFactory().constructCollectionType(List.class, DirectorServiceFindAllResponse.class));

		assertThat(response).hasSize(2);

		List<Long> responseIds = response.stream()
			.map(DirectorServiceFindAllResponse::getId)
			.toList();

		assertThat(responseIds).containsExactlyInAnyOrder(childService1.getId(), childService2.getId());
	}

	@Test
	@DisplayName("디렉터 서비스 전체 조회 (sortOrder 기준 정렬)")
	void findAllSortedBySortOrder() throws Exception {
		DirectorService service1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null, 2);
		DirectorService service2 = directorCategoryProvider.save(SERVICE_NAME_2_STR, null, 1);
		DirectorService service3 = directorCategoryProvider.save(SERVICE_NAME_3_STR, null, 3);

		entityManager.flush();
		entityManager.clear();

		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/director-services")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		List<DirectorServiceFindAllResponse> response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			objectMapper.getTypeFactory().constructCollectionType(List.class, DirectorServiceFindAllResponse.class));

		List<Long> responseIds = response.stream()
			.map(DirectorServiceFindAllResponse::getId)
			.toList();

		assertThat(responseIds).containsExactly(service2.getId(), service1.getId(), service3.getId());
	}
}
