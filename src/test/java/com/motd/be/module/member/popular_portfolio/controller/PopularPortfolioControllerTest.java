package com.motd.be.module.member.popular_portfolio.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.portfolio.dto.response.PopularPortfolioFindRandomResponse;
import com.motd.be.module.member.portfolio.dto.response.PopularPortfolioResponse;
import com.motd.be.module.member.portfolio.entity.Portfolio;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class PopularPortfolioControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("인기 포트폴리오 랜덤 조회 - 비로그인")
	void findRandomWithAnonymous() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService childService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);

		Portfolio portfolio1 = portfolioProvider.save(childService, directorInfo);
		Portfolio portfolio2 = portfolioProvider.save(childService, directorInfo);
		Portfolio portfolio3 = portfolioProvider.save(childService, directorInfo);
		Portfolio portfolio4 = portfolioProvider.save(childService, directorInfo);
		Portfolio portfolio5 = portfolioProvider.save(childService, directorInfo);

		portfolioFileProvider.save(director, portfolio1, Boolean.TRUE);
		portfolioFileProvider.save(director, portfolio2, Boolean.TRUE);
		portfolioFileProvider.save(director, portfolio3, Boolean.TRUE);
		portfolioFileProvider.save(director, portfolio4, Boolean.TRUE);
		portfolioFileProvider.save(director, portfolio5, Boolean.TRUE);

		popularPortfolioProvider.save(portfolio1);
		popularPortfolioProvider.save(portfolio2);
		popularPortfolioProvider.save(portfolio3);
		popularPortfolioProvider.save(portfolio4);
		popularPortfolioProvider.save(portfolio5);

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolios/popular")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		PopularPortfolioFindRandomResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(), PopularPortfolioFindRandomResponse.class);

		assertThat(response.getPortfolios()).hasSize(4);
	}

	@Test
	@DisplayName("인기 포트폴리오 랜덤 조회 - 4개 미만일 때 있는 만큼만 반환")
	void findRandomWithLessThanFour() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService childService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);

		Portfolio portfolio1 = portfolioProvider.save(childService, directorInfo);
		Portfolio portfolio2 = portfolioProvider.save(childService, directorInfo);

		portfolioFileProvider.save(director, portfolio1, Boolean.TRUE);
		portfolioFileProvider.save(director, portfolio2, Boolean.TRUE);

		popularPortfolioProvider.save(portfolio1);
		popularPortfolioProvider.save(portfolio2);

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolios/popular")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		PopularPortfolioFindRandomResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(), PopularPortfolioFindRandomResponse.class);

		assertThat(response.getPortfolios()).hasSize(2);
	}

	@Test
	@DisplayName("인기 포트폴리오 랜덤 조회 - 삭제된 포트폴리오 제외")
	void findRandomExcludesDeletedPortfolios() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService childService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);

		Portfolio activePortfolio = portfolioProvider.save(childService, directorInfo);
		Portfolio deletedPortfolio = portfolioProvider.saveIsDeletedTrue(childService, directorInfo);

		portfolioFileProvider.save(director, activePortfolio, Boolean.TRUE);

		popularPortfolioProvider.save(activePortfolio);
		popularPortfolioProvider.save(deletedPortfolio);

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolios/popular")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		PopularPortfolioFindRandomResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(), PopularPortfolioFindRandomResponse.class);

		assertThat(response.getPortfolios()).hasSize(1);
		assertThat(response.getPortfolios().get(0).getPortfolioId()).isEqualTo(activePortfolio.getId());
	}

	@Test
	@DisplayName("인기 포트폴리오 랜덤 조회 - 차단한 디렉터의 포트폴리오 제외")
	void findRandomExcludesBlockedDirectorPortfolios() throws Exception {
		// given
		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo2);

		Member requestMember = memberProvider.saveMember(SignInPlatform.KAKAO);

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService childService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);

		Portfolio portfolio1 = portfolioProvider.save(childService, directorInfo1);
		Portfolio portfolio2 = portfolioProvider.save(childService, directorInfo2);

		portfolioFileProvider.save(director1, portfolio1, Boolean.TRUE);
		portfolioFileProvider.save(director2, portfolio2, Boolean.TRUE);

		popularPortfolioProvider.save(portfolio1);
		popularPortfolioProvider.save(portfolio2);

		// requestMember가 director1을 차단
		memberBlockProvider.save(requestMember, director1);

		Jwt jwt = generateTokenWithMemberIdRoleMember(requestMember.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolios/popular")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		PopularPortfolioFindRandomResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(), PopularPortfolioFindRandomResponse.class);

		assertThat(response.getPortfolios()).hasSize(1);
		assertThat(response.getPortfolios().get(0).getPortfolioId()).isEqualTo(portfolio2.getId());
	}

	@Test
	@DisplayName("인기 포트폴리오 랜덤 조회 - 파일이 여러 개인 포트폴리오도 중복 없이 반환")
	void findRandomWithMultipleFilesNoDuplicates() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService childService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);

		Portfolio portfolio1 = portfolioProvider.save(childService, directorInfo);
		Portfolio portfolio2 = portfolioProvider.save(childService, directorInfo);

		// portfolio1에 파일 3개 저장
		portfolioFileProvider.save(director, portfolio1, Boolean.TRUE);
		portfolioFileProvider.save(director, portfolio1, Boolean.FALSE);
		portfolioFileProvider.save(director, portfolio1, Boolean.FALSE);

		// portfolio2에 파일 2개 저장
		portfolioFileProvider.save(director, portfolio2, Boolean.TRUE);
		portfolioFileProvider.save(director, portfolio2, Boolean.FALSE);

		popularPortfolioProvider.save(portfolio1);
		popularPortfolioProvider.save(portfolio2);

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolios/popular")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		PopularPortfolioFindRandomResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(), PopularPortfolioFindRandomResponse.class);

		assertThat(response.getPortfolios()).hasSize(2);
		assertThat(response.getPortfolios())
			.extracting(PopularPortfolioResponse::getPortfolioId)
			.doesNotHaveDuplicates()
			.containsExactlyInAnyOrder(portfolio1.getId(), portfolio2.getId());
	}

	@Test
	@DisplayName("인기 포트폴리오 랜덤 조회 - 등록된 인기 포트폴리오가 없을 때 빈 목록 반환")
	void findRandomWithNoPopularPortfolios() throws Exception {
		// given - no popular portfolios

		// when & then
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolios/popular")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		PopularPortfolioFindRandomResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(), PopularPortfolioFindRandomResponse.class);

		assertThat(response.getPortfolios()).isEmpty();
	}
}
