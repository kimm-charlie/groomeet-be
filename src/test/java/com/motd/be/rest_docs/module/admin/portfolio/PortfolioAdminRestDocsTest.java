package com.motd.be.rest_docs.module.admin.portfolio;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.admin.portfolio.dto.response.PortfolioFindAllResponseForAdmin;
import com.motd.be.module.admin.portfolio.dto.response.PortfolioFindDetailResponseForAdmin;
import com.motd.be.module.admin.portfolio.dto.response.PortfolioResponseForAdmin;

import jakarta.servlet.http.Cookie;

@RestDocsTest
class PortfolioAdminRestDocsTest extends BaseRestDocsTest {

	@Test
	@DisplayName("관리자 포트폴리오 전체 조회 API")
	void findAll() throws Exception {
		// given
		authenticationSetUp();

		PortfolioFindAllResponseForAdmin response = PortfolioFindAllResponseForAdmin.builder()
			.hasNext(Boolean.TRUE)
			.totalCount(2)
			.portfolios(List.of(
				PortfolioResponseForAdmin.builder()
					.portfolioId(1L)
					.title("웨딩 촬영 포트폴리오")
					.price(500000L)
					.directorName("김디렉터")
					.serviceName("웨딩 촬영")
					.thumbnailUrl(THUMBNAIL_IMAGE_URL)
					.isPopular(true)
					.popularAt(formatToDateString(LocalDateTime.now()))
					.createdAt(formatToDateString(LocalDateTime.now()))
					.build(),
				PortfolioResponseForAdmin.builder()
					.portfolioId(2L)
					.title("프로필 촬영 포트폴리오")
					.price(300000L)
					.directorName("이디렉터")
					.serviceName("프로필 촬영")
					.thumbnailUrl(THUMBNAIL_IMAGE_URL)
					.isPopular(false)
					.popularAt(null)
					.createdAt(formatToDateString(LocalDateTime.now()))
					.build()
			))
			.build();

		given(portfolioFacadeForAdmin.findAll(any(), any(), any(), any())).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/admin/portfolios")
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR))
				.param("cursorId", "100")
				.param("search", "")
				.param("isPopular", "true")
				.param("showIsDeleted", "false"))
			.andExpect(status().isOk())
			.andDo(document("admin-portfolio-find-all",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키")
				),

				org.springframework.restdocs.request.RequestDocumentation.queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("cursorId")
						.optional().description("커서 ID (마지막 포트폴리오 ID, 미지정 시 최신부터)"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("search")
						.optional().description("검색어 (디렉터 닉네임 or 회원 ID)"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("isPopular")
						.optional().description("인기 포트폴리오 필터 (true: popularAt 최신순, false/미지정: ID 최신순)"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("showIsDeleted")
						.optional().description("삭제된 포트폴리오 포함 여부 (true: 전체, false/미지정: 삭제되지 않은 것만)")
				),

				responseFields(
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
					fieldWithPath("totalCount").type(JsonFieldType.NUMBER).description("현재 페이지 항목 수"),
					fieldWithPath("portfolios").type(JsonFieldType.ARRAY).description("포트폴리오 목록"),
					fieldWithPath("portfolios[].portfolioId").type(JsonFieldType.NUMBER).description("포트폴리오 ID"),
					fieldWithPath("portfolios[].title").type(JsonFieldType.STRING).description("포트폴리오 제목"),
					fieldWithPath("portfolios[].price").type(JsonFieldType.NUMBER).description("가격"),
					fieldWithPath("portfolios[].directorName").type(JsonFieldType.STRING).description("디렉터 이름"),
					fieldWithPath("portfolios[].serviceName").type(JsonFieldType.STRING).description("서비스 이름"),
					fieldWithPath("portfolios[].thumbnailUrl").type(JsonFieldType.STRING).optional().description("썸네일 URL"),
					fieldWithPath("portfolios[].isPopular").type(JsonFieldType.BOOLEAN).description("인기 포트폴리오 여부"),
					fieldWithPath("portfolios[].popularAt").type(JsonFieldType.STRING).optional()
						.attributes(getDateTimeFormat()).description("인기 선정 일시"),
					fieldWithPath("portfolios[].createdAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat()).description("생성 일시")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 포트폴리오 전체 조회 API")
					.description("관리자 포트폴리오 전체 조회 API")
					.build())
			));
	}

	@Test
	@DisplayName("관리자 포트폴리오 상세 조회 API")
	void findDetail() throws Exception {
		// given
		authenticationSetUp();

		PortfolioFindDetailResponseForAdmin response = PortfolioFindDetailResponseForAdmin.builder()
			.portfolioId(1L)
			.title("웨딩 촬영 포트폴리오")
			.content("최고의 웨딩 촬영을 제공합니다.")
			.price(500000L)
			.directorName("김디렉터")
			.serviceName("웨딩 촬영")
			.isPopular(true)
			.popularAt(formatToDateString(LocalDateTime.now()))
			.createdAt(formatToDateString(LocalDateTime.now()))
			.files(List.of(
				PortfolioFindDetailResponseForAdmin.PortfolioFileResponseForAdmin.builder()
					.fileId(1L)
					.fileUrl(THUMBNAIL_IMAGE_URL)
					.sortOrder(1)
					.build()
			))
			.build();

		given(portfolioFacadeForAdmin.findDetail(any(Long.class))).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/admin/portfolios/{portfolioId}", ID)
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)))
			.andExpect(status().isOk())
			.andDo(document("admin-portfolio-find-detail",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키")
				),

				org.springframework.restdocs.request.RequestDocumentation.pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("portfolioId")
						.description("포트폴리오 ID")
				),

				responseFields(
					fieldWithPath("portfolioId").type(JsonFieldType.NUMBER).description("포트폴리오 ID"),
					fieldWithPath("title").type(JsonFieldType.STRING).description("포트폴리오 제목"),
					fieldWithPath("content").type(JsonFieldType.STRING).optional().description("포트폴리오 내용"),
					fieldWithPath("price").type(JsonFieldType.NUMBER).description("가격"),
					fieldWithPath("directorName").type(JsonFieldType.STRING).description("디렉터 이름"),
					fieldWithPath("serviceName").type(JsonFieldType.STRING).description("서비스 이름"),
					fieldWithPath("isPopular").type(JsonFieldType.BOOLEAN).description("인기 포트폴리오 여부"),
					fieldWithPath("popularAt").type(JsonFieldType.STRING).optional()
						.attributes(getDateTimeFormat()).description("인기 선정 일시"),
					fieldWithPath("createdAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat()).description("생성 일시"),
					fieldWithPath("files").type(JsonFieldType.ARRAY).description("파일 목록"),
					fieldWithPath("files[].fileId").type(JsonFieldType.NUMBER).description("파일 ID"),
					fieldWithPath("files[].fileUrl").type(JsonFieldType.STRING).description("파일 URL"),
					fieldWithPath("files[].sortOrder").type(JsonFieldType.NUMBER).description("정렬 순서")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 포트폴리오 상세 조회 API")
					.description("관리자 포트폴리오 상세 조회 API")
					.build())
			));
	}

	@Test
	@DisplayName("관리자 인기 포트폴리오 지정 API")
	void markAsPopular() throws Exception {
		// given
		authenticationSetUp();

		willDoNothing().given(portfolioFacadeForAdmin).markAsPopular(any(Long.class));

		// when & then
		mockMvc.perform(post("/api/admin/portfolios/{portfolioId}/popular", ID)
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)))
			.andExpect(status().isCreated())
			.andDo(document("admin-portfolio-mark-popular",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키")
				),

				org.springframework.restdocs.request.RequestDocumentation.pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("portfolioId")
						.description("인기 지정할 포트폴리오 ID")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 인기 포트폴리오 지정 API")
					.description("관리자 인기 포트폴리오 지정 API")
					.build())
			));
	}

	@Test
	@DisplayName("관리자 인기 포트폴리오 해제 API")
	void unmarkAsPopular() throws Exception {
		// given
		authenticationSetUp();

		willDoNothing().given(portfolioFacadeForAdmin).unmarkAsPopular(any(Long.class));

		// when & then
		mockMvc.perform(delete("/api/admin/portfolios/{portfolioId}/popular", ID)
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)))
			.andExpect(status().isOk())
			.andDo(document("admin-portfolio-unmark-popular",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키")
				),

				org.springframework.restdocs.request.RequestDocumentation.pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("portfolioId")
						.description("인기 해제할 포트폴리오 ID")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 인기 포트폴리오 해제 API")
					.description("관리자 인기 포트폴리오 해제 API")
					.build())
			));
	}
}
