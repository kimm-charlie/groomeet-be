package com.motd.be.rest_docs.module.director.service_request;

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
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.director.director_service.dto.response.DirectorServiceWithFullNameResponseForDirector;
import com.motd.be.module.director.file.dto.response.FileResponseForDirector;
import com.motd.be.module.director.location.dto.response.LocationResponseForDirector;
import com.motd.be.module.director.member.dto.response.MemberResponseWithReceivedEstimateCountForDirector;
import com.motd.be.module.director.service_request.dto.response.ServiceRequestFindAllResponseForDirector;
import com.motd.be.module.director.service_request.dto.response.ServiceRequestFindDetailResponseForDirector;
import com.motd.be.module.director.service_request.dto.response.ServiceRequestResponseForDirector;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;
import com.motd.be.module.member.service_request_wish_time.dto.response.WishTimeResponse;
import com.motd.be.shared.aws.enums.UploadFileType;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class ServiceRequestForDirectorRestDocsTest extends BaseRestDocsTest {

	@Test
	void 디렉터_서비스_요청_목록_조회() throws Exception {
		authenticationSetUp();

		ServiceRequestFindAllResponseForDirector response = ServiceRequestFindAllResponseForDirector.builder()
			.page(0)
			.hasNext(false)
			.serviceRequests(Arrays.asList(
				ServiceRequestResponseForDirector
					.builder()
					.id(1L)
					.createdAt(formatToDateString(LocalDateTime.now()))
					.member(MemberResponseWithReceivedEstimateCountForDirector.builder()
						.id(1L)
						.nickname(NICKNAME_STR)
						.profileImageUrl(IMAGE_URL_STR)
						.receivedEstimateCount(5)
						.build())
					.expiredAt(formatToDateString(LocalDateTime.now().plusDays(2)))
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
					.status(ServiceRequestStatus.PENDING.getDescription())
					.wishTimes(List.of(
						WishTimeResponse.builder().wishTime("2026-02-15 10:00").build(),
						WishTimeResponse.builder().wishTime("2026-02-15 14:00").build()
					))
					.isDirectRequest(Boolean.FALSE)
					.build(),
				ServiceRequestResponseForDirector
					.builder()
					.id(2L)
					.createdAt(formatToDateString(LocalDateTime.now().minusDays(1)))
					.member(MemberResponseWithReceivedEstimateCountForDirector.builder()
						.id(2L)
						.nickname(NICKNAME_STR)
						.profileImageUrl(IMAGE_URL_STR)
						.receivedEstimateCount(5)
						.build())
					.expiredAt(formatToDateString(LocalDateTime.now().plusDays(1)))
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
					.status(ServiceRequestStatus.PENDING.getDescription())
					.isDirectRequest(Boolean.FALSE)
					.files(List.of(FileResponseForDirector.builder()
							.id(1L)
							.fileUrl(IMAGE_URL_STR)
							.build(),
						FileResponseForDirector.builder()
							.id(2L)
							.fileUrl(IMAGE_URL_STR)
							.build()
					))
					.wishTimes(List.of(
						WishTimeResponse.builder().wishTime("2026-02-15 10:00").build(),
						WishTimeResponse.builder().wishTime("2026-02-15 14:00").build()
					))
					.build(),
				ServiceRequestResponseForDirector
					.builder()
					.id(3L)
					.createdAt(formatToDateString(LocalDateTime.now().minusDays(2)))
					.member(MemberResponseWithReceivedEstimateCountForDirector.builder()
						.id(3L)
						.nickname(NICKNAME_STR)
						.profileImageUrl(IMAGE_URL_STR)
						.receivedEstimateCount(5)
						.build())
					.expiredAt(formatToDateString(LocalDateTime.now().plusDays(3)))
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
					.status(ServiceRequestStatus.PENDING.getDescription())
					.isDirectRequest(Boolean.TRUE)
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
					.wishTimes(List.of(
						WishTimeResponse.builder().wishTime("2026-02-15 10:00").build(),
						WishTimeResponse.builder().wishTime("2026-02-15 14:00").build()
					))
					.build()
			))
			.build();

		given(serviceRequestFacadeForDirector.findAllForDirector(anyLong(), any(), anyInt(), anyBoolean())).willReturn(
			response);

		mockMvc.perform(get("/api/directors/service-requests")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.param(DIRECTOR_SERVICE_ID_STR, "1")
				.param(PAGE_STR, ZERO_STR)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("service-request-findAll-for-director",
				getRequestPreProcessor(),
				getResponsePreProcessor(),
				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("directorServiceId")
						.optional()
						.description("디렉터 서비스 ID (필터링용) (없으면 전체조회)"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("page")
						.optional()
						.description("페이지 번호 (0부터 시작)"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("showOnlyDirectRequest")
						.optional()
						.description("다이렉트 요청만 보기 여부 (기본값: false)")
				),
				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),

					fieldWithPath("serviceRequests").type(JsonFieldType.ARRAY).description("서비스 요청 목록"),
					fieldWithPath("serviceRequests[].id").type(JsonFieldType.NUMBER).description("서비스 아이디"),
					fieldWithPath("serviceRequests[].createdAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("서비스 요청 생성일"),
					fieldWithPath("serviceRequests[].expiredAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("서비스 요청 마감일"),
					fieldWithPath("serviceRequests[].isDirectRequest").type(JsonFieldType.BOOLEAN)
						.description("서비스 요청이 다이렉트 요청인지 여부"),

					fieldWithPath("serviceRequests[].member").type(JsonFieldType.OBJECT)
						.description("서비스 요청자"),
					fieldWithPath("serviceRequests[].member.id").type(JsonFieldType.NUMBER)
						.description("서비스 요청자 아이디"),
					fieldWithPath("serviceRequests[].member.nickname").type(JsonFieldType.STRING)
						.description("서비스 요청자 닉네임"),
					fieldWithPath("serviceRequests[].member.profileImageUrl").type(JsonFieldType.STRING)
						.description("서비스 요청자 프로필 이미지 URL"),
					fieldWithPath("serviceRequests[].member.receivedEstimateCount").type(JsonFieldType.NUMBER)
						.description("요청자가 받은 제안 개수"),

					fieldWithPath("serviceRequests[].service").type(JsonFieldType.OBJECT).description("요청 서비스 정보"),
					fieldWithPath("serviceRequests[].service.id").type(JsonFieldType.NUMBER).description("서비스 ID"),
					fieldWithPath("serviceRequests[].service.name").type(JsonFieldType.STRING).description("서비스명"),
					fieldWithPath("serviceRequests[].service.fullName").type(JsonFieldType.STRING)
						.description("서비스 전체 경로명"),

					fieldWithPath("serviceRequests[].locations").type(JsonFieldType.ARRAY).description("요청 지역 목록"),
					fieldWithPath("serviceRequests[].locations[].id").type(JsonFieldType.NUMBER).description("지역 ID"),
					fieldWithPath("serviceRequests[].locations[].name").type(JsonFieldType.STRING).description("지역명"),
					fieldWithPath("serviceRequests[].locations[].fullName").type(JsonFieldType.STRING)
						.description("지역 전체 경로명"),

					fieldWithPath("serviceRequests[].wishTimes").type(JsonFieldType.ARRAY)
						.description("희망 시간 목록"),
					fieldWithPath("serviceRequests[].wishTimes[].wishTime").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("희망 시간"),

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

					fieldWithPath("serviceRequests[].status")
						.attributes((enumFormat(ServiceRequestStatus.class, Enum::name)))
						.type(JsonFieldType.STRING).description("서비스 요청 상태")
				),

				resource(builder()
					.tag("📋 요청 API")
					.summary("신규 요청 목록 조회")
					.description("디렉터가 신규 요청 목록 조회를 합니다.")
					.build())
			));
	}

	@Test
	void 디렉터_서비스_요청_상세_조회() throws Exception {
		authenticationSetUp();

		ServiceRequestFindDetailResponseForDirector response = ServiceRequestFindDetailResponseForDirector.builder()
			.id(1L)
			.service(DirectorServiceWithFullNameResponseForDirector.builder()
				.id(1L)
				.name("헬스 케어")
				.fullName("건강 > 헬스 케어")
				.build())
			.createdAt(formatToDateString(LocalDateTime.now()))
			.expiredAt(formatToDateString(LocalDateTime.now().plusDays(7)))
			.member(MemberResponseWithReceivedEstimateCountForDirector.builder()
				.id(1L)
				.nickname(NICKNAME_STR)
				.profileImageUrl(IMAGE_URL_STR)
				.receivedEstimateCount(3)
				.build())
			.locations(List.of(
				LocationResponseForDirector.builder()
					.id(1L)
					.name("송파구")
					.fullName("서울시 송파구")
					.build(),
				LocationResponseForDirector.builder()
					.id(2L)
					.name("강동구")
					.fullName("서울시 강동구")
					.build()
			))
			.isDirectRequest(Boolean.TRUE)
			.status(ServiceRequestStatus.PENDING.name())
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
			.content("추가 요청 사항")
			.wishTimes(List.of(
				WishTimeResponse.builder().wishTime(formatToDateString(DEFAULT_WISH_DATE_TIME_1)).build(),
				WishTimeResponse.builder().wishTime(formatToDateString(DEFAULT_WISH_DATE_TIME_2)).build()
			))
			.build();

		given(serviceRequestFacadeForDirector.findDetailForDirector(anyLong(), anyLong())).willReturn(response);

		mockMvc.perform(get("/api/directors/service-requests/{serviceRequestId}", 1L)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("service-request-findDetail-for-director",

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
					fieldWithPath("status").type(JsonFieldType.STRING)
						.attributes((enumFormat(ServiceRequestStatus.class, Enum::name)))
						.type(JsonFieldType.STRING).description("서비스 요청 상태"),

					fieldWithPath("content").type(JsonFieldType.STRING)
						.optional()
						.description("요청 내용 (다이렉트 요청: 추가 요청 사항, 일반 요청: AI 생성 내용)"),

					fieldWithPath("expiredAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("요청 만료일"),

					fieldWithPath("isDirectRequest").type(JsonFieldType.BOOLEAN)
						.description("다이렉트 요청 여부"),

					fieldWithPath("member").type(JsonFieldType.OBJECT).description("회원 정보"),
					fieldWithPath("member.id").type(JsonFieldType.NUMBER).description("회원 아이디"),
					fieldWithPath("member.nickname").type(JsonFieldType.STRING).description("회원 닉네임"),
					fieldWithPath("member.profileImageUrl").type(JsonFieldType.STRING).description("회원 프로필 이미지 URL"),
					fieldWithPath("member.receivedEstimateCount").type(JsonFieldType.NUMBER)
						.description("해당 요청을 통해 받은 제안 개수"),

					fieldWithPath("locations").type(JsonFieldType.ARRAY).description("요청 지역 목록"),
					fieldWithPath("locations[].id").type(JsonFieldType.NUMBER).description("지역 ID"),
					fieldWithPath("locations[].name").type(JsonFieldType.STRING).description("지역명"),
					fieldWithPath("locations[].fullName").type(JsonFieldType.STRING).description("지역 전체 경로명"),

					fieldWithPath("wishTimes").type(JsonFieldType.ARRAY)
						.optional()
						.description("희망 시간 목록"),
					fieldWithPath("wishTimes[].wishTime").type(JsonFieldType.STRING)
						.optional()
						.attributes(getDateTimeFormat())
						.description("희망 시간 (yyyy.MM.dd HH:mm)"),

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
					.summary("디렉터가 신규 요청 상세 조회")
					.description("디렉터가 신규 요청의 상세 정보를 조회합니다.")
					.build())
			));
	}

	@Test
	void 서비스_요청_삭제() throws Exception {
		authenticationSetUp();

		Long serviceRequestId = 1L;

		willDoNothing().given(serviceRequestFacadeForDirector)
			.hideForDirector(anyLong(), anyLong());

		mockMvc.perform(post("/api/directors/service-requests/{serviceRequestId}/hide", serviceRequestId)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				))
			.andExpect(status().isNoContent())
			.andDo(document("director-hide-service-request-for-director",
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

				resource(builder()
					.tag("🎬 디렉터 서비스 요청 삭제 API")
					.summary("디렉터 서비스 요청 삭제")
					.description("디렉터가 서비스 요청을 삭제합니다.")
					.build())
			));
	}
}
