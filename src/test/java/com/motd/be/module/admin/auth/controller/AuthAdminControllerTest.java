package com.motd.be.module.admin.auth.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.admin.AdminTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.module.admin.admin.entity.Admin;
import com.motd.be.module.admin.auth.dto.request.AuthAdminSignInRequest;
import com.motd.be.module.member.jwt.Jwt;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class AuthAdminControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("관리자는 로그인을 할 수 있다.")
	void signIn() throws Exception {
		//given
		//1. 관리자 저장
		Admin admin = adminProvider.saveWithEncodedPassword(EMAIL, PASSWORD);

		AuthAdminSignInRequest request = AuthAdminSignInRequest.builder()
			.email(admin.getEmail())
			.password(PASSWORD)
			.build();

		entityManager.flush();
		entityManager.clear();

		String jsonRequest = objectMapper.writeValueAsString(request);

		//when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/admin/signIn")
					.contentType(MediaType.APPLICATION_JSON)
					.content(jsonRequest))
			.andExpect(status().isOk())
			.andReturn();

		//then
		// Set-Cookie 헤더 검증
		String[] setCookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE).toArray(new String[0]);

		// refreshToken 쿠키가 1 개 있는지 확인
		assertThat(setCookies).hasSize(1);
	}

	@Test
	@DisplayName("관리자는 로그아웃을 할 수 있다.")
	void signOut() throws Exception {
		//given
		//1. 관리자 저장
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		entityManager.flush();
		entityManager.clear();

		Jwt jwtCreatedBySavedAdmin = generateAdminTokenWithAdminId(admin.getId());

		//when
		mockMvc.perform(
				MockMvcRequestBuilders.post("/api/admin/signOut")
					.cookie(new Cookie(ACCESS_TOKEN_STR, jwtCreatedBySavedAdmin.getAccessToken())))
			.andExpect(status().isNoContent());

		//then
		assertThat(redisBlackListUtilProvider.isBlackListToken(jwtCreatedBySavedAdmin.getAccessToken())).isTrue();
	}
}
