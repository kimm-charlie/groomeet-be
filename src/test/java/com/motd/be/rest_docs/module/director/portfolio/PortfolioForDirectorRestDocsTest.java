package com.motd.be.rest_docs.module.director.portfolio;

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

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.director.portfolio.dto.request.PortfolioSaveAndUpdateRequestForDirector;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class PortfolioForDirectorRestDocsTest extends BaseRestDocsTest {

	@Test
	void 포트폴리오_저장() throws Exception {
		authenticationSetUp();

		PortfolioSaveAndUpdateRequestForDirector request = PortfolioSaveAndUpdateRequestForDirector.builder()
			.title(TITLE_STR)
			.content(CONTENT_STR)
			.directorServiceId(1L)
			.fileIds(Arrays.asList(1L, 2L))
			.thumbnailImageId(1L)
			.price(10000L)
			.build();

		willDoNothing().given(portfolioFacadeForDirector)
			.save(anyLong(), any(PortfolioSaveAndUpdateRequestForDirector.class));

		mockMvc.perform(post("/api/directors/my/portfolios")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andDo(document("portfolio-save",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("title").type(JsonFieldType.STRING).description("포트폴리오 제목"),
					fieldWithPath("content").type(JsonFieldType.STRING).description("포트폴리오 내용"),
					fieldWithPath("directorServiceId").type(JsonFieldType.NUMBER).description("디렉터 서비스 ID"),
					fieldWithPath("fileIds").type(JsonFieldType.ARRAY).description("이미지 또는 파일 ID 목록"),
					fieldWithPath("thumbnailImageId").type(JsonFieldType.NUMBER).description("썸네일 이미지 ID"),
					fieldWithPath("price").type(JsonFieldType.NUMBER).description("포트폴리오 가격")
				),

				resource(builder()
					.tag("📁 포트폴리오 API")
					.summary("포트폴리오 생성")
					.description("디렉터가 포트폴리오를 생성합니다.")
					.build())
			));
	}

	@Test
	void 포트폴리오_수정() throws Exception {
		authenticationSetUp();

		Long portfolioId = 1L;
		PortfolioSaveAndUpdateRequestForDirector request = PortfolioSaveAndUpdateRequestForDirector.builder()
			.title(TITLE_STR)
			.content(CONTENT_STR)
			.directorServiceId(2L)
			.fileIds(Arrays.asList(3L, 4L))
			.thumbnailImageId(3L)
			.price(10000L)
			.build();

		willDoNothing().given(portfolioFacadeForDirector)
			.update(anyLong(), anyLong(), any(PortfolioSaveAndUpdateRequestForDirector.class));

		mockMvc.perform(put("/api/directors/my/portfolios/{portfolioId}", portfolioId)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent())
			.andDo(document("portfolio-update",
				getRequestPreProcessor(),
				getResponsePreProcessor(),
				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("portfolioId")
						.description("수정할 포트폴리오 ID")
				),

				requestFields(
					fieldWithPath("title").type(JsonFieldType.STRING).description("포트폴리오 제목"),
					fieldWithPath("content").optional().type(JsonFieldType.STRING).description("포트폴리오 내용"),
					fieldWithPath("directorServiceId").type(JsonFieldType.NUMBER).description("디렉터 서비스 ID"),
					fieldWithPath("fileIds").type(JsonFieldType.ARRAY).description("이미지 또는 파일 ID 목록"),
					fieldWithPath("thumbnailImageId").type(JsonFieldType.NUMBER).description("썸네일 이미지 ID"),
					fieldWithPath("price").type(JsonFieldType.NUMBER).description("포트폴리오 가격")
				),

				resource(builder()
					.tag("📁 포트폴리오 API")
					.summary("포트폴리오 수정")
					.description("디렉터가 포트폴리오를 수정합니다.")
					.build())
			));
	}

	@Test
	void 포트폴리오_삭제() throws Exception {
		authenticationSetUp();

		Long portfolioId = 1L;
		willDoNothing().given(portfolioFacadeForDirector).delete(anyLong(), anyLong());

		mockMvc.perform(delete("/api/directors/my/portfolios/{portfolioId}", portfolioId)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isNoContent())
			.andDo(document("portfolio-delete",
				getRequestPreProcessor(),
				getResponsePreProcessor(),
				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("portfolioId")
						.description("삭제할 포트폴리오 ID")
				),

				resource(builder()
					.tag("📁 포트폴리오 API")
					.summary("포트폴리오 삭제")
					.description("디렉터가 포트폴리오를 삭제합니다.")
					.build())
			));
	}

}
