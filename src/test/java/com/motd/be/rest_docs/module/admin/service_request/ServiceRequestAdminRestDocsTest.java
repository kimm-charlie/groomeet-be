package com.motd.be.rest_docs.module.admin.service_request;

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
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.admin.location.dto.response.LocationResponseForAdmin;
import com.motd.be.module.admin.member.dto.response.MemberSummaryForAdmin;
import com.motd.be.module.admin.service_estimate.dto.response.ServiceEstimateSummaryForAdmin;
import com.motd.be.module.admin.service_request.dto.response.ServiceRequestFindAllResponseForAdmin;
import com.motd.be.module.admin.service_request.dto.response.ServiceRequestFindDetailResponseForAdmin;
import com.motd.be.module.admin.service_request.dto.response.ServiceRequestSummaryResponseForAdmin;
import com.motd.be.module.member.file.dto.response.FileResponse;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;
import com.motd.be.module.member.service_request.entity.StopReceivingEstimateReason;
import com.motd.be.module.member.service_request_wish_time.dto.response.WishTimeResponse;
import com.motd.be.shared.aws.enums.UploadFileType;

import jakarta.servlet.http.Cookie;

@RestDocsTest
class ServiceRequestAdminRestDocsTest extends BaseRestDocsTest {

	@Test
	@DisplayName("관리자 서비스 요청 목록 조회")
	void findAll() throws Exception {
		// given
		authenticationSetUp();

		ServiceRequestFindAllResponseForAdmin response = ServiceRequestFindAllResponseForAdmin.builder()
			.page(PAGE)
			.hasNext(Boolean.FALSE)
			.serviceRequests(List.of(
				ServiceRequestSummaryResponseForAdmin.builder()
					.serviceRequestId(1L)
					.member(MemberSummaryForAdmin.builder()
						.id(1L)
						.nickname("요청자닉네임")
						.build())
					.serviceName("PT")
					.isDirectRequest(Boolean.FALSE)
					.status(ServiceRequestStatus.PENDING.getDescription())
					.receivedEstimateCount(2)
					.isReceivingEstimate(true)
					.createdAt(formatToDateString(LocalDateTime.now()))
					.build()
			))
			.build();

		given(serviceRequestFacadeForAdmin.findAll(any(), any(), anyInt())).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/admin/service-requests")
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR))
				.param(PAGE_STR, String.valueOf(PAGE))
				.param("search", "닉네임")
				.param("status", ServiceRequestStatus.PENDING.name()))
			.andExpect(status().isOk())
			.andDo(document("admin-service-request-find-all",
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
						.description("검색어 (회원 ID 또는 닉네임)"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("status")
						.optional()
						.attributes(enumFormat(ServiceRequestStatus.class, Enum::name))
						.description("상태 필터 (PENDING, ONGOING, COMPLETED, EXPIRED, CANCELED)")
				),

				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER)
						.description("현재 페이지 번호"),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN)
						.description("다음 페이지 존재 여부"),
					fieldWithPath("serviceRequests").type(JsonFieldType.ARRAY)
						.description("서비스 요청 목록"),
					fieldWithPath("serviceRequests[].serviceRequestId").type(JsonFieldType.NUMBER)
						.description("서비스 요청 ID"),
					fieldWithPath("serviceRequests[].member").type(JsonFieldType.OBJECT)
						.description("요청 회원 정보"),
					fieldWithPath("serviceRequests[].member.id").type(JsonFieldType.NUMBER)
						.description("회원 ID"),
					fieldWithPath("serviceRequests[].member.nickname").type(JsonFieldType.STRING)
						.description("회원 닉네임"),
					fieldWithPath("serviceRequests[].serviceName").type(JsonFieldType.STRING)
						.description("서비스 카테고리명"),
					fieldWithPath("serviceRequests[].isDirectRequest").type(JsonFieldType.BOOLEAN)
						.description("지정 요청 여부"),
					fieldWithPath("serviceRequests[].directRequestedMember").type(JsonFieldType.OBJECT)
						.optional()
						.description("지정 요청 대상 고수 정보"),
					fieldWithPath("serviceRequests[].directRequestedMember.id").type(JsonFieldType.NUMBER)
						.optional()
						.description("지정 고수 ID"),
					fieldWithPath("serviceRequests[].directRequestedMember.nickname").type(JsonFieldType.STRING)
						.optional()
						.description("지정 고수 닉네임"),
					fieldWithPath("serviceRequests[].status").type(JsonFieldType.STRING)
						.description("상태"),
					fieldWithPath("serviceRequests[].receivedEstimateCount").type(JsonFieldType.NUMBER)
						.description("받은 제안 수"),
					fieldWithPath("serviceRequests[].isReceivingEstimate").type(JsonFieldType.BOOLEAN)
						.description("제안 수신 중 여부"),
					fieldWithPath("serviceRequests[].createdAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("요청 생성 일시")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 서비스 요청 목록 조회 API")
					.description("관리자 서비스 요청 목록 조회 API (검색 및 상태 필터링 지원)")
					.queryParameters(
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName(PAGE_STR)
							.optional()
							.description("페이지 번호"),
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName("search")
							.optional()
							.description("검색어"),
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName("status")
							.optional()
							.description("상태 필터")
					)
					.build()
				)
			));
	}

	@Test
	@DisplayName("관리자 서비스 요청 상세 조회")
	void findDetail() throws Exception {
		// given
		authenticationSetUp();

		ServiceRequestFindDetailResponseForAdmin response = ServiceRequestFindDetailResponseForAdmin.builder()
			.serviceRequestId(1L)
			.member(MemberSummaryForAdmin.builder()
				.id(1L)
				.nickname("요청자닉네임")
				.build())
			.serviceName("PT")
			.isDirectRequest(Boolean.FALSE)
			.wishTimes(List.of(
				WishTimeResponse.builder().wishTime("2026.02.15 10:00").build(),
				WishTimeResponse.builder().wishTime("2026.02.15 14:00").build()
			))
			.status(ServiceRequestStatus.PENDING.getDescription())
			.receivedEstimateCount(2)
			.isReceivingEstimate(true)
			.createdAt(formatToDateString(LocalDateTime.now()))
			.requestLocationMappings(List.of(
				LocationResponseForAdmin.builder()
					.id(100L)
					.name("강남구")
					.fullName("서울 강남구")
					.build()
			))
			.isDeleted(false)
			.stopReceivingEstimateReason(StopReceivingEstimateReason.ETC)
			.estimates(List.of(
				ServiceEstimateSummaryForAdmin.builder()
					.id(50L)
					.director(MemberSummaryForAdmin.builder()
						.id(20L)
						.nickname("고수닉네임")
						.build())
					.title("제안합니다")
					.content("안녕하세요, 열심히 하겠습니다.")
					.isHired(false)
					.files(List.of())
					.build()
			))
			.files(List.of(
				FileResponse.builder()
					.id(1L)
					.fileUrl("https://cdn.example.com/file.jpg")
					.fileName("file.jpg")
					.fileType(UploadFileType.IMAGE)
					.fileSize("1024")
					.build()
			))
			.build();

		given(serviceRequestFacadeForAdmin.findDetail(anyLong())).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/admin/service-requests/{serviceRequestId}", 1L)
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)))
			.andExpect(status().isOk())
			.andDo(document("admin-service-request-find-detail",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("serviceRequestId")
						.description("서비스 요청 ID")
				),

				responseFields(
					fieldWithPath("serviceRequestId").type(JsonFieldType.NUMBER)
						.description("서비스 요청 ID"),
					fieldWithPath("member").type(JsonFieldType.OBJECT)
						.description("요청 회원 정보"),
					fieldWithPath("member.id").type(JsonFieldType.NUMBER)
						.description("회원 ID"),
					fieldWithPath("member.nickname").type(JsonFieldType.STRING)
						.description("회원 닉네임"),
					fieldWithPath("serviceName").type(JsonFieldType.STRING)
						.description("서비스 카테고리명"),
					fieldWithPath("isDirectRequest").type(JsonFieldType.BOOLEAN)
						.description("지정 요청 여부"),
					fieldWithPath("directRequestedMember").type(JsonFieldType.OBJECT)
						.optional()
						.description("지정 요청 대상 고수 정보"),
					fieldWithPath("wishTimes").type(JsonFieldType.ARRAY)
						.description("희망 시간 목록"),
					fieldWithPath("wishTimes[].wishTime").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("희망 시간 (yyyy.MM.dd HH:mm)"),
					fieldWithPath("ongoingAt").type(JsonFieldType.STRING)
						.optional()
						.attributes(getDateTimeFormat())
						.description("진행 시작 일시"),
					fieldWithPath("completedAt").type(JsonFieldType.STRING)
						.optional()
						.attributes(getDateTimeFormat())
						.description("완료 일시"),
					fieldWithPath("canceledAt").type(JsonFieldType.STRING)
						.optional()
						.attributes(getDateTimeFormat())
						.description("취소 일시"),
					fieldWithPath("expiredAt").type(JsonFieldType.STRING)
						.optional()
						.attributes(getDateTimeFormat())
						.description("만료 일시"),
					fieldWithPath("status").type(JsonFieldType.STRING)
						.description("상태"),
					fieldWithPath("receivedEstimateCount").type(JsonFieldType.NUMBER)
						.description("받은 제안 수"),
					fieldWithPath("isReceivingEstimate").type(JsonFieldType.BOOLEAN)
						.description("제안 수신 중 여부"),
					fieldWithPath("createdAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("요청 생성 일시"),
					fieldWithPath("content").type(JsonFieldType.STRING)
						.optional()
						.description("요청 내용 (다이렉트 요청: 추가 요청 사항, 일반 요청: AI 생성 내용)"),
					fieldWithPath("requestLocationMappings").type(JsonFieldType.ARRAY)
						.description("요청 지역 목록"),
					fieldWithPath("requestLocationMappings[].id").type(JsonFieldType.NUMBER)
						.description("지역 ID"),
					fieldWithPath("requestLocationMappings[].name").type(JsonFieldType.STRING)
						.description("지역명"),
					fieldWithPath("requestLocationMappings[].fullName").type(JsonFieldType.STRING)
						.description("전체 지역명"),
					fieldWithPath("isDeleted").type(JsonFieldType.BOOLEAN)
						.description("삭제 여부"),
					fieldWithPath("stopReceivingEstimateReason").type(JsonFieldType.STRING)
						.optional()
						.description("제안 수신 중단 사유"),
					fieldWithPath("estimates").type(JsonFieldType.ARRAY)
						.description("받은 제안 목록"),
					fieldWithPath("estimates[].id").type(JsonFieldType.NUMBER)
						.description("제안 ID"),
					fieldWithPath("estimates[].director").type(JsonFieldType.OBJECT)
						.description("제안 보낸 고수 정보"),
					fieldWithPath("estimates[].director.id").type(JsonFieldType.NUMBER)
						.description("고수 ID"),
					fieldWithPath("estimates[].director.nickname").type(JsonFieldType.STRING)
						.description("고수 닉네임"),
					fieldWithPath("estimates[].title").type(JsonFieldType.STRING)
						.description("제안 제목"),
					fieldWithPath("estimates[].content").type(JsonFieldType.STRING)
						.description("제안 내용"),
					fieldWithPath("estimates[].files").type(JsonFieldType.ARRAY)
						.optional()
						.description("제안 첨부 파일 목록"),
					fieldWithPath("estimates[].isHired").type(JsonFieldType.BOOLEAN)
						.description("채택 여부"),
					fieldWithPath("files").type(JsonFieldType.ARRAY)
						.description("첨부 파일 목록"),
					fieldWithPath("files[].id").type(JsonFieldType.NUMBER)
						.description("파일 ID"),
					fieldWithPath("files[].fileUrl").type(JsonFieldType.STRING)
						.description("파일 URL"),
					fieldWithPath("files[].fileName").type(JsonFieldType.STRING)
						.description("원본 파일명"),
					fieldWithPath("files[].fileType").type(JsonFieldType.STRING)
						.description("파일 타입 (IMAGE, DOCUMENT)"),
					fieldWithPath("files[].fileSize").type(JsonFieldType.STRING)
						.description("파일 크기")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 서비스 요청 상세 조회 API")
					.description("관리자 서비스 요청 상세 조회 API")
					.pathParameters(
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName("serviceRequestId")
							.description("서비스 요청 ID")
					)
					.build()
				)
			));
	}
}
