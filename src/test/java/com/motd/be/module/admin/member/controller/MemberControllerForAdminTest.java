package com.motd.be.module.admin.member.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.admin.AdminTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.dreamsecurity.json.JSONArray;
import com.dreamsecurity.json.JSONObject;
import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.module.admin.admin.entity.Admin;
import com.motd.be.module.admin.member.dto.request.BanRequestForAdmin;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.member.entity.BanPeriod;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;

@ControllerIntegrationTest
public class MemberControllerForAdminTest extends BaseIntegrationTest {

	@Test
	@DisplayName("관리자는 회원 목록을 조회할 수 있다.")
	void findAll() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		memberProvider.saveMember(SignInPlatform.APPLE);
		memberProvider.saveMember(SignInPlatform.KAKAO);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, directorInfo);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/members")
					.param("page", "0")
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		assertThat(jsonObject.getInt("page")).isEqualTo(0);
		assertThat(jsonObject.getLong("totalCount")).isEqualTo(3L);
		assertThat(jsonObject.getLong("directorCount")).isEqualTo(1L);
		assertThat(jsonObject.getLong("memberCount")).isEqualTo(2L);
		JSONArray members = jsonObject.getJSONArray("members");
		assertThat(members.length()).isEqualTo(3);
	}

	@Test
	@DisplayName("관리자는 회원 ID로 검색할 수 있다.")
	void findAllWithSearchById() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		Member member1 = memberProvider.saveMember(SignInPlatform.APPLE);
		memberProvider.saveMember(SignInPlatform.KAKAO);

		entityManager.flush();
		entityManager.clear();

		// when - search by member ID
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/members")
					.param("search", String.valueOf(member1.getId()))
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		// Searching by ID should find the specific member
		assertThat(jsonObject.getLong("totalCount")).isGreaterThanOrEqualTo(1L);
	}

	@Test
	@DisplayName("관리자는 디렉터만 조회할 수 있다.")
	void findAllWithDirectorFilter() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		memberProvider.saveMember(SignInPlatform.APPLE);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, directorInfo);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/members")
					.param("showOnlyDirector", "true")
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		JSONArray members = jsonObject.getJSONArray("members");
		assertThat(members.length()).isEqualTo(1);
		assertThat(members.getJSONObject(0).getBoolean("isDirector")).isTrue();
	}

	@Test
	@DisplayName("관리자는 디렉터 조회 시 디렉터의 서비스를 확인할 수 있다.")
	void findAllWithDirectorServices() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, directorInfo);

		DirectorService parentService = directorServiceProvider.save("Hair", null);
		DirectorService childService = directorServiceProvider.save("Cut", parentService);
		directorServiceMappingProvider.save(directorInfo, childService);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/members")
					.param("showOnlyDirector", "true")
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);
		JSONArray members = jsonObject.getJSONArray("members");

		assertThat(members.length()).isEqualTo(1);
		JSONObject member = members.getJSONObject(0);
		JSONArray services = member.getJSONArray("services");
		assertThat(services.length()).isEqualTo(1);
		assertThat(services.getString(0)).isEqualTo("Hair");
	}

	@Test
	@DisplayName("관리자는 디렉터 조회 시 삭제된 서비스 매핑은 제외된다.")
	void findAllWithDirectorServices_excludesDeletedMappings() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, directorInfo);

		DirectorService parentService1 = directorServiceProvider.save("Hair", null);
		DirectorService childService1 = directorServiceProvider.save("Cut", parentService1);
		DirectorService parentService2 = directorServiceProvider.save("Makeup", null);
		DirectorService childService2 = directorServiceProvider.save("Wedding", parentService2);

		// 활성 매핑
		directorServiceMappingProvider.save(directorInfo, childService1);
		// 삭제된 매핑
		directorServiceMappingProvider.saveWithIsDeletedTrue(directorInfo, childService2);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/members")
					.param("showOnlyDirector", "true")
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);
		JSONArray members = jsonObject.getJSONArray("members");

		assertThat(members.length()).isEqualTo(1);
		JSONObject member = members.getJSONObject(0);
		JSONArray services = member.getJSONArray("services");
		
		// 삭제되지 않은 서비스만 포함되어야 함
		assertThat(services.length()).isEqualTo(1);
		assertThat(services.getString(0)).isEqualTo("Hair");
		// "Makeup"은 삭제된 매핑이므로 포함되지 않아야 함
	}

	@Test
	@DisplayName("관리자는 회원 상세 정보를 조회할 수 있다.")
	void findDetail() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/members/{memberId}", member.getId())
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		assertThat(jsonObject.getLong("id")).isEqualTo(member.getId());
		assertThat(jsonObject.getBoolean("isBanned")).isFalse();
	}

	@Test
	@DisplayName("관리자는 회원 상세 조회 시 디렉터의 서비스 목록을 계층 구조로 확인할 수 있다.")
	void findDetailWithServices() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, directorInfo);

		DirectorService parentService = directorServiceProvider.save("Hair", null);
		DirectorService childService = directorServiceProvider.save("Cut", parentService, 1);
		DirectorService childService2 = directorServiceProvider.save("Perm", parentService, 2);

		directorServiceMappingProvider.save(directorInfo, childService);
		directorServiceMappingProvider.save(directorInfo, childService2);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/members/{memberId}", member.getId())
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		JSONArray services = jsonObject.getJSONArray("services");
		assertThat(services.length()).isEqualTo(1);

		JSONObject hairService = services.getJSONObject(0);
		assertThat(hairService.getString("name")).isEqualTo("Hair");

		JSONArray children = hairService.getJSONArray("children");
		assertThat(children.length()).isEqualTo(2);
		assertThat(children.getString(0)).isEqualTo("Cut");
		assertThat(children.getString(1)).isEqualTo("Perm");
	}

	@Test
	@DisplayName("관리자는 회원을 밴 처리할 수 있다.")
	void ban() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);

		BanRequestForAdmin request = BanRequestForAdmin.builder()
			.banPeriod(BanPeriod.SEVEN_DAYS)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/admin/members/{memberId}/ban", member.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("관리자는 영구 밴 처리할 수 있다.")
	void banPermanent() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);

		BanRequestForAdmin request = BanRequestForAdmin.builder()
			.banPeriod(BanPeriod.PERMANENT)
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/admin/members/{memberId}/ban", member.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("밴 요청 시 banPeriod가 null이면 400 에러가 발생한다.")
	void ban_nullBanPeriod_shouldFail() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/admin/members/{memberId}/ban", member.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.content("{}")
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isBadRequest());
	}
}
