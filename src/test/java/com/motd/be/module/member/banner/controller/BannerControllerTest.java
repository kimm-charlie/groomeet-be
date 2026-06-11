package com.motd.be.module.member.banner.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.module.member.banner.dto.response.BannerFindAllResponse;
import com.motd.be.module.member.banner.dto.response.BannerResponse;
import com.motd.be.module.member.banner.entity.Banner;
import com.motd.be.module.member.banner.entity.BannerType;

@ControllerIntegrationTest
public class BannerControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("배너 전체 조회가 가능하다")
	void findAll() throws Exception {
		// given
		// 조회 가능한 배너
		Banner banner1 = bannerProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(1), 3);

		// 기한이 지나 조회가 안되는 배너
		Banner banner2 = bannerProvider.save(LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1), 0);

		// 조회 가능한 배너
		Banner banner4 = bannerProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1);
		Banner banner5 = bannerProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(1), 0);

		// 삭제된 배너
		Banner banner6 = bannerProvider.saveWithIsDeletedTrue(LocalDateTime.now(), LocalDateTime.now().plusDays(1), 0);

		// 디렉터 전용 배너
		bannerProvider.saveWithType(BannerType.DIRECTOR, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 0);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(get("/api/banners"))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String content = result.getResponse().getContentAsString();
		BannerFindAllResponse response = objectMapper.readValue(content, BannerFindAllResponse.class);

		assertThat(response.getTotalCount()).isEqualTo(3);
		assertThat(response.getEvents()).hasSize(3);

		List<Long> returnedIds = response.getEvents().stream()
			.map(BannerResponse::getId)
			.toList();

		assertThat(returnedIds).containsExactly(banner5.getId(), banner4.getId(), banner1.getId());
	}
}
