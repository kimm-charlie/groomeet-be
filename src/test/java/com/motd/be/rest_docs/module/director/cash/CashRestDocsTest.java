package com.motd.be.rest_docs.module.director.cash;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.common.utils.DateFormatUtils.*;
import static com.motd.be.rest_docs.Utils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.director.cash.dto.request.CashUseRequestForDirector;
import com.motd.be.module.director.cash.dto.response.CashFindResponseForDirector;
import com.motd.be.module.director.cash.dto.response.CashProductResponseForDirector;
import com.motd.be.module.director.cash.dto.response.CashProductsResponseForDirector;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class CashRestDocsTest extends BaseRestDocsTest {

	@Test
	void 캐시_상품_목록_조회() throws Exception {
		authenticationSetUp();

		CashProductResponseForDirector product1 = CashProductResponseForDirector.builder()
			.id(1L)
			.price(10000L)
			.amount(10000L)
			.discountRate(0)
			.build();

		CashProductResponseForDirector product2 = CashProductResponseForDirector.builder()
			.id(2L)
			.price(50000L)
			.amount(55000L)
			.discountRate(10)
			.build();

		CashProductsResponseForDirector response = CashProductsResponseForDirector.builder()
			.products(List.of(product1, product2))
			.build();

		given(cashFacadeForDirector.findCashProducts())
			.willReturn(response);

		mockMvc.perform(get("/api/directors/cash/products")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isOk())
			.andDo(document("cash-products-find-all",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("products").type(JsonFieldType.ARRAY).description("캐시 상품 목록"),
					fieldWithPath("products[].id").type(JsonFieldType.NUMBER).description("상품 ID"),
					fieldWithPath("products[].price").type(JsonFieldType.NUMBER).description("상품 가격 (원)"),
					fieldWithPath("products[].amount").type(JsonFieldType.NUMBER).description("지급 캐시 금액"),
					fieldWithPath("products[].discountRate").type(JsonFieldType.NUMBER).description("할인율 (%)")
				),

				resource(builder()
					.tag("💰 캐시 API")
					.summary("캐시 상품 목록 조회 (디렉터)")
					.description("디렉터가 충전 가능한 캐시 상품 목록을 조회합니다.")
					.build())
			));
	}

	@Test
	void 채팅_시작_캐시_차감() throws Exception {
		authenticationSetUp();

		CashUseRequestForDirector request = CashUseRequestForDirector.builder()
			.amount(1000L)
			.referenceId(1L)
			.build();

		willDoNothing().given(cashFacadeForDirector)
			.transactionChatStart(anyLong(), any(CashUseRequestForDirector.class));

		mockMvc.perform(post("/api/directors/cash/transaction")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.param(TYPE_STR, "chatStart")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isCreated())
			.andDo(document("cash-transaction-chat-start",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("type")
						.description("타입은 chatStart")
				),

				requestFields(
					fieldWithPath("amount").type(JsonFieldType.NUMBER).description("차감할 캐시 금액"),
					fieldWithPath("referenceId").type(JsonFieldType.NUMBER).description("채팅방 ID")
				),

				resource(builder()
					.tag("💰 캐시 API")
					.summary("채팅 시작 캐시 차감 (디렉터)")
					.description("디렉터가 채팅을 시작할 때 캐시를 차감합니다.")
					.build())
			));
	}

	@Test
	void 캐시_조회() throws Exception {
		authenticationSetUp();

		CashFindResponseForDirector response = CashFindResponseForDirector.builder()
			.cash(10000L)
			.onboardingPassEndsAt(formatToDateString(LocalDate.now()))
			.build();

		given(cashFacadeForDirector.findCash(anyLong()))
			.willReturn(response);

		mockMvc.perform(get("/api/directors/cash")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isOk())
			.andDo(document("find-cash",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("cash").type(JsonFieldType.NUMBER).description("캐시"),
					fieldWithPath("onboardingPassEndsAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.optional()
						.description("무료 정착 패스 끝나는 시간")
				),

				resource(builder()
					.tag("💰 캐시 API")
					.summary("캐시 조회 (디렉터)")
					.description("디렉터가 가지고 있는 캐시 또는 정착 패스 기간을 조회 합니다.")
					.build())
			));
	}
}
