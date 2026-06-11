package com.motd.be.rest_docs.module.member.member_block;

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
import com.motd.be.module.member.member.dto.response.MemberResponse;
import com.motd.be.module.member.member_block.dto.request.MemberBlockRequest;
import com.motd.be.module.member.member_block.dto.response.MemberBlockCheckResponse;
import com.motd.be.module.member.member_block.dto.response.MemberBlockFindAllResponse;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class MemberBlockRestDocsTest extends BaseRestDocsTest {

	@Test
	void 회원_차단() throws Exception {
		authenticationSetUp();

		// given
		MemberBlockRequest request = MemberBlockRequest.builder()
			.blockedId(2L)
			.build();

		willDoNothing().given(memberBlockFacade)
			.save(anyLong(), any(MemberBlockRequest.class));

		// when & then
		mockMvc.perform(post("/api/members/block")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andDo(document("member-block-save",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR)
						.description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("blockedId").type(JsonFieldType.NUMBER)
						.description("차단할 회원 ID")
				),

				resource(builder()
					.tag("🚫 회원 차단 API")
					.summary("회원 차단")
					.description("다른 회원을 차단합니다.")
					.build())
			));
	}

	@Test
	void 회원_차단_해제() throws Exception {
		authenticationSetUp();

		// given
		MemberBlockRequest request = MemberBlockRequest.builder()
			.blockedId(2L)
			.build();

		willDoNothing().given(memberBlockFacade)
			.delete(anyLong(), any(MemberBlockRequest.class));

		// when & then
		mockMvc.perform(delete("/api/members/block")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent())
			.andDo(document("member-block-delete",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR)
						.description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("blockedId").type(JsonFieldType.NUMBER)
						.description("차단 해제할 회원 ID")
				),

				resource(builder()
					.tag("🚫 회원 차단 API")
					.summary("회원 차단 해제")
					.description("차단한 회원을 차단 해제합니다.")
					.build())
			));
	}

	@Test
	void 차단_목록_조회() throws Exception {
		authenticationSetUp();

		// given
		MemberBlockFindAllResponse response = MemberBlockFindAllResponse.builder()
			.page(0)
			.hasNext(true)
			.blocks(List.of(
				MemberResponse.builder()
					.id(2L)
					.nickname("차단된회원1")
					.profileImageUrl("https://cdn.example.com/profile1.jpg")
					.isWithdrawal(false)
					.build(),
				MemberResponse.builder()
					.id(3L)
					.nickname("차단된회원2")
					.profileImageUrl("https://cdn.example.com/profile2.jpg")
					.isWithdrawal(false)
					.build()
			))
			.build();

		willReturn(response).given(memberBlockFacade)
			.findAll(anyLong(), anyInt());

		// when & then
		mockMvc.perform(get("/api/members/block")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.param("page", "0")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("member-block-find-all",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR)
						.description("HttpOnly refreshToken 쿠키")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("page").optional()
						.description("페이지 번호 (기본값: 0)")
				),

				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER)
						.description("현재 페이지 번호"),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN)
						.description("다음 페이지 존재 여부"),
					fieldWithPath("blocks").type(JsonFieldType.ARRAY)
						.description("차단한 회원 목록"),
					fieldWithPath("blocks[].id").type(JsonFieldType.NUMBER)
						.description("차단된 회원 ID"),
					fieldWithPath("blocks[].nickname").type(JsonFieldType.STRING)
						.description("차단된 회원 닉네임"),
					fieldWithPath("blocks[].profileImageUrl").type(JsonFieldType.STRING)
						.description("차단된 회원 프로필 이미지 URL"),
					fieldWithPath("blocks[].isWithdrawal").type(JsonFieldType.BOOLEAN)
						.description("회원 탈퇴 여부")
				),

				resource(builder()
					.tag("🚫 회원 차단 API")
					.summary("차단 목록 조회")
					.description("내가 차단한 회원 목록을 조회합니다.")
					.build())
			));
	}

	@Test
	void 차단_여부_확인() throws Exception {
		authenticationSetUp();

		// given
		Long targetMemberId = 2L;
		MemberBlockCheckResponse response = MemberBlockCheckResponse.from(true);

		willReturn(response).given(memberBlockFacade)
			.check(anyLong(), anyLong());

		// when & then
		mockMvc.perform(get("/api/members/block/check")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.param("targetMemberId", String.valueOf(targetMemberId))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("member-block-check",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR)
						.description("HttpOnly refreshToken 쿠키")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("targetMemberId")
						.description("차단 여부를 확인할 대상 회원 ID")
				),

				responseFields(
					fieldWithPath("blocked").type(JsonFieldType.BOOLEAN)
						.description("차단 여부 (true: 차단됨, false: 차단 안됨)")
				),

				resource(builder()
					.tag("🚫 회원 차단 API")
					.summary("차단 여부 확인")
					.description("특정 회원의 차단 여부를 확인합니다.")
					.build())
			));
	}

}
