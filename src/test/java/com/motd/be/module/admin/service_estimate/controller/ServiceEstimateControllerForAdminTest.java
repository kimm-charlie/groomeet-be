package com.motd.be.module.admin.service_estimate.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.admin.AdminTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

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
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.review.entity.Review;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

@ControllerIntegrationTest
public class ServiceEstimateControllerForAdminTest extends BaseIntegrationTest {

	@Test
	@DisplayName("관리자는 서비스 제안 목록을 조회할 수 있다.")
	void findAll() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		DirectorService parentService = directorServiceProvider.save("헬스케어", null);
		DirectorService childService = directorServiceProvider.save("PT", parentService);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		DirectorInfo directorInfo1 = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo1);
		DirectorInfo directorInfo2 = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, directorInfo2);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(childService, member);
		serviceEstimateProvider.save(directorInfo1, serviceRequest);
		serviceEstimateProvider.save(directorInfo2, serviceRequest);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/service-estimates")
					.param("page", "0")
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		assertThat(jsonObject.getInt("page")).isEqualTo(0);
		assertThat(jsonObject.getBoolean("hasNext")).isFalse();
		JSONArray serviceEstimates = jsonObject.getJSONArray("serviceEstimates");
		assertThat(serviceEstimates.length()).isEqualTo(2);
	}

	@Test
	@DisplayName("관리자는 directorId로 서비스 제안을 검색할 수 있다.")
	void findAll_withDirectorId() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		DirectorService parentService = directorServiceProvider.save("헬스케어", null);
		DirectorService childService = directorServiceProvider.save("PT", parentService);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		DirectorInfo directorInfo1 = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		Member director1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo1);
		DirectorInfo directorInfo2 = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, directorInfo2);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(childService, member);
		serviceEstimateProvider.save(directorInfo1, serviceRequest);
		serviceEstimateProvider.save(directorInfo2, serviceRequest);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/service-estimates")
					.param("search", String.valueOf(director1.getId()))
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		JSONArray serviceEstimates = jsonObject.getJSONArray("serviceEstimates");
		assertThat(serviceEstimates.length()).isEqualTo(1);
		assertThat(serviceEstimates.getJSONObject(0).getJSONObject("director").getLong("id"))
			.isEqualTo(director1.getId());
	}

	@Test
	@DisplayName("관리자는 director nickname으로 서비스 제안을 검색할 수 있다.")
	void findAll_withDirectorNickname() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		DirectorService parentService = directorServiceProvider.save("헬스케어", null);
		DirectorService childService = directorServiceProvider.save("PT", parentService);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		DirectorInfo directorInfo1 = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		Member director1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo1);
		DirectorInfo directorInfo2 = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, directorInfo2);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(childService, member);
		serviceEstimateProvider.save(directorInfo1, serviceRequest);
		serviceEstimateProvider.save(directorInfo2, serviceRequest);

		entityManager.flush();
		entityManager.clear();

		// when - nickname은 기본적으로 NICKNAME 상수값 사용하므로 부분 일치 검색
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/service-estimates")
					.param("search", director1.getNickname())
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then - 같은 닉네임 패턴을 가진 고수들의 제안이 검색됨
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		JSONArray serviceEstimates = jsonObject.getJSONArray("serviceEstimates");
		assertThat(serviceEstimates.length()).isGreaterThanOrEqualTo(1);
	}

	@Test
	@DisplayName("관리자는 status로 서비스 제안을 필터링할 수 있다.")
	void findAll_withStatus() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		DirectorService parentService = directorServiceProvider.save("헬스케어", null);
		DirectorService childService = directorServiceProvider.save("PT", parentService);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(childService, member);
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(childService, member);

		serviceEstimateProvider.save(directorInfo, serviceRequest1); // PENDING
		serviceEstimateProvider.saveOngoing(directorInfo, serviceRequest2, LocalDateTime.now()); // ONGOING

		entityManager.flush();
		entityManager.clear();

		// when - PENDING 상태만 조회
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/service-estimates")
					.param("status", "PENDING")
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		JSONArray serviceEstimates = jsonObject.getJSONArray("serviceEstimates");
		assertThat(serviceEstimates.length()).isEqualTo(1);
		assertThat(serviceEstimates.getJSONObject(0).getString("status")).isEqualTo("제안 진행중");
	}

	@Test
	@DisplayName("관리자는 삭제된 제안은 조회되지 않는다.")
	void findAll_excludeDeleted() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		DirectorService parentService = directorServiceProvider.save("헬스케어", null);
		DirectorService childService = directorServiceProvider.save("PT", parentService);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		ServiceRequest serviceRequest1 = serviceRequestProvider.savePending(childService, member);
		ServiceRequest serviceRequest2 = serviceRequestProvider.savePending(childService, member);

		serviceEstimateProvider.save(directorInfo, serviceRequest1); // 정상
		serviceEstimateProvider.saveWithIsDeletedTrue(directorInfo, serviceRequest2); // 삭제됨

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/service-estimates")
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then - 삭제되지 않은 제안만 조회됨
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		JSONArray serviceEstimates = jsonObject.getJSONArray("serviceEstimates");
		assertThat(serviceEstimates.length()).isEqualTo(1);
	}

	@Test
	@DisplayName("관리자는 서비스 제안 상세 정보를 조회할 수 있다.")
	void findDetail() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		DirectorService parentService = directorServiceProvider.save("헬스케어", null);
		DirectorService childService = directorServiceProvider.save("PT", parentService);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(childService, member);
		ServiceEstimate serviceEstimate = serviceEstimateProvider.save(directorInfo, serviceRequest);
		Review review = reviewProvider.save(member, serviceEstimate);

		// 채팅방 생성 및 매핑
		var chatRoom = chatRoomProvider.save();
		chatRoomServiceEstimateMappingProvider.save(chatRoom, serviceEstimate);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/service-estimates/{serviceEstimateId}",
					serviceEstimate.getId())
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		assertThat(jsonObject.getLong("serviceEstimateId")).isEqualTo(serviceEstimate.getId());
		assertThat(jsonObject.getString("title")).isEqualTo(TITLE_STR);
		assertThat(jsonObject.getString("status")).isEqualTo("제안 진행중");
		assertThat(jsonObject.getBoolean("isDeleted")).isFalse();
		assertThat(jsonObject.getBoolean("isHired")).isFalse();

		JSONObject directorObj = jsonObject.getJSONObject("director");
		assertThat(directorObj.getLong("id")).isEqualTo(director.getId());

		JSONObject memberObj = jsonObject.getJSONObject("member");
		assertThat(memberObj.getLong("id")).isEqualTo(member.getId());

		JSONObject serviceRequestObj = jsonObject.getJSONObject("serviceRequest");
		assertThat(serviceRequestObj.getLong("id")).isEqualTo(serviceRequest.getId());
		assertThat(serviceRequestObj.getString("serviceName")).isEqualTo("PT");

		JSONArray files = jsonObject.getJSONArray("files");
		assertThat(files.length()).isEqualTo(0);

		JSONObject reviewObj = jsonObject.getJSONObject("review");
		assertThat(reviewObj.getLong("id")).isEqualTo(review.getId());
		assertThat(reviewObj.getString("title")).isEqualTo(TITLE_STR);
		assertThat(reviewObj.getString("content")).isEqualTo(CONTENT_STR);
		assertThat(reviewObj.getBoolean("isDeleted")).isFalse();
		assertThat(reviewObj.getJSONObject("writer").getLong("id")).isEqualTo(member.getId());

		// chatRoomId 검증
		assertThat(jsonObject.has("chatRoomId")).isTrue();
		assertThat(jsonObject.getLong("chatRoomId")).isNotNull();
	}

	@Test
	@DisplayName("관리자는 진행중인 제안의 상세 정보를 조회할 수 있다.")
	void findDetail_ongoing() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		DirectorService parentService = directorServiceProvider.save("헬스케어", null);
		DirectorService childService = directorServiceProvider.save("PT", parentService);

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		ServiceRequest serviceRequest = serviceRequestProvider.savePending(childService, member);
		ServiceEstimate serviceEstimate = serviceEstimateProvider.saveOngoing(directorInfo, serviceRequest,
			LocalDateTime.now());

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/service-estimates/{serviceEstimateId}",
					serviceEstimate.getId())
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		assertThat(jsonObject.getString("status")).isEqualTo("진행중");
		assertThat(jsonObject.getBoolean("isHired")).isTrue();
		assertThat(jsonObject.has("ongoingAt")).isTrue();
	}

	@Test
	@DisplayName("존재하지 않는 서비스 제안 상세 조회 시 404 에러가 발생한다.")
	void findDetail_notFound() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		Long invalidServiceEstimateId = 99999L;

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/service-estimates/{serviceEstimateId}",
					invalidServiceEstimateId)
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("관리자 권한 없이 서비스 제안 목록 조회 시 401 에러가 발생한다.")
	void findAll_unauthorized() throws Exception {
		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/service-estimates")
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("관리자 권한 없이 서비스 제안 상세 조회 시 401 에러가 발생한다.")
	void findDetail_unauthorized() throws Exception {
		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/service-estimates/{serviceEstimateId}", 1L)
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized());
	}
}
