package com.motd.be.rest_docs.module.member.google_oauth;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.utils.DateFormatUtils.*;
import static com.motd.be.rest_docs.Utils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.member.auth.ClientType;
import com.motd.be.module.member.auth.dto.response.OAuthSignInResponse;
import com.motd.be.module.member.google_oauth.dto.request.GoogleOauthSignInRequest;
import com.motd.be.module.member.member.entity.Role;

@RestDocsTest
public class GoogleOauthRestDocsTest extends BaseRestDocsTest {

	@Test
	void 구글_기존회원_로그인_문서화_웹_버전() throws Exception {
		GoogleOauthSignInRequest requestBody = GoogleOauthSignInRequest.builder()
			.idToken(ID_TOKEN)
			.build();

		OAuthSignInResponse oAuthSignInResponse = OAuthSignInResponse.builder()
			.isExistingMember(Boolean.TRUE)
			.accessToken(null)
			.refreshToken(null)
			.role(Role.MEMBER.getRoleType())
			.isBanned(Boolean.FALSE)
			.memberId(1L)
			.build();

		willReturn(oAuthSignInResponse)
			.given(googleOauthFacade).signIn(any(GoogleOauthSignInRequest.class));

		willReturn(ResponseEntity.status(HttpStatus.OK)
			.headers(cookieUtils.createAuthCookiesHeaders(ACCESS_TOKEN_STR, REFRESH_TOKEN_STR))
			.body(oAuthSignInResponse))
			.given(oAuthCommonService).createSignInResponse(any(OAuthSignInResponse.class), any(ClientType.class));

		mockMvc.perform(post("/api/members/signIn/google")
				.contentType(MediaType.APPLICATION_JSON)
				.param(CLIENT_TYPE_STR, ClientType.WEB.name())
				.content(objectMapper.writeValueAsString(requestBody))) // ✅ JSON 변환 처리
			.andExpect(status().isOk())
			.andDo(document("google-oauth-sign-in-when-existing-member-web-version",
				getRequestPreProcessor(),
				getResponsePreProcessor(),
				requestFields(
					fieldWithPath(ID_TOKEN_STR).type(JsonFieldType.STRING)
						.description("구글에서 발급한 accessToken")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(CLIENT_TYPE_STR)
						.attributes(enumFormat(ClientType.class, Enum::name))
						.description("웹 및 앱 타입 구분")
				),

				responseCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키."),
					cookieWithName(REFRESH_TOKEN_STR)
						.description("HttpOnly refreshToken 쿠키.")
				),

				responseFields(
					fieldWithPath(IS_EXISTING_MEMBER_STR).type(JsonFieldType.BOOLEAN)
						.description("기존 회원 존재 여부"),

					fieldWithPath(ACCESS_TOKEN_STR).optional().type(JsonFieldType.STRING)
						.optional()
						.description("JWT 엑세스 토큰"),

					fieldWithPath(REFRESH_TOKEN_STR).optional().type(JsonFieldType.STRING)
						.optional()
						.description("JWT 리프레시 토큰"),

					fieldWithPath(UUID_STR).optional().type(JsonFieldType.STRING)
						.optional()
						.description("회원가입 요청시 보내야할 UUID"),

					fieldWithPath(ROLE_STR).optional().type(JsonFieldType.STRING)
						.attributes(enumFormat(Role.class, Enum::name))
						.description("회원 권한"),

					fieldWithPath(IS_BANNED_STR).optional().type(JsonFieldType.BOOLEAN)
						.description("벤여부"),

					fieldWithPath(UNBANNED_AT_STR).optional().type(JsonFieldType.STRING)
						.optional()
						.description("벤 해제 일시"),

					fieldWithPath(MEMBER_ID).type(JsonFieldType.NUMBER)
						.description("회원 아이디")
				),
				resource(builder()
					.tag("🔥 구글 로그인 관련 API")
					.summary("구글 로그인 API")
					.description("구글 로그인을 위한 API")
					.build()
				)
			));
	}

	@Test
	void 구글_기존회원_로그인_문서화_앱_버전() throws Exception {
		GoogleOauthSignInRequest requestBody = GoogleOauthSignInRequest.builder()
			.idToken(ID_TOKEN)
			.build();

		OAuthSignInResponse oAuthSignInResponse = OAuthSignInResponse.builder()
			.isExistingMember(Boolean.TRUE)
			.accessToken(ACCESS_TOKEN_STR)
			.refreshToken(REFRESH_TOKEN_STR)
			.role(Role.MEMBER.getRoleType())
			.isBanned(Boolean.FALSE)
			.memberId(1L)
			.build();

		willReturn(oAuthSignInResponse)
			.given(googleOauthFacade).signIn(any(GoogleOauthSignInRequest.class));

		willReturn(ResponseEntity.status(HttpStatus.OK).body(oAuthSignInResponse))
			.given(oAuthCommonService).createSignInResponse(any(OAuthSignInResponse.class), any(ClientType.class));

		mockMvc.perform(post("/api/members/signIn/google")
				.contentType(MediaType.APPLICATION_JSON)
				.param(CLIENT_TYPE_STR, ClientType.WEB.name())
				.content(objectMapper.writeValueAsString(requestBody))) // ✅ JSON 변환 처리
			.andExpect(status().isOk())
			.andDo(document("google-oauth-sign-in-when-existing-member-app-version",
				getRequestPreProcessor(),
				getResponsePreProcessor(),
				requestFields(
					fieldWithPath(ID_TOKEN_STR).type(JsonFieldType.STRING)
						.description("구글에서 발급한 accessToken")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(CLIENT_TYPE_STR)
						.attributes(enumFormat(ClientType.class, Enum::name))
						.description("웹 및 앱 타입 구분")
				),

				responseFields(
					fieldWithPath(IS_EXISTING_MEMBER_STR).type(JsonFieldType.BOOLEAN)
						.description("기존 회원 존재 여부"),

					fieldWithPath(ACCESS_TOKEN_STR).optional().type(JsonFieldType.STRING)
						.optional()
						.description("JWT 엑세스 토큰"),

					fieldWithPath(REFRESH_TOKEN_STR).optional().type(JsonFieldType.STRING)
						.optional()
						.description("JWT 리프레시 토큰"),

					fieldWithPath(UUID_STR).optional().type(JsonFieldType.STRING)
						.optional()
						.description("회원가입 요청시 보내야할 UUID"),

					fieldWithPath(ROLE_STR).optional().type(JsonFieldType.STRING)
						.attributes(enumFormat(Role.class, Enum::name))
						.description("회원 권한"),

					fieldWithPath(IS_BANNED_STR).optional().type(JsonFieldType.BOOLEAN)
						.description("벤여부"),

					fieldWithPath(UNBANNED_AT_STR).optional().type(JsonFieldType.STRING)
						.optional()
						.description("벤 해제 일시"),

					fieldWithPath(MEMBER_ID).type(JsonFieldType.NUMBER)
						.description("회원 아이디")
				),
				resource(builder()
					.tag("🔥 구글 로그인 관련 API")
					.summary("구글 로그인 API")
					.description("구글 로그인을 위한 API")
					.build()
				)
			));
	}

	@Test
	void 구글_새로가입하는_회원_로그인_문서화() throws Exception {
		GoogleOauthSignInRequest requestBody = GoogleOauthSignInRequest.builder()
			.idToken(ID_TOKEN)
			.build();

		OAuthSignInResponse oAuthSignInResponse = OAuthSignInResponse.builder()
			.isExistingMember(Boolean.FALSE)
			.uuid(UUID_STR)
			.isBanned(Boolean.FALSE)
			.memberId(1L)
			.build();

		willReturn(oAuthSignInResponse)
			.given(googleOauthFacade).signIn(any(GoogleOauthSignInRequest.class));

		willReturn(ResponseEntity.status(HttpStatus.OK).body(oAuthSignInResponse))
			.given(oAuthCommonService).createSignInResponse(any(OAuthSignInResponse.class), any(ClientType.class));

		mockMvc.perform(post("/api/members/signIn/google")
				.contentType(MediaType.APPLICATION_JSON)
				.param(CLIENT_TYPE_STR, ClientType.WEB.name())
				.content(objectMapper.writeValueAsString(requestBody))) // ✅ JSON 변환 처리
			.andExpect(status().isOk())
			.andDo(document("google-oauth-sign-in-when-not-existing-member",
				getRequestPreProcessor(),
				getResponsePreProcessor(),
				requestFields(
					fieldWithPath(ID_TOKEN_STR).type(JsonFieldType.STRING)
						.description("구글에서 발급한 accessToken")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(CLIENT_TYPE_STR)
						.attributes(enumFormat(ClientType.class, Enum::name))
						.description("웹 및 앱 타입 구분")
				),

				responseFields(
					fieldWithPath(IS_EXISTING_MEMBER_STR).type(JsonFieldType.BOOLEAN)
						.description("기존 회원 존재 여부"),

					fieldWithPath(ACCESS_TOKEN_STR).optional().type(JsonFieldType.STRING)
						.optional()
						.description("JWT 엑세스 토큰"),

					fieldWithPath(REFRESH_TOKEN_STR).optional().type(JsonFieldType.STRING)
						.optional()
						.description("JWT 리프레시 토큰"),

					fieldWithPath(UUID_STR).optional().type(JsonFieldType.STRING)
						.optional()
						.description("회원가입 요청시 보내야할 UUID"),

					fieldWithPath(ROLE_STR).optional().type(JsonFieldType.STRING)
						.attributes(enumFormat(Role.class, Enum::name))
						.description("회원 권한"),

					fieldWithPath(IS_BANNED_STR).optional().type(JsonFieldType.BOOLEAN)
						.description("벤여부"),

					fieldWithPath(UNBANNED_AT_STR).optional().type(JsonFieldType.STRING)
						.optional()
						.description("벤 해제 일시"),

					fieldWithPath(MEMBER_ID).type(JsonFieldType.NUMBER)
						.description("회원 아이디")
				),
				resource(builder()
					.tag("🔥 구글 로그인 관련 API")
					.summary("구글 로그인 API")
					.description("구글 로그인을 위한 API")
					.build()
				)
			));
	}

	@Test
	void 구글_벤된_회원_로그인_문서화() throws Exception {
		GoogleOauthSignInRequest requestBody = GoogleOauthSignInRequest.builder()
			.idToken(ID_TOKEN)
			.build();

		OAuthSignInResponse oAuthSignInResponse = OAuthSignInResponse.builder()
			.isExistingMember(Boolean.TRUE)
			.isBanned(Boolean.TRUE)
			.unBannedAt(formatToDateString(LocalDateTime.now()))
			.memberId(1L)
			.build();

		willReturn(oAuthSignInResponse)
			.given(googleOauthFacade).signIn(any(GoogleOauthSignInRequest.class));

		willReturn(ResponseEntity.status(HttpStatus.OK).body(oAuthSignInResponse))
			.given(oAuthCommonService).createSignInResponse(any(OAuthSignInResponse.class), any(ClientType.class));

		mockMvc.perform(post("/api/members/signIn/google")
				.contentType(MediaType.APPLICATION_JSON)
				.param(CLIENT_TYPE_STR, ClientType.WEB.name())
				.content(objectMapper.writeValueAsString(requestBody))) // ✅ JSON 변환 처리
			.andExpect(status().isOk())
			.andDo(document("google-oauth-sign-in-when-banned-member",
				getRequestPreProcessor(),
				getResponsePreProcessor(),
				requestFields(
					fieldWithPath(ID_TOKEN_STR).type(JsonFieldType.STRING)
						.description("구글에서 발급한 accessToken")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(CLIENT_TYPE_STR)
						.attributes(enumFormat(ClientType.class, Enum::name))
						.description("웹 및 앱 타입 구분")
				),

				responseFields(
					fieldWithPath(IS_EXISTING_MEMBER_STR).type(JsonFieldType.BOOLEAN)
						.description("기존 회원 존재 여부"),

					fieldWithPath(ACCESS_TOKEN_STR).optional().type(JsonFieldType.STRING)
						.optional()
						.description("JWT 엑세스 토큰"),

					fieldWithPath(REFRESH_TOKEN_STR).optional().type(JsonFieldType.STRING)
						.optional()
						.description("JWT 리프레시 토큰"),

					fieldWithPath(UUID_STR).optional().type(JsonFieldType.STRING)
						.optional()
						.description("회원가입 요청시 보내야할 UUID"),

					fieldWithPath(ROLE_STR).optional().type(JsonFieldType.STRING)
						.attributes(enumFormat(Role.class, Enum::name))
						.description("회원 권한"),

					fieldWithPath(IS_BANNED_STR).optional().type(JsonFieldType.BOOLEAN)
						.description("벤여부"),

					fieldWithPath(UNBANNED_AT_STR).optional().type(JsonFieldType.STRING)
						.optional()
						.description("벤 해제 일시"),

					fieldWithPath(MEMBER_ID).type(JsonFieldType.NUMBER)
						.description("회원 아이디")
				),
				resource(builder()
					.tag("🔥 구글 로그인 관련 API")
					.summary("구글 로그인 API")
					.description("구글 로그인을 위한 API")
					.build()
				)
			));
	}
}
