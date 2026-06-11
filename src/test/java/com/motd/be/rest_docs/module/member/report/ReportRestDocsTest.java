package com.motd.be.rest_docs.module.member.report;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.motd.be.Constants.*;
import static com.motd.be.rest_docs.Utils.*;
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

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.member.report.dto.request.ReportRequest;
import com.motd.be.module.member.report.entity.ReportReason;
import com.motd.be.module.member.report.entity.ReportType;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class ReportRestDocsTest extends BaseRestDocsTest {

	@Test
	void 회원_신고() throws Exception {
		authenticationSetUp();

		// given
		ReportRequest request = ReportRequest.builder()
			.reportedId(2L)
			.reason(ReportReason.불편함_유발.name())
			.reportType(ReportType.CHAT_ROOM.name())
			.description(CONTENT_STR)
			.imageIds(List.of(1L, 2L))
			.build();

		willDoNothing().given(reportFacade)
			.save(anyLong(), any(ReportRequest.class));

		// when & then
		mockMvc.perform(post("/api/reports")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andDo(document("member-report-save",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("reportedId").type(JsonFieldType.NUMBER)
						.description("신고 대상 회원 ID"),
					fieldWithPath("reason").type(JsonFieldType.STRING)
						.attributes((enumFormat(ReportReason.class, Enum::name)))
						.description("신고 사유"),
					fieldWithPath("reportType").type(JsonFieldType.STRING)
						.attributes((enumFormat(ReportType.class, Enum::name)))
						.description("신고 유형"),
					fieldWithPath("description").type(JsonFieldType.STRING)
						.optional()
						.description("신고 상세 내용"),
					fieldWithPath("imageIds").type(JsonFieldType.ARRAY)
						.optional()
						.description("신고 이미지 아이디 목록")
				),

				resource(ResourceSnippetParameters.builder()
					.tag("👤 회원 신고 API")
					.summary("회원 신고")
					.description("다른 회원을 신고합니다. 신고 시 자동으로 차단되며, 즐겨찾기가 해제되고, 공통 채팅방에서 나가게 됩니다.")
					.build())
			));
	}
}
