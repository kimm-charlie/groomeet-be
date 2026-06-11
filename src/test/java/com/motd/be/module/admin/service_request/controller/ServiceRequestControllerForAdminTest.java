package com.motd.be.module.admin.service_request.controller;

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
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.entity.LocationType;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

@ControllerIntegrationTest
public class ServiceRequestControllerForAdminTest extends BaseIntegrationTest {

	@Test
	@DisplayName("관리자는 서비스 요청 목록을 조회할 수 있다.")
	void findAll() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		DirectorService parentService = directorServiceProvider.save("헬스케어", null);
		DirectorService childService = directorServiceProvider.save("PT", parentService);

		Member member1 = memberProvider.saveMember(SignInPlatform.APPLE);
		Member member2 = memberProvider.saveMember(SignInPlatform.KAKAO);

		serviceRequestProvider.savePending(childService, member1);
		serviceRequestProvider.savePending(childService, member2);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/service-requests")
					.param("page", "0")
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		assertThat(jsonObject.getInt("page")).isEqualTo(0);
		assertThat(jsonObject.getBoolean("hasNext")).isFalse();
		JSONArray serviceRequests = jsonObject.getJSONArray("serviceRequests");
		assertThat(serviceRequests.length()).isEqualTo(2);
	}

	@Test
	@DisplayName("관리자는 memberId로 서비스 요청를 검색할 수 있다.")
	void findAll_withMemberId() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		DirectorService parentService = directorServiceProvider.save("헬스케어", null);
		DirectorService childService = directorServiceProvider.save("PT", parentService);

		Member member1 = memberProvider.saveMember(SignInPlatform.APPLE);
		Member member2 = memberProvider.saveMember(SignInPlatform.KAKAO);

		serviceRequestProvider.savePending(childService, member1);
		serviceRequestProvider.savePending(childService, member2);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/service-requests")
					.param("search", String.valueOf(member1.getId()))
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		JSONArray serviceRequests = jsonObject.getJSONArray("serviceRequests");
		assertThat(serviceRequests.length()).isEqualTo(1);
		assertThat(serviceRequests.getJSONObject(0).getJSONObject("member").getLong("id"))
			.isEqualTo(member1.getId());
	}

	@Test
	@DisplayName("관리자는 nickname으로 서비스 요청를 검색할 수 있다.")
	void findAll_withNickname() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		DirectorService parentService = directorServiceProvider.save("헬스케어", null);
		DirectorService childService = directorServiceProvider.save("PT", parentService);

		Member member1 = memberProvider.saveMember(SignInPlatform.APPLE);
		Member member2 = memberProvider.saveMember(SignInPlatform.KAKAO);

		serviceRequestProvider.savePending(childService, member1);
		serviceRequestProvider.savePending(childService, member2);

		entityManager.flush();
		entityManager.clear();

		// when - nickname은 기본적으로 NICKNAME 상수값 사용하므로 부분 일치 검색
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/service-requests")
					.param("search", member1.getNickname())
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then - 같은 닉네임 패턴을 가진 회원들이 검색됨
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		JSONArray serviceRequests = jsonObject.getJSONArray("serviceRequests");
		assertThat(serviceRequests.length()).isGreaterThanOrEqualTo(1);
	}

	@Test
	@DisplayName("관리자는 서비스 요청 상세 정보를 조회할 수 있다.")
	void findDetail() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		DirectorService parentService = directorServiceProvider.save("헬스케어", null);
		DirectorService childService = directorServiceProvider.save("PT", parentService);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		Location parentLocation = locationProvider.save("서울", LocationType.CITY);
		Location childLocation = locationProvider.saveWithParent("강남구", LocationType.DISTRICT, parentLocation);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(childService, member);
		requestLocationMappingProvider.save(childLocation, serviceRequest);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/service-requests/{serviceRequestId}", serviceRequest.getId())
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		assertThat(jsonObject.getLong("serviceRequestId")).isEqualTo(serviceRequest.getId());
		assertThat(jsonObject.getString("serviceName")).isEqualTo("PT");
		assertThat(jsonObject.getString("status")).isEqualTo("제안 받는중");
		assertThat(jsonObject.getBoolean("isReceivingEstimate")).isTrue();
		assertThat(jsonObject.getBoolean("isDeleted")).isFalse();

		JSONArray locations = jsonObject.getJSONArray("requestLocationMappings");
		assertThat(locations.length()).isEqualTo(1);
		assertThat(locations.getJSONObject(0).getString("name")).isEqualTo("강남구");
		assertThat(locations.getJSONObject(0).getString("fullName")).isEqualTo("서울 강남구");
	}

	@Test
	@DisplayName("관리자는 다이렉트 요청 서비스 요청의 상세 정보를 조회할 수 있다.")
	void findDetail_directRequest() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		DirectorService parentService = directorServiceProvider.save("헬스케어", null);
		DirectorService childService = directorServiceProvider.save("PT", parentService);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		ServiceRequest serviceRequest = serviceRequestProvider.saveWithIsPendingAndIsDirectRequestTrue(
			childService, requester, director);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/service-requests/{serviceRequestId}", serviceRequest.getId())
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		assertThat(jsonObject.getBoolean("isDirectRequest")).isTrue();
		assertThat(jsonObject.getJSONObject("directRequestedMember").getLong("id")).isEqualTo(director.getId());
	}

	@Test
	@DisplayName("관리자는 제안이 있는 서비스 요청의 상세 정보를 조회할 수 있다.")
	void findDetail_withEstimates() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		DirectorService parentService = directorServiceProvider.save("헬스케어", null);
		DirectorService childService = directorServiceProvider.save("PT", parentService);

		Member requester = memberProvider.saveMember(SignInPlatform.APPLE);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(childService, requester);
		serviceEstimateProvider.save(directorInfo, serviceRequest);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/service-requests/{serviceRequestId}", serviceRequest.getId())
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		JSONArray estimates = jsonObject.getJSONArray("estimates");
		assertThat(estimates.length()).isEqualTo(1);
		assertThat(estimates.getJSONObject(0).has("title")).isTrue();
		assertThat(estimates.getJSONObject(0).has("director")).isTrue();
	}

	@Test
	@DisplayName("존재하지 않는 서비스 요청 상세 조회 시 404 에러가 발생한다.")
	void findDetail_notFound() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		Long invalidServiceRequestId = 99999L;

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/service-requests/{serviceRequestId}", invalidServiceRequestId)
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("관리자 권한 없이 서비스 요청 목록 조회 시 401 에러가 발생한다.")
	void findAll_unauthorized() throws Exception {
		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/service-requests")
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized());
	}
}
