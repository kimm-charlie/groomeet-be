package com.motd.be.rest_docs.module.admin.service_estimate;

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
import com.motd.be.module.admin.member.dto.response.MemberSummaryForAdmin;
import com.motd.be.module.admin.revice.dto.response.ReviewSummaryForAdmin;
import com.motd.be.module.admin.service_estimate.dto.response.ServiceEstimateFindAllResponseForAdmin;
import com.motd.be.module.admin.service_estimate.dto.response.ServiceEstimateFindDetailResponseForAdmin;
import com.motd.be.module.admin.service_estimate.dto.response.ServiceEstimateSummaryResponseForAdmin;
import com.motd.be.module.admin.service_estimate.dto.response.ServiceRequestSummaryForServiceEstimate;
import com.motd.be.module.member.file.dto.response.FileResponse;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.motd.be.shared.aws.enums.UploadFileType;

import jakarta.servlet.http.Cookie;

@RestDocsTest
class ServiceEstimateAdminRestDocsTest extends BaseRestDocsTest {

	@Test
	@DisplayName("관리자 서비스 제안 목록 조회")
	void findAll() throws Exception {
		// given
		authenticationSetUp();

		ServiceEstimateFindAllResponseForAdmin response = ServiceEstimateFindAllResponseForAdmin.builder()
			.page(PAGE)
			.hasNext(Boolean.FALSE)
			.serviceEstimates(List.of(
				ServiceEstimateSummaryResponseForAdmin.builder()
					.id(1L)
					.title("PT 제안서 제안드립니다")
					.price(100000L)
					.status(ServiceEstimateStatus.PENDING.getDescription())
					.createdAt(formatToDateString(LocalDateTime.now()))
					.isDeleted(Boolean.FALSE)
					.isHired(Boolean.FALSE)
					.director(MemberSummaryForAdmin.builder()
						.id(10L)
						.nickname("전문고수")
						.build())
					.member(MemberSummaryForAdmin.builder()
						.id(5L)
						.nickname("일반회원")
						.build())
					.serviceName("PT")
					.build()
			))
			.build();

		given(serviceEstimateFacadeForAdmin.findAll(any(), any(), anyInt())).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/admin/service-estimates")
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR))
				.param(PAGE_STR, String.valueOf(PAGE))
				.param("search", "전문고수")
				.param("status", ServiceEstimateStatus.PENDING.name()))
			.andExpect(status().isOk())
			.andDo(document("admin-service-estimate-find-all",
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
						.description("검색어 (고수 ID 또는 닉네임)"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("status")
						.optional()
						.attributes(enumFormat(ServiceEstimateStatus.class, Enum::name))
						.description(
							"상태 필터 (PENDING, ONGOING, EXPIRED, CANCELED, DIRECTOR_DONE, COMPLETED_BY_MEMBER, REVIEW_COMPLETED)")
				),

				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER)
						.description("현재 페이지 번호"),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN)
						.description("다음 페이지 존재 여부"),
					fieldWithPath("serviceEstimates").type(JsonFieldType.ARRAY)
						.description("서비스 제안 목록"),
					fieldWithPath("serviceEstimates[].id").type(JsonFieldType.NUMBER)
						.description("제안 ID"),
					fieldWithPath("serviceEstimates[].title").type(JsonFieldType.STRING)
						.description("제안 제목"),
					fieldWithPath("serviceEstimates[].price").type(JsonFieldType.NUMBER)
						.description("제안 금액"),
					fieldWithPath("serviceEstimates[].status").type(JsonFieldType.STRING)
						.description("제안 상태"),
					fieldWithPath("serviceEstimates[].createdAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("제안 생성 일시"),
					fieldWithPath("serviceEstimates[].isDeleted").type(JsonFieldType.BOOLEAN)
						.description("삭제 여부"),
					fieldWithPath("serviceEstimates[].isHired").type(JsonFieldType.BOOLEAN)
						.description("채택 여부"),
					fieldWithPath("serviceEstimates[].director").type(JsonFieldType.OBJECT)
						.description("제안 작성 고수 정보"),
					fieldWithPath("serviceEstimates[].director.id").type(JsonFieldType.NUMBER)
						.description("고수 ID"),
					fieldWithPath("serviceEstimates[].director.nickname").type(JsonFieldType.STRING)
						.description("고수 닉네임"),
					fieldWithPath("serviceEstimates[].member").type(JsonFieldType.OBJECT)
						.description("요청 회원 정보"),
					fieldWithPath("serviceEstimates[].member.id").type(JsonFieldType.NUMBER)
						.description("회원 ID"),
					fieldWithPath("serviceEstimates[].member.nickname").type(JsonFieldType.STRING)
						.description("회원 닉네임"),
					fieldWithPath("serviceEstimates[].serviceName").type(JsonFieldType.STRING)
						.description("서비스 카테고리명")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 서비스 제안 목록 조회 API")
					.description("관리자 서비스 제안 목록 조회 API (검색 및 상태 필터링 지원)")
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
	@DisplayName("관리자 서비스 제안 상세 조회")
	void findDetail() throws Exception {
		// given
		authenticationSetUp();

		ServiceEstimateFindDetailResponseForAdmin response = ServiceEstimateFindDetailResponseForAdmin.builder()
			.serviceEstimateId(1L)
			.title("PT 제안서 제안드립니다")
			.content("상세한 제안 내용입니다.")
			.price(100000L)
			.status(ServiceEstimateStatus.ONGOING.getDescription())
			.createdAt(formatToDateString(LocalDateTime.now()))
			.ongoingAt(formatToDateString(LocalDateTime.now()))
			.canceledAt(null)
			.expiredAt(null)
			.directorDoneAt(null)
			.memberCompletedAt(null)
			.isDeleted(Boolean.FALSE)
			.isHired(Boolean.TRUE)
			.director(MemberSummaryForAdmin.builder()
				.id(10L)
				.nickname("전문고수")
				.build())
			.member(MemberSummaryForAdmin.builder()
				.id(5L)
				.nickname("일반회원")
				.build())
			.serviceRequest(ServiceRequestSummaryForServiceEstimate.builder()
				.id(50L)
				.serviceName("PT")
				.build())
			.files(List.of(
				FileResponse.builder()
					.id(1L)
					.fileUrl("https://cdn.example.com/file.jpg")
					.fileName("제안서.pdf")
					.fileType(UploadFileType.DOCUMENT)
					.fileSize("2048")
					.build()
			))
			.review(ReviewSummaryForAdmin.builder()
				.id(200L)
				.title("리뷰 제목")
				.content("리뷰 내용")
				.createdAt(formatToDateString(LocalDateTime.now()))
				.isDeleted(Boolean.FALSE)
				.writer(MemberSummaryForAdmin.builder()
					.id(5L)
					.nickname("일반회원")
					.build())
				.images(List.of(
					FileResponse.builder()
						.id(2L)
						.fileUrl("https://cdn.example.com/review.jpg")
						.fileName("리뷰이미지.jpg")
						.fileType(UploadFileType.IMAGE)
						.fileSize("1024")
						.build()
				))
				.build())
			.chatRoomId(300L)
			.build();

		given(serviceEstimateFacadeForAdmin.findDetail(anyLong())).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/admin/service-estimates/{serviceEstimateId}", 1L)
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)))
			.andExpect(status().isOk())
			.andDo(document("admin-service-estimate-find-detail",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("serviceEstimateId")
						.description("제안 ID")
				),

				responseFields(
					fieldWithPath("serviceEstimateId").type(JsonFieldType.NUMBER)
						.description("제안 ID"),
					fieldWithPath("title").type(JsonFieldType.STRING)
						.description("제안 제목"),
					fieldWithPath("content").type(JsonFieldType.STRING)
						.description("제안 내용"),
					fieldWithPath("price").type(JsonFieldType.NUMBER)
						.description("제안 금액"),
					fieldWithPath("status").type(JsonFieldType.STRING)
						.description("제안 상태"),
					fieldWithPath("createdAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("제안 생성 일시"),
					fieldWithPath("ongoingAt").type(JsonFieldType.STRING)
						.optional()
						.attributes(getDateTimeFormat())
						.description("진행 시작 일시"),
					fieldWithPath("canceledAt").type(JsonFieldType.STRING)
						.optional()
						.attributes(getDateTimeFormat())
						.description("취소 일시"),
					fieldWithPath("expiredAt").type(JsonFieldType.STRING)
						.optional()
						.attributes(getDateTimeFormat())
						.description("만료 일시"),
					fieldWithPath("directorDoneAt").type(JsonFieldType.STRING)
						.optional()
						.attributes(getDateTimeFormat())
						.description("고수 작업 완료 일시"),
					fieldWithPath("memberCompletedAt").type(JsonFieldType.STRING)
						.optional()
						.attributes(getDateTimeFormat())
						.description("회원 거래 확정 일시"),
					fieldWithPath("isDeleted").type(JsonFieldType.BOOLEAN)
						.description("삭제 여부"),
					fieldWithPath("isHired").type(JsonFieldType.BOOLEAN)
						.description("채택 여부"),
					fieldWithPath("director").type(JsonFieldType.OBJECT)
						.description("제안 작성 고수 정보"),
					fieldWithPath("director.id").type(JsonFieldType.NUMBER)
						.description("고수 ID"),
					fieldWithPath("director.nickname").type(JsonFieldType.STRING)
						.description("고수 닉네임"),
					fieldWithPath("member").type(JsonFieldType.OBJECT)
						.description("요청 회원 정보"),
					fieldWithPath("member.id").type(JsonFieldType.NUMBER)
						.description("회원 ID"),
					fieldWithPath("member.nickname").type(JsonFieldType.STRING)
						.description("회원 닉네임"),
					fieldWithPath("serviceRequest").type(JsonFieldType.OBJECT)
						.description("서비스 요청 요약 정보"),
					fieldWithPath("serviceRequest.id").type(JsonFieldType.NUMBER)
						.description("서비스 요청 ID"),
					fieldWithPath("serviceRequest.serviceName").type(JsonFieldType.STRING)
						.description("서비스 카테고리명"),
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
						.description("파일 크기"),
					fieldWithPath("review").type(JsonFieldType.OBJECT)
						.optional()
						.description("리뷰 정보"),
					fieldWithPath("review.id").type(JsonFieldType.NUMBER)
						.optional()
						.description("리뷰 ID"),
					fieldWithPath("review.title").type(JsonFieldType.STRING)
						.optional()
						.description("리뷰 제목"),
					fieldWithPath("review.content").type(JsonFieldType.STRING)
						.optional()
						.description("리뷰 내용"),
					fieldWithPath("review.createdAt").type(JsonFieldType.STRING)
						.optional()
						.attributes(getDateTimeFormat())
						.description("리뷰 생성 일시"),
					fieldWithPath("review.isDeleted").type(JsonFieldType.BOOLEAN)
						.optional()
						.description("리뷰 삭제 여부"),
					fieldWithPath("review.writer").type(JsonFieldType.OBJECT)
						.optional()
						.description("리뷰 작성자 정보"),
					fieldWithPath("review.writer.id").type(JsonFieldType.NUMBER)
						.optional()
						.description("리뷰 작성자 ID"),
					fieldWithPath("review.writer.nickname").type(JsonFieldType.STRING)
						.optional()
						.description("리뷰 작성자 닉네임"),
					fieldWithPath("review.images").type(JsonFieldType.ARRAY)
						.optional()
						.description("리뷰 이미지 목록"),
					fieldWithPath("review.images[].id").type(JsonFieldType.NUMBER)
						.optional()
						.description("리뷰 이미지 ID"),
					fieldWithPath("review.images[].fileUrl").type(JsonFieldType.STRING)
						.optional()
						.description("리뷰 이미지 URL"),
					fieldWithPath("review.images[].fileName").type(JsonFieldType.STRING)
						.optional()
						.description("리뷰 이미지 원본 파일명"),
					fieldWithPath("review.images[].fileType").type(JsonFieldType.STRING)
						.optional()
						.description("리뷰 이미지 타입 (IMAGE, DOCUMENT)"),
					fieldWithPath("review.images[].fileSize").type(JsonFieldType.STRING)
						.optional()
						.description("리뷰 이미지 크기"),
					fieldWithPath("chatRoomId").type(JsonFieldType.NUMBER)
						.optional()
						.description("채팅방 ID (채팅방이 존재하지 않을 경우 null)")
				),

				resource(builder()
					.tag("⭐ 관리자 관련 API")
					.summary("관리자 서비스 제안 상세 조회 API")
					.description("관리자 서비스 제안 상세 조회 API")
					.pathParameters(
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName("serviceEstimateId")
							.description("제안 ID")
					)
					.build()
				)
			));
	}
}
