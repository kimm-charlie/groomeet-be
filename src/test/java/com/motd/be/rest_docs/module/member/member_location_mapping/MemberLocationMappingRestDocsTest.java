package com.motd.be.rest_docs.module.member.member_location_mapping;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.member.location.dto.response.LocationResponse;
import com.motd.be.module.member.member_location_mapping.dto.request.MemberLocationMappingUpdateRequest;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class MemberLocationMappingRestDocsTest extends BaseRestDocsTest {

	@Test
	void 활동_지역_조회() throws Exception {
		authenticationSetUp();

		List<LocationResponse> response = List.of(
			LocationResponse.builder()
				.id(1L)
				.name("강남구")
				.fullName("서울특별시 강남구")
				.build(),
			LocationResponse.builder()
				.id(2L)
				.name("서초구")
				.fullName("서울특별시 서초구")
				.build()
		);

		willReturn(response).given(memberLocationMappingFacade).findAll(anyLong());

		mockMvc.perform(get("/api/members/my/location")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
			)
			.andExpect(status().isOk())
			.andDo(document("member-location-mapping-find-all",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				responseFields(
					fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("지역 ID"),
					fieldWithPath("[].name").type(JsonFieldType.STRING).description("지역 이름"),
					fieldWithPath("[].fullName").type(JsonFieldType.STRING).description("지역 전체 이름")
				),

				resource(builder()
					.tag("📍 회원 활동 지역 API")
					.summary("활동 지역 조회")
					.description("현재 로그인한 회원의 활동 지역 목록을 조회합니다.")
					.build())
			));
	}

	@Test
	void 활동_지역_저장_또는_수정() throws Exception {
		authenticationSetUp();

		MemberLocationMappingUpdateRequest request = MemberLocationMappingUpdateRequest.builder()
			.locationIds(List.of(1L, 2L, 3L))
			.build();

		willDoNothing().given(memberLocationMappingFacade)
			.saveOrUpdate(anyLong(), any(MemberLocationMappingUpdateRequest.class));

		mockMvc.perform(patch("/api/members/my/location")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isNoContent())
			.andDo(document("member-location-mapping-save-or-update",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("locationIds").type(JsonFieldType.ARRAY).description("활동 지역 ID 목록")
				),

				resource(builder()
					.tag("📍 회원 활동 지역 API")
					.summary("활동 지역 저장 또는 수정")
					.description("현재 로그인한 회원의 활동 지역을 저장하거나 수정합니다.")
					.build())
			));
	}
}
