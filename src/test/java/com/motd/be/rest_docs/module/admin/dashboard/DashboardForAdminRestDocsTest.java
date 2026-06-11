package com.motd.be.rest_docs.module.admin.dashboard;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.admin.dashboard.dto.response.AdminDashboardResponseForAdmin;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class DashboardForAdminRestDocsTest extends BaseRestDocsTest {

	@Test
	void 관리자_대시보드_조회_문서화() throws Exception {
		authenticationSetUp();

		willReturn(AdminDashboardResponseForAdmin.builder()
			.totalMemberCount(10L)
			.directorCount(4L)
			.todayReportCount(3L)
			.todayOngoingServiceRequestCount(2L)
			.todayMemberCount(1L)
			.todayDirectorCount(1L)
			.serviceRequestWithoutEstimateCount(5L)
			.build())
			.given(adminDashboardFacadeForAdmin).findDashboard();

		mockMvc.perform(get("/api/admin/dashboard")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)
				))
			.andExpect(status().isOk())
			.andDo(document("admin-dashboard-find",
					getRequestPreProcessor(),
					getResponsePreProcessor(),

					requestCookies(
						cookieWithName(ACCESS_TOKEN_STR)
							.description("HttpOnly accessToken 쿠키")
					),

					responseFields(
						fieldWithPath("totalMemberCount").type(JsonFieldType.NUMBER)
							.description("전체 회원 수"),
						fieldWithPath("directorCount").type(JsonFieldType.NUMBER)
							.description("디렉터 수"),
						fieldWithPath("todayReportCount").type(JsonFieldType.NUMBER)
							.description("당일 신고 건수"),
						fieldWithPath("todayOngoingServiceRequestCount").type(JsonFieldType.NUMBER)
							.description("오늘 진행 중으로 변경된 서비스 요청 수"),
						fieldWithPath("todayMemberCount").type(JsonFieldType.NUMBER)
							.description("오늘 가입한 회원 수"),
						fieldWithPath("todayDirectorCount").type(JsonFieldType.NUMBER)
							.description("오늘 가입한 디렉터 수"),
						fieldWithPath("serviceRequestWithoutEstimateCount").type(JsonFieldType.NUMBER)
							.description("제안을 아직 받지 못한 서비스 요청 수")
					),

					resource(builder()
						.tag("⭐ 관리자 관련 API")
						.summary("관리자 대시보드 조회 API")
						.description("관리자 대시보드 정보를 조회하는 API")
						.build()
					)

				)
			);
	}
}
