package com.motd.be.rest_docs.module.director.service_estimate;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.common.utils.DateFormatUtils.*;
import static com.motd.be.provider.module.member.ServiceRequestProvider.*;
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

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.director.director_service.dto.response.DirectorServiceWithFullNameResponseForDirector;
import com.motd.be.module.director.file.dto.response.FileResponseForDirector;
import com.motd.be.module.director.location.dto.response.LocationResponseForDirector;
import com.motd.be.module.director.member.dto.response.MemberResponseForDirector;
import com.motd.be.module.director.service_estimate.dto.request.ServiceEstimateSaveAdditionalRequestForDirector;
import com.motd.be.module.director.service_estimate.dto.request.ServiceEstimateSaveRequestForDirector;
import com.motd.be.module.director.service_estimate.dto.request.ServiceEstimateUpdateRequestForDirector;
import com.motd.be.module.director.service_estimate.dto.response.ServiceEstimateFindAllResponseForDirector;
import com.motd.be.module.director.service_estimate.dto.response.ServiceEstimateFindCountsResponseForDirector;
import com.motd.be.module.director.service_estimate.dto.response.ServiceEstimateFindDetailResponseForDirector;
import com.motd.be.module.director.service_estimate.dto.response.ServiceEstimateHistoriesResponseForDirector;
import com.motd.be.module.director.service_estimate.dto.response.ServiceEstimateHistoryResponseForDirector;
import com.motd.be.module.director.service_estimate.dto.response.ServiceEstimateResponseForDirector;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.motd.be.shared.aws.enums.UploadFileType;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class ServiceEstimateForDirectorRestDocsTest extends BaseRestDocsTest {

	@Test
	void 서비스_제안_저장() throws Exception {
		authenticationSetUp();

		ServiceEstimateSaveRequestForDirector request = ServiceEstimateSaveRequestForDirector.builder()
			.serviceRequestId(1L)
			.title("제안 제목")
			.price(10000L)
			.content("제안 내용")
			.scheduledAt(DEFAULT_SCHEDULED_AT)
			.fileIds(Arrays.asList(1L, 2L, 3L))
			.build();

		willDoNothing().given(serviceEstimateFacadeForDirector).save(anyLong(), any(
			ServiceEstimateSaveRequestForDirector.class));

		mockMvc.perform(post("/api/directors/service-estimates")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isCreated())
			.andDo(document("service-estimate-save",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("serviceRequestId").type(JsonFieldType.NUMBER)
						.description("서비스 요청 ID"),
					fieldWithPath("title").type(JsonFieldType.STRING)
						.description("제안 제목"),
					fieldWithPath("price").type(JsonFieldType.NUMBER)
						.description("제안 가격"),
					fieldWithPath("content").type(JsonFieldType.STRING)
						.description("제안 내용"),
					fieldWithPath("scheduledAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("제안 시간 (yyyy.MM.dd HH:mm)"),
					fieldWithPath("fileIds").type(JsonFieldType.ARRAY).description("이미지 또는 파일 ID 목록").optional()
				),

				resource(builder()
					.tag("📄 서비스 제안 API")
					.summary("디렉터 서비스 제안 저장")
					.description("디렉터가 서비스 요청에 대해 제안을 저장합니다.")
					.build())
			));
	}

	@Test
	void 서비스_추가_제안_저장() throws Exception {
		authenticationSetUp();

		Long chatRoomId = 1L;

		ServiceEstimateSaveAdditionalRequestForDirector request = ServiceEstimateSaveAdditionalRequestForDirector.builder()
			.title("추가 제안 제목")
			.price(20000L)
			.content("추가 제안 내용")
			.serviceId(2L)
			.fileIds(Arrays.asList(10L, 11L))
			.build();

		willDoNothing().given(serviceEstimateFacadeForDirector)
			.saveAdditionalEstimate(anyLong(), anyLong(), any(ServiceEstimateSaveAdditionalRequestForDirector.class));

		mockMvc.perform(post("/api/directors/chat-rooms/{chatRoomId}/service-estimates", chatRoomId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isCreated())
			.andDo(document("service-estimate-save-additional",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("chatRoomId")
						.description("채팅방 ID")
				),

				requestFields(
					fieldWithPath("title").type(JsonFieldType.STRING).description("제안 제목"),
					fieldWithPath("price").type(JsonFieldType.NUMBER).description("제안 가격"),
					fieldWithPath("content").type(JsonFieldType.STRING).description("제안 내용"),
					fieldWithPath("serviceId").type(JsonFieldType.NUMBER).description("디렉터 서비스 ID"),
					fieldWithPath("fileIds").type(JsonFieldType.ARRAY).description("이미지 또는 파일 ID 목록").optional()
				),

				resource(builder()
					.tag("📄 서비스 제안 API")
					.summary("디렉터 추가 제안 저장")
					.description("디렉터가 채팅방에서 추가 제안을 생성합니다.")
					.build())
			));
	}

	@Test
	void 서비스_제안_전체_조회() throws Exception {
		authenticationSetUp();

		ServiceEstimateFindAllResponseForDirector response = ServiceEstimateFindAllResponseForDirector.builder()
			.page(0)
			.hasNext(false)
			.serviceEstimates(Arrays.asList(
				ServiceEstimateResponseForDirector
					.builder()
					.id(1L)
					.serviceRequestId(1L)
					.createdAt(formatToDateString(LocalDateTime.now()))
					.requester(MemberResponseForDirector.builder()
						.id(1L)
						.profileImageUrl(PROFILE_IMAGE_STR)
						.nickname(NICKNAME_STR)
						.isWithdrawal(Boolean.FALSE)
						.build())
					.service(DirectorServiceWithFullNameResponseForDirector.builder()
						.id(1L)
						.name("헬스 케어")
						.fullName("건강 > 헬스 케어")
						.build())
					.locations(Arrays.asList(LocationResponseForDirector.builder()
						.id(1L)
						.name("송파구")
						.fullName("서울시 송파구")
						.build()))
					.status(ServiceEstimateStatus.PENDING.getDescription())
					.scheduledAt(formatToDateString(LocalDateTime.now().plusDays(7)))
					.price(50000L)
					.completedAt(null)
					.isDirectRequest(Boolean.FALSE)
					.chatRoomId(1L)
					.files(Arrays.asList(
						FileResponseForDirector.builder()
							.id(1L)
							.fileUrl(IMAGE_URL_STR)
							.fileType(UploadFileType.IMAGE)
							.fileSize("20MB")
							.fileName(null)
							.build(),
						FileResponseForDirector.builder()
							.id(2L)
							.fileUrl(IMAGE_URL_STR)
							.fileType(UploadFileType.IMAGE)
							.fileSize("20MB")
							.fileName(null)
							.build()
					))
					.build(),
				ServiceEstimateResponseForDirector
					.builder()
					.id(2L)
					.serviceRequestId(2L)
					.createdAt(formatToDateString(LocalDateTime.now().minusDays(1)))
					.requester(MemberResponseForDirector.builder()
						.id(2L)
						.profileImageUrl(PROFILE_IMAGE_STR)
						.nickname(NICKNAME_STR)
						.isWithdrawal(Boolean.FALSE)
						.build())
					.service(DirectorServiceWithFullNameResponseForDirector.builder()
						.id(2L)
						.name("필라테스")
						.fullName("운동 > 필라테스")
						.build())
					.locations(Arrays.asList(LocationResponseForDirector.builder()
						.id(2L)
						.name("강남구")
						.fullName("서울시 강남구")
						.build()))
					.status(ServiceEstimateStatus.PENDING.getDescription())
					.scheduledAt(formatToDateString(LocalDateTime.now().plusDays(7)))
					.price(50000L)
					.completedAt(null)
					.isDirectRequest(Boolean.FALSE)
					.chatRoomId(1L)
					.files(Arrays.asList(
						FileResponseForDirector.builder()
							.id(1L)
							.fileUrl(IMAGE_URL_STR)
							.fileType(UploadFileType.IMAGE)
							.fileSize("20MB")
							.fileName(null)
							.build(),
						FileResponseForDirector.builder()
							.id(2L)
							.fileUrl(IMAGE_URL_STR)
							.fileType(UploadFileType.IMAGE)
							.fileSize("20MB")
							.fileName(null)
							.build()
					))
					.build(),
				ServiceEstimateResponseForDirector
					.builder()
					.id(3L)
					.serviceRequestId(3L)
					.createdAt(formatToDateString(LocalDateTime.now().minusDays(2)))
					.requester(MemberResponseForDirector.builder()
						.id(3L)
						.profileImageUrl(PROFILE_IMAGE_STR)
						.nickname(NICKNAME_STR)
						.isWithdrawal(Boolean.FALSE)
						.build())
					.service(DirectorServiceWithFullNameResponseForDirector.builder()
						.id(3L)
						.name("요가")
						.fullName("운동 > 요가")
						.build())
					.locations(Arrays.asList(LocationResponseForDirector.builder()
						.id(3L)
						.name("마포구")
						.fullName("서울시 마포구")
						.build()))
					.status(ServiceEstimateStatus.PENDING.getDescription())
					.scheduledAt(formatToDateString(LocalDateTime.now().plusDays(7)))
					.price(50000L)
					.completedAt(null)
					.isDirectRequest(Boolean.TRUE)
					.chatRoomId(1L)
					.files(Arrays.asList(
						FileResponseForDirector.builder()
							.id(1L)
							.fileUrl(IMAGE_URL_STR)
							.fileType(UploadFileType.IMAGE)
							.fileSize("20MB")
							.fileName(null)
							.build(),
						FileResponseForDirector.builder()
							.id(2L)
							.fileUrl(IMAGE_URL_STR)
							.fileType(UploadFileType.IMAGE)
							.fileSize("20MB")
							.fileName(null)
							.build()
					))
					.build()))
			.build();

		given(serviceEstimateFacadeForDirector.findAllForDirector(anyString(), anyLong(), anyInt(),
			anyLong(), any())).willReturn(
			response);

		mockMvc.perform(get("/api/directors/service-estimates")
				.param(STATUS_STR, ServiceEstimateStatus.PENDING.name())
				.param(PAGE_STR, ZERO_STR)
				.param(DIRECTOR_SERVICE_ID_STR, "1")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isOk())
			.andDo(document("service-estimate-find-all-for-director",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("status")
						.optional()
						.attributes((enumFormat(ServiceEstimateStatus.class, Enum::name)))
						.description("status (기본값: pending) "),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("directorServiceId")
						.optional()
						.description("디렉터 서비스 ID (필터링용)"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("page")
						.optional()
						.description("페이지 번호 (0부터 시작)"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("showOnlyDirectRequest")
						.optional()
						.description("다이렉트 요청만 조회 여부 (true로 설정 시 다이렉트 요청만 조회)")
				),

				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
					fieldWithPath("serviceEstimates").type(JsonFieldType.ARRAY).description("서비스 제안 목록"),
					fieldWithPath("serviceEstimates[].id").type(JsonFieldType.NUMBER).description("제안 ID"),
					fieldWithPath("serviceEstimates[].createdAt").type(JsonFieldType.STRING).description("제안 생성 날짜"),
					fieldWithPath("serviceEstimates[].serviceRequestId").type(JsonFieldType.NUMBER)
						.description("요청 아이디"),
					fieldWithPath("serviceEstimates[].chatRoomId").type(JsonFieldType.NUMBER)
						.description("채팅방 아이디"),

					fieldWithPath("serviceEstimates[].requester").type(JsonFieldType.OBJECT).description("요청자"),
					fieldWithPath("serviceEstimates[].requester.id").type(JsonFieldType.NUMBER).description("요청자 ID"),
					fieldWithPath("serviceEstimates[].requester.nickname").type(JsonFieldType.STRING)
						.description("요청자 닉네임"),
					fieldWithPath("serviceEstimates[].requester.profileImageUrl").type(JsonFieldType.STRING)
						.description("요청자 프로필 이미지 URL"),
					fieldWithPath("serviceEstimates[].requester.isWithdrawal").type(JsonFieldType.BOOLEAN)
						.description("요청자 회원탈퇴 여부"),

					fieldWithPath("serviceEstimates[].service").type(JsonFieldType.OBJECT).description("디렉터 서비스 정보"),
					fieldWithPath("serviceEstimates[].service.id").type(JsonFieldType.NUMBER).description("디렉터 서비스 ID"),
					fieldWithPath("serviceEstimates[].service.name").type(JsonFieldType.STRING)
						.description("디렉터 서비스 이름"),
					fieldWithPath("serviceEstimates[].service.fullName").type(JsonFieldType.STRING)
						.description("디렉터 서비스 전체 경로 이름"),
					fieldWithPath("serviceEstimates[].locations").type(JsonFieldType.ARRAY).description("위치 목록"),
					fieldWithPath("serviceEstimates[].locations[].id").type(JsonFieldType.NUMBER).description("위치 ID"),
					fieldWithPath("serviceEstimates[].locations[].name").type(JsonFieldType.STRING)
						.description("위치 이름"),
					fieldWithPath("serviceEstimates[].locations[].fullName").type(JsonFieldType.STRING)
						.description("위치 전체 경로 이름"),
					fieldWithPath("serviceEstimates[].status").type(JsonFieldType.STRING)
						.attributes((enumFormat(ServiceEstimateStatus.class, Enum::name)))
						.description("제안 상태"),

					fieldWithPath("serviceEstimates[].scheduledAt").optional()
						.type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("예약 시간"),

					fieldWithPath("serviceEstimates[].price").type(JsonFieldType.NUMBER).description("제안 가격"),

					fieldWithPath("serviceEstimates[].completedAt").type(JsonFieldType.STRING)
						.optional()
						.description("완료 날짜"),
					fieldWithPath("serviceEstimates[].canceledAt").optional().type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("취소 일"),
					fieldWithPath("serviceEstimates[].expiredAt").optional().type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("만료일"),
					fieldWithPath("serviceEstimates[].isDirectRequest").type(JsonFieldType.BOOLEAN)
						.description("서비스 요청이 다이렉트 요청인지 여부"),

					fieldWithPath("serviceEstimates[].files").type(JsonFieldType.ARRAY)
						.optional()
						.description("리뷰 파일 목록"),
					fieldWithPath("serviceEstimates[].files[].id").type(JsonFieldType.NUMBER)
						.optional()
						.description("파일 ID"),
					fieldWithPath("serviceEstimates[].files[].fileUrl").type(JsonFieldType.STRING)
						.optional()
						.description("파일 URL"),
					fieldWithPath("serviceEstimates[].files[].fileType").type(JsonFieldType.STRING)
						.optional()
						.attributes(enumFormat(UploadFileType.class, Enum::name))
						.description("파일 타입"),
					fieldWithPath("serviceEstimates[].files[].fileName").type(JsonFieldType.STRING)
						.optional()
						.description("파일 이름"),
					fieldWithPath("serviceEstimates[].files[].fileSize").type(JsonFieldType.STRING)
						.optional()
						.description("파일 크기")
				),

				resource(builder()
					.tag("📄 서비스 제안 API")
					.summary("디렉터 서비스 제안 전체 조회")
					.description("디렉터가 자신의 서비스 제안 목록을 조회합니다.")
					.build())
			));
	}

	@Test
	void 서비스_제안_상세_조회() throws Exception {
		authenticationSetUp();

		Long serviceEstimateId = 1L;

		ServiceEstimateFindDetailResponseForDirector response = ServiceEstimateFindDetailResponseForDirector.builder()
			.id(serviceEstimateId)
			.status(ServiceEstimateStatus.PENDING.getDescription())
			.title("헬스 케어 제안")
			.price(50000L)
			.content("상세 제안 내용")
			.createdAt(formatToDateString(LocalDateTime.now()))
			.scheduledAt(formatToDateString(LocalDateTime.now().plusDays(3)))
			.completedAt(null)
			.canceledAt(null)
			.expiredAt(formatToDateString(LocalDateTime.now().plusDays(7)))
			.files(Arrays.asList(
				FileResponseForDirector.builder()
					.id(1L)
					.fileUrl(IMAGE_URL_STR)
					.fileType(UploadFileType.IMAGE)
					.fileSize("20MB")
					.fileName(null)
					.build(),
				FileResponseForDirector.builder()
					.id(2L)
					.fileUrl(IMAGE_URL_STR)
					.fileType(UploadFileType.IMAGE)
					.fileSize("20MB")
					.fileName(null)
					.build()
			))
			.chatRoomId(1L)
			.build();

		given(serviceEstimateFacadeForDirector.findDetailForDirector(anyLong(), anyLong())).willReturn(response);

		mockMvc.perform(get("/api/directors/service-estimates/{serviceEstimateId}", serviceEstimateId)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isOk())
			.andDo(document("service-estimate-find-detail-for-director",
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
					fieldWithPath("status").type(JsonFieldType.STRING)
						.attributes((enumFormat(ServiceEstimateStatus.class, Enum::name)))
						.description("제안 상태"),
					fieldWithPath("title").type(JsonFieldType.STRING).description("제안 제목"),
					fieldWithPath("price").type(JsonFieldType.NUMBER).description("제안 가격"),
					fieldWithPath("content").type(JsonFieldType.STRING).description("제안 내용"),
					fieldWithPath("createdAt").type(JsonFieldType.STRING).description("제안 생성일"),
					fieldWithPath("scheduledAt").optional().type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("예약 시간"),
					fieldWithPath("completedAt").optional().type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("완료 날짜"),
					fieldWithPath("canceledAt").optional().type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("취소 일"),
					fieldWithPath("expiredAt").optional().type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("만료일"),
					fieldWithPath("chatRoomId").type(JsonFieldType.NUMBER).description("채팅방 ID"),

					fieldWithPath("files").type(JsonFieldType.ARRAY).optional().description("제안 파일 목록"),
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
					.tag("📄 서비스 제안 API")
					.summary("디렉터 서비스 제안 상세 조회")
					.description("디렉터가 서비스 제안 상세 정보를 조회합니다.")
					.build())
			));
	}

	@Test
	void 제안_기반_갯수_조회() throws Exception {
		// given
		authenticationSetUp();

		ServiceEstimateFindCountsResponseForDirector countsResponse = ServiceEstimateFindCountsResponseForDirector.builder()
			.requestCount(2)
			.pendingCount(5)
			.ongoingCount(5)
			.completedCount(7)
			.canceledCount(10)
			.expiredCount(10)
			.build();

		given(serviceEstimateFacadeForDirector.findDirectRequestCounts(anyLong(), anyLong())).willReturn(
			countsResponse);

		// when & then
		mockMvc.perform(get("/api/directors/service-estimates/counts")
				.param(DIRECTOR_SERVICE_ID_STR, "1")
				.param("showOnlyDirectRequest", "true")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isOk())
			.andDo(document("service-estimate-counts",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(DIRECTOR_SERVICE_ID_STR)
						.optional()
						.description("디렉터 서비스 아이디"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("showOnlyDirectRequest")
						.optional()
						.description("다이렉트 요청만 조회 여부 (필터링이 필요할떄만 보내기 true 로!)")
				),

				responseFields(
					fieldWithPath("requestCount").type(JsonFieldType.NUMBER).description("새로운 요청 수"),
					fieldWithPath("pendingCount").type(JsonFieldType.NUMBER).description("진행중 (PENDING)"),
					fieldWithPath("ongoingCount").type(JsonFieldType.NUMBER).description("진행 중(ONGOING)"),
					fieldWithPath("completedCount").type(JsonFieldType.NUMBER).description("완료된 수"),
					fieldWithPath("canceledCount").type(JsonFieldType.NUMBER).description("취소된 수"),
					fieldWithPath("expiredCount").type(JsonFieldType.NUMBER).description("만료된 수")
				),

				resource(builder()
					.tag("📄 서비스 제안 API")
					.summary("디렉터 메인페이지에서 사용하는 제안 기반 카운트 조회")
					.description("디렉터 메인페이지에서 사용하는 카운트를 조회합니다.")
					.build())
			));
	}

	@Test
	void 서비스_제안_수정() throws Exception {
		// given
		authenticationSetUp();

		Long serviceEstimateId = 1L;

		ServiceEstimateUpdateRequestForDirector request = ServiceEstimateUpdateRequestForDirector.builder()
			.price(50000L)
			.scheduledAt(DEFAULT_SCHEDULED_AT)
			.build();

		willDoNothing().given(serviceEstimateFacadeForDirector)
			.updateEstimate(anyLong(), anyLong(), any(ServiceEstimateUpdateRequestForDirector.class));

		// when & then
		mockMvc.perform(patch("/api/directors/service-estimates/{serviceEstimateId}", serviceEstimateId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isNoContent())
			.andDo(document("service-estimate-update",
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

				requestFields(
					fieldWithPath("price").type(JsonFieldType.NUMBER)
						.description("제안 가격"),
					fieldWithPath("scheduledAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("제안 시간 (yyyy.MM.dd HH:mm)")
				),

				resource(builder()
					.tag("📄 서비스 제안 API")
					.summary("디렉터 서비스 제안 수정")
					.description("디렉터가 자신의 제안의 가격과 예약일을 수정합니다. PENDING, ONGOING 상태의 제안만 수정 가능합니다.")
					.build())
			));
	}

	@Test
	void 서비스_제안_취소() throws Exception {
		// given
		authenticationSetUp();
		willDoNothing().given(serviceEstimateFacadeForDirector).cancel(anyLong(), anyLong());
		Long serviceEstimateId = 1L;

		// when & then
		mockMvc.perform(patch("/api/directors/service-estimates/{serviceEstimateId}/cancel", serviceEstimateId)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isNoContent())
			.andDo(document("service-estimate-cancel",
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

				resource(builder()
					.tag("📄 서비스 제안 API")
					.summary("디렉터 서비스 제안 취소")
					.description("디렉터가 자신의 제안을 취소합니다. PENDING, ONGOING, DIRECTOR_DONE 상태의 제안만 취소 가능하며, hired 된 제안의 경우 요청도 함께 취소됩니다.")
					.build())
			));
	}

	@Test
	void 서비스_제안_완료() throws Exception {
		// given
		authenticationSetUp();
		willDoNothing().given(serviceEstimateFacadeForDirector).completeForDirector(anyLong(), anyLong());
		Long serviceEstimateId = 1L;

		// when & then
		mockMvc.perform(patch("/api/directors/service-estimates/{serviceEstimateId}/complete", serviceEstimateId)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isNoContent())
			.andDo(document("service-estimate-complete-for-director",
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

				resource(builder()
					.tag("📄 서비스 제안 API")
					.summary("디렉터 서비스 제안 완료")
					.description(
						"디렉터가 서비스 제안을 완료 처리합니다. 디렉터와 회원 모두 완료 처리하면 제안 상태가 COMPLETED로 변경됩니다. 본 API는 204 No Content를 반환합니다.")
					.build())
			));
	}

	@Test
	void 서비스_제안_히스토리_조회() throws Exception {
		authenticationSetUp();

		ServiceEstimateHistoriesResponseForDirector response = ServiceEstimateHistoriesResponseForDirector.builder()
			.page(0)
			.hasNext(true)
			.histories(Arrays.asList(
				ServiceEstimateHistoryResponseForDirector.builder()
					.serviceEstimateId(1L)
					.serviceRequestId(10L)
					.memberNickname("테스트유저1")
					.title("헬스 케어 제안")
					.price(50000L)
					.completedAt(formatToDateString(LocalDateTime.now().minusDays(3)))
					.scheduledAt(formatToDateString(LocalDateTime.now().plusDays(3)))
					.build(),
				ServiceEstimateHistoryResponseForDirector.builder()
					.serviceEstimateId(2L)
					.serviceRequestId(11L)
					.memberNickname("테스트유저2")
					.title("필라테스 제안")
					.price(80000L)
					.completedAt(formatToDateString(LocalDateTime.now().minusDays(7)))
					.scheduledAt(formatToDateString(LocalDateTime.now().plusDays(3)))
					.build()
			))
			.build();

		given(serviceEstimateFacadeForDirector.findServiceEstimateHistoriesForDirector(anyLong(), anyInt())).willReturn(
			response);

		mockMvc.perform(get("/api/directors/service-estimates/histories")
				.param(PAGE_STR, ZERO_STR)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isOk())
			.andDo(document("service-estimate-find-histories-for-director",
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
					fieldWithPath("histories").type(JsonFieldType.ARRAY).description("완료된 제안 히스토리 목록"),
					fieldWithPath("histories[].serviceEstimateId").type(JsonFieldType.NUMBER).description("제안 ID"),
					fieldWithPath("histories[].serviceRequestId").type(JsonFieldType.NUMBER).description("서비스 요청 ID"),
					fieldWithPath("histories[].memberNickname").type(JsonFieldType.STRING).description("회원 닉네임"),
					fieldWithPath("histories[].title").type(JsonFieldType.STRING).description("제안 제목"),
					fieldWithPath("histories[].price").type(JsonFieldType.NUMBER).description("제안 가격"),
					fieldWithPath("histories[].completedAt").type(JsonFieldType.STRING).description("완료 날짜"),
					fieldWithPath("histories[].scheduledAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("예약 시간")
				),

				resource(builder()
					.tag("📄 서비스 제안 API")
					.summary("디렉터 서비스 제안 히스토리 조회")
					.description("디렉터가 완료된 서비스 제안 목록을 조회합니다.")
					.build())
			));
	}
}
