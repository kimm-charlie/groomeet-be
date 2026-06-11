package com.motd.be.rest_docs.module.member.director_profile_detail;

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
import org.springframework.test.web.servlet.ResultActions;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.member.director_profile_detail.dto.response.DirectorProfileFindDetailResponseForPublic;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class DirectorProfileDetailRestDocsTest extends BaseRestDocsTest {

	@Test
	void 디렉터_프로필_상세_조회() throws Exception {
		// given
		authenticationSetUp();
		DirectorProfileFindDetailResponseForPublic response = DirectorProfileFindDetailResponseForPublic.builder()
			.id(1L)
			.contentJson("{\"content\":\"소개 내용\"}")
			.build();

		given(directorProfileDetailFacade.findDetail(anyLong())).willReturn(response);

		// when
		ResultActions result = mockMvc.perform(get("/api/directors/{memberId}/profile-detail", ID)
			.cookie(
				new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
				new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
			)
			.contentType(MediaType.APPLICATION_JSON));

		// then
		result.andExpect(status().isOk())
			.andDo(document("director-profile-detail-find-detail-for-public",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("memberId")
						.description("프로필을 조회할 회원 ID (디렉터 프로필 상세 조회지만 회원 ID 로 조회한다.)")
				),

				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER).description("프로필 상세 ID"),
					fieldWithPath("contentJson").optional().type(JsonFieldType.STRING).description("프로필 상세 내용 JSON")
				),

				resource(builder()
					.tag("🎬 디렉터 프로필 상세 API")
					.summary("디렉터 프로필 상세 조회")
					.description("내 디렉터 프로필의 상세 정보를 조회합니다.")
					.build())
			));
	}
}
