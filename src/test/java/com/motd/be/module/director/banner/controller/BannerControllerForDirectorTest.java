package com.motd.be.module.director.banner.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.module.director.banner.dto.response.BannerFindAllResponseForDirector;
import com.motd.be.module.director.banner.dto.response.BannerResponseForDirector;
import com.motd.be.module.member.banner.entity.Banner;
import com.motd.be.module.member.banner.entity.BannerType;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class BannerControllerForDirectorTest extends BaseIntegrationTest {

	@Test
	@DisplayName("디렉터 배너 전체 조회가 가능하다")
	void findAll() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		// 조회 가능한 배너
		Banner banner1 = bannerProvider.saveWithType(BannerType.DIRECTOR, LocalDateTime.now(),
			LocalDateTime.now().plusDays(1), 3);

		// 기한이 지나 조회가 안되는 배너
		bannerProvider.saveWithType(BannerType.DIRECTOR, LocalDateTime.now().minusDays(3),
			LocalDateTime.now().minusDays(1), 0);

		// 조회 불가능한 배너 (type)
		Banner banner4 = bannerProvider.saveWithType(BannerType.DIRECTOR, LocalDateTime.now(),
			LocalDateTime.now().plusDays(1), 1);

		// 조회 가능한 배너
		Banner banner5 = bannerProvider.saveWithType(BannerType.MEMBER, LocalDateTime.now(),
			LocalDateTime.now().plusDays(1), 0);

		// 삭제된 배너
		bannerProvider.saveWithTypeAndIsDeletedTrue(BannerType.DIRECTOR, LocalDateTime.now(),
			LocalDateTime.now().plusDays(1), 0);

		// 멤버 전용 배너
		bannerProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(1), 0);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(get("/api/directors/banners")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isOk())
			.andReturn();

		// then
		String content = result.getResponse().getContentAsString();
		BannerFindAllResponseForDirector response = objectMapper.readValue(content,
			BannerFindAllResponseForDirector.class);

		assertThat(response.getTotalCount()).isEqualTo(2);
		assertThat(response.getEvents()).hasSize(2);

		List<Long> returnedIds = response.getEvents().stream()
			.map(BannerResponseForDirector::getId)
			.toList();

		assertThat(returnedIds).containsExactly(banner4.getId(), banner1.getId());
	}
}
