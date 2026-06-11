package com.motd.be.rest_docs.module.admin.director_service;

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
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.admin.director_service.dto.request.DirectorServiceSaveRequestForAdmin;
import com.motd.be.module.admin.director_service.dto.request.DirectorServiceUpdateRequestForAdmin;
import com.motd.be.module.admin.director_service.dto.response.DirectorServiceFindAllResponseForAdmin;
import com.motd.be.module.admin.director_service.dto.response.DirectorServiceResponseForAdmin;

import jakarta.servlet.http.Cookie;

@RestDocsTest
class DirectorServiceAdminRestDocsTest extends BaseRestDocsTest {

	@Test
	@DisplayName("관리자 디렉터 서비스 전체 조회")
	void findAll() throws Exception {
		// given
		authenticationSetUp();

		DirectorServiceFindAllResponseForAdmin response = DirectorServiceFindAllResponseForAdmin.builder()
			.totalCount(2)
			.page(PAGE)
			.hasNext(false)
			.directorServices(List.of(
				DirectorServiceResponseForAdmin.builder()
					.id(1L)
					.name("부모 서비스")
					.parentId(null)
					.parentName(null)
					.isActive(true)
					.isDeleted(false)
					.sortOrder(0)
					.createdAt(formatToDateString(LocalDateTime.now()))
					.build(),
				DirectorServiceResponseForAdmin.builder()
					.id(2L)
					.name("자식 서비스")
					.parentId(1L)
					.parentName("부모 서비스")
					.isActive(true)
					.isDeleted(false)
					.sortOrder(1)
					.createdAt(formatToDateString(LocalDateTime.now()))
					.build()
			))
			.build();

		given(directorServiceFacadeForAdmin.findAll(anyInt(), any(Boolean.class), any())).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/admin/director-services")
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR))
				.param(PAGE_STR, String.valueOf(PAGE))
				.param(SHOW_IS_DELETED_STR, Boolean.FALSE.toString())
				.param("parentId", "1"))
			.andExpect(status().isOk())
			.andDo(document("admin-director-service-find-all",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(PAGE_STR)
						.optional()
						.description("페이지 번호 (기본값: 0)"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(SHOW_IS_DELETED_STR)
						.optional()
						.description("삭제된 서비스 포함 여부"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("parentId")
						.optional()
						.description("부모 서비스 ID (없으면 최상위 서비스 조회)")
				),

				responseFields(
					fieldWithPath("totalCount").type(JsonFieldType.NUMBER)
						.description("서비스 총 개수"),
					fieldWithPath("page").type(JsonFieldType.NUMBER)
						.description("현재 페이지 번호"),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN)
						.description("다음 페이지 존재 여부"),
					fieldWithPath("directorServices").type(JsonFieldType.ARRAY)
						.description("서비스 목록"),
					fieldWithPath("directorServices[].id").type(JsonFieldType.NUMBER)
						.description("서비스 ID"),
					fieldWithPath("directorServices[].name").type(JsonFieldType.STRING)
						.description("서비스 이름"),
					fieldWithPath("directorServices[].parentId").type(JsonFieldType.NUMBER)
						.optional()
						.description("부모 서비스 ID"),
					fieldWithPath("directorServices[].parentName").type(JsonFieldType.STRING)
						.optional()
						.description("부모 서비스 이름"),
					fieldWithPath("directorServices[].isActive").type(JsonFieldType.BOOLEAN)
						.description("활성화 여부"),
					fieldWithPath("directorServices[].isDeleted").type(JsonFieldType.BOOLEAN)
						.description("삭제 여부"),
					fieldWithPath("directorServices[].sortOrder").type(JsonFieldType.NUMBER)
						.description("정렬 순서"),
					fieldWithPath("directorServices[].createdAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("생성 일시")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 디렉터 서비스 전체 조회 API")
					.description("관리자 디렉터 서비스 전체 조회 API")
					.queryParameters(
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName(PAGE_STR)
							.optional()
							.description("페이지 번호"),
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName(SHOW_IS_DELETED_STR)
							.optional()
							.description("삭제된 서비스 포함 여부"),
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName("parentId")
							.optional()
							.description("부모 서비스 ID")
					)
					.build()
				)
			));
	}

	@Test
	@DisplayName("관리자 디렉터 서비스 상세 조회")
	void findDetail() throws Exception {
		// given
		authenticationSetUp();

		DirectorServiceResponseForAdmin response = DirectorServiceResponseForAdmin.builder()
			.id(2L)
			.name("자식 서비스")
			.parentId(1L)
			.parentName("부모 서비스")
			.isActive(true)
			.isDeleted(false)
			.sortOrder(1)
			.createdAt(formatToDateString(LocalDateTime.now()))
			.build();

		given(directorServiceFacadeForAdmin.findDetail(anyLong())).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/admin/director-services/{directorServiceId}", ID)
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)))
			.andExpect(status().isOk())
			.andDo(document("admin-director-service-find-detail",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(DIRECTOR_SERVICE_ID_STR)
						.description("디렉터 서비스 ID")
				),

				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER)
						.description("서비스 ID"),
					fieldWithPath("name").type(JsonFieldType.STRING)
						.description("서비스 이름"),
					fieldWithPath("parentId").type(JsonFieldType.NUMBER)
						.optional()
						.description("부모 서비스 ID"),
					fieldWithPath("parentName").type(JsonFieldType.STRING)
						.optional()
						.description("부모 서비스 이름"),
					fieldWithPath("isActive").type(JsonFieldType.BOOLEAN)
						.description("활성화 여부"),
					fieldWithPath("isDeleted").type(JsonFieldType.BOOLEAN)
						.description("삭제 여부"),
					fieldWithPath("sortOrder").type(JsonFieldType.NUMBER)
						.description("정렬 순서"),
					fieldWithPath("createdAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("생성 일시")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 디렉터 서비스 상세 조회 API")
					.description("관리자 디렉터 서비스 상세 조회 API")
					.pathParameters(
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName(
								DIRECTOR_SERVICE_ID_STR)
							.description("디렉터 서비스 ID")
					)
					.build()
				)
			));
	}

	@Test
	@DisplayName("관리자 디렉터 서비스 등록")
	void save() throws Exception {
		// given
		authenticationSetUp();

		DirectorServiceSaveRequestForAdmin request = DirectorServiceSaveRequestForAdmin.builder()
			.name("새 서비스")
			.parentId(1L)
			.isActive(true)
			.sortOrder(1)
			.build();

		willDoNothing().given(directorServiceFacadeForAdmin).save(any());

		// when & then
		mockMvc.perform(post("/api/admin/director-services")
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andDo(document("admin-director-service-save",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				requestFields(
					fieldWithPath("name").type(JsonFieldType.STRING)
						.description("서비스 이름"),
					fieldWithPath("parentId").type(JsonFieldType.NUMBER)
						.optional()
						.description("부모 서비스 ID (없으면 최상위 서비스)"),
					fieldWithPath("isActive").type(JsonFieldType.BOOLEAN)
						.description("활성화 여부"),
					fieldWithPath("sortOrder").type(JsonFieldType.NUMBER)
						.description("정렬 순서")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 디렉터 서비스 등록 API")
					.description("관리자 디렉터 서비스 등록 API")
					.build()
				)
			));
	}

	@Test
	@DisplayName("관리자 디렉터 서비스 수정")
	void update() throws Exception {
		// given
		authenticationSetUp();

		DirectorServiceUpdateRequestForAdmin request = DirectorServiceUpdateRequestForAdmin.builder()
			.name("수정된 서비스")
			.parentId(1L)
			.isActive(true)
			.sortOrder(2)
			.build();

		willDoNothing().given(directorServiceFacadeForAdmin).update(anyLong(), any());

		// when & then
		mockMvc.perform(put("/api/admin/director-services/{directorServiceId}", ID)
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent())
			.andDo(document("admin-director-service-update",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(DIRECTOR_SERVICE_ID_STR)
						.description("수정할 디렉터 서비스 ID")
				),

				requestFields(
					fieldWithPath("name").type(JsonFieldType.STRING)
						.description("서비스 이름"),
					fieldWithPath("isActive").type(JsonFieldType.BOOLEAN)
						.description("활성화 여부"),
					fieldWithPath("sortOrder").type(JsonFieldType.NUMBER)
						.description("정렬 순서"),
					fieldWithPath("parentId").type(JsonFieldType.NUMBER)
						.optional()
						.description("부모 서비스 ID (없으면 최상위 서비스)")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 디렉터 서비스 수정 API")
					.description("관리자 디렉터 서비스 수정 API")
					.pathParameters(
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName(
								DIRECTOR_SERVICE_ID_STR)
							.description("수정할 디렉터 서비스 ID")
					)
					.build()
				)
			));
	}

	@Test
	@DisplayName("관리자 디렉터 서비스 삭제")
	void deleteDirectorService() throws Exception {
		// given
		authenticationSetUp();

		willDoNothing().given(directorServiceFacadeForAdmin).delete(anyLong());

		// when & then
		mockMvc.perform(delete("/api/admin/director-services/{directorServiceId}", ID)
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)))
			.andExpect(status().isNoContent())
			.andDo(document("admin-director-service-delete",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(DIRECTOR_SERVICE_ID_STR)
						.description("삭제할 디렉터 서비스 ID")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 디렉터 서비스 삭제 API")
					.description("관리자 디렉터 서비스 삭제 API")
					.pathParameters(
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName(
								DIRECTOR_SERVICE_ID_STR)
							.description("삭제할 디렉터 서비스 ID")
					)
					.build()
				)
			));
	}
}
