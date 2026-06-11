package com.motd.be.module.admin.portfolio.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.admin.AdminTokenProvider.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.PortfolioException;
import com.motd.be.module.admin.admin.entity.Admin;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.portfolio.entity.Portfolio;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class PortfolioControllerForAdminTest extends BaseIntegrationTest {

	@Test
	@DisplayName("관리자 포트폴리오 상세 조회 API - 존재하지 않는 포트폴리오")
	void findDetail_notFound() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/portfolios/{portfolioId}", 999999L)
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath(ERROR_STATUS).value(PortfolioException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(PortfolioException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(PortfolioException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("관리자 인기 포트폴리오 지정 API - 이미 인기 포트폴리오")
	void markAsPopular_alreadyPopular() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService childService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);

		Portfolio portfolio = portfolioProvider.save(childService, directorInfo);
		popularPortfolioProvider.save(portfolio);

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/admin/portfolios/{portfolioId}/popular", portfolio.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isConflict())
			.andExpect(jsonPath(ERROR_STATUS).value(PortfolioException.ALREADY_POPULAR.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(PortfolioException.ALREADY_POPULAR.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(PortfolioException.ALREADY_POPULAR.getCode()));
	}

	@Test
	@DisplayName("관리자 인기 포트폴리오 해제 API - 인기 포트폴리오가 아닌 경우")
	void unmarkAsPopular_notPopular() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService childService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);

		Portfolio portfolio = portfolioProvider.save(childService, directorInfo);

		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		entityManager.flush();
		entityManager.clear();

		mockMvc.perform(
				MockMvcRequestBuilders.delete("/api/admin/portfolios/{portfolioId}/popular", portfolio.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isConflict())
			.andExpect(jsonPath(ERROR_STATUS).value(PortfolioException.NOT_POPULAR.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(PortfolioException.NOT_POPULAR.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(PortfolioException.NOT_POPULAR.getCode()));
	}

	@Test
	@DisplayName("관리자 인기 포트폴리오 지정 API - 존재하지 않는 포트폴리오")
	void markAsPopular_notFound() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/admin/portfolios/{portfolioId}/popular", 999999L)
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath(ERROR_STATUS).value(PortfolioException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(PortfolioException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(PortfolioException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("관리자 인기 포트폴리오 해제 API - 존재하지 않는 포트폴리오")
	void unmarkAsPopular_notFound() throws Exception {
		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwt = generateAdminTokenWithAdminId(admin.getId());

		mockMvc.perform(
				MockMvcRequestBuilders.delete("/api/admin/portfolios/{portfolioId}/popular", 999999L)
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath(ERROR_STATUS).value(PortfolioException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(PortfolioException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(PortfolioException.NOT_FOUND.getCode()));
	}
}
