package com.motd.be.rest_docs.module.member.banner;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.member.banner.dto.response.BannerFindAllResponse;
import com.motd.be.module.member.banner.dto.response.BannerResponse;

@RestDocsTest
public class BannerRestDocsTest extends BaseRestDocsTest {

	@Test
	void 배너_목록_조회() throws Exception {
		BannerResponse banner1 = BannerResponse.builder()
			.id(1L)
			.title("배너 1")
			.contentImageCdnUrl("https://cdn.example.com/content1.jpg")
			.thumbnailImageCdnUrl("https://cdn.example.com/thumb1.jpg")
			.isWebViewBanner(false)
			.webViewUrl(null)
			.build();

		BannerResponse banner2 = BannerResponse.builder()
			.id(2L)
			.title("배너 2")
			.contentImageCdnUrl("https://cdn.example.com/content2.jpg")
			.thumbnailImageCdnUrl("https://cdn.example.com/thumb2.jpg")
			.isWebViewBanner(true)
			.webViewUrl("https://example.com/webview")
			.build();

		BannerFindAllResponse response = BannerFindAllResponse.builder()
			.events(List.of(banner1, banner2))
			.totalCount(2)
			.build();

		given(bannerFacade.findAll()).willReturn(response);

		mockMvc.perform(get("/api/banners")
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andDo(document("banner-find-all",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

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
					.tag("🎨 배너 API")
					.summary("배너 목록 조회")
					.description("활성화된 모든 배너 목록을 조회합니다.")
					.build())
			));
	}
}
