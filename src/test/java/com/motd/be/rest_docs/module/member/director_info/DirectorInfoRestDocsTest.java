package com.motd.be.rest_docs.module.member.director_info;

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
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.member.director_info.dto.request.DirectorInfoRegisterRequest;
import com.motd.be.module.member.director_info.dto.response.DirectorRankMainViewResponse;
import com.motd.be.module.member.director_info.dto.response.DirectorRankPageResponse;
import com.motd.be.module.member.director_info.dto.response.DirectorRankResponse;
import com.motd.be.module.member.director_service.dto.response.DirectorServiceResponse;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.dto.response.MemberResponse;
import com.motd.be.module.member.member.entity.Gender;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class DirectorInfoRestDocsTest extends BaseRestDocsTest {

	@Test
	void 디렉터_정보_등록() throws Exception {
		authenticationSetUp();

		// given
		DirectorInfoRegisterRequest request = DirectorInfoRegisterRequest.builder()
			.nickname(NICKNAME_STR)
			.locationIds(Arrays.asList(1L, 2L))
			.directorServiceIds(Arrays.asList(1L, 2L))
			.gender(Gender.MAN.name())
			.phoneNumber(PHONE_NUMBER)
			.build();

		willReturn(Jwt.builder().accessToken(ACCESS_TOKEN_STR).refreshToken(REFRESH_TOKEN_STR).build()).given(
				directorInfoFacade)
			.register(anyLong(), any(DirectorInfoRegisterRequest.class), any(String.class), any(String.class));

		// when & then
		mockMvc.perform(post("/api/members/me/director").cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andDo(document("director-register", getRequestPreProcessor(), getResponsePreProcessor(),

				requestCookies(cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키 (role : MEMBER)"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키 (role : MEMBER)")),

				requestFields(
					fieldWithPath("nickname")
						.type(JsonFieldType.STRING)
						.description("활동명"),
					fieldWithPath("locationIds")
						.type(JsonFieldType.ARRAY)
						.description("지역 ID 리스트 (반드시 1개이상 필요)"),
					fieldWithPath("directorServiceIds")
						.type(JsonFieldType.ARRAY)
						.description("디렉터가 설정한 카테고리 (반드시 1개이상 필요)"), fieldWithPath("gender")
						.type(JsonFieldType.STRING)
						.attributes(enumFormat(Gender.class, Enum::name))
						.description("성별"),
					fieldWithPath("phoneNumber")
						.optional()
						.type(JsonFieldType.STRING)
						.description("전화번호")
				),

				responseCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키 (role : DIRECTOR)"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키 (role : DIRECTOR)")),

				resource(builder().tag("🎬 디렉터 API").summary("디렉터 정보 등록").description("회원이 디렉터 정보를 등록합니다.").build())));
	}

	@Test
	void 디렉터_랭킹_조회_메인뷰() throws Exception {
		// given
		DirectorRankMainViewResponse response = DirectorRankMainViewResponse.builder()
			.directors(Arrays.asList(DirectorRankResponse.builder()
				.director(MemberResponse.builder()
					.id(1L)
					.nickname("활동명")
					.profileImageUrl(CDN_URL_STR)
					.isWithdrawal(false)
					.build())
				.services(Arrays.asList(DirectorServiceResponse.builder().id(1L).name("서비스 이름1").build(),
					DirectorServiceResponse.builder().id(1L).name("서비스 이름2").build()))
				.completedEstimateCount(10)
				.build()))
			.build();

		willReturn(response).given(directorInfoFacade).findDirectorRankInMainView();

		// when & then
		mockMvc.perform(get("/api/directors/rank").param("viewType", "mainView"))
			.andExpect(status().isOk())
			.andDo(document("director-rank-main-view", getRequestPreProcessor(), getResponsePreProcessor(),

				queryParameters(org.springframework.restdocs.request.RequestDocumentation.parameterWithName("viewType")
					.description("뷰 타입 (mainView)")),

				responseFields(fieldWithPath("directors").type(JsonFieldType.ARRAY).description("디렉터 랭킹 리스트"),
					fieldWithPath("directors[].director").type(JsonFieldType.OBJECT).description("디렉터 회원 정보"),
					fieldWithPath("directors[].director.id").type(JsonFieldType.NUMBER).description("회원 ID"),
					fieldWithPath("directors[].director.nickname").type(JsonFieldType.STRING).description("닉네임"),
					fieldWithPath("directors[].director.profileImageUrl").type(JsonFieldType.STRING)
						.description("프로필 이미지 URL"),
					fieldWithPath("directors[].director.isWithdrawal").type(JsonFieldType.BOOLEAN)
						.description("회원 탈퇴 여부"),

					fieldWithPath("directors[].services").type(JsonFieldType.ARRAY).description("제공 서비스 목록"),
					fieldWithPath("directors[].services[].id").type(JsonFieldType.NUMBER).description("서비스 ID"),
					fieldWithPath("directors[].services[].name").type(JsonFieldType.STRING).description("서비스 이름"),
					fieldWithPath("directors[].completedEstimateCount").type(JsonFieldType.NUMBER)
						.description("완료된 제안 수")),

				resource(builder().tag("🎬 디렉터 API")
					.summary("디렉터 랭킹 조회 (메인 뷰)")
					.description("메인 화면에서 디렉터 랭킹을 조회합니다. 인증 불필요.")
					.build())));
	}

	@Test
	void 디렉터_랭킹_조회_랭킹뷰() throws Exception {
		// given
		DirectorRankPageResponse response = DirectorRankPageResponse.builder()
			.page(0)
			.hasNext(true)
			.directors(Arrays.asList(DirectorRankResponse.builder()
				.director(MemberResponse.builder()
					.id(1L)
					.nickname("활동명")
					.profileImageUrl(CDN_URL_STR)
					.isWithdrawal(false)
					.build())
				.services(Arrays.asList(DirectorServiceResponse.builder().id(1L).name("서비스 이름1").build(),
					DirectorServiceResponse.builder().id(1L).name("서비스 이름2").build()))
				.completedEstimateCount(10)
				.build()))
			.build();

		willReturn(response).given(directorInfoFacade).findDirectorRankInRankView(anyInt());

		// when & then
		mockMvc.perform(get("/api/directors/rank").param("viewType", "rankView").param("page", "0"))
			.andExpect(status().isOk())
			.andDo(document("director-rank-rank-view", getRequestPreProcessor(), getResponsePreProcessor(),

				queryParameters(org.springframework.restdocs.request.RequestDocumentation.parameterWithName("viewType")
						.description("뷰 타입 (rankView)"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("page")
						.description("페이지 번호 (0부터 시작, 기본값: 0)")
						.optional()),

				responseFields(fieldWithPath("page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
					fieldWithPath("directors").type(JsonFieldType.ARRAY).description("디렉터 랭킹 리스트"),
					fieldWithPath("directors[].director").type(JsonFieldType.OBJECT).description("디렉터 회원 정보"),
					fieldWithPath("directors[].director.id").type(JsonFieldType.NUMBER).description("회원 ID"),
					fieldWithPath("directors[].director.nickname").type(JsonFieldType.STRING).description("닉네임"),
					fieldWithPath("directors[].director.profileImageUrl").type(JsonFieldType.STRING)
						.description("프로필 이미지 URL"),
					fieldWithPath("directors[].director.isWithdrawal").type(JsonFieldType.BOOLEAN)
						.description("회원 탈퇴 여부"),

					fieldWithPath("directors[].services").type(JsonFieldType.ARRAY).description("제공 서비스 목록"),
					fieldWithPath("directors[].services[].id").type(JsonFieldType.NUMBER).description("서비스 ID"),
					fieldWithPath("directors[].services[].name").type(JsonFieldType.STRING).description("서비스 이름"),
					fieldWithPath("directors[].completedEstimateCount").type(JsonFieldType.NUMBER)
						.description("완료된 제안 수")),

				resource(builder().tag("🎬 디렉터 API")
					.summary("디렉터 랭킹 조회 (랭킹 페이지)")
					.description("랭킹 페이지에서 디렉터 랭킹을 페이징하여 조회합니다. 인증 불필요.")
					.build())));
	}
}
