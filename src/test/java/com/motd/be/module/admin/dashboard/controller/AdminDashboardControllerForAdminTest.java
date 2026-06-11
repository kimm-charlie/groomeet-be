package com.motd.be.module.admin.dashboard.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.provider.module.admin.AdminTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.dreamsecurity.json.JSONObject;
import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.module.admin.admin.entity.Admin;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.report.entity.ReportReason;
import com.motd.be.module.member.report.entity.ReportType;

@ControllerIntegrationTest
public class AdminDashboardControllerForAdminTest extends BaseIntegrationTest {

	@Test
	@DisplayName("관리자는 대시보드 정보를 조회 할 수 있다.")
	void findDashboard() throws Exception {
		//given
		LocalDate baseDate = LocalDate.now(clock);

		Admin admin = adminProvider.save(EMAIL, PASSWORD);
		Jwt jwtCreatedBySavedAdmin = generateAdminTokenWithAdminId(admin.getId());

		Member reporter = memberProvider.saveMember(SignInPlatform.APPLE);
		Member reported = memberProvider.saveMember(SignInPlatform.KAKAO);
		DirectorInfo directorInfo = directorInfoProvider.save(INTRODUCE_STR, ADDRESS_STR);
		memberProvider.saveMemberWithDirectorInfo(SignInPlatform.GOOGLE, directorInfo);
		memberReportProvider.save(reporter, reported, ReportReason.부적절한_언어_사용, ReportType.CHAT_ROOM);

		DirectorService directorService = directorServiceProvider.save(SERVICE_NAME_1_STR, null);
		serviceRequestProvider.savePending(directorService, reporter);

		serviceRequestProvider.saveWithIsOngoingTrue(directorService, reporter, baseDate.atTime(0, 1));
		serviceRequestProvider.saveWithIsOngoingTrue(directorService, reporter, baseDate.minusDays(1).atTime(12, 0));

		entityManager.flush();
		entityManager.clear();

		//when
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get("/api/admin/dashboard")
					.header(AUTHORIZATION_STR, BEARER_STR + jwtCreatedBySavedAdmin.getAccessToken()))
			.andExpect(status().isOk())
			.andReturn();

		//then
		String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		JSONObject jsonObject = new JSONObject(responseBody);

		assertThat(jsonObject.getLong("totalMemberCount")).isEqualTo(3L);
		assertThat(jsonObject.getLong("directorCount")).isEqualTo(1L);
		assertThat(jsonObject.getLong("todayReportCount")).isEqualTo(1L);
		assertThat(jsonObject.getLong("todayOngoingServiceRequestCount")).isEqualTo(1L);
		assertThat(jsonObject.getLong("todayMemberCount")).isEqualTo(3L);
		assertThat(jsonObject.getLong("todayDirectorCount")).isEqualTo(1L);
		assertThat(jsonObject.getLong("serviceRequestWithoutEstimateCount")).isEqualTo(1L);
	}
}
