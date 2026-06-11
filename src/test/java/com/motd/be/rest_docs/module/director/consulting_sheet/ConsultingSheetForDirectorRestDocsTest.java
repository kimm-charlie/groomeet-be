package com.motd.be.rest_docs.module.director.consulting_sheet;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.director.consulting_sheet.dto.request.ConsultingSheetSaveRequestForDirector;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class ConsultingSheetForDirectorRestDocsTest extends BaseRestDocsTest {

	@Test
	void 디렉터_컨설팅지_발송() throws Exception {
		authenticationSetUp();

		// given
		ConsultingSheetSaveRequestForDirector request = ConsultingSheetSaveRequestForDirector.builder()
			.consultingRequestId(1L)
			.content("고객님의 얼굴형과 두상을 분석한 결과, 투블록 컷을 추천드립니다.")
			.price("50,000원")
			.fileIds(List.of(1L, 2L))
			.build();

		// when & then
		mockMvc.perform(post("/api/directors/consulting-sheets")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				))
			.andExpect(status().isCreated())
			.andDo(document("consulting-sheet-save-for-director",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("consultingRequestId").type(JsonFieldType.NUMBER)
						.description("컨설팅 요청 ID"),
					fieldWithPath("content").type(JsonFieldType.STRING)
						.description("컨설팅지 내용 (최대 2000자)"),
					fieldWithPath("price").type(JsonFieldType.STRING)
						.description("가격 정보 (최대 50자)"),
					fieldWithPath("fileIds").type(JsonFieldType.ARRAY)
						.description("첨부 파일 ID 목록 (최대 5개, 생략 가능)").optional()
				),

				resource(builder()
					.tag("💆 디렉터 컨설팅 API")
					.summary("컨설팅지 발송")
					.description("디렉터가 선점한 컨설팅 요청에 대해 컨설팅지를 작성하여 발송합니다. 발송 후 컨설팅 요청 상태는 COMPLETED로 전환됩니다.")
					.build())
			));
	}
}
