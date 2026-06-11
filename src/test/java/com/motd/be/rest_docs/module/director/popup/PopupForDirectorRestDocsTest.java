package com.motd.be.rest_docs.module.director.popup;

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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.director.popup.dto.response.PopupFindAllResponseForDirector;
import com.motd.be.module.director.popup.dto.response.PopupResponseForDirector;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class PopupForDirectorRestDocsTest extends BaseRestDocsTest {

	@Test
	@DisplayName("디렉터 팝업 목록 조회 API")
	void 디렉터_팝업_목록_조회() throws Exception {
		authenticationSetUp();

		PopupResponseForDirector popup1 = PopupResponseForDirector.builder()
			.id(1L)
			.title("디렉터 팝업 1")
			.thumbnailImageUrl("https://cdn.example.com/director-popup-thumb1.jpg")
			.linkUrl(LINK_URL)
			.build();

		PopupResponseForDirector popup2 = PopupResponseForDirector.builder()
			.id(2L)
			.title("디렉터 팝업 2")
			.thumbnailImageUrl("https://cdn.example.com/director-popup-thumb2.jpg")
			.linkUrl(LINK_URL)
			.build();

		PopupFindAllResponseForDirector response = PopupFindAllResponseForDirector.builder()
			.popups(List.of(popup1, popup2))
			.totalCount(2)
			.build();

		given(popupFacadeForDirector.findAll()).willReturn(response);

		mockMvc.perform(get("/api/directors/popups")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andDo(document("popup-director-find-all",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("popups").type(JsonFieldType.ARRAY).description("팝업 목록"),
					fieldWithPath("popups[].id").type(JsonFieldType.NUMBER).description("팝업 ID"),
					fieldWithPath("popups[].title").type(JsonFieldType.STRING).description("팝업 제목"),
					fieldWithPath("popups[].thumbnailImageUrl").type(JsonFieldType.STRING)
						.description("팝업 썸네일 이미지 URL"),
					fieldWithPath("popups[].linkUrl").type(JsonFieldType.STRING).description("팝업 링크 URL"),
					fieldWithPath("totalCount").type(JsonFieldType.NUMBER).description("전체 팝업 개수")
				),

				resource(builder()
					.tag("🎨 디렉터 팝업 API")
					.summary("디렉터 팝업 목록 조회")
					.description("디렉터용 활성화 팝업 목록을 조회합니다.")
					.build())
			));
	}
}
