package com.motd.be.rest_docs.module.admin.member;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
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
import com.motd.be.module.admin.member.dto.request.BanRequestForAdmin;
import com.motd.be.module.admin.director_service.dto.response.DirectorServiceChildrenResponseForAdmin;
import com.motd.be.module.admin.member.dto.response.MemberDetailResponseForAdmin;
import com.motd.be.module.admin.member.dto.response.MemberFindAllResponseForAdmin;
import com.motd.be.module.admin.member.dto.response.MemberSummaryResponseForAdmin;
import com.motd.be.module.member.member.entity.BanPeriod;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class MemberForAdminRestDocsTest extends BaseRestDocsTest {

	@Test
	void 관리자_회원_목록_조회_문서화() throws Exception {
		authenticationSetUp();

		MemberSummaryResponseForAdmin memberSummary = MemberSummaryResponseForAdmin.builder()
			.id(1L)
			.nickname("테스트유저")
			.profileImageUrl("https://cdn.example.com/profile.jpg")
			.isDirector(false)
			.isBanned(false)
			.services(List.of("헤어", "네일"))
			.build();

		MemberFindAllResponseForAdmin response = MemberFindAllResponseForAdmin.builder()
			.page(0)
			.hasNext(true)
			.members(List.of(memberSummary))
			.totalCount(150L)
			.directorCount(50L)
			.memberCount(100L)
			.build();

		given(memberFacadeForAdmin.findAll(anyInt(), any(), any(), any())).willReturn(response);

		mockMvc.perform(get("/api/admin/members")
				.param("page", "0")
				.param("search", "테스트")
				.param("showOnlyDirector", "false")
				.param("showOnlyMember", "true")
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)))
			.andExpect(status().isOk())
			.andDo(document("admin-member-find-all",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("page")
						.optional()
						.description("페이지 번호 (기본값: 0)"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("search")
						.optional()
						.description("검색어 (닉네임 또는 회원 ID)"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("showOnlyDirector")
						.optional()
						.description("디렉터만 조회 여부"),
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("showOnlyMember")
						.optional()
						.description("일반회원만 조회 여부")
				),

				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
					fieldWithPath("totalCount").type(JsonFieldType.NUMBER).description("전체 회원 수"),
					fieldWithPath("directorCount").type(JsonFieldType.NUMBER).description("디렉터 수"),
					fieldWithPath("memberCount").type(JsonFieldType.NUMBER).description("일반회원 수"),
					fieldWithPath("members").type(JsonFieldType.ARRAY).description("회원 목록"),
					fieldWithPath("members[].id").type(JsonFieldType.NUMBER).description("회원 ID"),
					fieldWithPath("members[].nickname").type(JsonFieldType.STRING).description("닉네임"),
					fieldWithPath("members[].profileImageUrl").type(JsonFieldType.STRING).optional().description("프로필 이미지 URL"),
					fieldWithPath("members[].isDirector").type(JsonFieldType.BOOLEAN).description("디렉터 여부"),
					fieldWithPath("members[].isBanned").type(JsonFieldType.BOOLEAN).description("밴 여부"),
					fieldWithPath("members[].services").type(JsonFieldType.ARRAY).optional().description("제공 서비스 목록 (디렉터인 경우)")
				),

				resource(builder()
					.tag("⭐ 관리자 - 회원 관리 API")
					.summary("회원 목록 조회 API")
					.description("관리자가 회원 목록을 조회하는 API. 검색 및 역할 필터링 지원.")
					.queryParameters(
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName("page")
							.optional()
							.description("페이지 번호 (기본값: 0)"),
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName("search")
							.optional()
							.description("검색어 (닉네임 또는 회원 ID)"),
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName("showOnlyDirector")
							.optional()
							.description("디렉터만 조회 여부"),
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName("showOnlyMember")
							.optional()
							.description("일반회원만 조회 여부")
					)
					.build()
				)
			));
	}

	@Test
	void 관리자_회원_상세_조회_문서화() throws Exception {
		authenticationSetUp();

		MemberDetailResponseForAdmin response = MemberDetailResponseForAdmin.builder()
			.id(1L)
			.nickname("테스트유저")
			.profileImageUrl("https://cdn.example.com/profile.jpg")
			.phoneNumber("010-1234-5678")
			.name("홍길동")
			.birth("1990-01-01")
			.createdAt("2024-01-15")
			.signInPlatform("KAKAO")
			.isDirector(false)
			.isAuthenticated(true)
			.isBanned(false)
			.bannedAt(null)
			.email("test@example.com")
			.reportedCount(0)
			.services(List.of(
				DirectorServiceChildrenResponseForAdmin.of(1L, "헤어", List.of("커트", "펌")),
				DirectorServiceChildrenResponseForAdmin.of(2L, "네일", List.of("젤네일"))
			))
			.build();

		given(memberFacadeForAdmin.findDetail(anyLong())).willReturn(response);

		mockMvc.perform(get("/api/admin/members/{memberId}", 1L)
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)))
			.andExpect(status().isOk())
			.andDo(document("admin-member-find-detail",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("memberId")
						.description("회원 ID")
				),

				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER).description("회원 ID"),
					fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임"),
					fieldWithPath("profileImageUrl").type(JsonFieldType.STRING).optional().description("프로필 이미지 URL"),
					fieldWithPath("phoneNumber").type(JsonFieldType.STRING).optional().description("전화번호"),
					fieldWithPath("name").type(JsonFieldType.STRING).optional().description("이름"),
					fieldWithPath("birth").type(JsonFieldType.STRING).optional().description("생년월일"),
					fieldWithPath("createdAt").type(JsonFieldType.STRING).optional().description("가입일"),
					fieldWithPath("signInPlatform").type(JsonFieldType.STRING).optional().description("가입 플랫폼"),
					fieldWithPath("isDirector").type(JsonFieldType.BOOLEAN).description("디렉터 여부"),
					fieldWithPath("isAuthenticated").type(JsonFieldType.BOOLEAN).description("인증 여부"),
					fieldWithPath("isBanned").type(JsonFieldType.BOOLEAN).description("밴 여부"),
					fieldWithPath("bannedAt").type(JsonFieldType.STRING).optional().description("밴 날짜"),
					fieldWithPath("email").type(JsonFieldType.STRING).optional().description("이메일"),
					fieldWithPath("reportedCount").type(JsonFieldType.NUMBER).description("신고 횟수"),
					fieldWithPath("services").type(JsonFieldType.ARRAY).optional().description("제공 서비스 목록 (디렉터인 경우)"),
					fieldWithPath("services[].id").type(JsonFieldType.NUMBER).description("서비스 ID"),
					fieldWithPath("services[].name").type(JsonFieldType.STRING).description("서비스 이름"),
					fieldWithPath("services[].children").type(JsonFieldType.ARRAY).description("하위 서비스 목록")
				),

				resource(builder()
					.tag("⭐ 관리자 - 회원 관리 API")
					.summary("회원 상세 조회 API")
					.description("관리자가 특정 회원의 상세 정보를 조회하는 API")
					.build()
				)
			));
	}

	@Test
	void 관리자_회원_밴_문서화() throws Exception {
		authenticationSetUp();

		BanRequestForAdmin request = BanRequestForAdmin.builder()
			.banPeriod(BanPeriod.SEVEN_DAYS)
			.build();

		willDoNothing().given(memberFacadeForAdmin).ban(anyLong(), any(BanRequestForAdmin.class));

		mockMvc.perform(post("/api/admin/members/{memberId}/ban", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.cookie(new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR)))
			.andExpect(status().isNoContent())
			.andDo(document("admin-member-ban",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키")
				),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("memberId")
						.description("밴 대상 회원 ID")
				),

				requestFields(
					fieldWithPath("banPeriod").type(JsonFieldType.STRING)
						.description("밴 기간. ONE_DAY, THREE_DAYS, SEVEN_DAYS, THIRTY_DAYS, PERMANENT 중 선택")
				),

				resource(builder()
					.tag("⭐ 관리자 - 회원 관리 API")
					.summary("회원 밴 API")
					.description("관리자가 특정 회원을 밴 처리하는 API. 밴 기간 지정 가능.")
					.pathParameters(
						org.springframework.restdocs.request.RequestDocumentation.parameterWithName("memberId")
							.description("밴 대상 회원 ID")
					)
					.build()
				)
			));
	}
}
