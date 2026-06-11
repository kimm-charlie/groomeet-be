package com.motd.be.module.director.popup.controller;

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
import com.motd.be.module.director.popup.dto.response.PopupFindAllResponseForDirector;
import com.motd.be.module.director.popup.dto.response.PopupResponseForDirector;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.popup.entity.Popup;
import com.motd.be.module.member.popup.entity.PopupType;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class PopupControllerForDirectorTest extends BaseIntegrationTest {

	@Test
	@DisplayName("디렉터 팝업 전체 조회가 가능하다")
	void findAll() throws Exception {
		// given
		DirectorInfo directorInfo = directorInfoProvider.saveWithOnboardingPass(INTRODUCE_STR, STORE_ADDRESS_STR,
			LocalDate.now().plusMonths(1));
		Member director = memberProvider.saveMemberWithDirectorInfo(SignInPlatform.KAKAO, directorInfo);
		Jwt jwt = generateTokenWithMemberIdRoleDirector(director.getId());

		// 조회 가능한 DIRECTOR 타입 팝업
		Popup popup1 = popUpProvider.saveWithType(PopupType.DIRECTOR, LocalDateTime.now(),
			LocalDateTime.now().plusDays(3), 0);
		Popup popup2 = popUpProvider.saveWithType(PopupType.DIRECTOR, LocalDateTime.now(),
			LocalDateTime.now().plusDays(5), 1);

		// 조회 불가능한 MEMBER 타입 팝업
		popUpProvider.saveWithType(PopupType.MEMBER, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 2);

		// 기한이 지나 조회 불가능한 팝업
		popUpProvider.saveWithType(PopupType.DIRECTOR, LocalDateTime.now().minusDays(3),
			LocalDateTime.now().minusDays(1), 3);

		// 삭제된 팝업
		popUpProvider.saveWithTypeAndIsDeletedTrue(PopupType.DIRECTOR, LocalDateTime.now(),
			LocalDateTime.now().plusDays(1), 4);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(get("/api/directors/popups")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
			)
			.andExpect(status().isOk())
			.andReturn();

		// then
		String content = result.getResponse().getContentAsString();
		PopupFindAllResponseForDirector response = objectMapper.readValue(content,
			PopupFindAllResponseForDirector.class);

		assertThat(response.getTotalCount()).isEqualTo(2);
		assertThat(response.getPopups()).hasSize(2);

		List<Long> returnedIds = response.getPopups().stream()
			.map(PopupResponseForDirector::getId)
			.toList();

		assertThat(returnedIds).containsExactlyInAnyOrder(popup1.getId(), popup2.getId());
	}
}
