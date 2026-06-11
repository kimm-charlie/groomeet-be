package com.motd.be.module.admin.consulting_request.controller;

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
import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_request.enums.ConsultingRequestStatus;
import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
import com.motd.be.module.member.consulting_sheet.enums.ConsultingSheetStatus;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;

@ControllerIntegrationTest
public class ConsultingRequestControllerForAdminTest extends BaseIntegrationTest {

	@Test
	@DisplayName("관리자 컨설팅 요청서 목록 조회가 가능하다")
	void 관리자_컨설팅_요청서_목록_조회가_가능하다() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		consultingRequestProvider.save(member);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/consulting-requests")
					.param("page", "0")
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		assertThat(jsonObject.getInt("page")).isEqualTo(0);
		assertThat(jsonObject.getBoolean("hasNext")).isFalse();
		assertThat(jsonObject.getLong("totalCount")).isEqualTo(1);
		JSONArray consultingRequests = jsonObject.getJSONArray("consultingRequests");
		assertThat(consultingRequests.length()).isEqualTo(1);
		assertThat(consultingRequests.getJSONObject(0).getString("requestStatus"))
			.isEqualTo(ConsultingRequestStatus.PENDING.name());
		assertThat(consultingRequests.getJSONObject(0).isNull("sheetStatus")).isTrue();
	}

	@Test
	@DisplayName("관리자 컨설팅 요청서 목록 조회가 가능하다 (컨설팅지 있는 요청서)")
	void 관리자_컨설팅_요청서_목록_조회가_가능하다_컨설팅지_있는_요청서() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		ConsultingRequest consultingRequest = consultingRequestProvider.save(member);
		consultingSheetProvider.savePendingApproval(consultingRequest, directorInfo);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/consulting-requests")
					.param("page", "0")
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		JSONArray consultingRequests = jsonObject.getJSONArray("consultingRequests");
		assertThat(consultingRequests.length()).isEqualTo(1);
		assertThat(consultingRequests.getJSONObject(0).getString("sheetStatus"))
			.isEqualTo(ConsultingSheetStatus.PENDING_APPROVAL.name());
	}

	@Test
	@DisplayName("관리자 컨설팅 요청서 목록 조회가 가능하다 (기본값은 승인된 건 제외)")
	void 관리자_컨설팅_요청서_목록_조회가_가능하다_기본값은_승인된_건_제외() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		Member member1 = memberProvider.saveMember(SignInPlatform.APPLE);
		Member member2 = memberProvider.saveMember(SignInPlatform.GOOGLE);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		ConsultingRequest consultingRequest1 = consultingRequestProvider.save(member1);
		ConsultingRequest consultingRequest2 = consultingRequestProvider.save(member2);
		consultingSheetProvider.savePendingApproval(consultingRequest1, directorInfo);
		consultingSheetProvider.saveApproved(consultingRequest2, directorInfo);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/consulting-requests")
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		JSONArray consultingRequests = jsonObject.getJSONArray("consultingRequests");
		assertThat(consultingRequests.length()).isEqualTo(1);
	}

	@Test
	@DisplayName("관리자 컨설팅 요청서 목록 조회가 가능하다 (기본값은 반려된 건도 제외)")
	void 관리자_컨설팅_요청서_목록_조회가_가능하다_기본값은_반려된_건도_제외() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		Member member1 = memberProvider.saveMember(SignInPlatform.APPLE);
		Member member2 = memberProvider.saveMember(SignInPlatform.GOOGLE);
		Member member3 = memberProvider.saveMember(SignInPlatform.KAKAO);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		// PENDING_APPROVAL 시트 → 기본 목록에 포함
		ConsultingRequest consultingRequest1 = consultingRequestProvider.save(member1);
		consultingSheetProvider.savePendingApproval(consultingRequest1, directorInfo);

		// REJECTED 시트 → 기본 목록에서 제외
		ConsultingRequest consultingRequest2 = consultingRequestProvider.save(member2);
		consultingSheetProvider.saveRejected(consultingRequest2, directorInfo);

		// 시트 없음 (PENDING) → 기본 목록에 포함
		consultingRequestProvider.save(member3);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/consulting-requests")
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		JSONArray consultingRequests = jsonObject.getJSONArray("consultingRequests");
		assertThat(consultingRequests.length()).isEqualTo(2);
		assertThat(jsonObject.getLong("totalCount")).isEqualTo(2);
	}

	@Test
	@DisplayName("관리자 컨설팅 요청서 목록 조회가 가능하다 (showAll=true)")
	void 관리자_컨설팅_요청서_목록_조회가_가능하다_showAll_true() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		Member member1 = memberProvider.saveMember(SignInPlatform.APPLE);
		Member member2 = memberProvider.saveMember(SignInPlatform.GOOGLE);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		ConsultingRequest consultingRequest1 = consultingRequestProvider.save(member1);
		ConsultingRequest consultingRequest2 = consultingRequestProvider.save(member2);
		consultingSheetProvider.savePendingApproval(consultingRequest1, directorInfo);
		consultingSheetProvider.saveApproved(consultingRequest2, directorInfo);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/consulting-requests")
					.param("showAll", "true")
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		JSONArray consultingRequests = jsonObject.getJSONArray("consultingRequests");
		assertThat(consultingRequests.length()).isEqualTo(2);
		assertThat(jsonObject.getLong("totalCount")).isEqualTo(2);
	}

	@Test
	@DisplayName("관리자 컨설팅 요청서 목록 조회가 가능하다 (회원명 검색)")
	void 관리자_컨설팅_요청서_목록_조회가_가능하다_회원명_검색() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		Member member1 = memberProvider.saveMember(SignInPlatform.APPLE);
		Member member2 = memberProvider.saveMember(SignInPlatform.GOOGLE);
		consultingRequestProvider.save(member1);
		consultingRequestProvider.save(member2);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/consulting-requests")
					.param("search", member1.getNickname())
					.param("showAll", "true")
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		JSONArray consultingRequests = jsonObject.getJSONArray("consultingRequests");
		assertThat(consultingRequests.length()).isGreaterThanOrEqualTo(1);
	}

	@Test
	@DisplayName("관리자 컨설팅 요청서 목록 조회가 가능하다 (디렉터명 검색)")
	void 관리자_컨설팅_요청서_목록_조회가_가능하다_디렉터명_검색() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		Member member1 = memberProvider.saveMember(SignInPlatform.APPLE);
		Member member2 = memberProvider.saveMember(SignInPlatform.GOOGLE);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		Member directorMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		directorMember.updateNickname("고유디렉터");

		ConsultingRequest consultingRequest1 = consultingRequestProvider.save(member1);
		consultingRequestProvider.save(member2);
		consultingSheetProvider.savePendingApproval(consultingRequest1, directorInfo);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/consulting-requests")
					.param("search", "고유디렉터")
					.param("showAll", "true")
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		JSONArray consultingRequests = jsonObject.getJSONArray("consultingRequests");
		assertThat(consultingRequests.length()).isEqualTo(1);
		assertThat(consultingRequests.getJSONObject(0).getLong("id"))
			.isEqualTo(consultingRequest1.getId());
	}

	@Test
	@DisplayName("관리자 컨설팅 요청서 상세 조회가 가능하다")
	void 관리자_컨설팅_요청서_상세_조회가_가능하다() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		ConsultingRequest consultingRequest = consultingRequestProvider.save(member);
		consultingSheetProvider.savePendingApproval(consultingRequest, directorInfo);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/consulting-requests/{consultingRequestId}",
					consultingRequest.getId())
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		assertThat(jsonObject.getLong("id")).isEqualTo(consultingRequest.getId());
		assertThat(jsonObject.getString("requestStatus")).isEqualTo(ConsultingRequestStatus.PENDING.name());

		JSONObject memberObj = jsonObject.getJSONObject("member");
		assertThat(memberObj.getLong("id")).isEqualTo(member.getId());

		JSONObject sheetObj = jsonObject.getJSONObject("consultingSheet");
		assertThat(sheetObj.getString("status")).isEqualTo(ConsultingSheetStatus.PENDING_APPROVAL.name());
	}

	@Test
	@DisplayName("관리자 컨설팅 요청서 상세 조회가 가능하다 (컨설팅지 없는 경우)")
	void 관리자_컨설팅_요청서_상세_조회가_가능하다_컨설팅지_없는_경우() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		Member member = memberProvider.saveMember(SignInPlatform.APPLE);
		ConsultingRequest consultingRequest = consultingRequestProvider.save(member);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/consulting-requests/{consultingRequestId}",
					consultingRequest.getId())
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		assertThat(jsonObject.getLong("id")).isEqualTo(consultingRequest.getId());
		assertThat(jsonObject.getString("requestStatus")).isEqualTo(ConsultingRequestStatus.PENDING.name());
		assertThat(jsonObject.isNull("consultingSheet")).isTrue();
	}

	@Test
	@DisplayName("관리자 컨설팅 요청서 상세 조회가 가능하다 (존재하지 않으면 404)")
	void 관리자_컨설팅_요청서_상세_조회가_가능하다_존재하지_않으면_404() throws Exception {
		// given
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		Long invalidId = 99999L;

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/consulting-requests/{consultingRequestId}", invalidId)
					.header(AUTHORIZATION_STR, BEARER_STR + jwt.getAccessToken()))
			.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("관리자 컨설팅 요청서 목록 조회가 가능하다 (권한 없으면 401)")
	void 관리자_컨설팅_요청서_목록_조회가_가능하다_권한_없으면_401() throws Exception {
		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/consulting-requests")
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("관리자 컨설팅 요청서 상세 조회가 가능하다 (권한 없으면 401)")
	void 관리자_컨설팅_요청서_상세_조회가_가능하다_권한_없으면_401() throws Exception {
		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/consulting-requests/{consultingRequestId}", 1L)
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized());
	}
}
