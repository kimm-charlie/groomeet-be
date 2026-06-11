package com.motd.be.rest_docs.module.director.director_profile_detail;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.director.director_profile_detail.dto.request.DirectorProfileUpdateRequestForDirector;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class DirectorProfileDetailForDirectorRestDocsTest extends BaseRestDocsTest {

	@Test
	void 디렉터_프로필_상세_수정() throws Exception {
		// given
		authenticationSetUp();
		DirectorProfileUpdateRequestForDirector request = DirectorProfileUpdateRequestForDirector.builder()
			.contentJson("{\"content\":\"소개 내용\"}")
			.fileIds(List.of(1L, 2L, 3L))
			.build();

		willDoNothing().given(directorProfileDetailFacadeForDirector).update(anyLong(), any());

		// when
		ResultActions result = mockMvc.perform(put("/api/directors/my/profile-detail")
			.contentType(MediaType.APPLICATION_JSON)
			.cookie(
				new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
				new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
			)
			.content(objectMapper.writeValueAsString(request)));

		// then
		result.andExpect(status().isNoContent())
			.andDo(document("director-profile-detail-update",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("contentJson").optional().type(JsonFieldType.STRING).description("프로필 상세 내용 JSON"),
					fieldWithPath("fileIds").optional().type(JsonFieldType.ARRAY).description("프로필 상세 내용 파일 아이디")
				),

				resource(builder()
					.tag("🎬 디렉터 프로필 상세 API")
					.summary("디렉터 프로필 상세 수정")
					.description("내 디렉터 프로필의 상세 정보를 수정합니다.")
					.build())
			));
	}
}
