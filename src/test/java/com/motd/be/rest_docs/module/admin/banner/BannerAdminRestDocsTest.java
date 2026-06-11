package com.motd.be.rest_docs.module.admin.banner;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.common.utils.DateFormatUtils.*;
import static com.motd.be.rest_docs.Utils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.admin.banner.dto.request.BannerSaveRequestForAdmin;
import com.motd.be.module.admin.banner.dto.request.BannerUpdateRequestForAdmin;
import com.motd.be.module.admin.banner.dto.response.BannerAdminFindAllResponseForAdmin;
import com.motd.be.module.admin.banner.dto.response.BannerAdminResponseForAdmin;
import com.motd.be.module.member.banner.entity.BannerType;

import jakarta.servlet.http.Cookie;

@RestDocsTest
class BannerAdminRestDocsTest extends BaseRestDocsTest {

	@Test
	@DisplayName("관리자 배너 전체 조회")
	void getAllBanners() throws Exception {
		// given
		authenticationSetUp();

		BannerAdminFindAllResponseForAdmin response = BannerAdminFindAllResponseForAdmin.builder()
			.page(PAGE)
			.hasNext(Boolean.FALSE)
			.totalCount(2)
			.banners(List.of(
				BannerAdminResponseForAdmin.builder()
					.id(ID)
					.title(TITLE_STR)
					.thumbnailImageUrl(THUMBNAIL_IMAGE_URL)
					.contentImageUrl(CONTENT_IMAGE_URL)
					.createdAt(formatToDateString(LocalDateTime.now()))
					.startAt(formatToDateString(LocalDateTime.now().minusDays(1)))
					.endAt(formatToDateString(LocalDateTime.now().plusDays(1)))
					.isDeleted(false)
					.sortOrder(1)
					.isWebViewBanner(false)
					.webViewUrl(null)
					.thumbnailFileId(1L)
					.contentFileId(1L)
					.type("MEMBER")
					.build(),
				BannerAdminResponseForAdmin.builder()
					.id(ID)
					.title(TITLE_STR)
					.thumbnailImageUrl(THUMBNAIL_IMAGE_URL)
					.contentImageUrl(null)
					.createdAt(formatToDateString(LocalDateTime.now()))
					.startAt(formatToDateString(LocalDateTime.now().minusDays(1)))
					.endAt(formatToDateString(LocalDateTime.now().plusDays(1)))
					.isDeleted(false)
					.sortOrder(2)
					.isWebViewBanner(true)
					.webViewUrl("https://example.com")
					.thumbnailFileId(1L)
					.contentFileId(1L)
					.type("MEMBER")
					.build()
			))
			.build();

		given(bannerFacadeForAdmin.findAll(anyInt(), any(Boolean.class), any())).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/admin/banners")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)
				)
				.param(PAGE_STR, String.valueOf(PAGE))
				.param(SHOW_IS_DELETED_STR, Boolean.FALSE.toString())
				.param(TYPE_STR, BannerType.MEMBER.name()))
			.andExpect(status().isOk())
			.andDo(document("admin-banner-find-all",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(PAGE_STR)
						.optional()
						.description("페이지 번호"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(SHOW_IS_DELETED_STR)
						.optional()
						.description("삭제된 배너 포함 여부 true -> 삭제된 배너 포함, false -> 삭제된 배너 제외"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(TYPE_STR)
						.optional()
						.attributes(enumFormat(BannerType.class, Enum::name))
						.description("배너 타입")
				),

				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER)
						.description("현재 페이지 번호"),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN)
						.description("다음 페이지 존재 여부"),
					fieldWithPath("totalCount").type(JsonFieldType.NUMBER)
						.description("배너 총 개수"),
					fieldWithPath("banners").type(JsonFieldType.ARRAY)
						.description("배너 목록"),
					fieldWithPath("banners[].id").type(JsonFieldType.NUMBER)
						.description("배너 ID"),
					fieldWithPath("banners[].title").type(JsonFieldType.STRING)
						.optional()
						.description("배너 제목"),
					fieldWithPath("banners[].thumbnailImageUrl").type(JsonFieldType.STRING)
						.description("배너 썸네일 이미지 URL"),
					fieldWithPath("banners[].contentImageUrl").type(JsonFieldType.STRING)
						.optional()
						.description("배너 컨텐트 이미지 URL"),
					fieldWithPath("banners[].createdAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("배너 생성 시간"),
					fieldWithPath("banners[].startAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("배너 시작 시간"),
					fieldWithPath("banners[].endAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("배너 종료 시간"),
					fieldWithPath("banners[].sortOrder").type(JsonFieldType.NUMBER)
						.description("배너 정렬 순서"),
					fieldWithPath("banners[].isDeleted").type(JsonFieldType.BOOLEAN)
						.description("배너 삭제 여부"),
					fieldWithPath("banners[].isWebViewBanner").type(JsonFieldType.BOOLEAN)
						.description("웹뷰 배너 여부"),
					fieldWithPath("banners[].webViewUrl").type(JsonFieldType.STRING)
						.optional()
						.description("웹뷰 URL"),
					fieldWithPath("banners[].type").type(JsonFieldType.STRING)
						.description("배너 타입"),
					fieldWithPath("banners[].thumbnailFileId").type(JsonFieldType.NUMBER)
						.description("배너 썸네일 파일 ID"),
					fieldWithPath("banners[].contentFileId").type(JsonFieldType.NUMBER)
						.optional()
						.description("배너 컨텐츠 파일 ID")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 배너 전체 조회 API")
					.description("관리자 배너 전체 조회 API")
					.queryParameters(
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName(PAGE_STR)
							.optional()
							.description("페이지 번호"),
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName(SHOW_IS_DELETED_STR)
							.optional()
							.description("삭제된 배너 포함 여부"),
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName(TYPE_STR)
							.optional()
							.description("배너 타입 (MEMBER, DIRECTOR)")
					)
					.build()
				)
			));
	}

	@Test
	@DisplayName("관리자 배너 상세 조회")
	void getBannerDetail() throws Exception {
		// given
		authenticationSetUp();

		BannerAdminResponseForAdmin response = BannerAdminResponseForAdmin.builder()
			.id(ID)
			.title(TITLE_STR)
			.thumbnailImageUrl(THUMBNAIL_IMAGE_URL)
			.contentImageUrl(CONTENT_IMAGE_URL)
			.createdAt(formatToDateString(LocalDateTime.now()))
			.startAt(formatToDateString(LocalDateTime.now().minusDays(1)))
			.endAt(formatToDateString(LocalDateTime.now().plusDays(1)))
			.isDeleted(false)
			.sortOrder(1)
			.isWebViewBanner(false)
			.webViewUrl(null)
			.type("MEMBER")
			.thumbnailFileId(1L)
			.contentFileId(1L)
			.build();

		given(bannerFacadeForAdmin.findDetail(any(Long.class))).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/admin/banners/{bannerId}", ID)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)
				))
			.andExpect(status().isOk())
			.andDo(document("admin-banner-find-detail",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("bannerId")
						.description("상세 조회할 배너 ID")
				),

				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER)
						.description("배너 ID"),
					fieldWithPath("title").type(JsonFieldType.STRING)
						.optional()
						.description("배너 제목"),
					fieldWithPath("thumbnailImageUrl").type(JsonFieldType.STRING)
						.description("배너 썸네일 이미지 URL"),
					fieldWithPath("contentImageUrl").type(JsonFieldType.STRING)
						.optional()
						.description("배너 컨텐트 이미지 URL"),
					fieldWithPath("createdAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("배너 생성 시간"),
					fieldWithPath("startAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("배너 시작 시간"),
					fieldWithPath("endAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("배너 종료 시간"),
					fieldWithPath("sortOrder").type(JsonFieldType.NUMBER)
						.description("배너 정렬 순서"),
					fieldWithPath("isDeleted").type(JsonFieldType.BOOLEAN)
						.description("배너 삭제 여부"),
					fieldWithPath("isWebViewBanner").type(JsonFieldType.BOOLEAN)
						.description("웹뷰 배너 여부"),
					fieldWithPath("webViewUrl").type(JsonFieldType.STRING)
						.optional()
						.description("웹뷰 URL"),
					fieldWithPath("type").type(JsonFieldType.STRING)
						.description("배너 타입"),
					fieldWithPath("thumbnailFileId").type(JsonFieldType.NUMBER)
						.description("배너 썸네일 파일 ID"),
					fieldWithPath("contentFileId").type(JsonFieldType.NUMBER)
						.optional()
						.description("배너 컨텐츠 파일 ID")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 배너 상세 조회 API")
					.description("관리자 배너 상세 조회 API")
					.build()
				)
			));
	}

	@Test
	@DisplayName("관리자 배너 등록")
	void createBanner() throws Exception {
		// given
		authenticationSetUp();

		willDoNothing().given(bannerFacadeForAdmin).save(any(BannerSaveRequestForAdmin.class));

		BannerSaveRequestForAdmin request = BannerSaveRequestForAdmin.builder()
			.title(TITLE_STR)
			.startAt(formatToDateString(LocalDateTime.now().plusDays(1)))
			.endAt(formatToDateString(LocalDateTime.now().plusDays(7)))
			.sortOrder(1)
			.isWebViewBanner(false)
			.webViewUrl(null)
			.type(BannerType.MEMBER)
			.thumbnailFileId(1L)
			.contentFileId(2L)
			.build();

		// when & then
		mockMvc.perform(post("/api/admin/banners")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)
				)
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andDo(document("admin-banner-create",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				requestFields(
					fieldWithPath("title").type(JsonFieldType.STRING)
						.optional()
						.description("배너 제목"),
					fieldWithPath("startAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("배너 시작 시간"),
					fieldWithPath("endAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("배너 종료 시간"),
					fieldWithPath("sortOrder").type(JsonFieldType.NUMBER)
						.description("배너 정렬 순서"),
					fieldWithPath("isWebViewBanner").type(JsonFieldType.BOOLEAN)
						.description("웹뷰 배너 여부"),
					fieldWithPath("webViewUrl").type(JsonFieldType.STRING)
						.optional()
						.description("웹뷰 URL"),
					fieldWithPath("type").type(JsonFieldType.STRING)
						.description("배너 타입"),
					fieldWithPath("thumbnailFileId").type(JsonFieldType.NUMBER)
						.description("썸네일 파일 ID"),
					fieldWithPath("contentFileId").type(JsonFieldType.NUMBER)
						.optional()
						.description("컨텐츠 파일 ID")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 배너 등록 API")
					.description("관리자 배너 등록 API")
					.build()
				)
			));
	}

	@Test
	@DisplayName("관리자 배너 수정")
	void updateBanner() throws Exception {
		// given
		authenticationSetUp();

		willDoNothing().given(bannerFacadeForAdmin).update(any(Long.class), any(BannerUpdateRequestForAdmin.class));

		BannerUpdateRequestForAdmin request = BannerUpdateRequestForAdmin.builder()
			.title(TITLE_STR)
			.startAt(formatToDateString(LocalDateTime.now().plusDays(1)))
			.endAt(formatToDateString(LocalDateTime.now().plusDays(7)))
			.sortOrder(1)
			.isWebViewBanner(false)
			.webViewUrl(null)
			.thumbnailFileId(1L)
			.contentFileId(2L)
			.build();

		// when & then
		mockMvc.perform(put("/api/admin/banners/{bannerId}", ID)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)
				)
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent())
			.andDo(document("admin-banner-update",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("bannerId")
						.description("수정할 배너 ID")
				),

				requestFields(
					fieldWithPath("title").type(JsonFieldType.STRING)
						.optional()
						.description("배너 제목"),
					fieldWithPath("startAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("배너 시작 시간"),
					fieldWithPath("endAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("배너 종료 시간"),
					fieldWithPath("sortOrder").type(JsonFieldType.NUMBER)
						.description("배너 정렬 순서"),
					fieldWithPath("isWebViewBanner").type(JsonFieldType.BOOLEAN)
						.description("웹뷰 배너 여부"),
					fieldWithPath("webViewUrl").type(JsonFieldType.STRING)
						.optional()
						.description("웹뷰 URL"),
					fieldWithPath("thumbnailFileId").type(JsonFieldType.NUMBER)
						.description("썸네일 파일 ID"),
					fieldWithPath("contentFileId").type(JsonFieldType.NUMBER)
						.optional()
						.description("컨텐츠 파일 ID")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 배너 수정 API")
					.description("관리자 배너 수정 API")
					.pathParameters(
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName("bannerId")
							.description("수정할 배너 ID")
					)
					.build()
				)
			));
	}

	@Test
	@DisplayName("관리자 배너 삭제")
	void deleteBanner() throws Exception {
		// given
		authenticationSetUp();

		willDoNothing().given(bannerFacadeForAdmin).delete(any(Long.class));

		// when & then
		mockMvc.perform(delete("/api/admin/banners/{bannerId}", ID)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)
				))
			.andExpect(status().isNoContent())
			.andDo(document("admin-banner-delete",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("bannerId")
						.description("삭제할 배너 ID")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 배너 삭제 API")
					.description("관리자 배너 삭제 API")
					.pathParameters(
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName("bannerId")
							.description("삭제할 배너 ID")
					)
					.build()
				)
			));
	}
}
