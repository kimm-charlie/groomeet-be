package com.motd.be.rest_docs.module.admin.auth;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.Constants.EMAIL;
import static com.motd.be.common.constants.Constants.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.admin.auth.dto.request.AuthAdminSignInRequest;
import com.motd.be.module.admin.auth.dto.response.AuthAdminSignInResponse;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class AuthAdminRestDocsTest extends BaseRestDocsTest {

	@Test
	void 관리자_로그인_문서화() throws Exception {
		AuthAdminSignInRequest requestBody = AuthAdminSignInRequest.builder()
			.email(EMAIL)
			.password(PASSWORD)
			.build();

		willReturn(AuthAdminSignInResponse.builder()
			.accessToken(ACCESS_TOKEN)
			.build())
			.given(authAdminService).signIn(any(AuthAdminSignInRequest.class));

		mockMvc.perform(post("/api/admin/signIn")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestBody))) // ✅ JSON 변환 처리
			.andExpect(status().isOk())
			.andDo(document("admin-sign-in",
					getRequestPreProcessor(),
					getResponsePreProcessor(),

					requestFields(
						fieldWithPath("email").type(JsonFieldType.STRING)
							.description("이메일"),

						fieldWithPath("password").type(JsonFieldType.STRING)
							.description("비밀번호")
					),

					responseCookies(
						cookieWithName(ACCESS_TOKEN_STR)
							.description("HttpOnly accessToken 쿠키. 회원가입시 발급된다.")
					),

					resource(builder()
						.tag("⭐ 관리자 관련 API")
						.summary("관리자 로그인 API")
						.description("관리자 로그인 API")
						.build()
					)
				)
			);
	}

	@Test
	void 관리자_로그아웃_문서화() throws Exception {
		authenticationSetUp();

		willDoNothing()
			.given(authAdminService).signOut(any(String.class));

		mockMvc.perform(post("/api/admin/signOut")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)
				))
			.andExpect(status().isNoContent())
			.andDo(document("admin-sign-out",
					getRequestPreProcessor(),
					getResponsePreProcessor(),

					requestCookies(
						cookieWithName(ACCESS_TOKEN_STR)
							.description("HttpOnly accessToken 쿠키")
					),

					resource(builder()
						.tag("⭐ 관리자 관련 API")
						.summary("관리자 로그아웃 API")
						.description("관리자 로그아웃 API")
						.build()
					)

				)
			);
	}
}
