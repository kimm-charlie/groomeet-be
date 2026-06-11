package com.motd.be.rest_docs.module.director.director_service_mapping;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.director.director_service_mapping.dto.request.DirectorServiceMappingUpdateServiceRequestForDirector;
import com.motd.be.module.director.director_service_mapping.dto.response.DirectorServiceFindActivationProgressResponseForDirector;
import com.motd.be.module.director.director_service_mapping.dto.response.DirectorServiceFindAllResponseForDirector;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class DirectorServiceMappingForDirectorRestDocsTest extends BaseRestDocsTest {

	@Test
	void 서비스_정보_수정() throws Exception {
		authenticationSetUp();

		DirectorServiceMappingUpdateServiceRequestForDirector request = DirectorServiceMappingUpdateServiceRequestForDirector.builder()
			.serviceIds(Arrays.asList(1L, 2L, 3L))
			.build();

		willDoNothing().given(directorServiceMappingFacadeForDirector)
			.update(anyLong(), any(DirectorServiceMappingUpdateServiceRequestForDirector.class));

		mockMvc.perform(put("/api/directors/my/services")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent())
			.andDo(document("director-service-mapping-update",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					// 실제 DTO 필드에 맞게 작성하세요
					fieldWithPath("serviceIds").type(JsonFieldType.ARRAY).description("서비스 아이디")
				),

				resource(builder()
					.tag("🎬 디렉터 서비스 매핑 API")
					.summary("디렉터 서비스 정보 수정")
					.description("디렉터가 서비스 정보를 수정합니다.")
					.build())
			));
	}

	@Test
	void 디렉터_서비스_목록_조회() throws Exception {
		authenticationSetUp();

		// given
		List<DirectorServiceFindAllResponseForDirector> responseList = Arrays.asList(
			DirectorServiceFindAllResponseForDirector.builder()
				.id(1L)
				.name("서비스명1")
				.parentName("부모서비스명1")
				.build(),
			DirectorServiceFindAllResponseForDirector.builder()
				.id(2L)
				.name("서비스명2")
				.parentName("부모서비스명2")
				.build()
		);

		willReturn(responseList).given(directorServiceMappingFacadeForDirector).findAll(anyLong());

		// when & then
		mockMvc.perform(get("/api/directors/my/services")
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				))
			.andExpect(status().isOk())
			.andDo(document("director-service-mapping-find-all-for-director",

				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("서비스 ID"),
					fieldWithPath("[].name").type(JsonFieldType.STRING).description("서비스명"),
					fieldWithPath("[].parentName").type(JsonFieldType.STRING).description("부모 서비스명")
				),

				resource(builder()
					.tag("🎬 특정 디렉터 서비스 매핑 API (회원용)")
					.summary("특정 디렉터 서비스 목록 조회 (회원용)")
					.description("특정 디렉터의 서비스 목록을 조회합니다.(회원용)")
					.build())
			));
	}

	@Test
	void 제안_템플릿용_디렉터_서비스_목록_조회() throws Exception {
		authenticationSetUp();

		List<DirectorServiceFindAllResponseForDirector> response = Arrays.asList(
			DirectorServiceFindAllResponseForDirector.builder()
				.id(2L)
				.name("헬스 케어")
				.parentName("건강")
				.build(),
			DirectorServiceFindAllResponseForDirector.builder()
				.id(3L)
				.name("필라테스")
				.parentName("운동")
				.build()
		);

		given(directorServiceMappingFacadeForDirector.findAllForEstimateTemplate(anyLong())).willReturn(response);

		mockMvc.perform(get("/api/directors/my/services/with-estimate-templates")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isOk())
			.andDo(document("director-service-mapping-find-all-for-estimate-template",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("디렉터 서비스 ID"),
					fieldWithPath("[].name").type(JsonFieldType.STRING).description("디렉터 서비스 이름"),
					fieldWithPath("[].parentName").type(JsonFieldType.STRING).description("상위 카테고리 이름")
				),

				resource(builder()
					.tag("📄 서비스 제안 API")
					.summary("제안 템플릿용 디렉터 서비스 목록 조회")
					.description("디렉터가 제안 작성 시 사용할 수 있는 자신의 서비스 목록을 조회합니다.")
					.build())
			));
	}

	@Test
	void 디렉터_서비스_활성화_진행률_조회() throws Exception {
		authenticationSetUp();

		DirectorServiceFindActivationProgressResponseForDirector response =
			DirectorServiceFindActivationProgressResponseForDirector.builder()
				.progressPercentage(75)
				.build();

		given(directorServiceMappingFacadeForDirector.findActivationProgress(anyLong())).willReturn(response);

		mockMvc.perform(get("/api/directors/services/activation/progress")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isOk())
			.andDo(document("director-service-mapping-find-activation-progress",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("progressPercentage").type(JsonFieldType.NUMBER).description("활성화 진행률 (%)")
				),

				resource(builder()
					.tag("🎬 디렉터 서비스 매핑 API")
					.summary("디렉터 서비스 활성화 진행률 조회")
					.description("디렉터의 서비스 활성화 진행률을 조회합니다.")
					.build())
			));
	}
}
