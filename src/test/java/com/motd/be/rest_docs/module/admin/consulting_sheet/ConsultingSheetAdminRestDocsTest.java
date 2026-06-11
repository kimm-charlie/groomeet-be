package com.motd.be.rest_docs.module.admin.consulting_sheet;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.rest_docs.Utils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ConsultingSheetException;

import jakarta.servlet.http.Cookie;

@RestDocsTest
class ConsultingSheetAdminRestDocsTest extends BaseRestDocsTest {

	@Test
	@DisplayName("관리자 컨설팅지 승인")
	void approve() throws Exception {
		// given
		authenticationSetUp();

		// when & then
		mockMvc.perform(patch("/api/admin/consulting-sheets/{consultingSheetId}/approve", 1L)
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)))
			.andExpect(status().isNoContent())
			.andDo(document("admin-consulting-sheet-approve",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("consultingSheetId")
						.description("승인할 컨설팅지 ID")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 컨설팅지 승인 API")
					.description("관리자가 컨설팅지를 승인합니다. PENDING_APPROVAL 상태에서만 승인이 가능합니다.")
					.build()
				)
			));
	}

	@Test
	@DisplayName("관리자 컨설팅지 반려")
	void reject() throws Exception {
		// given
		authenticationSetUp();

		// when & then
		mockMvc.perform(patch("/api/admin/consulting-sheets/{consultingSheetId}/reject", 1L)
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)))
			.andExpect(status().isNoContent())
			.andDo(document("admin-consulting-sheet-reject",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("consultingSheetId")
						.description("반려할 컨설팅지 ID")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 컨설팅지 반려 API")
					.description("관리자가 컨설팅지를 반려합니다. PENDING_APPROVAL 상태에서만 반려가 가능하며, 반려 시 디렉터가 재발송할 수 있도록 컨설팅 요청 상태가 복구됩니다.")
					.build()
				)
			));
	}

	@Test
	@DisplayName("관리자 컨설팅지 승인 실패 (상태 오류)")
	void approve_fail_bad_request() throws Exception {
		// given
		authenticationSetUp();

		willThrow(new CustomRuntimeException(ConsultingSheetException.NOT_PENDING_APPROVAL))
			.given(consultingSheetFacadeForAdmin).approve(anyLong());

		// when & then
		mockMvc.perform(patch("/api/admin/consulting-sheets/{consultingSheetId}/approve", 1L)
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)))
			.andExpect(status().isBadRequest())
			.andDo(document("admin-consulting-sheet-approve-fail-bad-request",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				responseFields(
					fieldWithPath("status").type(JsonFieldType.STRING)
						.description("HTTP 상태 코드"),
					fieldWithPath("message").type(JsonFieldType.STRING)
						.description("에러 메시지"),
					fieldWithPath("code").type(JsonFieldType.STRING)
						.description("에러 코드")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 컨설팅지 승인 API")
					.description("관리자가 컨설팅지를 승인합니다. PENDING_APPROVAL 상태에서만 승인이 가능합니다.")
					.build()
				)
			));
	}

	@Test
	@DisplayName("관리자 컨설팅지 승인 실패 (존재하지 않음)")
	void approve_fail_not_found() throws Exception {
		// given
		authenticationSetUp();

		willThrow(new CustomRuntimeException(ConsultingSheetException.NOT_FOUND))
			.given(consultingSheetFacadeForAdmin).approve(anyLong());

		// when & then
		mockMvc.perform(patch("/api/admin/consulting-sheets/{consultingSheetId}/approve", 99999L)
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)))
			.andExpect(status().isNotFound())
			.andDo(document("admin-consulting-sheet-approve-fail-not-found",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				responseFields(
					fieldWithPath("status").type(JsonFieldType.STRING)
						.description("HTTP 상태 코드"),
					fieldWithPath("message").type(JsonFieldType.STRING)
						.description("에러 메시지"),
					fieldWithPath("code").type(JsonFieldType.STRING)
						.description("에러 코드")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 컨설팅지 승인 API")
					.description("관리자가 컨설팅지를 승인합니다. PENDING_APPROVAL 상태에서만 승인이 가능합니다.")
					.build()
				)
			));
	}

	@Test
	@DisplayName("관리자 컨설팅지 반려 실패 (상태 오류)")
	void reject_fail_bad_request() throws Exception {
		// given
		authenticationSetUp();

		willThrow(new CustomRuntimeException(ConsultingSheetException.NOT_PENDING_APPROVAL))
			.given(consultingSheetFacadeForAdmin).reject(anyLong());

		// when & then
		mockMvc.perform(patch("/api/admin/consulting-sheets/{consultingSheetId}/reject", 1L)
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)))
			.andExpect(status().isBadRequest())
			.andDo(document("admin-consulting-sheet-reject-fail-bad-request",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				responseFields(
					fieldWithPath("status").type(JsonFieldType.STRING)
						.description("HTTP 상태 코드"),
					fieldWithPath("message").type(JsonFieldType.STRING)
						.description("에러 메시지"),
					fieldWithPath("code").type(JsonFieldType.STRING)
						.description("에러 코드")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 컨설팅지 반려 API")
					.description("관리자가 컨설팅지를 반려합니다. PENDING_APPROVAL 상태에서만 반려가 가능하며, 반려 시 디렉터가 재발송할 수 있도록 컨설팅 요청 상태가 복구됩니다.")
					.build()
				)
			));
	}

	@Test
	@DisplayName("관리자 컨설팅지 반려 실패 (존재하지 않음)")
	void reject_fail_not_found() throws Exception {
		// given
		authenticationSetUp();

		willThrow(new CustomRuntimeException(ConsultingSheetException.NOT_FOUND))
			.given(consultingSheetFacadeForAdmin).reject(anyLong());

		// when & then
		mockMvc.perform(patch("/api/admin/consulting-sheets/{consultingSheetId}/reject", 99999L)
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)))
			.andExpect(status().isNotFound())
			.andDo(document("admin-consulting-sheet-reject-fail-not-found",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				responseFields(
					fieldWithPath("status").type(JsonFieldType.STRING)
						.description("HTTP 상태 코드"),
					fieldWithPath("message").type(JsonFieldType.STRING)
						.description("에러 메시지"),
					fieldWithPath("code").type(JsonFieldType.STRING)
						.description("에러 코드")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 컨설팅지 반려 API")
					.description("관리자가 컨설팅지를 반려합니다. PENDING_APPROVAL 상태에서만 반려가 가능하며, 반려 시 디렉터가 재발송할 수 있도록 컨설팅 요청 상태가 복구됩니다.")
					.build()
				)
			));
	}
}
