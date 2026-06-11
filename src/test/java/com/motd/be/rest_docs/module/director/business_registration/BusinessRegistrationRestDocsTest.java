package com.motd.be.rest_docs.module.director.business_registration;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static org.mockito.ArgumentMatchers.*;
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
import com.motd.be.module.director.business_registration.dto.request.BusinessRegistrationCreateRequestForDirector;
import com.motd.be.module.director.business_registration.dto.response.BusinessRegistrationFindResponseForDirector;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class BusinessRegistrationRestDocsTest extends BaseRestDocsTest {

	@Test
	void 사업자등록_등록() throws Exception {
		authenticationSetUp();

		BusinessRegistrationCreateRequestForDirector request = BusinessRegistrationCreateRequestForDirector.builder()
			.businessRegistrationNumber("123-45-67890")
			.residentRegistrationNumber("900101-1")
			.fileIds(List.of(1L, 2L))
			.build();

		willDoNothing().given(businessRegistrationFacadeForDirector)
			.register(anyLong(), any(BusinessRegistrationCreateRequestForDirector.class));

		mockMvc.perform(post("/api/directors/my/business-registrations")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isCreated())
			.andDo(document("business-registration-register",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("businessRegistrationNumber").type(JsonFieldType.STRING)
						.description("사업자 등록 번호"),
					fieldWithPath("residentRegistrationNumber").type(JsonFieldType.STRING)
						.description("대표자 주민등록번호"),
					fieldWithPath("fileIds").type(JsonFieldType.ARRAY)
						.description("첨부된 사업자등록증 파일 ID 목록 (최소 1개 ~ 최대 10개)")
				),

				resource(builder()
					.tag("🧾 사업자등록 API")
					.summary("사업자 등록증 등록 (디렉터)")
					.description("디렉터가 사업자등록 정보를 제출합니다.")
					.build())
			));
	}

	@Test
	void 사업자등록_조회() throws Exception {
		authenticationSetUp();

		BusinessRegistrationFindResponseForDirector response = BusinessRegistrationFindResponseForDirector.builder()
			.id(1L)
			.businessRegistrationNumber("123-45-67890")
			.residentRegistrationNumber("900101-1")
			.files(List.of())
			.build();

		given(businessRegistrationFacadeForDirector.find(anyLong()))
			.willReturn(response);

		mockMvc.perform(get("/api/directors/my/business-registrations")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isOk())
			.andDo(document("business-registration-find",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER)
						.optional()
						.description("사업자등록 ID"),
					fieldWithPath("businessRegistrationNumber").type(JsonFieldType.STRING)
						.optional()
						.description("사업자 등록 번호"),
					fieldWithPath("residentRegistrationNumber").type(JsonFieldType.STRING)
						.optional()
						.description("대표자 주민등록번호"),
					fieldWithPath("files").type(JsonFieldType.ARRAY)
						.optional()
						.description("첨부된 사업자등록증 파일 목록"),
					fieldWithPath("files[].id").type(JsonFieldType.NUMBER)
						.optional()
						.description("파일 ID"),
					fieldWithPath("files[].fileUrl").type(JsonFieldType.STRING)
						.optional()
						.description("파일 URL"),
					fieldWithPath("files[].fileType").type(JsonFieldType.STRING)
						.optional()
						.description("파일 타입"),
					fieldWithPath("files[].fileName").type(JsonFieldType.STRING)
						.optional()
						.description("파일명"),
					fieldWithPath("files[].fileSize").type(JsonFieldType.STRING)
						.optional()
						.description("파일 크기")
				),

				resource(builder()
					.tag("🧾 사업자등록 API")
					.summary("사업자 등록증 조회 (디렉터)")
					.description("디렉터가 등록한 사업자등록 정보를 조회합니다.")
					.build())
			));
	}
}
