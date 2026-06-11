package com.motd.be.rest_docs.module.admin.admin;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.Constants.EMAIL;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.admin.admin.dto.response.AdminFindDetailResponse;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class AdminRestDocsTest extends BaseRestDocsTest {

	@Test
	void 관리자_정보_조회_문서화() throws Exception {
		authenticationSetUp();

		willReturn(AdminFindDetailResponse.builder()
			.id(ID)
			.email(EMAIL)
			.nickname(NICKNAME)
			.build())
			.given(adminService).findInfo(any(Long.class));

		mockMvc.perform(get("/api/admin/infos")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)
				))
			.andExpect(status().isOk())
			.andDo(document("admin-find-info",
					getRequestPreProcessor(),
					getResponsePreProcessor(),

					requestCookies(
						cookieWithName(ACCESS_TOKEN_STR)
							.description("HttpOnly accessToken 쿠키")
					),

					responseFields(
						fieldWithPath("id").type(JsonFieldType.NUMBER)
							.description("관리자 아이디"),

						fieldWithPath("email").type(JsonFieldType.STRING)
							.description("관리자 이메일"),

						fieldWithPath("nickname").type(JsonFieldType.STRING)
							.description("관지라 닉네임")
					),

					resource(builder()
						.tag("⭐ 관리자 관련 API")
						.summary("관리자 정보 조회 API")
						.description("관리자 정보를 조회하는 API")
						.build()
					)

				)
			);
	}
}
