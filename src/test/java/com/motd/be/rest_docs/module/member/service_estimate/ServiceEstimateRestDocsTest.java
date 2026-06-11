package com.motd.be.rest_docs.module.member.service_estimate;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.common.utils.DateFormatUtils.*;
import static com.motd.be.rest_docs.Utils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.member.file.dto.response.FileResponse;
import com.motd.be.module.member.member.dto.response.MemberResponseWithCompletedAndReviewCountResponse;
import com.motd.be.module.member.service_estimate.dto.response.ServiceEstimateFindAllResponse;
import com.motd.be.module.member.service_estimate.dto.response.ServiceEstimateFindDetailResponse;
import com.motd.be.module.member.service_estimate.dto.response.ServiceEstimateHistoriesResponse;
import com.motd.be.module.member.service_estimate.dto.response.ServiceEstimateHistory;
import com.motd.be.module.member.service_estimate.dto.response.ServiceEstimateResponseWithStatusAndMember;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.motd.be.shared.aws.enums.UploadFileType;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class ServiceEstimateRestDocsTest extends BaseRestDocsTest {

	@Test
	void 서비스_제안_조회_for_member() throws Exception {
		authenticationSetUp();

		ServiceEstimateFindAllResponse response = ServiceEstimateFindAllResponse.builder()
			.page(0)
			.hasNext(false)
			.serviceEstimates(Arrays.asList(
				ServiceEstimateResponseWithStatusAndMember.builder()
					.id(1L)
					.member(MemberResponseWithCompletedAndReviewCountResponse.builder()
						.id(ID)
						.nickname(NICKNAME_STR)
						.profileImageUrl(IMAGE_URL_STR)
						.completedEstimateCount(3)
						.reviewCount(10)
						.isWithdrawal(false)
						.build())
					.status(ServiceEstimateStatus.PENDING.getDescription())
					.price(50000L)
					.scheduledAt(formatToDateString(LocalDateTime.now().plusDays(3)))
					.chatRoomId(1L)
					.build(),
				ServiceEstimateResponseWithStatusAndMember.builder()
					.id(2L)
					.member(MemberResponseWithCompletedAndReviewCountResponse.builder()
						.id(ID)
						.nickname(NICKNAME_STR)
						.profileImageUrl(IMAGE_URL_STR)
						.completedEstimateCount(3)
						.reviewCount(10)
						.isWithdrawal(false)
						.build())
					.price(40000L)
					.scheduledAt(formatToDateString(LocalDateTime.now().plusDays(5)))
					.status(ServiceEstimateStatus.PENDING.getDescription())
					.chatRoomId(2L)
					.build()
			))
			.build();

		given(serviceEstimateFacade.findAllByRequestIdForPublic(anyLong(), anyLong(), anyInt())).willReturn(response);

		mockMvc.perform(get("/api/service-estimates")
				.param("serviceRequestId", "1")
				.param("page", ZERO_STR)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isOk())
			.andDo(document("service-estimate-findAll-for-public",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("serviceRequestId")
						.description("서비스 요청 ID"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("page")
						.optional()
						.description("페이지 번호 (0부터 시작)")
				),

				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
					fieldWithPath("serviceEstimates").type(JsonFieldType.ARRAY).description("서비스 제안 목록"),

					fieldWithPath("serviceEstimates[].id").type(JsonFieldType.NUMBER).description("제안 ID"),
					fieldWithPath("serviceEstimates[].price").type(JsonFieldType.NUMBER).description("제안 가격"),
					fieldWithPath("serviceEstimates[].chatRoomId").type(JsonFieldType.NUMBER).description("채팅방 아이디"),
					fieldWithPath("serviceEstimates[].member").type(JsonFieldType.OBJECT).description("디렉터 정보"),
					fieldWithPath("serviceEstimates[].member.id").type(JsonFieldType.NUMBER).description("디렉터 회원 ID"),
					fieldWithPath("serviceEstimates[].member.nickname").type(JsonFieldType.STRING)
						.description("디렉터 닉네임"),
					fieldWithPath("serviceEstimates[].member.profileImageUrl").type(JsonFieldType.STRING)
						.description("디렉터 프로필 이미지 URL"),
					fieldWithPath("serviceEstimates[].member.completedEstimateCount").type(JsonFieldType.NUMBER)
						.description("디렉터의 완료된 제안 수"),
					fieldWithPath("serviceEstimates[].member.reviewCount").type(JsonFieldType.NUMBER)
						.description("디렉터의 리뷰 갯수"),
					fieldWithPath("serviceEstimates[].member.isWithdrawal").type(JsonFieldType.BOOLEAN)
						.description("탈퇴 여부"),

					fieldWithPath("serviceEstimates[].scheduledAt").type(JsonFieldType.STRING)
						.optional()
						.attributes(getDateTimeFormat())
						.description("제안 시간"),
					fieldWithPath("serviceEstimates[].status").type(JsonFieldType.STRING)
						.attributes((enumFormat(ServiceEstimateStatus.class, Enum::name)))
						.description("제안 상태")
				),

				resource(builder()
					.tag("📄 제안 API")
					.summary("회원 제안 전체 조회")
					.description("회원이 자신의 요청에 온 제안을 전체 조회 합니다.")
					.build())
			));
	}

	@Test
	void 서비스_제안_상세조회_for_member() throws Exception {
		authenticationSetUp();

		ServiceEstimateFindDetailResponse response = ServiceEstimateFindDetailResponse.builder()
			.id(1L)
			.member(MemberResponseWithCompletedAndReviewCountResponse.builder()
				.id(ID)
				.nickname(NICKNAME_STR)
				.profileImageUrl(IMAGE_URL_STR)
				.completedEstimateCount(3)
				.reviewCount(5)
				.isWithdrawal(true)
				.build())
			.price(75000L)
			.createdAt(formatToDateString(LocalDate.now()))
			.title(TITLE_STR)
			.content(CONTENT_STR)
			.completedAt(formatToDateString(LocalDate.now().plusDays(7)))
			.files(Arrays.asList(
				FileResponse.builder()
					.id(1L)
					.fileUrl(IMAGE_URL_STR)
					.fileType(UploadFileType.IMAGE)
					.fileSize("20MB")
					.fileName(null)
					.build(),
				FileResponse.builder()
					.id(2L)
					.fileUrl(IMAGE_URL_STR)
					.fileType(UploadFileType.IMAGE)
					.fileSize("20MB")
					.fileName(null)
					.build()
			))
			.scheduledAt(formatToDateString(LocalDateTime.now().plusDays(3)))
			.status(ServiceEstimateStatus.PENDING.getDescription())
			.chatRoomId(1L)
			.build();

		given(serviceEstimateFacade.findDetailForPublic(anyLong(), anyLong())).willReturn(response);

		mockMvc.perform(get("/api/service-estimates/{serviceEstimateId}", 1L)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isOk())
			.andDo(document("service-estimate-findDetail-for-public",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("serviceEstimateId")
						.description("서비스 제안 ID")
				),

				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER).description("제안 ID"),
					fieldWithPath("member").type(JsonFieldType.OBJECT).description("디렉터 정보"),
					fieldWithPath("member.id").type(JsonFieldType.NUMBER).description("디렉터 회원 ID"),
					fieldWithPath("member.nickname").type(JsonFieldType.STRING).description("디렉터 닉네임"),
					fieldWithPath("member.profileImageUrl").type(JsonFieldType.STRING).description("디렉터 프로필 이미지 URL"),
					fieldWithPath("member.completedEstimateCount").type(JsonFieldType.NUMBER)
						.description("디렉터의 완료된 제안 수"),
					fieldWithPath("member.reviewCount").type(JsonFieldType.NUMBER).description("리뷰 수"),
					fieldWithPath("member.isWithdrawal").type(JsonFieldType.BOOLEAN).description("탈퇴 여부"),
					fieldWithPath("price").type(JsonFieldType.NUMBER).description("제안 가격"),
					fieldWithPath("createdAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("생성일자"),
					fieldWithPath("title").type(JsonFieldType.STRING).description("제안 내용"),
					fieldWithPath("content").type(JsonFieldType.STRING).description("제안 내용"),
					fieldWithPath("completedAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("완료 일자"),
					fieldWithPath("scheduledAt").type(JsonFieldType.STRING)
						.optional()
						.attributes(getDateTimeFormat())
						.description("제안 시간"),
					fieldWithPath("status").type(JsonFieldType.STRING)
						.attributes((enumFormat(ServiceEstimateStatus.class, Enum::name)))
						.description("제안 상태"),
					fieldWithPath("chatRoomId").type(JsonFieldType.NUMBER)
						.description("채팅방 아이디"),

					fieldWithPath("files").type(JsonFieldType.ARRAY)
						.optional()
						.description("리뷰 파일 목록"),
					fieldWithPath("files[].id").type(JsonFieldType.NUMBER)
						.optional()
						.description("파일 ID"),
					fieldWithPath("files[].fileUrl").type(JsonFieldType.STRING)
						.optional()
						.description("파일 URL"),
					fieldWithPath("files[].fileType").type(JsonFieldType.STRING)
						.optional()
						.attributes(enumFormat(UploadFileType.class, Enum::name))
						.description("파일 타입"),
					fieldWithPath("files[].fileName").type(JsonFieldType.STRING)
						.optional()
						.description("파일 이름"),
					fieldWithPath("files[].fileSize").type(JsonFieldType.STRING)
						.optional()
						.description("파일 크기")
				),

				resource(builder()
					.tag("📄 제안 API")
					.summary("회원 제안 상세 조회")
					.description("회원이 자신의 요청에 온 특정 제안의 상세 정보를 조회합니다.")
					.build())
			));
	}

	@Test
	void 서비스_제안_수락_for_member() throws Exception {
		authenticationSetUp();

		doNothing().when(serviceEstimateFacade).acceptForPublic(anyLong(), anyLong());

		mockMvc.perform(post("/api/service-estimates/{serviceEstimateId}/accept", 1L)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isNoContent())
			.andDo(document("service-estimate-accept-for-public",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("serviceEstimateId")
						.description("수락할 서비스 제안 ID")
				),

				resource(builder()
					.tag("📄 제안 API")
					.summary("회원 제안 수락")
					.description("회원이 받은 제안을 수락합니다. 제안 상태가 PENDING -> ONGOING으로 변경되고, 같은 요청에 대한 다른 제안들은 자동으로 만료 처리됩니다. 제안 수락 리마인더가 초기화됩니다.")
					.build())
			));
	}

	@Test
	void 서비스_제안_완료처리_for_member() throws Exception {
		authenticationSetUp();

		doNothing().when(serviceEstimateFacade).completeForPublic(anyLong(), anyLong());

		mockMvc.perform(patch("/api/service-estimates/{serviceEstimateId}/complete", 1L)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isNoContent())
			.andDo(document("service-estimate-complete-for-public",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("serviceEstimateId")
						.description("완료 처리할 서비스 제안 ID")
				),

				resource(builder()
					.tag("📄 제안 API")
					.summary("회원 제안 완료 처리")
					.description("회원이 진행 중인 제안을 완료 처리합니다. 제안 상태가 ONGOING -> COMPLETED로 변경됩니다.")
					.build())
			));
	}

	@Test
	void 서비스_제안_진행_내역_조회_for_member() throws Exception {
		authenticationSetUp();

		ServiceEstimateHistoriesResponse response = ServiceEstimateHistoriesResponse.builder()
			.page(0)
			.hasNext(false)
			.histories(Arrays.asList(
				ServiceEstimateHistory.builder()
					.serviceEstimateId(1L)
					.serviceRequestId(10L)
					.content("웨딩 촬영 제안")
					.directorName("김디렉터")
					.price(150000L)
					.completedAt(formatToDateString(LocalDateTime.now().minusDays(3)))
					.build(),
				ServiceEstimateHistory.builder()
					.serviceEstimateId(2L)
					.serviceRequestId(11L)
					.content("돌잔치 촬영 제안")
					.directorName("이디렉터")
					.price(200000L)
					.completedAt(formatToDateString(LocalDateTime.now().minusDays(7)))
					.build()
			))
			.build();

		given(serviceEstimateFacade.findServiceEstimateHistoriesForPublic(anyLong(), anyInt())).willReturn(response);

		mockMvc.perform(get("/api/members/service-estimate/histories")
				.param("page", ZERO_STR)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isOk())
			.andDo(document("service-estimate-histories-for-public",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("page")
						.optional()
						.description("페이지 번호 (0부터 시작)")
				),

				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
					fieldWithPath("histories").type(JsonFieldType.ARRAY).description("서비스 제안 진행 내역 목록"),
					fieldWithPath("histories[].serviceEstimateId").type(JsonFieldType.NUMBER).description("제안 ID"),
					fieldWithPath("histories[].serviceRequestId").type(JsonFieldType.NUMBER).description("서비스 요청 ID"),
					fieldWithPath("histories[].content").type(JsonFieldType.STRING).description("제안 수락 내용"),
					fieldWithPath("histories[].directorName").type(JsonFieldType.STRING).description("디렉터 닉네임"),
					fieldWithPath("histories[].price").type(JsonFieldType.NUMBER).description("제안 가격"),
					fieldWithPath("histories[].completedAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("완료 일자")
				),

				resource(builder()
					.tag("📄 제안 API")
					.summary("회원 서비스 진행 내역 조회")
					.description("일반 회원이 자신의 서비스 제안 진행 내역을 전체 조회합니다.")
					.build())
			));
	}

}
