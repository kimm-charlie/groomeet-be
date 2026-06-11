package com.motd.be.module.director.member.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.HandlerException;
import com.motd.be.module.director.member.dto.response.MemberProfileSummaryResponseForDirector;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class MemberControllerForDirectorTest extends BaseIntegrationTest {

	@Test
	@DisplayName("디렉터는 회원의 프로필 요약 정보를 조회할 수 있다.")
	void findProfileSummary() throws Exception {
		// given
		Member targetMember = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member otherMember = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR);
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);

		// targetMember의 서비스 요청 생성: PENDING 2개, ONGOING 1개, COMPLETED 2개, EXPIRED 1개, CANCELED 1개
		ServiceRequest pendingRequest1 = serviceRequestProvider.savePending(directorService, targetMember);
		ServiceRequest pendingRequest2 = serviceRequestProvider.savePending(directorService, targetMember);

		ServiceRequest ongoingRequest = serviceRequestProvider.saveWithIsOngoingTrue(directorService, targetMember,
			LocalDateTime.now());

		ServiceRequest completedRequest1 = serviceRequestProvider.saveWithIsCompletedTrue(directorService, targetMember,
			LocalDateTime.now());
		ServiceRequest completedRequest2 = serviceRequestProvider.saveWithIsCompletedTrue(directorService, targetMember,
			LocalDateTime.now());
		ServiceRequest expiredRequest = serviceRequestProvider.saveWithIsExpiredTrue(directorService, targetMember,
			LocalDateTime.now());
		ServiceRequest canceledRequest = serviceRequestProvider.saveWithIsCanceledTrue(directorService, targetMember,
			LocalDateTime.now());

		// otherMember의 서비스 요청 생성 (조회 결과에 포함되면 안됨)
		ServiceRequest otherPendingRequest = serviceRequestProvider.savePending(directorService, otherMember);
		ServiceRequest otherCompletedRequest = serviceRequestProvider.saveWithIsCompletedTrue(directorService,
			otherMember,
			LocalDateTime.now());

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/members/{targetMemberId}/profile-summary", targetMember.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		// then
		MemberProfileSummaryResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), MemberProfileSummaryResponseForDirector.class);

		assertThat(response.getRequestCount()).isEqualTo(7); // targetMember의 전체 7개만 포함
		assertThat(response.getCompletedServiceCount()).isEqualTo(2); // targetMember의 완료 2개
		assertThat(response.getMatchingRate()).isEqualTo(42); // (1 + 2) / 7 * 100 = 42.857... -> 42
	}

	@Test
	@DisplayName("디렉터는 회원의 프로필 요약 정보를 조회할 수 있다. (회원이 아무런 요청를 보내지 않았을때)")
	void findProfileSummaryWithNoServiceRequest() throws Exception {
		// given
		Member targetMember = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member director = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/members/{targetMemberId}/profile-summary", targetMember.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		// then
		MemberProfileSummaryResponseForDirector response = objectMapper.readValue(
			result.getResponse().getContentAsString(), MemberProfileSummaryResponseForDirector.class);

		assertThat(response.getRequestCount()).isEqualTo(0);
		assertThat(response.getCompletedServiceCount()).isEqualTo(0);
		assertThat(response.getMatchingRate()).isEqualTo(0);
	}

	@Test
	@DisplayName("디렉터는 서비스 요청이 없는 회원의 프로필 요약 정보를 조회할 수 있다.(권한이 없을때)")
	void findProfileSummaryWithMemberRole() throws Exception {
		// given
		Member targetMember = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member otherMember = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(otherMember.getId());

		entityManager.flush();
		entityManager.clear();

		// when && then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/directors/members/{targetMemberId}/profile-summary", targetMember.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.FORBIDDEN.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(HandlerException.FORBIDDEN.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.FORBIDDEN.getCode()));
	}
}
