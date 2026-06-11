package com.motd.be.module.admin.admin.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.admin.AdminTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;

import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.module.admin.admin.entity.Admin;
import com.motd.be.module.member.jwt.Jwt;

@ControllerIntegrationTest
public class AdminControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("관리자는 관리자 정보를 조회 할 수 있다.")
	void findDetail() throws Exception {
		//given
		//1. 관리자 저장
		Admin admin = adminProvider.save(EMAIL, PASSWORD);

		entityManager.flush();
		entityManager.clear();

		Jwt jwtCreatedBySavedAdmin = generateAdminTokenWithAdminId(admin.getId());

		//when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/infos")
					.header(AUTHORIZATION_STR, BEARER_STR + jwtCreatedBySavedAdmin.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		//then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		assertThat(jsonObject.getLong(ID_STR)).isEqualTo(admin.getId());
		assertThat(jsonObject.get(EMAIL_STR)).isEqualTo(admin.getEmail());
		assertThat(jsonObject.get(NICKNAME_STR)).isEqualTo(admin.getNickname());
	}

}
