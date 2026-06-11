package com.motd.be.rest_docs.module.member.service_request;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.Constants.*;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.member.director_service.dto.response.DirectorServiceResponse;
import com.motd.be.module.member.director_service.dto.response.DirectorServiceWithFullNameResponse;
import com.motd.be.module.member.file.dto.response.FileResponse;
import com.motd.be.module.member.location.dto.response.LocationResponse;
import com.motd.be.module.member.member.dto.response.MemberResponse;
import com.motd.be.module.member.service_request.dto.request.ServiceRequestSaveDirectRequest;
import com.motd.be.module.member.service_request.dto.request.ServiceRequestSaveRequest;
import com.motd.be.module.member.service_request.dto.response.ServiceRequestFindAllResponseForPublic;
import com.motd.be.module.member.service_request.dto.response.ServiceRequestFindCountResponseForPublic;
import com.motd.be.module.member.service_request.dto.response.ServiceRequestFindDetailResponseForPublic;
import com.motd.be.module.member.service_request.dto.response.ServiceRequestResponseForPublic;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;
import com.motd.be.module.member.service_request.entity.StopReceivingEstimateReason;
import com.motd.be.module.member.service_request_wish_time.dto.response.WishTimeResponse;
import com.motd.be.shared.aws.enums.UploadFileType;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class ServiceRequestRestDocsTest extends BaseRestDocsTest {

	@Test
	void 요청_저장() throws Exception {
		authenticationSetUp();

		ServiceRequestSaveRequest request = ServiceRequestSaveRequest.builder()
			.locationIds(Arrays.asList(1L, 2L))
			.fileIds(Arrays.asList(1L, 2L))
			.directorServiceId(1L)
			.aiContent("테스트 AI 요청 내용")
			.wishTimes(Arrays.asList("2026.02.15 10:00", "2026.02.15 14:00", "2026.02.15 16:30"))
			.build();

		willDoNothing().given(serviceRequestFacade).save(anyLong(), any(ServiceRequestSaveRequest.class));

		mockMvc.perform(post("/api/service-requests")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andDo(document("service-request-save",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("locationIds")
						.type(JsonFieldType.ARRAY)
						.description("서비스 요청 지역 ID 리스트"),

					fieldWithPath("fileIds")
						.type(JsonFieldType.ARRAY)
						.optional()
						.description("이미지 ID 리스트"),

					fieldWithPath("directorServiceId")
						.type(JsonFieldType.NUMBER)
						.description("디렉터 서비스 ID"),

					fieldWithPath("aiContent")
						.type(JsonFieldType.STRING)
						.description("AI 생성 요청 내용"),

					fieldWithPath("wishTimes")
						.type(JsonFieldType.ARRAY)
						.attributes(getDateTimeFormat())
						.description("희망 시간 슬롯 리스트 (yyyy.MM.dd HH:mm 형식, 최대 3개)")
				),

				resource(builder()
					.tag("📋 요청 API")
					.summary("요청 저장.")
					.description("회원이 자신이 보낸 요청을 저장 합니다.")
					.build())
			));
	}

	@Test
	void 다이렉트_요청_저장() throws Exception {
		authenticationSetUp();

		ServiceRequestSaveDirectRequest request = ServiceRequestSaveDirectRequest.builder()
			.directRequestedMemberId(1L)
			.wishTimes(List.of("2026.02.15 10:00", "2026.02.15 14:00"))
			.directorServiceId(1L)
			.additionalRequest("추가 요청 사항")
			.build();

		willDoNothing().given(serviceRequestFacade).saveDirectRequest(anyLong(), any(ServiceRequestSaveDirectRequest.class));

		mockMvc.perform(post("/api/service-requests/direct")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andDo(document("service-request-save-direct",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("wishTimes")
						.type(JsonFieldType.ARRAY)
						.attributes(getDateTimeFormat())
						.description("희망 시간 슬롯 리스트 (yyyy.MM.dd HH:mm 형식, 최대 3개)"),

					fieldWithPath("directorServiceId")
						.type(JsonFieldType.NUMBER)
						.description("디렉터 서비스 ID"),

					fieldWithPath("directRequestedMemberId")
						.type(JsonFieldType.NUMBER)
						.description("다이렉트 요청한 회원 ID"),

					fieldWithPath("additionalRequest").type(JsonFieldType.STRING)
						.optional()
						.description("추가 요청 사항")
				),

				resource(builder()
					.tag("📋 요청 API")
					.summary("다이렉트 요청 저장.")
					.description("회원이 자신이 보낸 다이렉트 요청을 저장 합니다.")
					.build())
			));
	}

	@Test
	void 요청_목록_조회() throws Exception {
		authenticationSetUp();

		ServiceRequestFindAllResponseForPublic response = ServiceRequestFindAllResponseForPublic.builder()
			.page(0)
			.hasNext(false)
			.serviceRequests(Arrays.asList(
				ServiceRequestResponseForPublic
					.builder()
					.id(1L)
					.expiredAt(formatToDateString(LocalDateTime.now().plusDays(2)))
					.status(ServiceRequestStatus.PENDING.getDescription())
					.service(DirectorServiceWithFullNameResponse.builder()
						.id(1L)
						.name("헬스 케어")
						.fullName("건강 > 헬스 케어")
						.build())
					.locations(Arrays.asList(LocationResponse.builder()
						.id(1L)
						.name("송파구")
						.fullName("서울시 송파구")
						.build()))
					.isReceivingEstimate(Boolean.TRUE)
					.receivedEstimateCount(10)
					.createdAt(formatToDateString(LocalDateTime.now().minusDays(1)))
					.isDirectRequest(Boolean.TRUE)
					.wishTimes(List.of(
						WishTimeResponse.builder().wishTime("2026.02.15 10:00").build(),
						WishTimeResponse.builder().wishTime("2026.02.15 14:00").build()
					))
					.build(),
				ServiceRequestResponseForPublic
					.builder()
					.id(2L)
					.status(ServiceRequestStatus.ONGOING.getDescription())
					.expiredAt(formatToDateString(LocalDateTime.now().plusDays(5)))
					.service(DirectorServiceWithFullNameResponse.builder()
						.id(2L)
						.name("필라테스")
						.fullName("운동 > 필라테스")
						.build())
					.locations(Arrays.asList(LocationResponse.builder()
						.id(2L)
						.name("강남구")
						.fullName("서울시 강남구")
						.build()))
					.isReceivingEstimate(Boolean.FALSE)
					.receivedEstimateCount(10)
					.createdAt(formatToDateString(LocalDateTime.now().minusDays(1)))
					.isDirectRequest(Boolean.TRUE)
					.directors(List.of(MemberResponse.builder()
						.id(1L)
						.profileImageUrl(PROFILE_IMAGE)
						.nickname("디렉터 닉네임")
						.isWithdrawal(Boolean.FALSE)
						.build()))
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
					.wishTimes(List.of(
						WishTimeResponse.builder().wishTime("2026.02.15 10:00").build(),
						WishTimeResponse.builder().wishTime("2026.02.15 14:00").build()
					))
					.build(),
				ServiceRequestResponseForPublic
					.builder()
					.id(3L)
					.status(ServiceRequestStatus.ONGOING.getDescription())
					.expiredAt(formatToDateString(LocalDateTime.now().plusDays(7)))
					.service(DirectorServiceWithFullNameResponse.builder()
						.id(3L)
						.name("요가")
						.fullName("운동 > 요가")
						.build())
					.locations(Arrays.asList(LocationResponse.builder()
						.id(3L)
						.name("마포구")
						.fullName("서울시 마포구")
						.build()))
					.isReceivingEstimate(Boolean.FALSE)
					.receivedEstimateCount(10)
					.createdAt(formatToDateString(LocalDateTime.now().minusDays(1)))
					.isDirectRequest(Boolean.FALSE)
					.directors(List.of(MemberResponse.builder()
						.id(1L)
						.profileImageUrl(PROFILE_IMAGE)
						.nickname("디렉터 닉네임")
						.isWithdrawal(Boolean.FALSE)
						.build()))
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
					.wishTimes(List.of(
						WishTimeResponse.builder().wishTime("2026.02.15 10:00").build(),
						WishTimeResponse.builder().wishTime("2026.02.15 14:00").build()
					))
					.hiredServiceEstimateId(1L)
					.build()
			))
			.build();

		given(serviceRequestFacade.findAllForMember(anyLong(), anyBoolean(), anyInt(), anyLong())).willReturn(response);

		mockMvc.perform(get("/api/members/service-requests")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.param(SHOW_ONLY_PENDING_STR, Boolean.FALSE.toString())
				.param(PAGE_STR, ZERO_STR)
				.param(DIRECTOR_SERVICE_ID_STR, "1")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("service-request-findAll-for-member",
				getRequestPreProcessor(),
				getResponsePreProcessor(),
				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("showOnlyPending")
						.optional()
						.description("진행중인 요청만 조회할지 여부 (기본값: true)"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("page")
						.optional()
						.description("페이지 번호 (0부터 시작)"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("directorServiceId")
						.optional()
						.description("디렉터 서비스 ID")
				),

				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
					fieldWithPath("serviceRequests").type(JsonFieldType.ARRAY).description("요청 목록"),
					fieldWithPath("serviceRequests[].id").type(JsonFieldType.NUMBER).description("서비스 아이디"),
					fieldWithPath("serviceRequests[].receivedEstimateCount").type(JsonFieldType.NUMBER)
						.description("받은 제안 갯수"),
					fieldWithPath("serviceRequests[].status").type(JsonFieldType.STRING)
						.attributes((enumFormat(ServiceRequestStatus.class, Enum::name)))
						.description("서비스 아이디"),
					fieldWithPath("serviceRequests[].expiredAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("서비스 요청 마감일"),
					fieldWithPath("serviceRequests[].isReceivingEstimate").type(JsonFieldType.BOOLEAN)
						.description("요청이 현재 제안을 받고 있는지 여부"),

					fieldWithPath("serviceRequests[].isDirectRequest").type(JsonFieldType.BOOLEAN)
						.description("서비스 요청이 다이렉트 요청인지 여부"),
					fieldWithPath("serviceRequests[].hiredServiceEstimateId").type(JsonFieldType.NUMBER)
						.optional()
						.description("고용한 제안 아이디"),

					fieldWithPath("serviceRequests[].wishTimes").type(JsonFieldType.ARRAY)
						.description("희망 시간 목록"),
					fieldWithPath("serviceRequests[].wishTimes[].wishTime").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("희망 시간"),

					fieldWithPath("serviceRequests[].directors").type(JsonFieldType.ARRAY)
						.optional()
						.description("디렉터  목록"),
					fieldWithPath("serviceRequests[].directors[].id").type(JsonFieldType.NUMBER).description("디렉터 아이디"),
					fieldWithPath("serviceRequests[].directors[].profileImageUrl").type(JsonFieldType.STRING)
						.description("디렉터 프로필 이미지 URL"),
					fieldWithPath("serviceRequests[].directors[].nickname").type(JsonFieldType.STRING)
						.description("디렉터 닉네임"),
					fieldWithPath("serviceRequests[].directors[].isWithdrawal").type(JsonFieldType.BOOLEAN)
						.description("디렉터 탈퇴 여부"),

					fieldWithPath("serviceRequests[].files").type(JsonFieldType.ARRAY)
						.optional()
						.description("리뷰 파일 목록"),
					fieldWithPath("serviceRequests[].files[].id").type(JsonFieldType.NUMBER)
						.optional()
						.description("파일 ID"),
					fieldWithPath("serviceRequests[].files[].fileUrl").type(JsonFieldType.STRING)
						.optional()
						.description("파일 URL"),
					fieldWithPath("serviceRequests[].files[].fileType").type(JsonFieldType.STRING)
						.optional()
						.attributes(enumFormat(UploadFileType.class, Enum::name))
						.description("파일 타입"),
					fieldWithPath("serviceRequests[].files[].fileName").type(JsonFieldType.STRING)
						.optional()
						.description("파일 이름"),
					fieldWithPath("serviceRequests[].files[].fileSize").type(JsonFieldType.STRING)
						.optional()
						.description("파일 크기"),

					fieldWithPath("serviceRequests[].createdAt").type(JsonFieldType.STRING)
						.description("생성된 시간"),
					fieldWithPath("serviceRequests[].service").type(JsonFieldType.OBJECT).description("요청 서비스 정보"),
					fieldWithPath("serviceRequests[].service.id").type(JsonFieldType.NUMBER).description("서비스 ID"),
					fieldWithPath("serviceRequests[].service.name").type(JsonFieldType.STRING).description("서비스명"),
					fieldWithPath("serviceRequests[].service.fullName").type(JsonFieldType.STRING)
						.description("서비스 전체 경로명"),
					fieldWithPath("serviceRequests[].locations").type(JsonFieldType.ARRAY).description("요청 지역 목록"),
					fieldWithPath("serviceRequests[].locations[].id").type(JsonFieldType.NUMBER).description("지역 ID"),
					fieldWithPath("serviceRequests[].locations[].name").type(JsonFieldType.STRING).description("지역명"),
					fieldWithPath("serviceRequests[].locations[].fullName").type(JsonFieldType.STRING)
						.description("지역 전체 경로명")
				),

				resource(builder()
					.tag("📋 요청 API")
					.summary("회원이 자신이 보낸 요청 전체 조회")
					.description("회원이 자신이 보낸 요청을 전체 조회 합니다.")
					.build())
			));
	}

	@Test
	void 요청_상세_조회() throws Exception {
		authenticationSetUp();

		ServiceRequestFindDetailResponseForPublic response = ServiceRequestFindDetailResponseForPublic.builder()
			.id(1L)
			.service(DirectorServiceWithFullNameResponse.builder()
				.id(1L)
				.name("헬스 케어")
				.fullName("건강 > 헬스 케어")
				.build())
			.createdAt(formatToDateString(LocalDateTime.now()))
			.content("AI 생성 요청 내용")
			.wishTimes(List.of(
				WishTimeResponse.builder().wishTime("2026.02.15 10:00").build(),
				WishTimeResponse.builder().wishTime("2026.02.15 14:00").build()
			))
			.locations(List.of(
				LocationResponse.builder()
					.id(1L)
					.name("송파구")
					.fullName("서울시 송파구")
					.build(),
				LocationResponse.builder()
					.id(2L)
					.name("강동구")
					.fullName("서울시 강동구")
					.build()
			))
			.isReceivingEstimate(Boolean.TRUE)
			.isDirectRequest(Boolean.FALSE)
			.status(ServiceRequestStatus.PENDING.getDescription())
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
			.completedAt(formatToDateString(LocalDateTime.now()))
			.canceledAt(formatToDateString(LocalDateTime.now()))
			.expiredAt(formatToDateString(LocalDateTime.now().plusDays(7)))
			.build();

		given(serviceRequestFacade.findDetailForPublic(anyLong(), anyLong())).willReturn(response);

		mockMvc.perform(get("/api/members/service-requests/{serviceRequestId}", 1L)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("service-request-findDetail-for-member",
				getRequestPreProcessor(),
				getResponsePreProcessor(),
				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("serviceRequestId")
						.description("서비스 요청 ID")
				),

				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER).description("요청 아이디"),

					fieldWithPath("service").type(JsonFieldType.OBJECT).description("요청 서비스 정보"),
					fieldWithPath("service.id").type(JsonFieldType.NUMBER).description("서비스 ID"),
					fieldWithPath("service.name").type(JsonFieldType.STRING).description("서비스명"),
					fieldWithPath("service.fullName").type(JsonFieldType.STRING).description("서비스 전체 경로명"),

					fieldWithPath("createdAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("요청 생성일"),
					fieldWithPath("content").type(JsonFieldType.STRING)
						.optional()
						.description("요청 내용 (다이렉트 요청: 추가 요청 사항, 일반 요청: AI 생성 내용)"),
					fieldWithPath("completedAt").optional().type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("요청 완료일"),
					fieldWithPath("canceledAt").optional().type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("요청 취소 일"),
					fieldWithPath("expiredAt").optional().type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("요청 만료일"),

					fieldWithPath("isDirectRequest").type(JsonFieldType.BOOLEAN)
						.description("다이렉트 요청 여부"),

					fieldWithPath("status").type(JsonFieldType.STRING)
						.attributes((enumFormat(ServiceRequestStatus.class, Enum::name)))
						.description("요청 상태"),

					fieldWithPath("wishTimes").type(JsonFieldType.ARRAY).description("희망 시간 슬롯 목록"),
					fieldWithPath("wishTimes[].wishTime").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("희망 시간 (yyyy.MM.dd HH:mm)"),

					fieldWithPath("locations").type(JsonFieldType.ARRAY).description("요청 지역 목록"),
					fieldWithPath("locations[].id").type(JsonFieldType.NUMBER).description("지역 ID"),
					fieldWithPath("locations[].name").type(JsonFieldType.STRING).description("지역명"),
					fieldWithPath("locations[].fullName").type(JsonFieldType.STRING).description("지역 전체 경로명"),

					fieldWithPath("isReceivingEstimate").type(JsonFieldType.BOOLEAN)
						.description("제안 받고 있는지 여부"),

					fieldWithPath("files").type(JsonFieldType.ARRAY).optional().description("리뷰 파일 목록"),
					fieldWithPath("files[].id").type(JsonFieldType.NUMBER).optional().description("파일 ID"),
					fieldWithPath("files[].fileUrl").type(JsonFieldType.STRING).optional().description("파일 URL"),
					fieldWithPath("files[].fileType").type(JsonFieldType.STRING)
						.optional()
						.attributes(enumFormat(UploadFileType.class, Enum::name))
						.description("파일 타입"),
					fieldWithPath("files[].fileName").type(JsonFieldType.STRING).optional().description("파일 이름"),
					fieldWithPath("files[].fileSize").type(JsonFieldType.STRING).optional().description("파일 크기")
				),

				resource(builder()
					.tag("📋 요청 API")
					.summary("회원이 보낸 요청 상세 조회")
					.description("회원이 자신이 보낸 요청의 상세 정보를 조회합니다.")
					.build())
			));
	}

	@Test
	void 요청_제안_수신_상태_변경() throws Exception {
		authenticationSetUp();

		willDoNothing().given(serviceRequestFacade).updateIsReceivingEstimate(anyLong(), anyLong(), any());

		mockMvc.perform(patch("/api/members/service-requests/{serviceRequestId}", 1L)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("reason",
					StopReceivingEstimateReason.CONFIRMED_WITH_DIRECTOR.name()))))
			.andExpect(status().isNoContent())
			.andDo(document("service-request-update-is-receiving-estimate",
				getRequestPreProcessor(),
				getResponsePreProcessor(),
				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("serviceRequestId")
						.description("서비스 요청 ID")
				),
				requestFields(
					fieldWithPath("reason").type(JsonFieldType.STRING)
						.attributes((enumFormat(StopReceivingEstimateReason.class, Enum::name)))
						.description("제안 수신 상태 변경 사유")
				),
				resource(builder()
					.tag("📋 요청 API")
					.summary("회원이 보낸 요청의 제안 수신 상태 변경")
					.description("회원이 자신이 보낸 요청의 제안 수신 상태를 변경합니다.")
					.build())
			));
	}

	@Test
	void 요청_카운트_조회() throws Exception {
		authenticationSetUp();

		// given
		ServiceRequestFindCountResponseForPublic response = ServiceRequestFindCountResponseForPublic.builder()
			.pendingRequestCount(5)
			.totalRequestCount(10)
			.build();

		given(serviceRequestFacade.findCounts(anyLong())).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/members/service-requests/counts")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("service-request-findCounts-for-member",
				getRequestPreProcessor(),
				getResponsePreProcessor(),
				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("pendingRequestCount").type(JsonFieldType.NUMBER)
						.description("현재 제안을 받고 있는(PENDING) 요청 수"),
					fieldWithPath("totalRequestCount").type(JsonFieldType.NUMBER)
						.description("전체 요청 수")
				),

				resource(builder()
					.tag("📋 요청 API")
					.summary("회원이 보낸 요청 카운트 조회")
					.description("회원이 보낸 요청의 통계(진행중 요청 수, 받은 제안 수)를 조회합니다.")
					.build())
			));
	}

	@Test
	void 요청_관련_서비스_목록_조회() throws Exception {
		authenticationSetUp();

		// given
		DirectorServiceResponse response = DirectorServiceResponse.builder()
			.id(1L)
			.name("헬스 케어")
			.build();

		given(serviceRequestFacade.findServicesRelatedToServiceRequest(anyLong(), anyBoolean())).willReturn(
			Arrays.asList(response));

		// when & then
		mockMvc.perform(get("/api/members/service-requests/services")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.param(SHOW_ONLY_PENDING, Boolean.TRUE.toString())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("service-request-find-service-for-member",
				getRequestPreProcessor(),
				getResponsePreProcessor(),
				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("showOnlyPending")
						.optional()
						.description("진행중인 요청의 서비스만 조회할지 여부 (기본값: true)")
				),

				responseFields(
					fieldWithPath("[].id").type(JsonFieldType.NUMBER)
						.description("디렉터 서비스 ID"),
					fieldWithPath("[].name").type(JsonFieldType.STRING)
						.description("디렉터 서비스명")
				),

				resource(builder()
					.tag("📋 요청 API")
					.summary("회원이 보낸 요청 관련 서비스 목록 조회")
					.description("회원이 보낸 요청과 관련된 서비스 목록을 조회합니다.")
					.build())
			));
	}
}
