package com.motd.be.rest_docs.module.director.director_info;

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

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.director.director_info.dto.request.DirectorInfoUpdateIntroduceTextRequestForDirector;
import com.motd.be.module.director.director_info.dto.request.DirectorInfoUpdateStoreAddressRequestForDirector;
import com.motd.be.module.director.director_info.dto.response.DirectorInfoFindProfileBasicInfoResponseForDirector;
import com.motd.be.module.director.director_info.dto.response.DirectorInfoFindProfileCompletenessResponseForDirector;
import com.motd.be.module.director.director_service.dto.response.DirectorServiceWithFullNameResponseForDirector;
import com.motd.be.module.director.location.dto.response.LocationResponseForDirector;
import com.motd.be.module.director.member.dto.response.MemberResponseForDirector;
import com.motd.be.module.member.member.entity.Gender;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class DirectorInfoForDirectorRestDocsTest extends BaseRestDocsTest {

	@Test
	void 소개글_수정() throws Exception {
		authenticationSetUp();

		// given
		DirectorInfoUpdateIntroduceTextRequestForDirector request = DirectorInfoUpdateIntroduceTextRequestForDirector.builder()
			.introduceText("새로운 소개글입니다.")
			.build();

		willDoNothing().given(directorInfoFacadeForDirector).updateIntroduceText(anyLong(), anyString());

		// when & then
		mockMvc.perform(patch("/api/directors/my/introduce-text")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent())
			.andDo(document("director-update-introduce-text",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR)
						.description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("introduceText").type(JsonFieldType.STRING)
						.optional()
						.description("수정할 소개글")
				),

				resource(builder()
					.tag("🎬 디렉터 API")
					.summary("디렉터 소개글 수정")
					.description("디렉터가 자신의 소개글을 수정합니다.")
					.build())
			));
	}

	@Test
	void 매장주소_수정() throws Exception {
		authenticationSetUp();

		// given
		DirectorInfoUpdateStoreAddressRequestForDirector request = DirectorInfoUpdateStoreAddressRequestForDirector.builder()
			.storeAddress("서울특별시 강남구")
			.build();

		willDoNothing().given(directorInfoFacadeForDirector).updateStoreAddress(anyLong(), anyString());

		// when & then
		mockMvc.perform(patch("/api/directors/my/store-address")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent())
			.andDo(document("director-update-store-address",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR)
						.description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("storeAddress").type(JsonFieldType.STRING)
						.optional()
						.description("수정할 매장 주소")
				),

				resource(builder()
					.tag("🎬 디렉터 API")
					.summary("디렉터 매장 주소 수정")
					.description("디렉터가 자신의 매장 주소를 수정합니다.")
					.build())
			));
	}

	@Test
	void 프로필_완성도_조회() throws Exception {
		// 인증 셋업
		authenticationSetUp();

		// given: 응답 더미
		DirectorInfoFindProfileCompletenessResponseForDirector response = DirectorInfoFindProfileCompletenessResponseForDirector.builder()
			.memberId(1L)
			.isProfileDetailExist(true)
			.isPortfolioExist(false)
			.isFirstCashCharged(true)
			.isEstimateTemplateExist(false)
			.build();

		given(directorInfoFacadeForDirector.findProfileCompleteness(anyLong())).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/directors/me/profile-completeness")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("director-find-profile-completeness",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("memberId").type(JsonFieldType.NUMBER)
						.description("회원 아이디"),
					fieldWithPath("isProfileDetailExist").type(JsonFieldType.BOOLEAN)
						.description("디렉터 프로필 상세 작성 여부"),
					fieldWithPath("isPortfolioExist").type(JsonFieldType.BOOLEAN)
						.description("포트폴리오 존재 여부"),
					fieldWithPath("isFirstCashCharged").type(JsonFieldType.BOOLEAN)
						.description("첫 캐쉬 충전 여부"),
					fieldWithPath("isEstimateTemplateExist").type(JsonFieldType.BOOLEAN)
						.description("자주쓰는 제안 등록 여부")
				),

				resource(builder()
					.tag("🎬 디렉터 API")
					.summary("디렉터 프로필 완성도 조회")
					.description("디렉터가 자신의 프로필 완성도 상태를 조회합니다.")
					.build())
			));
	}

	@Test
	void 프로필_기본정보_조회() throws Exception {
		// 인증 셋업
		authenticationSetUp();

		// given: 응답 더미
		DirectorServiceWithFullNameResponseForDirector service1 = DirectorServiceWithFullNameResponseForDirector.builder()
			.id(10L)
			.name("컷트")
			.fullName("헤어 > 컷트")
			.build();
		DirectorServiceWithFullNameResponseForDirector service2 = DirectorServiceWithFullNameResponseForDirector.builder()
			.id(11L)
			.name("염색")
			.fullName("헤어 > 염색")
			.build();

		LocationResponseForDirector location1 = LocationResponseForDirector.builder()
			.id(100L)
			.name("강남구")
			.fullName("서울특별시 강남구")
			.build();
		LocationResponseForDirector location2 = LocationResponseForDirector.builder()
			.id(101L)
			.name("서초구")
			.fullName("서울특별시 서초구")
			.build();

		DirectorInfoFindProfileBasicInfoResponseForDirector response = DirectorInfoFindProfileBasicInfoResponseForDirector.builder()
			.id(1L)
			.member(MemberResponseForDirector.builder()
				.id(1L)
				.nickname(NICKNAME_STR)
				.profileImageUrl(PROFILE_IMAGE_STR)
				.isWithdrawal(false)
				.build())
			.gender("MALE")
			.services(java.util.List.of(service1, service2))
			.introduceText("안녕하세요. 디렉터입니다.")
			.locations(java.util.List.of(location1, location2))
			.storeAddress("서울특별시 강남구 역삼동 123-45")
			.build();

		given(directorInfoFacadeForDirector.findProfileBasicInfo(anyLong())).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/directors/me/profile-basic")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("director-find-profile-basic",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER).description("디렉터 ID"),
					fieldWithPath("gender").type(JsonFieldType.STRING)
						.attributes(enumFormat(Gender.class, Enum::name))
						.description("성별"),
					fieldWithPath("introduceText").type(JsonFieldType.STRING).description("소개글"),
					fieldWithPath("storeAddress").type(JsonFieldType.STRING).description("매장 주소"),

					fieldWithPath("member").type(JsonFieldType.OBJECT).description("회원"),
					fieldWithPath("member.id").type(JsonFieldType.NUMBER).description("회원 아이디"),
					fieldWithPath("member.nickname").type(JsonFieldType.STRING).description("닉네임"),
					fieldWithPath("member.profileImageUrl").type(JsonFieldType.STRING).description("프로필 이미지 url"),
					fieldWithPath("member.isWithdrawal").type(JsonFieldType.BOOLEAN).description("회원 탈퇴 여부"),

					fieldWithPath("services").type(JsonFieldType.ARRAY).description("제공 서비스 목록"),
					fieldWithPath("services[].id").type(JsonFieldType.NUMBER).description("서비스 ID"),
					fieldWithPath("services[].name").type(JsonFieldType.STRING).description("서비스명"),
					fieldWithPath("services[].fullName").type(JsonFieldType.STRING).description("서비스 전체 경로명"),

					fieldWithPath("locations").type(JsonFieldType.ARRAY).description("활동 지역 목록"),
					fieldWithPath("locations[].id").type(JsonFieldType.NUMBER).description("지역 ID"),
					fieldWithPath("locations[].name").type(JsonFieldType.STRING).description("지역명"),
					fieldWithPath("locations[].fullName").type(JsonFieldType.STRING).description("지역 전체 경로명")
				),

				resource(builder()
					.tag("🎬 디렉터 API")
					.summary("디렉터 프로필 기본정보 조회")
					.description("디렉터가 자신의 프로필 기본정보를 조회합니다.")
					.build())
			));
	}
}
