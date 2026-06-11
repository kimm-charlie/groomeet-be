package com.motd.be.rest_docs.module.member.fcm_token;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.member.fcm_token.dto.request.FcmTokenRequest;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class FcmTokenRestDocsTest extends BaseRestDocsTest {

	@Test
	void FCM_토큰_등록_문서화() throws Exception {
		FcmTokenRequest requestBody = FcmTokenRequest.builder()
			.token("test-fcm-token-string")
			.build();

		willReturn(ID)
			.given(fcmTokenFacade).register(any(FcmTokenRequest.class));

		mockMvc.perform(post("/api/fcm-tokens")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestBody)))
			.andExpect(status().isCreated())
			.andDo(document("fcm-token-register",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestFields(
					fieldWithPath("token").type(JsonFieldType.STRING)
						.description("FCM 토큰")
				),

				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER)
						.description("등록된 FCM 토큰 ID")
				),

				resource(builder()
					.tag("🔔 FCM 토큰 API")
					.summary("FCM 토큰 등록 API")
					.description("FCM 토큰을 등록하는 API")
					.build()
				)
			));
	}

	@Test
	void FCM_토큰_회원_매핑_문서화() throws Exception {
		authenticationSetUp();

		FcmTokenRequest requestBody = FcmTokenRequest.builder()
			.token("test-fcm-token-string")
			.build();

		willDoNothing()
			.given(fcmTokenFacade).mapMember(any(Long.class), any(Long.class), any(FcmTokenRequest.class));

		mockMvc.perform(post("/api/fcm-tokens/{fcmTokenId}", ID)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestBody)))
			.andExpect(status().isCreated())
			.andDo(document("fcm-token-map-member",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR)
						.description("HttpOnly refreshToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("fcmTokenId")
						.description("FCM 토큰 ID")
				),

				requestFields(
					fieldWithPath("token").type(JsonFieldType.STRING)
						.description("FCM 토큰")
				),

				resource(builder()
					.tag("🔔 FCM 토큰 API")
					.summary("FCM 토큰 회원 매핑 API")
					.description("FCM 토큰을 회원과 매핑하는 API")
					.build()
				)
			));
	}

	@Test
	void FCM_토큰_회원_매핑_해제_문서화() throws Exception {
		authenticationSetUp();

		FcmTokenRequest requestBody = FcmTokenRequest.builder()
			.token("test-fcm-token-string")
			.build();

		willDoNothing()
			.given(fcmTokenFacade).unmapMember(any(Long.class), any(FcmTokenRequest.class), any(Long.class));

		mockMvc.perform(delete("/api/fcm-tokens/{fcmTokenId}", ID)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestBody)))
			.andExpect(status().isNoContent())
			.andDo(document("fcm-token-unmap-member",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR)
						.description("HttpOnly refreshToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("fcmTokenId")
						.description("FCM 토큰 ID")
				),

				requestFields(
					fieldWithPath("token").type(JsonFieldType.STRING)
						.description("FCM 토큰")
				),

				resource(builder()
					.tag("🔔 FCM 토큰 API")
					.summary("FCM 토큰 회원 매핑 해제 API")
					.description("FCM 토큰과 회원 간의 매핑을 해제하는 API")
					.build()
				)
			));
	}
}
