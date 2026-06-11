package com.motd.be.module.admin.director_service.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.admin.AdminTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.DirectorServiceException;
import com.motd.be.module.admin.admin.entity.Admin;
import com.motd.be.module.admin.director_service.dto.request.DirectorServiceSaveRequestForAdmin;
import com.motd.be.module.admin.director_service.dto.request.DirectorServiceUpdateRequestForAdmin;
import com.motd.be.module.admin.director_service.dto.response.DirectorServiceFindAllResponseForAdmin;
import com.motd.be.module.admin.director_service.dto.response.DirectorServiceResponseForAdmin;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.jwt.Jwt;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class DirectorServiceControllerForAdminTest extends BaseIntegrationTest {

	@Test
	@DisplayName("관리자는 디렉터 서비스를 전체조회 할 수 있다. (삭제된 서비스 제외, 정렬 순서 기준)")
	void findAll_withoutDeleted_sortedBySortOrder() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		DirectorService parent = directorServiceProvider.save("부모 서비스", null, 0, true);
		directorServiceProvider.save("서비스 2", parent, 2, true);
		directorServiceProvider.save("서비스 0", parent, 0, true);
		directorServiceProvider.save("서비스 1", parent, 1, true);
		directorServiceProvider.saveWithIsDeletedTrue("삭제된 서비스", parent, 3);

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/director-services")
					.param(PAGE_STR, "0")
					.param("parentId", parent.getId().toString())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		DirectorServiceFindAllResponseForAdmin response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			DirectorServiceFindAllResponseForAdmin.class);
		List<DirectorServiceResponseForAdmin> directorServices = response.getDirectorServices();

		assertThat(response.getTotalCount()).isEqualTo(3);
		assertThat(directorServices).hasSize(3);
		assertThat(directorServices.get(0).getName()).isEqualTo("서비스 0");
		assertThat(directorServices.get(1).getName()).isEqualTo("서비스 1");
		assertThat(directorServices.get(2).getName()).isEqualTo("서비스 2");
	}

	@Test
	@DisplayName("관리자는 자식 서비스 조회 시 비활성 서비스도 포함한다.")
	void findAll_includesInactiveChildren() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		DirectorService parent = directorServiceProvider.save("부모 서비스", null, 0, true);
		directorServiceProvider.save("활성 서비스", parent, 0, true);
		directorServiceProvider.save("비활성 서비스", parent, 1, false);

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/director-services")
					.param(PAGE_STR, "0")
					.param("parentId", parent.getId().toString())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		DirectorServiceFindAllResponseForAdmin response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			DirectorServiceFindAllResponseForAdmin.class);
		List<DirectorServiceResponseForAdmin> directorServices = response.getDirectorServices();

		assertThat(response.getTotalCount()).isEqualTo(2);
		assertThat(directorServices).hasSize(2);
		assertThat(directorServices.stream().anyMatch(service -> !service.getIsActive())).isTrue();
	}

	@Test
	@DisplayName("관리자는 디렉터 서비스를 전체조회 할 수 있다. (삭제된 서비스 포함, 생성순 정렬)")
	void findAll_withDeleted_sortedByCreatedAtDesc() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		DirectorService parent = directorServiceProvider.save("부모 서비스", null, 0, true);
		DirectorService service1 = directorServiceProvider.save("서비스 1", parent, 0, true);
		Thread.sleep(10);
		DirectorService service2 = directorServiceProvider.save("서비스 2", parent, 1, true);
		Thread.sleep(10);
		DirectorService service3 = directorServiceProvider.saveWithIsDeletedTrue("삭제된 서비스", parent, 2);

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/director-services")
					.param(PAGE_STR, "0")
					.param(SHOW_IS_DELETED_STR, Boolean.TRUE.toString())
					.param("parentId", parent.getId().toString())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		DirectorServiceFindAllResponseForAdmin response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			DirectorServiceFindAllResponseForAdmin.class);
		List<DirectorServiceResponseForAdmin> directorServices = response.getDirectorServices();

		assertThat(response.getTotalCount()).isEqualTo(3);
		assertThat(directorServices).hasSize(3);
		assertThat(directorServices.get(0).getId()).isEqualTo(service3.getId());
		assertThat(directorServices.get(1).getId()).isEqualTo(service2.getId());
		assertThat(directorServices.get(2).getId()).isEqualTo(service1.getId());
	}

	@Test
	@DisplayName("관리자는 디렉터 서비스 전체 조회를 페이징 할 수 있다.")
	void findAll_withPagination() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		DirectorService parent = directorServiceProvider.save("부모 서비스", null, 0, true);
		for (int i = 0; i < 11; i++) {
			directorServiceProvider.save("서비스 " + i, parent, i, true);
		}

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/director-services")
					.param(PAGE_STR, "0")
					.param("parentId", parent.getId().toString())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		DirectorServiceFindAllResponseForAdmin response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			DirectorServiceFindAllResponseForAdmin.class);

		assertThat(response.getTotalCount()).isEqualTo(10);
		assertThat(response.getHasNext()).isTrue();
	}

	@Test
	@DisplayName("관리자는 디렉터 서비스 상세조회가 가능하다. (삭제된 서비스 조회)")
	void findDetailIncludingDeleted() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		DirectorService service = directorServiceProvider.saveWithIsDeletedTrue("삭제된 서비스", null, 0);

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/director-services/{directorServiceId}", service.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		DirectorServiceResponseForAdmin response = objectMapper.readValue(result.getResponse().getContentAsString(),
			DirectorServiceResponseForAdmin.class);

		assertThat(response.getId()).isEqualTo(service.getId());
		assertThat(response.getIsDeleted()).isTrue();
	}

	@Test
	@DisplayName("관리자는 디렉터 서비스를 저장할 수 있다. (정렬 순서 조정)")
	void save_reordersSortOrder() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		DirectorService parent = directorServiceProvider.save("부모 서비스", null, 0, true);
		DirectorService service1 = directorServiceProvider.save("서비스 1", parent, 0, true);
		DirectorService service2 = directorServiceProvider.save("서비스 2", parent, 1, true);
		DirectorService service3 = directorServiceProvider.save("서비스 3", parent, 2, true);

		DirectorServiceSaveRequestForAdmin request = DirectorServiceSaveRequestForAdmin.builder()
			.name("새 서비스")
			.parentId(parent.getId())
			.isActive(true)
			.sortOrder(1)
			.build();

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/admin/director-services")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		entityManager.flush();
		entityManager.clear();

		List<DirectorService> services = directorServiceProvider.findAll()
			.stream()
			.filter(s -> s.getParent() != null && s.getParent().getId().equals(parent.getId()))
			.sorted(Comparator.comparing(DirectorService::getSortOrder))
			.toList();

		assertThat(services).hasSize(4);
		assertThat(services.get(0).getId()).isEqualTo(service1.getId());
		assertThat(services.get(2).getId()).isEqualTo(service2.getId());
		assertThat(services.get(3).getId()).isEqualTo(service3.getId());
	}

	@Test
	@DisplayName("관리자는 디렉터 서비스를 수정할 수 있다. (정렬 순서 조정)")
	void update_reordersSortOrder() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		DirectorService parent = directorServiceProvider.save("부모 서비스", null, 0, true);
		DirectorService service0 = directorServiceProvider.save("서비스 0", parent, 0, true);
		DirectorService service1 = directorServiceProvider.save("서비스 1", parent, 1, true);
		DirectorService service2 = directorServiceProvider.save("서비스 2", parent, 2, true);
		DirectorService service3 = directorServiceProvider.save("서비스 3", parent, 3, true);

		DirectorServiceUpdateRequestForAdmin request = DirectorServiceUpdateRequestForAdmin.builder()
			.name("수정된 서비스")
			.isActive(true)
			.parentId(parent.getId())
			.sortOrder(3)
			.build();

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(
				MockMvcRequestBuilders.put("/api/admin/director-services/{directorServiceId}", service1.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		List<DirectorService> services = directorServiceProvider.findAll()
			.stream()
			.filter(s -> s.getParent() != null && s.getParent().getId().equals(parent.getId()))
			.sorted(Comparator.comparing(DirectorService::getSortOrder))
			.toList();

		assertThat(services.get(0).getId()).isEqualTo(service0.getId());
		assertThat(services.get(1).getId()).isEqualTo(service2.getId());
		assertThat(services.get(2).getId()).isEqualTo(service3.getId());
		assertThat(services.get(3).getId()).isEqualTo(service1.getId());
	}

	@Test
	@DisplayName("관리자는 디렉터 서비스를 삭제할 수 있다. (정렬 순서 조정)")
	void delete_reordersSortOrder() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		DirectorService parent = directorServiceProvider.save("부모 서비스", null, 0, true);
		DirectorService service1 = directorServiceProvider.save("서비스 1", parent, 0, true);
		DirectorService service2 = directorServiceProvider.save("서비스 2", parent, 1, true);
		DirectorService service3 = directorServiceProvider.save("서비스 3", parent, 2, true);

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(
				MockMvcRequestBuilders.delete("/api/admin/director-services/{directorServiceId}", service2.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

		entityManager.flush();
		entityManager.clear();

		List<DirectorService> services = directorServiceProvider.findAll()
			.stream()
			.filter(s -> s.getParent() != null && s.getParent().getId().equals(parent.getId()))
			.sorted(Comparator.comparing(DirectorService::getSortOrder))
			.toList();

		DirectorService deletedService = services.stream()
			.filter(service -> service.getId().equals(service2.getId()))
			.findFirst()
			.orElseThrow();
		assertThat(deletedService.getIsDeleted()).isTrue();

		DirectorService remainedService3 = services.stream()
			.filter(service -> service.getId().equals(service3.getId()))
			.findFirst()
			.orElseThrow();
		assertThat(remainedService3.getSortOrder()).isEqualTo(1);
	}

	@Test
	@DisplayName("관리자는 디렉터 서비스를 상세조회 할 수 있다. (존재하지 않는 서비스 조회 시 실패)")
	void findDetail_notFound() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/director-services/{directorServiceId}", 999999L)
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath(ERROR_STATUS).value(
				DirectorServiceException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(
				DirectorServiceException.NOT_FOUND.getErrorMessage()))
			.andExpect(
				jsonPath(ERROR_CODE).value(DirectorServiceException.NOT_FOUND.getCode()));
	}
}
