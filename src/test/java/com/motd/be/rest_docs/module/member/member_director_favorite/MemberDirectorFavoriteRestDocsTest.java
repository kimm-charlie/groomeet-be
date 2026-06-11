package com.motd.be.rest_docs.module.member.member_director_favorite;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.member.director_service.dto.response.DirectorServiceResponse;
import com.motd.be.module.member.member.dto.response.MemberResponseWithCompletedAndReviewCountResponse;
import com.motd.be.module.member.member_director_favorite.dto.request.MemberDirectorFavoriteRequest;
import com.motd.be.module.member.member_director_favorite.dto.response.MemberDirectorFavoriteFindAllResponse;
import com.motd.be.module.member.member_director_favorite.dto.response.MemberDirectorFavoriteResponse;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class MemberDirectorFavoriteRestDocsTest extends BaseRestDocsTest {

	@Test
	void 즐겨찾기_추가() throws Exception {
		authenticationSetUp();

		MemberDirectorFavoriteRequest request = MemberDirectorFavoriteRequest.builder()
			.targetMemberId(2L)
			.build();

		willDoNothing().given(memberDirectorFavoriteFacade).save(anyLong(), any(MemberDirectorFavoriteRequest.class));

		mockMvc.perform(post("/api/members/favorites")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isCreated())
			.andDo(document("member-director-favorite-save",
				getRequestPreProcessor(),
				getResponsePreProcessor(),
				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),
				requestFields(
					fieldWithPath("targetMemberId").type(JsonFieldType.NUMBER).description("즐겨찾기할 디렉터 ID")
				),
				resource(builder()
					.tag("⭐ 디렉터 즐겨찾기 API")
					.summary("디렉터 즐겨찾기 추가")
					.description("특정 디렉터를 즐겨찾기에 추가합니다.")
					.build())
			));
	}

	@Test
	void 즐겨찾기_삭제() throws Exception {
		authenticationSetUp();

		MemberDirectorFavoriteRequest request = MemberDirectorFavoriteRequest.builder()
			.targetMemberId(2L)
			.build();

		willDoNothing().given(memberDirectorFavoriteFacade).delete(anyLong(), any(MemberDirectorFavoriteRequest.class));

		mockMvc.perform(delete("/api/members/favorites")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isNoContent())
			.andDo(document("member-director-favorite-delete",
				getRequestPreProcessor(),
				getResponsePreProcessor(),
				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),
				requestFields(
					fieldWithPath("targetMemberId").type(JsonFieldType.NUMBER).description("즐겨찾기 해제할 디렉터 ID")
				),
				resource(builder()
					.tag("⭐ 디렉터 즐겨찾기 API")
					.summary("디렉터 즐겨찾기 삭제")
					.description("특정 디렉터를 즐겨찾기에서 삭제합니다.")
					.build())
			));
	}

	@Test
	void 즐겨찾기_목록_조회() throws Exception {
		authenticationSetUp();

		MemberDirectorFavoriteFindAllResponse response = MemberDirectorFavoriteFindAllResponse.builder()
			.page(0)
			.hasNext(false)
			.directors(List.of(
				MemberDirectorFavoriteResponse.builder()
					.director(MemberResponseWithCompletedAndReviewCountResponse.builder()
						.id(2L)
						.nickname("디렉터1")
						.profileImageUrl("https://cdn.example.com/profile1.jpg")
						.completedEstimateCount(10)
						.reviewCount(5)
						.isWithdrawal(false)
						.build())
					.services(List.of(
						DirectorServiceResponse.builder()
							.id(1L)
							.name("웨딩촬영")
							.build()
					))
					.build(),
				MemberDirectorFavoriteResponse.builder()
					.director(MemberResponseWithCompletedAndReviewCountResponse.builder()
						.id(3L)
						.nickname("디렉터2")
						.profileImageUrl("https://cdn.example.com/profile2.jpg")
						.completedEstimateCount(15)
						.reviewCount(8)
						.isWithdrawal(false)
						.build())
					.services(List.of(
						DirectorServiceResponse.builder()
							.id(2L)
							.name("프로필촬영")
							.build()
					))
					.build()
			))
			.build();

		willReturn(response).given(memberDirectorFavoriteFacade).findAll(anyLong(), anyInt());

		mockMvc.perform(get("/api/members/favorites")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.param(PAGE_STR, "0")
			)
			.andExpect(status().isOk())
			.andDo(document("member-director-favorite-find-all",
				getRequestPreProcessor(),
				getResponsePreProcessor(),
				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),
				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(PAGE_STR)
						.optional().description("페이지 번호 (기본값: 0)")
				),
				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
					fieldWithPath("directors").type(JsonFieldType.ARRAY).description("즐겨찾기한 디렉터 목록"),
					fieldWithPath("directors[].director").type(JsonFieldType.OBJECT).description("디렉터 정보"),
					fieldWithPath("directors[].director.id").type(JsonFieldType.NUMBER).description("디렉터 ID"),
					fieldWithPath("directors[].director.nickname").type(JsonFieldType.STRING).description("디렉터 닉네임"),
					fieldWithPath("directors[].director.profileImageUrl").type(JsonFieldType.STRING)
						.description("디렉터 프로필 이미지 URL"),
					fieldWithPath("directors[].director.completedEstimateCount").type(JsonFieldType.NUMBER)
						.description("완료된 제안 수"),
					fieldWithPath("directors[].director.reviewCount").type(JsonFieldType.NUMBER).description("리뷰 수"),
					fieldWithPath("directors[].director.isWithdrawal").type(JsonFieldType.BOOLEAN)
						.description("디렉터 탈퇴 여부"),
					fieldWithPath("directors[].services").type(JsonFieldType.ARRAY).description("디렉터 서비스 목록"),
					fieldWithPath("directors[].services[].id").type(JsonFieldType.NUMBER).description("서비스 ID"),
					fieldWithPath("directors[].services[].name").type(JsonFieldType.STRING).description("서비스 이름")
				),
				resource(builder()
					.tag("⭐ 디렉터 즐겨찾기 API")
					.summary("즐겨찾기 목록 조회")
					.description("현재 사용자가 즐겨찾기한 디렉터 목록을 조회합니다.")
					.build())
			));
	}
}
