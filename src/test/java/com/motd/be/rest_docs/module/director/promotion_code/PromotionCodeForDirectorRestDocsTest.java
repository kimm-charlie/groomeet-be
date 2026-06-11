package com.motd.be.rest_docs.module.director.promotion_code;

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

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.director.promotion_code.dto.request.PromotionCodeUseRequestForDirector;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class PromotionCodeForDirectorRestDocsTest extends BaseRestDocsTest {

	@Test
	void 프로모션_코드_사용() throws Exception {
		authenticationSetUp();

		PromotionCodeUseRequestForDirector request = PromotionCodeUseRequestForDirector.builder()
			.promotionCode("DIRECTOR2025")
			.build();

		willDoNothing().given(promotionCodeFacadeForDirector)
			.use(anyLong(), any(PromotionCodeUseRequestForDirector.class));

		mockMvc.perform(post("/api/directors/promotion-codes/use")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andDo(document("promotion-code-use",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("promotionCode").type(JsonFieldType.STRING).description("사용할 프로모션 코드")
				),

				resource(builder()
					.tag("🎫 프로모션 코드 API")
					.summary("프로모션 코드 사용")
					.description("디렉터가 프로모션 코드를 사용합니다.")
					.build())
			));
	}
}
