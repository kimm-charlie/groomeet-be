package com.motd.be.rest_docs.module.member.consulting_request;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.rest_docs.Utils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
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
import com.motd.be.module.member.consulting_request.dto.request.ConsultingImageFileRequest;
import com.motd.be.module.member.consulting_request.dto.request.ConsultingRequestSaveRequest;
import com.motd.be.module.member.consulting_request.dto.response.ConsultingEligibilityResponse;
import com.motd.be.module.member.consulting_request_file.enums.ConsultingRequestImageCategory;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class ConsultingRequestRestDocsTest extends BaseRestDocsTest {

	@Test
	void 컨설팅_자격_확인_인증된_사용자() throws Exception {
		authenticationSetUp();

		// given
		ConsultingEligibilityResponse response = ConsultingEligibilityResponse.builder()
			.hasUsedInviteCode(true)
			.hasConsultingRequest(true)
			.consultingSheetId(1L)
			.referralCode("ABC123")
			.build();

		given(consultingRequestFacade.checkEligibility(anyLong())).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/members/consulting/eligibility")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				))
			.andExpect(status().isOk())
			.andDo(document("consulting-eligibility-check",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("hasUsedInviteCode").type(JsonFieldType.BOOLEAN)
						.description("초대 코드 사용 여부 (본인이 사용했거나 본인 코드가 사용된 경우 true)"),
					fieldWithPath("hasConsultingRequest").type(JsonFieldType.BOOLEAN)
						.description("컨설팅 신청서 제출 여부"),
					fieldWithPath("consultingSheetId").type(JsonFieldType.NUMBER).optional()
						.description("승인된 컨설팅 시트 ID (없으면 null)"),
					fieldWithPath("referralCode").type(JsonFieldType.STRING).optional()
						.description("회원 추천 코드 (비인증 사용자는 null)")
				),

				resource(builder()
					.tag("💆 컨설팅 API")
					.summary("컨설팅 신청 자격 확인")
					.description("회원의 컨설팅 신청 가능 여부를 확인합니다. 초대 코드 사용 이력이 있으면 신청 가능합니다. 비인증 사용자도 호출 가능합니다.")
					.build())
			));
	}

	@Test
	void 컨설팅_요청() throws Exception {
		authenticationSetUp();

		// given
		ConsultingRequestSaveRequest request = ConsultingRequestSaveRequest.builder()
			.usesHairProduct(true)
			.prefersExposedForehead(false)
			.recentProcedure("3개월 전 펌")
			.locations(List.of(1L, 2L))
			.files(List.of(
				ConsultingImageFileRequest.builder().fileId(101L).category(ConsultingRequestImageCategory.FRONT).build(),
				ConsultingImageFileRequest.builder().fileId(102L).category(ConsultingRequestImageCategory.FRONT).build(),
				ConsultingImageFileRequest.builder().fileId(103L).category(ConsultingRequestImageCategory.FRONT).build(),
				ConsultingImageFileRequest.builder().fileId(201L).category(ConsultingRequestImageCategory.SIDE).build(),
				ConsultingImageFileRequest.builder().fileId(202L).category(ConsultingRequestImageCategory.SIDE).build(),
				ConsultingImageFileRequest.builder().fileId(203L).category(ConsultingRequestImageCategory.SIDE).build(),
				ConsultingImageFileRequest.builder().fileId(301L).category(ConsultingRequestImageCategory.TOP).build(),
				ConsultingImageFileRequest.builder().fileId(302L).category(ConsultingRequestImageCategory.TOP).build(),
				ConsultingImageFileRequest.builder().fileId(303L).category(ConsultingRequestImageCategory.TOP).build(),
				ConsultingImageFileRequest.builder().fileId(401L).category(ConsultingRequestImageCategory.ASPIRATION).build(),
				ConsultingImageFileRequest.builder().fileId(402L).category(ConsultingRequestImageCategory.ASPIRATION).build(),
				ConsultingImageFileRequest.builder().fileId(403L).category(ConsultingRequestImageCategory.ASPIRATION).build()
			))
			.build();

		willDoNothing().given(consultingRequestFacade).save(anyLong(), any(ConsultingRequestSaveRequest.class));

		// when & then
		mockMvc.perform(post("/api/consulting-requests")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andDo(document("consulting-request-save",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키 (role: MEMBER, DIRECTOR)"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키 (role: MEMBER, DIRECTOR)")
				),

				requestFields(
					fieldWithPath("usesHairProduct").type(JsonFieldType.BOOLEAN)
						.description("헤어 제품 사용 여부"),
					fieldWithPath("prefersExposedForehead").type(JsonFieldType.BOOLEAN)
						.description("이마 노출 선호 여부"),
					fieldWithPath("recentProcedure").type(JsonFieldType.STRING)
						.description("최근 시술 여부 (자유 텍스트)"),
					fieldWithPath("locations").type(JsonFieldType.ARRAY)
						.description("선택 지역 ID 목록 (최소 1개, 최대 3개)"),
					fieldWithPath("files").type(JsonFieldType.ARRAY)
						.description("컨설팅 이미지 파일 목록"),
					fieldWithPath("files[].fileId").type(JsonFieldType.NUMBER)
						.description("파일 ID (presigned-url로 업로드된 파일)"),
					fieldWithPath("files[].category").type(JsonFieldType.STRING)
						.attributes(enumFormat(ConsultingRequestImageCategory.class, Enum::name))
						.description("이미지 카테고리")
				),

				resource(builder()
					.tag("💆 컨설팅 API")
					.summary("컨설팅 요청")
					.description("회원이 카테고리별 이미지 + 질문 응답으로 컨설팅을 요청합니다. 카테고리별 최소 1장, 최대 3장의 이미지가 필요합니다.")
					.build())
			));
	}

	@Test
	void 컨설팅_자격_확인_비인증_사용자() throws Exception {
		// given
		ConsultingEligibilityResponse response = ConsultingEligibilityResponse.ofUnauthenticated();

		given(consultingRequestFacade.checkEligibility(null)).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/members/consulting/eligibility"))
			.andExpect(status().isOk())
			.andDo(document("consulting-eligibility-check-unauthenticated",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				responseFields(
					fieldWithPath("hasUsedInviteCode").type(JsonFieldType.BOOLEAN)
						.description("초대 코드 사용 여부 (비인증 사용자는 항상 false)"),
					fieldWithPath("hasConsultingRequest").type(JsonFieldType.BOOLEAN)
						.description("컨설팅 신청서 제출 여부 (비인증 사용자는 항상 false)"),
					fieldWithPath("consultingSheetId").type(JsonFieldType.NULL)
						.description("승인된 컨설팅 시트 ID (비인증 사용자는 null)"),
					fieldWithPath("referralCode").type(JsonFieldType.NULL)
						.description("회원 추천 코드 (비인증 사용자는 null)")
				),

				resource(builder()
					.tag("💆 컨설팅 API")
					.summary("컨설팅 신청 자격 확인 (비인증)")
					.description("비인증 사용자의 컨설팅 신청 가능 여부를 확인합니다.")
					.build())
			));
	}
}
