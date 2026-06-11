package com.motd.be.rest_docs.shared.mobile_ok;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.Constants.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.shared.mobile_ok.dto.response.MobileOkCreateTokenResponse;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class MobileOkRestDocsTest extends BaseRestDocsTest {

	@Test
	void 본인확인_토큰_요청() throws Exception {
		authenticationSetUp();

		MobileOkCreateTokenResponse mockResponse = MobileOkCreateTokenResponse.builder()
			.usageCode("01006")
			.serviceId("test-service-id")
			.encryptReqClientInfo("encrypted-info")
			.serviceType("telcoAuth")
			.retTransferType("MOKToken")
			.returnUrl("https://{도메인명}/api/mobile-ok/authentication")
			.build();

		given(mobileOkFacade.createMobileOkToken(anyLong()))
			.willReturn(mockResponse);

		mockMvc.perform(post("/api/mobile-ok/token")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isCreated())
			.andDo(document("mobile-ok-request-token",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("usageCode").description("사용 코드"),
					fieldWithPath("serviceId").description("서비스 ID"),
					fieldWithPath("encryptReqClientInfo").description("암호화된 요청 정보"),
					fieldWithPath("serviceType").description("서비스 타입"),
					fieldWithPath("retTransferType").description("반환 전송 타입"),
					fieldWithPath("returnUrl").description("반환 URL")
				),
				resource(builder()
					.tag("🔐 MobileOK API")
					.summary("MobileOK 을 위한 토큰을 요청")
					.description("MobileOK 을 위한 토큰을 요청을 합니다.")
					.build())
			));
	}

	@Test
	void 모바일OK_인증결과조회_웹버전() throws Exception {
		authenticationSetUp();

		willDoNothing().given(mobileOkFacade)
			.processResultForWeb(anyLong(), any());

		mockMvc.perform(post("/api/mobile-ok/web/authentication")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.queryParam("data", DATA_STR)
			)
			.andExpect(status().isOk())
			.andDo(document("mobile-ok-authentication-web",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("data")
						.description("MobileOK 인증 결과 데이터 (URL-encoded JSON)")
				),

				resource(builder()
					.tag("🔐 MobileOK API")
					.summary("MobileOK 인증 결과 조회 (웹 버전)")
					.description("웹 클라이언트에서 사용자의 MobileOK 인증 결과를 처리합니다.")
					.build())
			));
	}

	@Test
	void 모바일OK_인증결과조회_앱버전() throws Exception {
		willDoNothing().given(mobileOkFacade)
			.processResultForApp(any(), any());

		mockMvc.perform(post("/api/mobile-ok/authentication")
				.cookie(new Cookie(MOBILE_OK_KEY_FOR_APP, "test-mobile-ok-auth-token"))
				.queryParam("data", DATA_STR)
			)
			.andExpect(status().isFound())
			.andDo(document("mobile-ok-authentication-app",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(MOBILE_OK_KEY_FOR_APP).description("MobileOK 앱 인증용 쿠키 토큰")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("data")
						.description("MobileOK 인증 결과 데이터 (URL-encoded JSON)")
				),

				resource(builder()
					.tag("🔐 MobileOK API")
					.summary("MobileOK 인증 결과 조회 (앱 버전)")
					.description("앱 클라이언트에서 MobileOK 인증 결과를 처리합니다. 성공 시 리다이렉트됩니다.")
					.build())
			));
	}

	@Test
	void 모바일OK_본인인증_뷰페이지_요청() throws Exception {
		authenticationSetUp();

		given(redisMobileOkRepository.createAuthToken(anyLong()))
			.willReturn("test-auth-token-uuid");

		mockMvc.perform(get("/api/mobile-ok/view")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isOk())
			.andDo(document("mobile-ok-view",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				responseCookies(
					cookieWithName(MOBILE_OK_KEY_FOR_APP).description("MobileOK 앱 인증용 임시 토큰 쿠키 (30분 유효)")
				),

				resource(builder()
					.tag("🔐 MobileOK API")
					.summary("MobileOK 본인인증 뷰 페이지 요청")
					.description("앱에서 본인인증을 위한 웹뷰 페이지를 요청합니다. 응답으로 본인인증용 임시 쿠키가 설정됩니다.")
					.build())
			));
	}
}
