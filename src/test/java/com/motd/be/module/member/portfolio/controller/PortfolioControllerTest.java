package com.motd.be.module.member.portfolio.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.Constants.TARGET_MEMBER_ID;
import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
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
import com.motd.be.exception.exceptions.MemberBlockException;
import com.motd.be.exception.exceptions.PortfolioException;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.entity.LocationType;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.portfolio.dto.response.PortfolioFindAllResponse;
import com.motd.be.module.member.portfolio.dto.response.PortfolioFindDetailResponse;
import com.motd.be.module.member.portfolio.dto.response.PortfolioResponse;
import com.motd.be.module.member.portfolio.entity.Portfolio;
import com.motd.be.module.member.portfolio.entity.PortfolioSortType;
import com.motd.be.module.member.portfolio_file.entity.PortfolioFile;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class PortfolioControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("포트폴리오 목록을 조회할 수 있다. (비로그인)")
	void findAllWithAnonymousMember() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);

		// 포트폴리오 3개 저장
		Portfolio portfolio1 = portfolioProvider.save(directorService2, directorInfo);
		Portfolio portfolio2 = portfolioProvider.save(directorService3, directorInfo);
		Portfolio portfolio3 = portfolioProvider.save(directorService2, directorInfo);
		Portfolio portfolio4 = portfolioProvider.saveIsDeletedTrue(directorService2, directorInfo);

		// 각 포트폴리오에 이미지 저장
		PortfolioFile image1 = portfolioFileProvider.save(member, portfolio1, Boolean.TRUE);
		PortfolioFile image2 = portfolioFileProvider.save(member, portfolio1, Boolean.FALSE);

		PortfolioFile image3 = portfolioFileProvider.save(member, portfolio2, Boolean.TRUE);

		PortfolioFile image4 = portfolioFileProvider.save(member, portfolio3, Boolean.TRUE);

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolios")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		PortfolioFindAllResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			PortfolioFindAllResponse.class);

		assertThat(response.getPortfolios()).hasSize(3);
		assertThat(response.getPortfolios()).extracting(ID_STR)
			.containsExactlyInAnyOrder(portfolio1.getId(), portfolio2.getId(), portfolio3.getId());
	}

	@Test
	@DisplayName("포트폴리오 목록을 조회할 수 있다. (제외할 포트폴리오가 존재할떄)")
	void findAllWhenExcludePortfolioIdExist() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);

		// 포트폴리오 3개 저장
		Portfolio portfolio1 = portfolioProvider.save(directorService2, directorInfo);
		Portfolio portfolio2 = portfolioProvider.save(directorService3, directorInfo);
		Portfolio portfolio3 = portfolioProvider.save(directorService2, directorInfo);
		Portfolio portfolio4 = portfolioProvider.saveIsDeletedTrue(directorService2, directorInfo);

		// 각 포트폴리오에 이미지 저장
		PortfolioFile image1 = portfolioFileProvider.save(member, portfolio1, Boolean.TRUE);
		PortfolioFile image2 = portfolioFileProvider.save(member, portfolio1, Boolean.FALSE);

		PortfolioFile image3 = portfolioFileProvider.save(member, portfolio2, Boolean.TRUE);

		PortfolioFile image4 = portfolioFileProvider.save(member, portfolio3, Boolean.TRUE);

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolios")
				.contentType(MediaType.APPLICATION_JSON)
				.param(EXCLUDE_PORTFOLIO_ID, portfolio2.getId().toString()))
			.andExpect(status().isOk())
			.andReturn();

		PortfolioFindAllResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			PortfolioFindAllResponse.class);

		assertThat(response.getPortfolios()).hasSize(2);
		assertThat(response.getPortfolios()).extracting(ID_STR)
			.containsExactlyInAnyOrder(portfolio1.getId(), portfolio3.getId());
	}

	@Test
	@DisplayName("포트폴리오 목록을 조회할 수 있다. (커서 기반 페이징 - 첫번째 페이지)")
	void findAllWithCursorPagingFirstPage() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location districtLocation1 = locationProvider.saveWithParent(LOCATION_NAME_1_STR, LocationType.DISTRICT,
			cityLocation);
		Location districtLocation2 = locationProvider.saveWithParent(LOCATION_NAME_1_STR, LocationType.DISTRICT,
			cityLocation);

		directorLocationMappingProvider.save(directorInfo, districtLocation1);
		directorLocationMappingProvider.save(directorInfo, districtLocation2);

		// 총 25개의 포트폴리오 저장
		for (int i = 0; i < 25; i++) {
			Portfolio portfolio = portfolioProvider.save(directorService2, directorInfo);
			portfolioFileProvider.save(member, portfolio, Boolean.TRUE);
		}

		entityManager.flush();
		entityManager.clear();

		// when & then - 첫번째 페이지 (cursorId 없이)
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolios")
				.param(DIRECTOR_SERVICE_ID_STR, directorService1.getId().toString())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		// then
		PortfolioFindAllResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			PortfolioFindAllResponse.class);

		assertThat(response.getPortfolios()).hasSize(20);
		assertThat(response.getHasNext()).isTrue();
	}

	@Test
	@DisplayName("포트폴리오 목록을 조회할 수 있다. (커서 기반 페이징 - 두 번째 페이지)")
	void findAllWithCursorPagingSecondPage() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location districtLocation1 = locationProvider.saveWithParent(LOCATION_NAME_1_STR, LocationType.DISTRICT,
			cityLocation);
		Location districtLocation2 = locationProvider.saveWithParent(LOCATION_NAME_1_STR, LocationType.DISTRICT,
			cityLocation);

		directorLocationMappingProvider.save(directorInfo, districtLocation1);
		directorLocationMappingProvider.save(directorInfo, districtLocation2);

		// 총 25개의 포트폴리오 저장
		for (int i = 0; i < 25; i++) {
			Portfolio portfolio = portfolioProvider.save(directorService1, directorInfo);
			portfolioFileProvider.save(member, portfolio, Boolean.TRUE);
		}

		entityManager.flush();
		entityManager.clear();

		// when - 첫 번째 페이지 조회 후 마지막 포트폴리오 ID를 커서로 사용
		MvcResult firstResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolios")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(DIRECTOR_SERVICE_ID_STR, directorService1.getId().toString())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		PortfolioFindAllResponse firstResponse = objectMapper.readValue(
			firstResult.getResponse().getContentAsString(), PortfolioFindAllResponse.class);
		Long lastPortfolioId = firstResponse.getPortfolios()
			.get(firstResponse.getPortfolios().size() - 1).getId();

		// when - 두 번째 페이지 (cursorId 사용)
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolios")
				.param(CURSOR_ID, lastPortfolioId.toString())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.param(DIRECTOR_SERVICE_ID_STR, directorService1.getId().toString())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		// then
		PortfolioFindAllResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			PortfolioFindAllResponse.class);

		assertThat(response.getPortfolios()).hasSize(5);
		assertThat(response.getHasNext()).isFalse();
	}

	@Test
	@DisplayName("포트폴리오 목록을 조회할 수 있다. (고용순으로 필터링)")
	void findAllFilteredByCompletedEstimateCount() throws Exception {
		// given
		DirectorInfo directorInfo1 = directorInfoProvider.saveWithCompletedCount(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			10);
		Member director1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithCompletedCount(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			7);
		Member director2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo2);

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo1, cityLocation);
		directorLocationMappingProvider.save(directorInfo2, cityLocation);

		// 포트폴리오 3개 저장
		Portfolio portfolio1 = portfolioProvider.save(directorService2, directorInfo1);
		Portfolio portfolio2 = portfolioProvider.save(directorService2, directorInfo2);

		// 각 포트폴리오에 이미지 저장
		PortfolioFile image1 = portfolioFileProvider.save(director2, portfolio1, Boolean.TRUE);
		PortfolioFile image2 = portfolioFileProvider.save(director2, portfolio1, Boolean.FALSE);

		PortfolioFile image3 = portfolioFileProvider.save(director2, portfolio2, Boolean.TRUE);

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolios")
				.contentType(MediaType.APPLICATION_JSON)
				.param(SORT_TYPE, PortfolioSortType.MOST_HIRED.name()))
			.andExpect(status().isOk())
			.andReturn();

		PortfolioFindAllResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			PortfolioFindAllResponse.class);

		assertThat(response.getPortfolios()).hasSize(2);
		assertThat(response.getPortfolios()).extracting(ID_STR)
			.containsExactlyInAnyOrder(portfolio1.getId(), portfolio2.getId());
	}

	@Test
	@DisplayName("포트폴리오 목록을 조회할 수 있다. (고용순 커서 페이지네이션, 동점 tie-break)")
	void findAllWithCursorPagingMostHired() throws Exception {
		// given
		// 동일한 completedEstimateCount(=10)를 가진 디렉터 2명으로 포트폴리오 21개 생성 → 2페이지 발생
		DirectorInfo directorInfo1 = directorInfoProvider.saveWithCompletedCount(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			10);
		Member director1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithCompletedCount(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			10);
		Member director2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo2);

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_1_STR, parentService);

		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		directorLocationMappingProvider.save(directorInfo1, cityLocation);
		directorLocationMappingProvider.save(directorInfo2, cityLocation);

		for (int i = 0; i < 11; i++) {
			Portfolio p = portfolioProvider.save(directorService, directorInfo1);
			portfolioFileProvider.save(director1, p, Boolean.TRUE);
		}
		for (int i = 0; i < 10; i++) {
			Portfolio p = portfolioProvider.save(directorService, directorInfo2);
			portfolioFileProvider.save(director2, p, Boolean.TRUE);
		}

		entityManager.flush();
		entityManager.clear();

		// when - 1페이지: 커서 없이 고용순 조회
		MvcResult firstResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolios")
				.contentType(MediaType.APPLICATION_JSON)
				.param(SORT_TYPE, PortfolioSortType.MOST_HIRED.name()))
			.andExpect(status().isOk())
			.andReturn();

		PortfolioFindAllResponse firstResponse = objectMapper.readValue(
			firstResult.getResponse().getContentAsString(), PortfolioFindAllResponse.class);
		List<Long> firstPageIds = firstResponse.getPortfolios().stream()
			.map(PortfolioResponse::getId)
			.toList();
		Long lastPortfolioId = firstPageIds.get(firstPageIds.size() - 1);

		// when - 2페이지: 커서 사용
		MvcResult secondResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolios")
				.param(CURSOR_ID, lastPortfolioId.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.param(SORT_TYPE, PortfolioSortType.MOST_HIRED.name()))
			.andExpect(status().isOk())
			.andReturn();

		PortfolioFindAllResponse secondResponse = objectMapper.readValue(
			secondResult.getResponse().getContentAsString(), PortfolioFindAllResponse.class);
		List<Long> secondPageIds = secondResponse.getPortfolios().stream()
			.map(PortfolioResponse::getId)
			.toList();

		// then - 1페이지와 2페이지에 중복이 없고, 합치면 전체 21개
		assertThat(firstPageIds).hasSize(20);
		assertThat(secondPageIds).hasSize(1);
		assertThat(firstPageIds).doesNotContainAnyElementsOf(secondPageIds);
		assertThat(secondResponse.getHasNext()).isFalse();
	}

	@Test
	@DisplayName("포트폴리오 목록을 조회할 수 있다.(디렉터가 사용자를 차단한 경우)")
	void findAll_blockedMemberCannotSeePortfolios() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, otherDirectorInfo);

		Member blockedMember = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(blockedMember.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);
		directorLocationMappingProvider.save(otherDirectorInfo, cityLocation);

		Portfolio portfolio = portfolioProvider.save(directorService, directorInfo);
		portfolioFileProvider.save(director, portfolio, Boolean.TRUE);

		Portfolio otherPortfolio = portfolioProvider.save(directorService, otherDirectorInfo);
		portfolioFileProvider.save(otherDirector, portfolio, Boolean.TRUE);

		memberBlockProvider.save(director, blockedMember);

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolios")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		PortfolioFindAllResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			PortfolioFindAllResponse.class);

		assertThat(response.getPortfolios().size()).isEqualTo(1);
		assertThat(response.getPortfolios().get(0).getId()).isEqualTo(otherPortfolio.getId());
	}

	@Test
	@DisplayName("포트폴리오 목록을 조회할 수 있다.(사용자가 디렉터를 차단한 경우)")
	void findAll_memberBlockDirectorCannotSeePortfolios() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member blockedDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorInfo otherDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member otherDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, otherDirectorInfo);

		Member blockerMember = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleMember(blockerMember.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);
		directorLocationMappingProvider.save(otherDirectorInfo, cityLocation);

		Portfolio portfolio = portfolioProvider.save(directorService, directorInfo);
		portfolioFileProvider.save(blockedDirector, portfolio, Boolean.TRUE);

		Portfolio otherPortfolio = portfolioProvider.save(directorService, otherDirectorInfo);
		portfolioFileProvider.save(otherDirector, portfolio, Boolean.TRUE);

		memberBlockProvider.save(blockerMember, blockedDirector);

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolios")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		PortfolioFindAllResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			PortfolioFindAllResponse.class);

		assertThat(response.getPortfolios().size()).isEqualTo(1);
		assertThat(response.getPortfolios().get(0).getId()).isEqualTo(otherPortfolio.getId());
	}

	@Test
	@DisplayName("포트폴리오 목록을 조회할 수 있다. (로그인)")
	void findAllWithSignInMember() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);

		// 포트폴리오 3개 저장
		Portfolio portfolio1 = portfolioProvider.save(directorService2, directorInfo);
		Portfolio portfolio2 = portfolioProvider.save(directorService3, directorInfo);
		Portfolio portfolio3 = portfolioProvider.save(directorService2, directorInfo);
		Portfolio portfolio4 = portfolioProvider.saveIsDeletedTrue(directorService2, directorInfo);

		// 각 포트폴리오에 이미지 저장
		PortfolioFile image1 = portfolioFileProvider.save(member, portfolio1, Boolean.TRUE);
		PortfolioFile image2 = portfolioFileProvider.save(member, portfolio1, Boolean.FALSE);

		PortfolioFile image3 = portfolioFileProvider.save(member, portfolio2, Boolean.TRUE);

		PortfolioFile image4 = portfolioFileProvider.save(member, portfolio3, Boolean.TRUE);

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolios")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		PortfolioFindAllResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			PortfolioFindAllResponse.class);

		assertThat(response.getPortfolios()).hasSize(3);
		assertThat(response.getPortfolios()).extracting(ID_STR)
			.containsExactlyInAnyOrder(portfolio1.getId(), portfolio2.getId(), portfolio3.getId());
	}

	@Test
	@DisplayName("포트폴리오 목록을 조회할 수 있다. (특정 디렉터 서비스 ID로 필터링)")
	void findAllWithDirectorServiceIdFilter() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);

		// 서로 다른 서비스로 포트폴리오 저장
		Portfolio portfolio1 = portfolioProvider.save(directorService2, directorInfo);
		Portfolio portfolio2 = portfolioProvider.save(directorService3, directorInfo);
		Portfolio portfolio3 = portfolioProvider.save(directorService2, directorInfo);

		PortfolioFile image1 = portfolioFileProvider.save(member, portfolio1, Boolean.TRUE);
		PortfolioFile image2 = portfolioFileProvider.save(member, portfolio2, Boolean.TRUE);
		PortfolioFile image3 = portfolioFileProvider.save(member, portfolio3, Boolean.TRUE);

		entityManager.flush();
		entityManager.clear();

		// when & then - 첫 번째 서비스 ID로 필터링
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolios")
				.param(DIRECTOR_SERVICE_ID_STR, directorService2.getId().toString())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		PortfolioFindAllResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			PortfolioFindAllResponse.class);

		assertThat(response.getPortfolios()).hasSize(2);
		assertThat(response.getPortfolios()).extracting(ID_STR)
			.containsExactlyInAnyOrder(portfolio1.getId(), portfolio3.getId());
	}

	@Test
	@DisplayName("포트폴리오 목록을 조회할 수 있다. (특정 상위 디렉터 서비스 ID로 필터링)")
	void findAllFilteredByDirectorServiceWithParentIdNull() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		DirectorService otherParentDirectorService1 = directorCategoryProvider.save(SERVICE_NAME_2_STR, null);
		DirectorService otherService1 = directorCategoryProvider.save(SERVICE_NAME_2_STR, otherParentDirectorService1);

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);

		// 서로 다른 서비스로 포트폴리오 저장
		Portfolio portfolio1 = portfolioProvider.save(directorService2, directorInfo);
		Portfolio portfolio2 = portfolioProvider.save(directorService3, directorInfo);
		Portfolio portfolio3 = portfolioProvider.save(directorService2, directorInfo);

		PortfolioFile image1 = portfolioFileProvider.save(member, portfolio1, Boolean.TRUE);
		PortfolioFile image2 = portfolioFileProvider.save(member, portfolio2, Boolean.TRUE);
		PortfolioFile image3 = portfolioFileProvider.save(member, portfolio3, Boolean.TRUE);

		Portfolio otherPortfolio1 = portfolioProvider.save(otherService1, directorInfo);
		Portfolio otherPortfolio2 = portfolioProvider.save(otherService1, directorInfo);

		entityManager.flush();
		entityManager.clear();

		// when & then - 첫 번째 서비스 ID로 필터링
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolios")
				.param(DIRECTOR_SERVICE_ID_STR, directorService1.getId().toString())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		PortfolioFindAllResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			PortfolioFindAllResponse.class);

		assertThat(response.getPortfolios()).hasSize(3);
		assertThat(response.getPortfolios()).extracting(ID_STR)
			.containsExactlyInAnyOrder(portfolio1.getId(), portfolio2.getId(), portfolio3.getId());
	}

	@Test
	@DisplayName("포트폴리오 목록을 조회할 수 있다. (특정 회원 ID로 필터링)")
	void findAllWithTargetMemberIdFilter() throws Exception {
		// given
		DirectorInfo directorInfo1 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member1 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo1);

		DirectorInfo directorInfo2 = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member2 = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo2);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(member1.getId());

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo1, cityLocation);
		directorLocationMappingProvider.save(directorInfo2, cityLocation);

		// 각각 다른 회원의 포트폴리오 저장
		Portfolio portfolio1 = portfolioProvider.save(directorService2, directorInfo1);
		Portfolio portfolio2 = portfolioProvider.save(directorService2, directorInfo2);
		Portfolio portfolio3 = portfolioProvider.save(directorService2, directorInfo1);

		PortfolioFile image1 = portfolioFileProvider.save(member1, portfolio1, Boolean.TRUE);
		PortfolioFile image2 = portfolioFileProvider.save(member2, portfolio2, Boolean.TRUE);
		PortfolioFile image3 = portfolioFileProvider.save(member1, portfolio3, Boolean.TRUE);

		entityManager.flush();
		entityManager.clear();

		// when & then - member1의 포트폴리오만 조회
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolios")
				.param(TARGET_MEMBER_ID, member1.getId().toString())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		PortfolioFindAllResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			PortfolioFindAllResponse.class);

		assertThat(response.getPortfolios()).hasSize(2);
		assertThat(response.getPortfolios()).extracting(ID_STR)
			.containsExactlyInAnyOrder(portfolio1.getId(), portfolio3.getId());
	}

	@Test
	@DisplayName("포트폴리오 목록을 조회할 수 있다. (커서 기반 페이징)")
	void findAllWithCursorPaging() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);

		// 많은 포트폴리오 저장 (페이지 크기보다 많이)
		for (int i = 0; i < 25; i++) {
			Portfolio portfolio = portfolioProvider.save(directorService2, directorInfo);
			portfolioFileProvider.save(member, portfolio, Boolean.TRUE);
		}

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolios")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		// then
		PortfolioFindAllResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			PortfolioFindAllResponse.class);

		assertThat(response.getHasNext()).isTrue();
	}

	@Test
	@DisplayName("포트폴리오 목록을 조회할 수 있다. (사용자가 전국으로 검색할때)")
	void findAllWhenMemberSearchesAllCity() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member allCityMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorInfo cityDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member cityMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, cityDirectorInfo);

		DirectorInfo districtDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member districtMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, districtDirectorInfo);

		DirectorInfo independentCityDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member independentCityMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			independentCityDirectorInfo);

		DirectorInfo independentDistrictDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member independentDistrictMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			independentDistrictDirectorInfo);

		//1. 위치 저장
		Location allCityLocation = locationProvider.save(CITY_STR, LocationType.ALL_CITY);
		Location cityLocation = locationProvider.save(CITY_STR, LocationType.CITY);
		Location districtLocation = locationProvider.saveWithParent(DISTRICT_STR, LocationType.DISTRICT,
			cityLocation);
		Location independentCityLocation = locationProvider.save(CITY_STR, LocationType.CITY);
		Location independentDistrictLocation = locationProvider.saveWithParent(DISTRICT_STR, LocationType.DISTRICT,
			independentCityLocation);

		//2. 디렉터 위치 설정
		directorLocationMappingProvider.save(directorInfo, allCityLocation);
		directorLocationMappingProvider.save(cityDirectorInfo, cityLocation);
		directorLocationMappingProvider.save(districtDirectorInfo, districtLocation);
		directorLocationMappingProvider.save(independentCityDirectorInfo, independentCityLocation);
		directorLocationMappingProvider.save(independentDistrictDirectorInfo, independentDistrictLocation);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(allCityMember.getId());

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		Portfolio allCityPortfolio = portfolioProvider.save(directorService2, directorInfo);
		portfolioFileProvider.save(allCityMember, allCityPortfolio, Boolean.TRUE);

		Portfolio cityPortfolio = portfolioProvider.save(directorService2, cityDirectorInfo);
		portfolioFileProvider.save(cityMember, cityPortfolio, Boolean.TRUE);

		Portfolio districtPortfolio = portfolioProvider.save(directorService2, districtDirectorInfo);
		portfolioFileProvider.save(districtMember, districtPortfolio, Boolean.TRUE);

		Portfolio independentCityPortfolio = portfolioProvider.save(directorService2,
			independentCityDirectorInfo);
		portfolioFileProvider.save(independentCityMember, independentCityPortfolio, Boolean.TRUE);

		Portfolio independentDistrictPortfolio = portfolioProvider.save(directorService2,
			independentDistrictDirectorInfo);
		portfolioFileProvider.save(independentDistrictMember, independentDistrictPortfolio, Boolean.TRUE);

		entityManager.flush();
		entityManager.clear();

		// when & then - 위치 필터링 (locationId가 있는 경우)
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolios")
				.param(LOCATION_ID_STR, allCityLocation.getId().toString())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		// then
		PortfolioFindAllResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			PortfolioFindAllResponse.class);

		List<PortfolioResponse> portfolios = response.getPortfolios();
		assertThat(portfolios.size()).isEqualTo(5);
	}

	@Test
	@DisplayName("포트폴리오 목록을 조회할 수 있다. (사용자가 특정 지역 전체로 검색 할때)")
	void findAllWhenMemberSearchesSpecificCity() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member allCityMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorInfo cityDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member cityMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, cityDirectorInfo);

		DirectorInfo districtDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member districtMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, districtDirectorInfo);

		DirectorInfo independentCityDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member independentCityMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			independentCityDirectorInfo);

		DirectorInfo independentDistrictDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member independentDistrictMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			independentDistrictDirectorInfo);

		//1. 위치 저장
		Location allCityLocation = locationProvider.save(CITY_STR, LocationType.ALL_CITY);
		Location cityLocation = locationProvider.save(CITY_STR, LocationType.CITY);
		Location districtLocation = locationProvider.saveWithParent(DISTRICT_STR, LocationType.DISTRICT,
			cityLocation);
		Location independentCityLocation = locationProvider.save(CITY_STR, LocationType.CITY);
		Location independentDistrictLocation = locationProvider.saveWithParent(DISTRICT_STR, LocationType.DISTRICT,
			independentCityLocation);

		//2. 디렉터 위치 설정
		directorLocationMappingProvider.save(directorInfo, allCityLocation);
		directorLocationMappingProvider.save(cityDirectorInfo, cityLocation);
		directorLocationMappingProvider.save(districtDirectorInfo, districtLocation);
		directorLocationMappingProvider.save(independentCityDirectorInfo, independentCityLocation);
		directorLocationMappingProvider.save(independentDistrictDirectorInfo, independentDistrictLocation);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(allCityMember.getId());

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		Portfolio allCityPortfolio = portfolioProvider.save(directorService2, directorInfo);
		portfolioFileProvider.save(allCityMember, allCityPortfolio, Boolean.TRUE);

		Portfolio cityPortfolio = portfolioProvider.save(directorService2, cityDirectorInfo);
		portfolioFileProvider.save(cityMember, cityPortfolio, Boolean.TRUE);

		Portfolio districtPortfolio = portfolioProvider.save(directorService2, districtDirectorInfo);
		portfolioFileProvider.save(districtMember, districtPortfolio, Boolean.TRUE);

		Portfolio independentCityPortfolio = portfolioProvider.save(directorService2,
			independentCityDirectorInfo);
		portfolioFileProvider.save(independentCityMember, independentCityPortfolio, Boolean.TRUE);

		Portfolio independentDistrictPortfolio = portfolioProvider.save(directorService2,
			independentDistrictDirectorInfo);
		portfolioFileProvider.save(independentDistrictMember, independentDistrictPortfolio, Boolean.TRUE);

		entityManager.flush();
		entityManager.clear();

		// when & then - 위치 필터링 (locationId가 있는 경우)
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolios")
				.param(LOCATION_ID_STR, cityLocation.getId().toString())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		// then
		PortfolioFindAllResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			PortfolioFindAllResponse.class);

		List<PortfolioResponse> portfolios = response.getPortfolios();
		assertThat(portfolios.size()).isEqualTo(3);

		assertThat(portfolios).extracting(ID_STR)
			.containsExactlyInAnyOrder(allCityPortfolio.getId(), cityPortfolio.getId(), districtPortfolio.getId());
	}

	@Test
	@DisplayName("포트폴리오 목록을 조회할 수 있다. (사용자가 특정 지역 의 특정 구로 검색 할때)")
	void findAllWhenMemberSearchesSpecificDistrict() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member allCityMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		DirectorInfo cityDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member cityMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, cityDirectorInfo);

		DirectorInfo districtDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member districtMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, districtDirectorInfo);

		DirectorInfo independentCityDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member independentCityMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			independentCityDirectorInfo);

		DirectorInfo independentDistrictDirectorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR,
			STORE_ADDRESS_STR, LocalDate.now());
		Member independentDistrictMember = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO,
			independentDistrictDirectorInfo);

		//1. 위치 저장
		Location allCityLocation = locationProvider.save(CITY_STR, LocationType.ALL_CITY);
		Location cityLocation = locationProvider.save(CITY_STR, LocationType.CITY);
		Location districtLocation = locationProvider.saveWithParent(DISTRICT_STR, LocationType.DISTRICT,
			cityLocation);
		Location independentCityLocation = locationProvider.save(CITY_STR, LocationType.CITY);
		Location independentDistrictLocation = locationProvider.saveWithParent(DISTRICT_STR, LocationType.DISTRICT,
			independentCityLocation);

		//2. 디렉터 위치 설정
		directorLocationMappingProvider.save(directorInfo, allCityLocation);
		directorLocationMappingProvider.save(cityDirectorInfo, cityLocation);
		directorLocationMappingProvider.save(districtDirectorInfo, districtLocation);
		directorLocationMappingProvider.save(independentCityDirectorInfo, independentCityLocation);
		directorLocationMappingProvider.save(independentDistrictDirectorInfo, independentDistrictLocation);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(allCityMember.getId());

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		Portfolio allCityPortfolio = portfolioProvider.save(directorService2, directorInfo);
		portfolioFileProvider.save(allCityMember, allCityPortfolio, Boolean.TRUE);

		Portfolio cityPortfolio = portfolioProvider.save(directorService2, cityDirectorInfo);
		portfolioFileProvider.save(cityMember, cityPortfolio, Boolean.TRUE);

		Portfolio districtPortfolio = portfolioProvider.save(directorService2, districtDirectorInfo);
		portfolioFileProvider.save(districtMember, districtPortfolio, Boolean.TRUE);

		Portfolio independentCityPortfolio = portfolioProvider.save(directorService2,
			independentCityDirectorInfo);
		portfolioFileProvider.save(independentCityMember, independentCityPortfolio, Boolean.TRUE);

		Portfolio independentDistrictPortfolio = portfolioProvider.save(directorService2,
			independentDistrictDirectorInfo);
		portfolioFileProvider.save(independentDistrictMember, independentDistrictPortfolio, Boolean.TRUE);

		entityManager.flush();
		entityManager.clear();

		// when & then - 위치 필터링 (locationId가 있는 경우)
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolios")
				.param(LOCATION_ID_STR, districtLocation.getId().toString())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		// then
		PortfolioFindAllResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			PortfolioFindAllResponse.class);

		List<PortfolioResponse> portfolios = response.getPortfolios();
		assertThat(portfolios.size()).isEqualTo(3);

		assertThat(portfolios).extracting(ID_STR)
			.containsExactlyInAnyOrder(allCityPortfolio.getId(), cityPortfolio.getId(), districtPortfolio.getId());
	}

	@Test
	@DisplayName("포트폴리오 상세 조회를 할 수 있다.(본인이 작성한 포트폴리오일 경우)")
	void findDetailOwned() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		// member2의 포트폴리오 생성
		Portfolio portfolio = portfolioProvider.save(directorService2, directorInfo);
		PortfolioFile image1 = portfolioFileProvider.saveWithSortOrder(member, portfolio, Boolean.TRUE, 0);
		PortfolioFile image2 = portfolioFileProvider.saveWithSortOrder(member, portfolio, Boolean.TRUE, 2);
		PortfolioFile image3 = portfolioFileProvider.saveWithSortOrder(member, portfolio, Boolean.TRUE, 1);

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/portfolios/{portfolioId}", portfolio.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		// then
		PortfolioFindDetailResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			PortfolioFindDetailResponse.class);

		assertThat(response.getId()).isEqualTo(portfolio.getId());
		assertThat(response.getIsOwner()).isTrue();
		assertThat(response.getFiles().size()).isEqualTo(3);

		// 이미지 순서 검증
		assertThat(response.getFiles().get(0).getId()).isEqualTo(image1.getId());
		assertThat(response.getFiles().get(1).getId()).isEqualTo(image3.getId());
		assertThat(response.getFiles().get(2).getId()).isEqualTo(image2.getId());

		// 서비스 비활성화 상태 검증
		assertThat(response.getIsPortfolioFromActiveService()).isTrue();
	}

	@Test
	@DisplayName("포트폴리오 상세 조회를 할 수 있다.(디렉터가 더이상 제공하지 않는 서비스의 관련된 포트폴리오일경우)")
	void findDetailWithIsPortfolioFromActiveServiceFalse() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.APPLE, directorInfo);

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);

		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		directorServiceMappingProvider.save(directorInfo, directorService2);

		// member2의 포트폴리오 생성
		Portfolio portfolio = portfolioProvider.save(directorService3, directorInfo);
		PortfolioFile image1 = portfolioFileProvider.saveWithSortOrder(member, portfolio, Boolean.TRUE, 0);
		PortfolioFile image2 = portfolioFileProvider.saveWithSortOrder(member, portfolio, Boolean.TRUE, 2);
		PortfolioFile image3 = portfolioFileProvider.saveWithSortOrder(member, portfolio, Boolean.TRUE, 1);

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/portfolios/{portfolioId}", portfolio.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		// then
		PortfolioFindDetailResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			PortfolioFindDetailResponse.class);

		assertThat(response.getId()).isEqualTo(portfolio.getId());
		assertThat(response.getIsOwner()).isTrue();
		assertThat(response.getFiles().size()).isEqualTo(3);

		// 이미지 순서 검증
		assertThat(response.getFiles().get(0).getId()).isEqualTo(image1.getId());
		assertThat(response.getFiles().get(1).getId()).isEqualTo(image3.getId());
		assertThat(response.getFiles().get(2).getId()).isEqualTo(image2.getId());

		// 서비스 비활성화 상태 검증
		assertThat(response.getIsPortfolioFromActiveService()).isFalse();
	}

	@Test
	@DisplayName("포트폴리오 상세 조회를 할 수 있다.(본인이 작성하지 않은 포트폴리오일 경우)")
	void findDetailNotOwned() throws Exception {
		Member otherMember = memberProvider.saveMember(SignInPlatform.APPLE);

		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(otherMember.getId());

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		Portfolio portfolio = portfolioProvider.save(directorService2, directorInfo);
		PortfolioFile image1 = portfolioFileProvider.save(member, portfolio, Boolean.TRUE);
		PortfolioFile image2 = portfolioFileProvider.save(member, portfolio, Boolean.FALSE);
		PortfolioFile image3 = portfolioFileProvider.save(member, portfolio, Boolean.FALSE);

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/portfolios/{portfolioId}", portfolio.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		// then
		PortfolioFindDetailResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			PortfolioFindDetailResponse.class);

		assertThat(response.getId()).isEqualTo(portfolio.getId());
		assertThat(response.getIsOwner()).isFalse();
		assertThat(response.getFiles().size()).isEqualTo(3);
	}

	@Test
	@DisplayName("포트폴리오 상세 조회를 할 수 있다.(로그인 하지 않았을때)")
	void findDetailWithNotSignIn() throws Exception {
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		Portfolio portfolio = portfolioProvider.save(directorService2, directorInfo);
		PortfolioFile image1 = portfolioFileProvider.save(member, portfolio, Boolean.TRUE);
		PortfolioFile image2 = portfolioFileProvider.save(member, portfolio, Boolean.FALSE);
		PortfolioFile image3 = portfolioFileProvider.save(member, portfolio, Boolean.FALSE);

		entityManager.flush();
		entityManager.clear();

		// when & then
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/portfolios/{portfolioId}", portfolio.getId())
					.contentType(MediaType.APPLICATION_JSON))
			.andReturn();

		entityManager.flush();
		entityManager.clear();

		// then
		PortfolioFindDetailResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
			PortfolioFindDetailResponse.class);

		assertThat(response.getId()).isEqualTo(portfolio.getId());
		assertThat(response.getIsOwner()).isFalse();
		assertThat(response.getFiles().size()).isEqualTo(3);
	}

	@Test
	@DisplayName("포트폴리오 상세 조회를 할 수 있다.(디렉터로부터 차단당한 경우)")
	void findDetailWhenBlockedByDirector() throws Exception {
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member blockedMember = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(blockedMember.getId());

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		Portfolio portfolio = portfolioProvider.save(directorService2, directorInfo);
		PortfolioFile image1 = portfolioFileProvider.save(director, portfolio, Boolean.TRUE);
		PortfolioFile image2 = portfolioFileProvider.save(director, portfolio, Boolean.FALSE);
		PortfolioFile image3 = portfolioFileProvider.save(director, portfolio, Boolean.FALSE);

		// 차단 여부 저장
		memberBlockProvider.save(director, blockedMember);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/portfolios/{portfolioId}", portfolio.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.status").value(
				MemberBlockException.BLOCKED_RESOURCE_ACCESS_DENIED.getHttpStatus().toString()))
			.andExpect(
				jsonPath("$.message").value(MemberBlockException.BLOCKED_RESOURCE_ACCESS_DENIED.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(MemberBlockException.BLOCKED_RESOURCE_ACCESS_DENIED.getCode()));
	}

	@Test
	@DisplayName("포트폴리오 상세 조회를 할 수 있다.(디렉터를 차단한 경우)")
	void findDetailWhenBlockedByMember() throws Exception {
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member blockedDirector = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);

		Member blockerMember = memberProvider.saveMember(SignInPlatform.APPLE);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(blockerMember.getId());

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);

		// director service 저장
		DirectorService directorService1 = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService directorService2 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);
		DirectorService directorService3 = directorCategoryProvider.save(SERVICE_NAME_1_STR, directorService1);

		Portfolio portfolio = portfolioProvider.save(directorService2, directorInfo);
		PortfolioFile image1 = portfolioFileProvider.save(blockedDirector, portfolio, Boolean.TRUE);
		PortfolioFile image2 = portfolioFileProvider.save(blockedDirector, portfolio, Boolean.FALSE);
		PortfolioFile image3 = portfolioFileProvider.save(blockedDirector, portfolio, Boolean.FALSE);

		// 차단 여부 저장
		memberBlockProvider.save(blockerMember, blockedDirector);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/portfolios/{portfolioId}", portfolio.getId())
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
					.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.status").value(
				MemberBlockException.BLOCKED_RESOURCE_ACCESS_DENIED.getHttpStatus().toString()))
			.andExpect(
				jsonPath("$.message").value(MemberBlockException.BLOCKED_RESOURCE_ACCESS_DENIED.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(MemberBlockException.BLOCKED_RESOURCE_ACCESS_DENIED.getCode()));
	}

	@Test
	@DisplayName("포트폴리오 상세 조회를 할 수 있다. (존재하지 않는 포트폴리오 ID 일 경우)")
	void findDetailWithNotExistingPortfolioId() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_TEXT_STR, STORE_ADDRESS_STR,
			LocalDate.now());
		Member member = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(member.getId());

		//지역 저장
		Location cityLocation = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);

		directorLocationMappingProvider.save(directorInfo, cityLocation);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.get("/api/portfolios/{portfolioId}", 9999L)
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(PortfolioException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath("$.message").value(PortfolioException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath("$.code").value(PortfolioException.NOT_FOUND.getCode()));
	}

}
