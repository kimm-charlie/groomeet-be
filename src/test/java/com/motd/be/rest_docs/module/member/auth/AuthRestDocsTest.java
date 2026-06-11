package com.motd.be.rest_docs.module.member.auth;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.rest_docs.Utils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.member.auth.ClientType;
import com.motd.be.module.member.auth.dto.request.AuthReissueTokenRequest;
import com.motd.be.module.member.auth.dto.request.AuthSignOutRequest;
import com.motd.be.module.member.auth.dto.request.AuthSignUpRequest;
import com.motd.be.module.member.auth.dto.request.AuthWithdrawalRequest;
import com.motd.be.module.member.auth.dto.response.AuthExchangeCodeForTokenResponse;
import com.motd.be.module.member.auth.dto.response.AuthGenerateBridgeCodeResponse;
import com.motd.be.module.member.auth.dto.response.AuthReissueResponse;
import com.motd.be.module.member.auth.dto.response.AuthSignUpResponse;
import com.motd.be.module.member.member.entity.WithdrawalReason;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class AuthRestDocsTest extends BaseRestDocsTest {

	@Test
	void 회원가입_웹_버전() throws Exception {

		AuthSignUpRequest requestBody = AuthSignUpRequest.builder()
			.uuid(UUID_STR)
			.serviceAgreed(Boolean.TRUE)
			.privacyPolicyAgreed(Boolean.TRUE)
			.marketingAgreed(Boolean.TRUE)
			.referralCode("REFERRAL")
			.build();

		willReturn(AuthSignUpResponse.builder()
			.accessToken(null)
			.refreshToken(null)
			.memberId(1L)
			.build())
			.given(authFacade).signUp(any(AuthSignUpRequest.class), any(ClientType.class));

		mockMvc.perform(post("/api/members/signUp")
				.contentType(MediaType.APPLICATION_JSON)
				.param(CLIENT_TYPE_STR, ClientType.WEB.name())
				.content(objectMapper.writeValueAsString(requestBody))) // ✅ JSON 변환 처리
			.andExpect(status().isCreated())
			.andDo(document("sign-up-web-version",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(CLIENT_TYPE_STR)
						.attributes(enumFormat(ClientType.class, Enum::name))
						.description("웹 및 앱 타입 구분")
				),

				requestFields(
					fieldWithPath("uuid").type(JsonFieldType.STRING)
						.description("회원의 고유 식별자(UUID)"),

					fieldWithPath("serviceAgreed").type(JsonFieldType.BOOLEAN)
						.description("서비스 이용 약관 동의 여부"),

					fieldWithPath("privacyPolicyAgreed").type(JsonFieldType.BOOLEAN)
						.description("개인정보 처리방침 동의 여부"),

					fieldWithPath("marketingAgreed").type(JsonFieldType.BOOLEAN)
						.description("마케팅 수신동의 여부"),

					fieldWithPath("referralCode").type(JsonFieldType.STRING)
						.optional()
						.description("추천인 코드")
				),

				responseCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키. 회원가입시 발급된다."),
					cookieWithName(REFRESH_TOKEN_STR)
						.description("HttpOnly refreshToken 쿠키. 회원가입시 발급된다.")
				),

				responseFields(
					fieldWithPath(ACCESS_TOKEN_STR).type(JsonFieldType.STRING)
						.optional()
						.description("서버에서 발급한 accessToken"),

					fieldWithPath(REFRESH_TOKEN_STR).type(JsonFieldType.STRING)
						.optional()
						.description("서버에서 발급한 refreshToken"),

					fieldWithPath("memberId").type(JsonFieldType.NUMBER)
						.description("회원 아이디")
				),

				resource(builder()
					.tag("🔥 인증 인가 관련 API")
					.summary("회원가입")
					.description("회원가입할때 사용")
					.build()
				)
			));
	}

	@Test
	void 회원가입_앱_버전() throws Exception {

		AuthSignUpRequest requestBody = AuthSignUpRequest.builder()
			.uuid(UUID_STR)
			.serviceAgreed(Boolean.TRUE)
			.privacyPolicyAgreed(Boolean.TRUE)
			.marketingAgreed(Boolean.TRUE)
			.build();

		willReturn(AuthSignUpResponse.builder()
			.accessToken(ACCESS_TOKEN_STR)
			.refreshToken(REFRESH_TOKEN_STR)
			.memberId(1L)
			.build())
			.given(authFacade).signUp(any(AuthSignUpRequest.class), any(ClientType.class));

		mockMvc.perform(post("/api/members/signUp")
				.contentType(MediaType.APPLICATION_JSON)
				.param(CLIENT_TYPE_STR, ClientType.APP.name())
				.content(objectMapper.writeValueAsString(requestBody)))
			.andExpect(status().isCreated())
			.andDo(document("sign-up-app-version",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(CLIENT_TYPE_STR)
						.attributes(enumFormat(ClientType.class, Enum::name))
						.description("웹 및 앱 타입 구분")
				),

				requestFields(
					fieldWithPath("uuid").type(JsonFieldType.STRING)
						.description("회원의 고유 식별자(UUID)"),

					fieldWithPath("serviceAgreed").type(JsonFieldType.BOOLEAN)
						.description("서비스 이용 약관 동의 여부"),

					fieldWithPath("privacyPolicyAgreed").type(JsonFieldType.BOOLEAN)
						.description("개인정보 처리방침 동의 여부"),

					fieldWithPath("marketingAgreed").type(JsonFieldType.BOOLEAN)
						.description("마케팅 수신동의 여부"),

					fieldWithPath("referralCode").type(JsonFieldType.STRING)
						.optional()
						.description("추천인 코드")
				),

				responseFields(
					fieldWithPath(ACCESS_TOKEN_STR).type(JsonFieldType.STRING)
						.description("서버에서 발급한 accessToken"),

					fieldWithPath(REFRESH_TOKEN_STR).type(JsonFieldType.STRING)
						.optional()
						.description("서버에서 발급한 refreshToken"),

					fieldWithPath("memberId").type(JsonFieldType.NUMBER)
						.description("회원 아이디")
				),

				resource(builder()
					.tag("🔥 인증 인가 관련 API")
					.summary("회원가입")
					.description("회원가입할때 사용")
					.build()
				)
			));
	}

	@Test
	void 로그아웃_앱_버전() throws Exception {
		authenticationSetUp();

		AuthSignOutRequest requestBody = AuthSignOutRequest.builder()
			.refreshToken(REFRESH_TOKEN_STR)
			.build();

		willDoNothing()
			.given(authFacade)
			.signOut(any(Long.class), any(String.class), any(String.class), any(ClientType.class), any(
				AuthSignOutRequest.class));

		mockMvc.perform(post("/api/members/signOut")
				.param(CLIENT_TYPE_STR, ClientType.APP.name())
				.header(AUTHORIZATION_STR, BEARER_STR + ACCESS_TOKEN_STR)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestBody)))
			.andExpect(status().isNoContent())
			.andDo(document("sign-out-app-version",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestHeaders(
					AUTH_HEADER
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(CLIENT_TYPE_STR)
						.attributes(enumFormat(ClientType.class, Enum::name))
						.description("웹 및 앱 타입 구분")
				),

				requestFields(
					fieldWithPath("refreshToken").type(JsonFieldType.STRING)
						.optional()
						.description("리프레쉬 토큰")
				),

				responseCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키. 로그아웃시 max-age 0으로 설정됨"),
					cookieWithName(REFRESH_TOKEN_STR)
						.description("HttpOnly refreshToken 쿠키. 로그아웃시 max-age 0으로 설정됨")
				),

				resource(builder()
					.tag("🔥 인증 인가 관련 API")
					.summary("로그아웃")
					.description("로그아웃할때 사용")
					.requestHeaders(
						AUTH_HEADER
					)
					.build()
				)
			));
	}

	@Test
	void 로그아웃_웹_버전() throws Exception {
		authenticationSetUp();

		AuthSignOutRequest requestBody = AuthSignOutRequest.builder()
			.refreshToken(null)
			.build();

		willDoNothing()
			.given(authFacade)
			.signOut(any(Long.class), any(String.class), any(String.class), any(ClientType.class), any(
				AuthSignOutRequest.class));

		mockMvc.perform(post("/api/members/signOut")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.param(CLIENT_TYPE_STR, ClientType.WEB.name())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestBody))
				.cookie(new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)))
			.andExpect(status().isNoContent())
			.andDo(document("sign-out-web-version",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR)
						.description("HttpOnly refreshToken 쿠키")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(CLIENT_TYPE_STR)
						.attributes(enumFormat(ClientType.class, Enum::name))
						.description("웹 및 앱 타입 구분")
				),

				requestFields(
					fieldWithPath("refreshToken").type(JsonFieldType.STRING)
						.optional()
						.description("리프레쉬 토큰")
				),

				responseCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키. 로그아웃시 max-age 0으로 설정됨"),
					cookieWithName(REFRESH_TOKEN_STR)
						.description("HttpOnly refreshToken 쿠키. 로그아웃시 max-age 0으로 설정됨")
				),

				resource(builder()
					.tag("🔥 인증 인가 관련 API")
					.summary("로그아웃")
					.description("로그아웃할때 사용")
					.build()
				)
			));
	}

	@Test
	void 회원탈퇴_웹버전() throws Exception {
		authenticationSetUp();

		AuthWithdrawalRequest requestBody = AuthWithdrawalRequest.builder()
			.withdrawalReason(WithdrawalReason.OTHER.name())
			.build();

		willDoNothing()
			.given(authFacade).withdrawal(any(Long.class), any(AuthWithdrawalRequest.class), any(String.class));

		mockMvc.perform(post("/api/members/withdrawal")
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestBody)))
			.andExpect(status().isNoContent())
			.andDo(document("withdrawal-web-version",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				requestFields(
					fieldWithPath("withdrawalReason").type(JsonFieldType.STRING)
						.attributes(enumFormat(WithdrawalReason.class, Enum::name))
						.description("회원탈퇴 이유")
				),

				responseCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키. 회원탈퇴시 max-age 0으로 설정됨"),
					cookieWithName(REFRESH_TOKEN_STR)
						.description("HttpOnly refreshToken 쿠키. 회원탈퇴시 max-age 0으로 설정됨")
				),

				resource(builder()
					.tag("🔥 인증 인가 관련 API")
					.summary("회원탈퇴")
					.description("회원탈퇴 할떄 사용")
					.build()
				)
			));
	}

	@Test
	void 회원탈퇴_앱버전() throws Exception {
		authenticationSetUp();

		AuthWithdrawalRequest requestBody = AuthWithdrawalRequest.builder()
			.withdrawalReason(WithdrawalReason.OTHER.name())
			.build();

		willDoNothing()
			.given(authFacade).withdrawal(any(Long.class), any(AuthWithdrawalRequest.class), any(String.class));

		mockMvc.perform(post("/api/members/withdrawal")
				.header(AUTHORIZATION_STR, BEARER_STR + ACCESS_TOKEN_STR)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestBody)))
			.andExpect(status().isNoContent())
			.andDo(document("withdrawal-app-version",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestHeaders(
					AUTH_HEADER
				),

				requestFields(
					fieldWithPath("withdrawalReason").type(JsonFieldType.STRING)
						.attributes(enumFormat(WithdrawalReason.class, Enum::name))
						.description("회원탈퇴 이유")
				),

				responseCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키. 로그아웃시 max-age 0으로 설정됨"),
					cookieWithName(REFRESH_TOKEN_STR)
						.description("HttpOnly refreshToken 쿠키. 로그아웃시 max-age 0으로 설정됨")
				),

				resource(builder()
					.tag("🔥 인증 인가 관련 API")
					.summary("회원탈퇴")
					.description("회원탈퇴 할떄 사용")
					.requestHeaders(
						AUTH_HEADER
					)
					.build()
				)
			));
	}

	@Test
	void 토큰_재발급_문서화_앱_버전() throws Exception {

		AuthReissueTokenRequest requestBody = AuthReissueTokenRequest.builder()
			.refreshToken(REFRESH_TOKEN_STR)
			.build();

		willReturn(AuthReissueResponse.builder()
			.accessToken(ACCESS_TOKEN_STR)
			.refreshToken(REFRESH_TOKEN_STR)
			.build())
			.given(authFacade)
			.reissueToken(any(), any(ClientType.class), any(AuthReissueTokenRequest.class));

		mockMvc.perform(post("/api/members/reissue")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestBody))
				.param(CLIENT_TYPE_STR, ClientType.APP.name()))
			.andExpect(status().isCreated())
			.andDo(document("reissue-token-app-version",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestFields(
					fieldWithPath("refreshToken").type(JsonFieldType.STRING)
						.optional()
						.description("리프레쉬 토큰")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(CLIENT_TYPE_STR)
						.attributes(enumFormat(ClientType.class, Enum::name))
						.description("웹 및 앱 타입 구분")
				),

				responseFields(
					fieldWithPath(ACCESS_TOKEN_STR).type(JsonFieldType.STRING)
						.description("JWT 엑세스 토큰"),
					fieldWithPath(REFRESH_TOKEN_STR).type(JsonFieldType.STRING)
						.optional()
						.description("리프레쉬 토큰")
				),

				resource(builder()
					.tag("🔥 인증 인가 관련 API")
					.summary("REFRESH TOKEN 을 기반으로 토큰을 재발급 하는 API (앱버전)")
					.description("REFRESH TOKEN 을 기반으로 토큰을 재발급 하는 API")
					.build()
				)
			));
	}

	@Test
	void Bridge_Code_생성_문서화() throws Exception {
		authenticationSetUp();

		// Mock 응답 데이터 설정
		willReturn(AuthGenerateBridgeCodeResponse.builder()
			.bridgeCode(BRIDGE_CODE_STR)
			.build())
			.given(authFacade).generateBridgeCode(any(String.class));

		mockMvc.perform(post("/api/members/bridge/code")
				.header(AUTHORIZATION_STR, BEARER_STR + ACCESS_TOKEN_STR))
			.andExpect(status().isCreated())
			.andDo(document("auth-generate-bridge-code",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestHeaders(
					AUTH_HEADER
				),

				responseFields(
					fieldWithPath("bridgeCode").type(JsonFieldType.STRING)
						.description("생성된 브릿지 코드")
				),

				resource(builder()
					.tag("🔥 인증 인가 관련 API")
					.summary("브릿지 코드 생성")
					.description("크로스 플랫폼 인증을 위한 브릿지 코드를 생성합니다")
					.requestHeaders(
						AUTH_HEADER
					)
					.build()
				)
			));
	}

	@Test
	void 코드_교환으로_토큰_획득_문서화() throws Exception {
		// Mock 응답 데이터 설정
		willReturn(AuthExchangeCodeForTokenResponse.builder()
			.accessToken(ACCESS_TOKEN_STR)
			.refreshToken(REFRESH_TOKEN_STR)
			.build())
			.given(authFacade).exchangeCodeForToken(any(String.class));

		mockMvc.perform(post("/api/members/bridge/token")
				.queryParam(BRIDGE_CODE_STR, BRIDGE_CODE_STR))
			.andExpect(status().isCreated())
			.andDo(document("auth-exchange-code-for-token",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("bridgeCode")
						.description("교환할 브릿지 코드")
				),

				responseCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키."),
					cookieWithName(REFRESH_TOKEN_STR)
						.description("HttpOnly refreshToken 쿠키.")
				),

				resource(builder()
					.tag("🔥 인증 인가 관련 API")
					.summary("브릿지 코드를 토큰으로 교환")
					.description("브릿지 코드를 사용하여 액세스 토큰과 리프레시 토큰을 획득합니다")
					.build()
				)
			));
	}

	@Test
	@DisplayName("회원은 자신의 인증 정보를 조회할 수 있다.")
	void 회원_인증_정보_조회() throws Exception {
		// given
		authenticationSetUp();

		// when & then
		mockMvc.perform(get("/api/members/auth/identity")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("auth-find-member-identity",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER)
						.description("회원의 고유 식별자 ID")
				),

				resource(builder()
					.tag("👤 회원 인증 API")
					.summary("회원 인증 정보 조회")
					.description("""
						JWT 쿠키를 기반으로 현재 로그인한 회원의 인증 정보를 조회합니다.
						AccessToken과 RefreshToken이 모두 유효해야 합니다.
						""")
					.build())
			));
	}

}
