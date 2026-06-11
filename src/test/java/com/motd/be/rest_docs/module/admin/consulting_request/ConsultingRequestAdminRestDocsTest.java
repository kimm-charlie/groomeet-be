package com.motd.be.rest_docs.module.admin.consulting_request;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.common.utils.DateFormatUtils.*;
import static com.motd.be.rest_docs.Utils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.admin.consulting_request.dto.response.ConsultingRequestFindAllResponseForAdmin;
import com.motd.be.module.admin.consulting_request.dto.response.ConsultingRequestFindDetailResponseForAdmin;
import com.motd.be.module.admin.consulting_request.dto.response.ConsultingRequestSummaryResponseForAdmin;
import com.motd.be.module.admin.consulting_request.dto.response.ConsultingSheetSummaryForConsultingRequest;
import com.motd.be.module.admin.member.dto.response.MemberSummaryForAdmin;
import com.motd.be.module.member.consulting_request.enums.ConsultingRequestStatus;
import com.motd.be.module.member.consulting_sheet.enums.ConsultingSheetStatus;
import com.motd.be.module.member.file.dto.response.FileResponse;
import com.motd.be.shared.aws.enums.UploadFileType;

import jakarta.servlet.http.Cookie;

@RestDocsTest
class ConsultingRequestAdminRestDocsTest extends BaseRestDocsTest {

	@Test
	@DisplayName("관리자 컨설팅 요청서 목록 조회")
	void findAll() throws Exception {
		// given
		authenticationSetUp();

		ConsultingRequestFindAllResponseForAdmin response = ConsultingRequestFindAllResponseForAdmin.builder()
			.page(PAGE)
			.hasNext(Boolean.FALSE)
			.totalCount(2L)
			.consultingRequests(List.of(
				ConsultingRequestSummaryResponseForAdmin.builder()
					.id(1L)
					.requestStatus(ConsultingRequestStatus.PENDING.name())
					.sheetStatus(null)
					.createdAt(formatToDateString(LocalDateTime.now()))
					.member(MemberSummaryForAdmin.builder()
						.id(5L)
						.nickname("일반회원")
						.build())
					.director(null)
					.content(null)
					.price(null)
					.build(),
				ConsultingRequestSummaryResponseForAdmin.builder()
					.id(2L)
					.requestStatus(ConsultingRequestStatus.COMPLETED.name())
					.sheetStatus(ConsultingSheetStatus.PENDING_APPROVAL.name())
					.createdAt(formatToDateString(LocalDateTime.now()))
					.member(MemberSummaryForAdmin.builder()
						.id(6L)
						.nickname("다른회원")
						.build())
					.director(MemberSummaryForAdmin.builder()
						.id(10L)
						.nickname("전문디렉터")
						.build())
					.content("컨설팅 상세 내용입니다.")
					.price("50000")
					.build()
			))
			.build();

		given(consultingRequestFacadeForAdmin.findAll(any(), any(), anyInt())).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/admin/consulting-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR))
				.param(PAGE_STR, String.valueOf(PAGE))
				.param("search", "일반회원")
				.param("showAll", "false"))
			.andExpect(status().isOk())
			.andDo(document("admin-consulting-request-find-all",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(PAGE_STR)
						.optional()
						.description("페이지 번호 (기본값: 0)"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("search")
						.optional()
						.description("검색어 (회원명 또는 디렉터명)"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("showAll")
						.optional()
						.description("전체 조회 여부 (승인 완료 건 포함) (기본값: false)")
				),

				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER)
						.description("현재 페이지 번호"),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN)
						.description("다음 페이지 존재 여부"),
					fieldWithPath("totalCount").type(JsonFieldType.NUMBER)
						.description("현재 필터 기준 전체 건수"),
					fieldWithPath("consultingRequests").type(JsonFieldType.ARRAY)
						.description("컨설팅 요청서 목록"),
					fieldWithPath("consultingRequests[].id").type(JsonFieldType.NUMBER)
						.description("요청서 ID"),
					fieldWithPath("consultingRequests[].requestStatus").type(JsonFieldType.STRING)
						.attributes(enumFormat(ConsultingRequestStatus.class, Enum::name))
						.description("요청서 상태"),
					fieldWithPath("consultingRequests[].sheetStatus").type(JsonFieldType.STRING)
						.optional()
						.attributes(enumFormat(ConsultingSheetStatus.class, Enum::name))
						.description("컨설팅지 상태 (없으면 null)"),
					fieldWithPath("consultingRequests[].createdAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("요청 생성 일시"),
					fieldWithPath("consultingRequests[].member").type(JsonFieldType.OBJECT)
						.description("요청 회원 정보"),
					fieldWithPath("consultingRequests[].member.id").type(JsonFieldType.NUMBER)
						.description("회원 ID"),
					fieldWithPath("consultingRequests[].member.nickname").type(JsonFieldType.STRING)
						.description("회원 닉네임"),
					fieldWithPath("consultingRequests[].director").type(JsonFieldType.OBJECT)
						.optional()
						.description("디렉터 정보 (없으면 null)"),
					fieldWithPath("consultingRequests[].director.id").type(JsonFieldType.NUMBER)
						.optional()
						.description("디렉터 ID"),
					fieldWithPath("consultingRequests[].director.nickname").type(JsonFieldType.STRING)
						.optional()
						.description("디렉터 닉네임"),
					fieldWithPath("consultingRequests[].content").type(JsonFieldType.STRING)
						.optional()
						.description("컨설팅 내용 (컨설팅지가 있는 경우)"),
					fieldWithPath("consultingRequests[].price").type(JsonFieldType.STRING)
						.optional()
						.description("컨설팅 가격 (컨설팅지가 있는 경우)")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 컨설팅 요청서 목록 조회 API")
					.description("관리자 컨설팅 요청서 목록 조회 API (검색 및 전체/미처리 필터링 지원)")
					.queryParameters(
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName(PAGE_STR)
							.optional()
							.description("페이지 번호"),
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName("search")
							.optional()
							.description("검색어"),
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName("showAll")
							.optional()
							.description("전체 조회 여부 (승인 완료 건 포함)")
					)
					.build()
				)
			));
	}

	@Test
	@DisplayName("관리자 컨설팅 요청서 상세 조회")
	void findDetail() throws Exception {
		// given
		authenticationSetUp();

		ConsultingRequestFindDetailResponseForAdmin response = ConsultingRequestFindDetailResponseForAdmin.builder()
			.id(1L)
			.usesHairProduct(Boolean.TRUE)
			.prefersExposedForehead(Boolean.FALSE)
			.recentProcedure("없음")
			.requestStatus(ConsultingRequestStatus.COMPLETED.name())
			.createdAt(formatToDateString(LocalDateTime.now()))
			.member(MemberSummaryForAdmin.builder()
				.id(5L)
				.nickname("일반회원")
				.build())
			.files(List.of(
				FileResponse.builder()
					.id(2L)
					.fileUrl("https://cdn.example.com/consulting-request.jpg")
					.fileName("요청이미지.jpg")
					.fileType(UploadFileType.IMAGE)
					.fileSize("2048")
					.build()
			))
			.consultingSheet(ConsultingSheetSummaryForConsultingRequest.builder()
				.id(10L)
				.content("컨설팅 상세 내용입니다.")
				.price("50000")
				.status(ConsultingSheetStatus.PENDING_APPROVAL.name())
				.createdAt(formatToDateString(LocalDateTime.now()))
				.approvedAt(null)
				.director(MemberSummaryForAdmin.builder()
					.id(10L)
					.nickname("전문디렉터")
					.build())
				.files(List.of(
					FileResponse.builder()
						.id(1L)
						.fileUrl("https://cdn.example.com/consulting-sheet.jpg")
						.fileName("컨설팅이미지.jpg")
						.fileType(UploadFileType.IMAGE)
						.fileSize("1024")
						.build()
				))
				.build())
			.build();

		given(consultingRequestFacadeForAdmin.findDetail(anyLong())).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/admin/consulting-requests/{consultingRequestId}", 1L)
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)))
			.andExpect(status().isOk())
			.andDo(document("admin-consulting-request-find-detail",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("consultingRequestId")
						.description("컨설팅 요청서 ID")
				),

				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER)
						.description("요청서 ID"),
					fieldWithPath("usesHairProduct").type(JsonFieldType.BOOLEAN)
						.description("헤어 제품 사용 여부"),
					fieldWithPath("prefersExposedForehead").type(JsonFieldType.BOOLEAN)
						.description("이마 노출 선호 여부"),
					fieldWithPath("recentProcedure").type(JsonFieldType.STRING)
						.description("최근 시술 이력"),
					fieldWithPath("requestStatus").type(JsonFieldType.STRING)
						.attributes(enumFormat(ConsultingRequestStatus.class, Enum::name))
						.description("요청서 상태"),
					fieldWithPath("createdAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("요청 생성 일시"),
					fieldWithPath("member").type(JsonFieldType.OBJECT)
						.description("요청 회원 정보"),
					fieldWithPath("member.id").type(JsonFieldType.NUMBER)
						.description("회원 ID"),
					fieldWithPath("member.nickname").type(JsonFieldType.STRING)
						.description("회원 닉네임"),
					fieldWithPath("files").type(JsonFieldType.ARRAY)
						.description("요청서 첨부 파일 목록"),
					fieldWithPath("files[].id").type(JsonFieldType.NUMBER)
						.description("파일 ID"),
					fieldWithPath("files[].fileUrl").type(JsonFieldType.STRING)
						.description("파일 URL"),
					fieldWithPath("files[].fileName").type(JsonFieldType.STRING)
						.description("원본 파일명"),
					fieldWithPath("files[].fileType").type(JsonFieldType.STRING)
						.description("파일 타입 (IMAGE, DOCUMENT)"),
					fieldWithPath("files[].fileSize").type(JsonFieldType.STRING)
						.description("파일 크기"),
					fieldWithPath("consultingSheet").type(JsonFieldType.OBJECT)
						.optional()
						.description("컨설팅지 정보 (없으면 null)"),
					fieldWithPath("consultingSheet.id").type(JsonFieldType.NUMBER)
						.description("컨설팅지 ID"),
					fieldWithPath("consultingSheet.content").type(JsonFieldType.STRING)
						.description("컨설팅 내용"),
					fieldWithPath("consultingSheet.price").type(JsonFieldType.STRING)
						.description("컨설팅 가격"),
					fieldWithPath("consultingSheet.status").type(JsonFieldType.STRING)
						.attributes(enumFormat(ConsultingSheetStatus.class, Enum::name))
						.description("컨설팅지 상태"),
					fieldWithPath("consultingSheet.createdAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("컨설팅지 생성 일시"),
					fieldWithPath("consultingSheet.approvedAt").type(JsonFieldType.STRING)
						.optional()
						.attributes(getDateTimeFormat())
						.description("승인 일시"),
					fieldWithPath("consultingSheet.director").type(JsonFieldType.OBJECT)
						.description("디렉터 정보"),
					fieldWithPath("consultingSheet.director.id").type(JsonFieldType.NUMBER)
						.description("디렉터 ID"),
					fieldWithPath("consultingSheet.director.nickname").type(JsonFieldType.STRING)
						.description("디렉터 닉네임"),
					fieldWithPath("consultingSheet.files").type(JsonFieldType.ARRAY)
						.description("컨설팅지 첨부 파일 목록"),
					fieldWithPath("consultingSheet.files[].id").type(JsonFieldType.NUMBER)
						.description("파일 ID"),
					fieldWithPath("consultingSheet.files[].fileUrl").type(JsonFieldType.STRING)
						.description("파일 URL"),
					fieldWithPath("consultingSheet.files[].fileName").type(JsonFieldType.STRING)
						.description("원본 파일명"),
					fieldWithPath("consultingSheet.files[].fileType").type(JsonFieldType.STRING)
						.description("파일 타입 (IMAGE, DOCUMENT)"),
					fieldWithPath("consultingSheet.files[].fileSize").type(JsonFieldType.STRING)
						.description("파일 크기")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 컨설팅 요청서 상세 조회 API")
					.description("관리자 컨설팅 요청서 상세 조회 API (요청서 + 컨설팅지 정보 포함)")
					.pathParameters(
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName("consultingRequestId")
							.description("컨설팅 요청서 ID")
					)
					.build()
				)
			));
	}
}
