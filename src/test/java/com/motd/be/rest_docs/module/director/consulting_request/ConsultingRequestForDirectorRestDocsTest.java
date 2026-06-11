package com.motd.be.rest_docs.module.director.consulting_request;

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
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.director.consulting_request.dto.response.ConsultingRequestFindAllResponseForDirector;
import com.motd.be.module.director.consulting_request.dto.response.ConsultingRequestImagesResponseForDirector;
import com.motd.be.module.director.consulting_request.dto.response.ConsultingRequestResponseForDirector;
import com.motd.be.module.director.location.dto.response.LocationResponseForDirector;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class ConsultingRequestForDirectorRestDocsTest extends BaseRestDocsTest {

	@Test
	void 디렉터_컨설팅_요청_목록_조회() throws Exception {
		authenticationSetUp();

		// given
		ConsultingRequestFindAllResponseForDirector response = ConsultingRequestFindAllResponseForDirector.builder()
			.hasNext(true)
			.totalCount(3)
			.consultingRequests(List.of(
				ConsultingRequestResponseForDirector.builder()
					.id(3L)
					.memberNickname("뽕순이")
					.images(ConsultingRequestImagesResponseForDirector.builder()
						.front(List.of("https://cdn.example.com/front1.jpg"))
						.side(List.of("https://cdn.example.com/side1.jpg"))
						.top(List.of("https://cdn.example.com/top1.jpg"))
						.aspiration(List.of("https://cdn.example.com/aspiration1.jpg"))
						.build())
					.usesHairProduct(true)
					.prefersExposedForehead(true)
					.recentProcedure("3개월 전 펌")
					.locations(List.of(
						LocationResponseForDirector.builder().id(1L).name("강남구").fullName("서울 강남구").build(),
						LocationResponseForDirector.builder().id(2L).name("서초구").fullName("서울 서초구").build()
					))
					.createdAt("2024.02.10 12:00")
					.build(),
				ConsultingRequestResponseForDirector.builder()
					.id(2L)
					.memberNickname("준혁이")
					.images(ConsultingRequestImagesResponseForDirector.builder()
						.front(List.of("https://cdn.example.com/front2.jpg"))
						.side(List.of("https://cdn.example.com/side2.jpg"))
						.top(List.of("https://cdn.example.com/top2.jpg"))
						.aspiration(List.of("https://cdn.example.com/aspiration2.jpg"))
						.build())
					.usesHairProduct(true)
					.prefersExposedForehead(false)
					.recentProcedure("없음")
					.locations(List.of(
						LocationResponseForDirector.builder().id(3L).name("서울").fullName("서울").build()
					))
					.createdAt("2024.02.14 15:30")
					.build()
			))
			.build();

		given(consultingRequestFacadeForDirector.findAll(any())).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/directors/consulting-requests")
				.param("cursorId", "5")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				))
			.andExpect(status().isOk())
			.andDo(document("consulting-request-findAll-for-director",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("cursorId").optional()
						.description("마지막 조회 항목의 ID (다음 페이지 조회 시 사용, 첫 페이지는 생략)")
				),

				responseFields(
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN)
						.description("다음 페이지 존재 여부"),
					fieldWithPath("totalCount").type(JsonFieldType.NUMBER)
						.description("전체 건수"),
					fieldWithPath("consultingRequests").type(JsonFieldType.ARRAY)
						.description("컨설팅 요청 목록"),
					fieldWithPath("consultingRequests[].id").type(JsonFieldType.NUMBER)
						.description("컨설팅 요청 ID"),
					fieldWithPath("consultingRequests[].memberNickname").type(JsonFieldType.STRING)
						.description("회원 닉네임"),
					fieldWithPath("consultingRequests[].images").type(JsonFieldType.OBJECT)
						.description("카테고리별 이미지 CDN URL 목록"),
					fieldWithPath("consultingRequests[].images.FRONT").type(JsonFieldType.ARRAY)
						.description("정면 이미지 CDN URL 목록"),
					fieldWithPath("consultingRequests[].images.SIDE").type(JsonFieldType.ARRAY)
						.description("측면 이미지 CDN URL 목록"),
					fieldWithPath("consultingRequests[].images.TOP").type(JsonFieldType.ARRAY)
						.description("윗면 이미지 CDN URL 목록"),
					fieldWithPath("consultingRequests[].images.ASPIRATION").type(JsonFieldType.ARRAY)
						.description("희망 이미지 CDN URL 목록"),
					fieldWithPath("consultingRequests[].usesHairProduct").type(JsonFieldType.BOOLEAN)
						.description("헤어 제품 사용 여부"),
					fieldWithPath("consultingRequests[].prefersExposedForehead").type(JsonFieldType.BOOLEAN)
						.description("이마 노출 선호 여부"),
					fieldWithPath("consultingRequests[].recentProcedure").type(JsonFieldType.STRING)
						.description("최근 시술 여부"),
					fieldWithPath("consultingRequests[].locations").type(JsonFieldType.ARRAY)
						.description("선택 지역 목록"),
					fieldWithPath("consultingRequests[].locations[].id").type(JsonFieldType.NUMBER)
						.description("지역 ID"),
					fieldWithPath("consultingRequests[].locations[].name").type(JsonFieldType.STRING)
						.description("지역명"),
					fieldWithPath("consultingRequests[].locations[].fullName").type(JsonFieldType.STRING)
						.description("전체 지역명 (시 구 형태)"),
					fieldWithPath("consultingRequests[].createdAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("요청일")
				),

				resource(builder()
					.tag("💆 디렉터 컨설팅 API")
					.summary("컨설팅 요청 목록 조회 (디렉터용)")
					.description("디렉터가 작성 가능한 컨설팅 요청 목록을 조회합니다. 유효 선점 건은 숨김 처리됩니다.")
					.build())
			));
	}

	@Test
	void 디렉터_컨설팅_요청_선점() throws Exception {
		authenticationSetUp();

		// given
		ConsultingRequestResponseForDirector response = ConsultingRequestResponseForDirector.builder()
			.id(10L)
			.memberNickname("뽕순이")
			.images(ConsultingRequestImagesResponseForDirector.builder()
				.front(List.of("https://cdn.example.com/front1.jpg"))
				.side(List.of("https://cdn.example.com/side1.jpg"))
				.top(List.of("https://cdn.example.com/top1.jpg"))
				.aspiration(List.of("https://cdn.example.com/aspiration1.jpg"))
				.build())
			.usesHairProduct(true)
			.prefersExposedForehead(false)
			.recentProcedure("없음")
			.locations(List.of(
				LocationResponseForDirector.builder().id(1L).name("강남구").fullName("서울 강남구").build()
			))
			.createdAt("2024.02.10 12:00")
			.build();

		given(consultingRequestFacadeForDirector.reserve(anyLong(), anyLong())).willReturn(response);

		// when & then
		mockMvc.perform(post("/api/directors/consulting-requests/{consultingRequestId}/reserve", 10L)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				))
			.andExpect(status().isOk())
			.andDo(document("consulting-request-reserve-for-director",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("consultingRequestId")
						.description("선점할 컨설팅 요청 ID")
				),

				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER)
						.description("컨설팅 요청 ID"),
					fieldWithPath("memberNickname").type(JsonFieldType.STRING)
						.description("회원 닉네임"),
					fieldWithPath("images").type(JsonFieldType.OBJECT)
						.description("카테고리별 이미지 CDN URL 목록"),
					fieldWithPath("images.FRONT").type(JsonFieldType.ARRAY)
						.description("정면 이미지 CDN URL 목록"),
					fieldWithPath("images.SIDE").type(JsonFieldType.ARRAY)
						.description("측면 이미지 CDN URL 목록"),
					fieldWithPath("images.TOP").type(JsonFieldType.ARRAY)
						.description("윗면 이미지 CDN URL 목록"),
					fieldWithPath("images.ASPIRATION").type(JsonFieldType.ARRAY)
						.description("희망 이미지 CDN URL 목록"),
					fieldWithPath("usesHairProduct").type(JsonFieldType.BOOLEAN)
						.description("헤어 제품 사용 여부"),
					fieldWithPath("prefersExposedForehead").type(JsonFieldType.BOOLEAN)
						.description("이마 노출 선호 여부"),
					fieldWithPath("recentProcedure").type(JsonFieldType.STRING)
						.description("최근 시술 여부"),
					fieldWithPath("locations").type(JsonFieldType.ARRAY)
						.description("선택 지역 목록"),
					fieldWithPath("locations[].id").type(JsonFieldType.NUMBER)
						.description("지역 ID"),
					fieldWithPath("locations[].name").type(JsonFieldType.STRING)
						.description("지역명"),
					fieldWithPath("locations[].fullName").type(JsonFieldType.STRING)
						.description("전체 지역명 (시 구 형태)"),
					fieldWithPath("createdAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("요청일")
				),

				resource(builder()
					.tag("💆 디렉터 컨설팅 API")
					.summary("컨설팅 요청 선점 및 상세 조회")
					.description("디렉터가 컨설팅 요청을 선점하고 해당 요청의 상세 정보를 조회합니다. 선점 요청 시 선점 시간은 30분으로 갱신되며, 기존 선점 건은 자동 해제됩니다.")
					.build())
			));
	}

	@Test
	void 디렉터_컨설팅_요청_선점_취소() throws Exception {
		authenticationSetUp();

		// when & then
		mockMvc.perform(patch("/api/directors/consulting-requests/{consultingRequestId}/reserve/cancel", 10L)
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				))
			.andExpect(status().isNoContent())
			.andDo(document("consulting-request-cancel-reservation-for-director",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("consultingRequestId")
						.description("선점 취소할 컨설팅 요청 ID")
				),

				resource(builder()
					.tag("💆 디렉터 컨설팅 API")
					.summary("컨설팅 요청 선점 취소")
					.description("디렉터가 선점한 컨설팅 요청을 취소합니다. 본인 선점 건만 취소되며, 타인 선점/PENDING/COMPLETED 건은 에러 없이 204를 반환합니다.")
					.build())
			));
	}
}
