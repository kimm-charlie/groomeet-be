package com.motd.be.rest_docs.module.director.member;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
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
import com.motd.be.module.director.member.dto.response.MemberProfileSummaryResponseForDirector;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class MemberRestDocsTestForDirector extends BaseRestDocsTest {

	@Test
	@DisplayName("디렉터 프로필 요약 조회")
	void findProfileSummary() throws Exception {
		authenticationSetUp();

		// given
		MemberProfileSummaryResponseForDirector response = MemberProfileSummaryResponseForDirector.builder()
			.requestCount(10)
			.completedServiceCount(7)
			.matchingRate(85)
			.build();

		willReturn(response).given(memberFacadeForDirector).findProfileSummary(anyLong());

		// when & then
		mockMvc.perform(get("/api/directors/members/{targetMemberId}/profile-summary", 1L)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("find-profile-summary-for-director",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("targetMemberId")
						.description("조회 대상 회원 ID")
				),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("requestCount").type(JsonFieldType.NUMBER).description("전체 서비스 요청 수"),
					fieldWithPath("completedServiceCount").type(JsonFieldType.NUMBER).description("완료된 서비스 수"),
					fieldWithPath("matchingRate").type(JsonFieldType.NUMBER).description("매칭률 (0-100, 정수)")
				),

				resource(builder()
					.tag("👤 회원 API")
					.summary("디렉터 프로필 요약 조회")
					.description("디렉터가 특정 회원의 프로필 요약 정보를 조회합니다.")
					.build())
			));
	}

}
