package com.motd.be.rest_docs.module.admin.popup;

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
import com.motd.be.module.admin.popup.dto.request.PopupSaveRequestForAdmin;
import com.motd.be.module.admin.popup.dto.request.PopupUpdateRequestForAdmin;
import com.motd.be.module.admin.popup.dto.response.PopupAdminFindAllResponseForAdmin;
import com.motd.be.module.admin.popup.dto.response.PopupAdminResponseForAdmin;
import com.motd.be.module.member.popup.entity.PopupType;

import jakarta.servlet.http.Cookie;

@RestDocsTest
class PopupAdminRestDocsTest extends BaseRestDocsTest {

	@Test
	@DisplayName("팝업 전체 조회")
	void getAllPopups() throws Exception {
		// given
		authenticationSetUp();

		PopupAdminFindAllResponseForAdmin response = PopupAdminFindAllResponseForAdmin.builder()
			.page(PAGE)
			.hasNext(Boolean.FALSE)
			.popups(List.of(PopupAdminResponseForAdmin.builder()
					.id(ID)
					.title(TITLE_STR)
					.thumbnailImageUrl(THUMBNAIL_IMAGE_URL)
					.linkUrl(LINK_URL)
					.type(PopupType.MEMBER)
					.createdAt(formatToDateString(LocalDateTime.now()))
					.startAt(formatToDateString(LocalDateTime.now().minusDays(1)))
					.endAt(formatToDateString(LocalDateTime.now().plusDays(7)))
					.isDeleted(false)
					.sortOrder(1)
					.thumbnailFileId(1L)
					.build(),
				PopupAdminResponseForAdmin.builder()
					.id(ID)
					.title(TITLE_STR)
					.thumbnailImageUrl(THUMBNAIL_IMAGE_URL)
					.linkUrl(LINK_URL)
					.type(PopupType.DIRECTOR)
					.createdAt(formatToDateString(LocalDateTime.now()))
					.startAt(formatToDateString(LocalDateTime.now().minusDays(1)))
					.endAt(formatToDateString(LocalDateTime.now().plusDays(7)))
					.isDeleted(false)
					.sortOrder(2)
					.thumbnailFileId(1L)
					.build()
			))
			.build();

		given(popupAdminFacade.findAll(anyInt(), any(Boolean.class), any())).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/admin/popups")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)
				)
				.param(PAGE_STR, String.valueOf(PAGE))
				.param(SHOW_IS_DELETED_STR, Boolean.FALSE.toString())
				.param(TYPE_STR, PopupType.MEMBER.name())
			)
			.andExpect(status().isOk())
			.andDo(document("admin-popup-find-all",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(PAGE_STR)
						.optional()
						.description("페이지 네이션 변수"),

					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(SHOW_IS_DELETED_STR)
						.optional()
						.description("삭제/종료된 팝업 포함 여부"),

					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(TYPE_STR)
						.optional()
						.attributes(enumFormat(PopupType.class, Enum::name))
						.description("팝업 타입")
				),

				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER)
						.description("현재 페이지 번호"),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN)
						.description("다음 페이지 존재 여부"),
					fieldWithPath("popups").type(JsonFieldType.ARRAY)
						.description("팝업 목록"),
					fieldWithPath("popups[].id").type(JsonFieldType.NUMBER)
						.description("팝업 ID"),
					fieldWithPath("popups[].title").type(JsonFieldType.STRING)
						.description("팝업 내용"),
					fieldWithPath("popups[].thumbnailImageUrl").type(JsonFieldType.STRING)
						.description("팝업 썸네일 이미지 URL"),
					fieldWithPath("popups[].linkUrl").type(JsonFieldType.STRING)
						.optional()
						.description("클릭 시 이동 URL"),
					fieldWithPath("popups[].startAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("팝업 시작 시간"),
					fieldWithPath("popups[].endAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("팝업 종료 시간"),
					fieldWithPath("popups[].isDeleted").type(JsonFieldType.BOOLEAN)
						.description("팝업 삭제 여부"),
					fieldWithPath("popups[].createdAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("팝업 생성 시간"),
					fieldWithPath("popups[].sortOrder").type(JsonFieldType.NUMBER)
						.description("정렬 순서"),
					fieldWithPath("popups[].thumbnailFileId").type(JsonFieldType.NUMBER)
						.description("팝업 썸네일 파일 ID"),
					fieldWithPath("popups[].type").type(JsonFieldType.STRING)
						.attributes(enumFormat(PopupType.class, Enum::name))
						.description("팝업 타입")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 팝업 전체 조회 API")
					.description("관리자 팝업 전체 조회 API")
					.queryParameters(
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName(PAGE_STR)
							.optional()
							.description("페이지 네이션 변수"),
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName(SHOW_IS_DELETED_STR)
							.optional()
							.description("삭제/종료된 팝업 포함 여부"),
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName(TYPE_STR)
							.optional()
							.attributes(enumFormat(PopupType.class, Enum::name))
							.description("팝업 타입")
					)
					.build()
				)
			));
	}

	@Test
	@DisplayName("팝업 상세 조회")
	void getPopupDetail() throws Exception {
		// given
		authenticationSetUp();

		PopupAdminResponseForAdmin response = PopupAdminResponseForAdmin.builder()
			.id(ID)
			.title(TITLE_STR)
			.thumbnailImageUrl(THUMBNAIL_IMAGE_URL)
			.linkUrl(LINK_URL)
			.type(PopupType.MEMBER)
			.createdAt(formatToDateString(LocalDateTime.now()))
			.startAt(formatToDateString(LocalDateTime.now().minusDays(1)))
			.endAt(formatToDateString(LocalDateTime.now().plusDays(7)))
			.isDeleted(false)
			.sortOrder(1)
			.thumbnailFileId(1L)
			.build();

		given(popupAdminFacade.findPopupById(any(Long.class))).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/admin/popups/{popupId}", ID)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)
				))
			.andExpect(status().isOk())
			.andDo(document("admin-popup-find-detail",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("popupId")
						.description("상세 조회할 팝업 ID")
				),

				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER)
						.description("팝업 ID"),
					fieldWithPath("title").type(JsonFieldType.STRING)
						.description("팝업 내용"),
					fieldWithPath("thumbnailImageUrl").type(JsonFieldType.STRING)
						.description("팝업 썸네일 이미지 URL"),
					fieldWithPath("linkUrl").type(JsonFieldType.STRING)
						.optional()
						.description("클릭 시 이동 URL"),
					fieldWithPath("startAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("팝업 시작 시간"),
					fieldWithPath("endAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("팝업 종료 시간"),
					fieldWithPath("isDeleted").type(JsonFieldType.BOOLEAN)
						.description("팝업 삭제 여부"),
					fieldWithPath("createdAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("팝업 생성 시간"),
					fieldWithPath("sortOrder").type(JsonFieldType.NUMBER)
						.description("정렬 순서"),
					fieldWithPath("thumbnailFileId").type(JsonFieldType.NUMBER)
						.description("팝업 썸네일 파일 ID"),
					fieldWithPath("type").type(JsonFieldType.STRING)
						.attributes(enumFormat(PopupType.class, Enum::name))
						.description("팝업 타입")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 팝업 상세 조회 API")
					.description("관리자 팝업 상세 조회 API")
					.build()
				)
			));
	}

	@Test
	@DisplayName("팝업 삭제")
	void deletePopup() throws Exception {
		// given
		authenticationSetUp();

		doNothing().when(popupAdminFacade).delete(any(Long.class));

		// when & then
		mockMvc.perform(delete("/api/admin/popups/{popupId}", ID)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)
				))
			.andExpect(status().isNoContent())
			.andDo(document("admin-popup-delete",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("popupId")
						.description("삭제할 팝업 ID")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 팝업 삭제 API")
					.description("관리자 팝업 삭제 API")
					.build()
				)
			));
	}

	@Test
	@DisplayName("팝업 저장")
	void savePopup() throws Exception {
		// given
		authenticationSetUp();

		PopupAdminResponseForAdmin response = PopupAdminResponseForAdmin.builder()
			.id(ID)
			.title(TITLE_STR)
			.thumbnailImageUrl(THUMBNAIL_IMAGE_URL)
			.linkUrl(LINK_URL)
			.type(PopupType.MEMBER)
			.createdAt(formatToDateString(LocalDateTime.now()))
			.startAt(formatToDateString(LocalDateTime.now().plusDays(1)))
			.endAt(formatToDateString(LocalDateTime.now().plusDays(7)))
			.isDeleted(false)
			.sortOrder(1)
			.thumbnailFileId(1L)
			.build();

		given(popupAdminFacade.save(any(PopupSaveRequestForAdmin.class))).willReturn(response);

		PopupSaveRequestForAdmin request = PopupSaveRequestForAdmin.builder()
			.title(TITLE_STR)
			.startAt(formatToDateString(LocalDateTime.now().plusDays(1)))
			.endAt(formatToDateString(LocalDateTime.now().plusDays(7)))
			.sortOrder(1)
			.thumbnailFileId(1L)
			.linkUrl(LINK_URL)
			.type(PopupType.MEMBER)
			.build();

		// when & then
		mockMvc.perform(post("/api/admin/popups")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)
				)
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andDo(document("admin-popup-save",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				requestFields(
					fieldWithPath("title").type(JsonFieldType.STRING)
						.description("팝업 제목 (최대 100자)"),
					fieldWithPath("startAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("팝업 시작 시간"),
					fieldWithPath("endAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("팝업 종료 시간"),
					fieldWithPath("sortOrder").type(JsonFieldType.NUMBER)
						.description("정렬 순서"),
					fieldWithPath("thumbnailFileId").type(JsonFieldType.NUMBER)
						.description("팝업 썸네일 파일 ID"),
					fieldWithPath("linkUrl").type(JsonFieldType.STRING)
						.optional()
						.description("클릭 시 이동 URL"),
					fieldWithPath("type").type(JsonFieldType.STRING)
						.attributes(enumFormat(PopupType.class, Enum::name))
						.description("팝업 타입")
				),

				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER)
						.description("생성된 팝업 ID"),
					fieldWithPath("title").type(JsonFieldType.STRING)
						.description("팝업 내용"),
					fieldWithPath("thumbnailImageUrl").type(JsonFieldType.STRING)
						.description("팝업 썸네일 이미지 URL"),
					fieldWithPath("linkUrl").type(JsonFieldType.STRING)
						.optional()
						.description("클릭 시 이동 URL"),
					fieldWithPath("type").type(JsonFieldType.STRING)
						.attributes(enumFormat(PopupType.class, Enum::name))
						.description("팝업 타입"),
					fieldWithPath("startAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("팝업 시작 시간"),
					fieldWithPath("endAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("팝업 종료 시간"),
					fieldWithPath("isDeleted").type(JsonFieldType.BOOLEAN)
						.description("팝업 삭제 여부"),
					fieldWithPath("createdAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("팝업 생성 시간"),
					fieldWithPath("sortOrder").type(JsonFieldType.NUMBER)
						.description("정렬 순서"),
					fieldWithPath("thumbnailFileId").type(JsonFieldType.NUMBER)
						.description("팝업 썸네일 파일 ID")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 팝업 저장 API")
					.description("관리자 팝업 저장 API")
					.build()
				)
			));
	}

	@Test
	@DisplayName("팝업 수정")
	void updatePopup() throws Exception {
		// given
		authenticationSetUp();

		willDoNothing().given(popupAdminFacade)
			.update(any(Long.class), any(PopupUpdateRequestForAdmin.class));

		PopupUpdateRequestForAdmin request = PopupUpdateRequestForAdmin.builder()
			.title(TITLE_STR)
			.startAt(formatToDateString(LocalDateTime.now().plusDays(1)))
			.endAt(formatToDateString(LocalDateTime.now().plusDays(5)))
			.sortOrder(1)
			.thumbnailFileId(1L)
			.linkUrl(LINK_URL)
			.build();

		// when & then
		mockMvc.perform(put("/api/admin/popups/{popupId}", ID)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)
				)
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent())
			.andDo(document("admin-popup-update",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("popupId")
						.description("수정할 팝업 ID")
				),

				requestFields(
					fieldWithPath("title").type(JsonFieldType.STRING)
						.description("팝업 제목 (최대 100자)"),
					fieldWithPath("startAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("팝업 시작 시간"),
					fieldWithPath("endAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("팝업 종료 시간"),
					fieldWithPath("sortOrder").type(JsonFieldType.NUMBER)
						.description("정렬 순서"),
					fieldWithPath("thumbnailFileId").type(JsonFieldType.NUMBER)
						.description("팝업 썸네일 파일 ID"),
					fieldWithPath("linkUrl").type(JsonFieldType.STRING)
						.optional()
						.description("클릭 시 이동 URL")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 팝업 수정 API")
					.description("관리자 팝업 수정 API")
					.pathParameters(
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName("popupId")
							.description("수정할 팝업 ID")
					)
					.build()
				)
			));
	}

}
