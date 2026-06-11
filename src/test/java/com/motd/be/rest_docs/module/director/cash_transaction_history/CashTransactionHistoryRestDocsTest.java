package com.motd.be.rest_docs.module.director.cash_transaction_history;

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

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.director.cash_transaction_history.dto.response.CashTransactionHistoryFindAllResponseForDirector;
import com.motd.be.module.director.cash_transaction_history.dto.response.CashTransactionHistoryResponseForDirector;
import com.motd.be.module.member.cash.entity.CashTransactionType;
import com.motd.be.module.member.cash.entity.CashUsageType;
import com.motd.be.module.member.notification.entity.ReferenceType;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class CashTransactionHistoryRestDocsTest extends BaseRestDocsTest {

	@Test
	void 캐시_거래_내역_전체_조회() throws Exception {
		authenticationSetUp();

		CashTransactionHistoryResponseForDirector history1 = CashTransactionHistoryResponseForDirector.builder()
			.id(1L)
			.cashUsageType(CashUsageType.CHARGE)
			.amount(10000L)
			.beforeBalance(0L)
			.referenceType(null)
			.referenceId(null)
			.createdAt(formatToDateString(LocalDateTime.now()))
			.cashTransactionType(CashTransactionType.CHARGE)
			.build();

		CashTransactionHistoryResponseForDirector history2 = CashTransactionHistoryResponseForDirector.builder()
			.id(2L)
			.cashUsageType(CashUsageType.CHAT_START)
			.amount(0L)
			.beforeBalance(10000L)
			.referenceType(ReferenceType.CHAT_ROOM)
			.referenceId(1L)
			.createdAt(formatToDateString(LocalDateTime.now()))
			.cashTransactionType(CashTransactionType.USE)
			.build();

		CashTransactionHistoryFindAllResponseForDirector response = CashTransactionHistoryFindAllResponseForDirector.builder()
			.page(0)
			.hasNext(false)
			.histories(List.of(history1, history2))
			.build();

		given(cashTransactionHistoryFacadeForDirector.findAll(anyLong(), anyInt(), any()))
			.willReturn(response);

		mockMvc.perform(get("/api/directors/cash-transaction-histories")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.param("cashTransactionType", CashTransactionType.USE.name())
				.param(PAGE_STR, ZERO_STR)
			)
			.andExpect(status().isOk())
			.andDo(document("cash-transaction-history-find-all",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("cashTransactionType")
						.optional()
						.attributes(enumFormat(CashTransactionType.class, Enum::name))
						.description("캐시 거래 유형"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(PAGE_STR)
						.optional()
						.description("페이지 번호 (0부터 시작)")
				),

				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
					fieldWithPath("histories").type(JsonFieldType.ARRAY).description("캐시 거래 내역 목록"),
					fieldWithPath("histories[].id").type(JsonFieldType.NUMBER).description("거래 내역 ID"),
					fieldWithPath("histories[].cashUsageType").type(JsonFieldType.STRING)
						.attributes(enumFormat(CashUsageType.class, Enum::name))
						.description("캐시 사용 유형"),
					fieldWithPath("histories[].amount").type(JsonFieldType.NUMBER).description("거래 금액"),
					fieldWithPath("histories[].beforeBalance").type(JsonFieldType.NUMBER).description("거래 전 잔액"),
					fieldWithPath("histories[].referenceType").type(JsonFieldType.STRING)
						.optional()
						.attributes(enumFormat(ReferenceType.class, Enum::name))
						.description("참조 타입"),
					fieldWithPath("histories[].referenceId").type(JsonFieldType.NUMBER)
						.optional()
						.description("참조 ID"),
					fieldWithPath("histories[].createdAt").type(JsonFieldType.STRING).description("거래 발생 시각"),
					fieldWithPath("histories[].cashTransactionType").type(JsonFieldType.STRING)
						.attributes(enumFormat(CashTransactionType.class, Enum::name))
						.description("거래 유형")
				),

				resource(builder()
					.tag("💰 캐시 거래 내역 API")
					.summary("캐시 거래 내역 전체 조회 (디렉터)")
					.description("디렉터의 캐시 충전/사용/지급 내역을 조회합니다.")
					.build())
			));
	}
}
