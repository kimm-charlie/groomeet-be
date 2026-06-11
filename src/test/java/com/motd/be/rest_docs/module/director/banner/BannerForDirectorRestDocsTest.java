package com.motd.be.rest_docs.module.director.banner;

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

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.director.banner.dto.response.BannerFindAllResponseForDirector;
import com.motd.be.module.director.banner.dto.response.BannerResponseForDirector;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class BannerForDirectorRestDocsTest extends BaseRestDocsTest {

	@Test
	void 디렉터_배너_목록_조회() throws Exception {
		authenticationSetUp();

		BannerResponseForDirector banner1 = BannerResponseForDirector.builder()
			.id(1L)
			.title("디렉터 배너 1")
			.contentImageCdnUrl("https://cdn.example.com/director-content1.jpg")
			.thumbnailImageCdnUrl("https://cdn.example.com/director-thumb1.jpg")
			.isWebViewBanner(false)
			.webViewUrl(null)
			.build();

		BannerResponseForDirector banner2 = BannerResponseForDirector.builder()
			.id(2L)
			.title("디렉터 배너 2")
			.contentImageCdnUrl("https://cdn.example.com/director-content2.jpg")
			.thumbnailImageCdnUrl("https://cdn.example.com/director-thumb2.jpg")
			.isWebViewBanner(true)
			.webViewUrl("https://example.com/director-webview")
			.build();

		BannerFindAllResponseForDirector response = BannerFindAllResponseForDirector.builder()
			.events(List.of(banner1, banner2))
			.totalCount(2)
			.build();

		given(bannerFacadeForDirector.findAll()).willReturn(response);

		mockMvc.perform(get("/api/directors/banners")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andDo(document("banner-director-find-all",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("events").type(JsonFieldType.ARRAY).description("배너 목록"),
					fieldWithPath("events[].id").type(JsonFieldType.NUMBER).description("배너 ID"),
					fieldWithPath("events[].title").type(JsonFieldType.STRING).description("배너 제목"),
					fieldWithPath("events[].contentImageCdnUrl").type(JsonFieldType.STRING)
						.optional()
						.description("배너 콘텐츠 이미지 CDN URL"),
					fieldWithPath("events[].thumbnailImageCdnUrl").type(JsonFieldType.STRING)
						.description("배너 썸네일 이미지 CDN URL"),
					fieldWithPath("events[].isWebViewBanner").type(JsonFieldType.BOOLEAN).description("웹뷰 배너 여부"),
					fieldWithPath("events[].webViewUrl")
						.optional()
						.type(JsonFieldType.STRING)
						.description("웹뷰 URL"),
					fieldWithPath("totalCount").type(JsonFieldType.NUMBER).description("전체 배너 개수")
				),

				resource(builder()
					.tag("🎨 디렉터 배너 API")
					.summary("디렉터 배너 목록 조회")
					.description("디렉터용 활성화 배너 목록을 조회합니다.")
					.build())
			));
	}
}
