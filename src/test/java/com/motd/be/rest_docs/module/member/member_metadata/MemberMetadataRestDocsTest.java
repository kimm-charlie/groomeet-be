package com.motd.be.rest_docs.module.member.member_metadata;

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

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.member.member_metadata.dto.request.MemberMetadataSaveOrUpdateRequest;
import com.motd.be.module.member.member_metadata.dto.response.MemberMetadataFindResponse;
import com.motd.be.module.member.member_metadata.entity.DeviceType;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class MemberMetadataRestDocsTest extends BaseRestDocsTest {

	@Test
	void 회원_메타데이터_저장_또는_수정() throws Exception {
		authenticationSetUp();

		// given
		MemberMetadataSaveOrUpdateRequest request = MemberMetadataSaveOrUpdateRequest.builder()
			.deviceType(DeviceType.WEB.name())
			.version("1.0.0")
			.build();

		willDoNothing().given(memberMetadataFacade)
			.saveOrUpdate(anyLong(), any(MemberMetadataSaveOrUpdateRequest.class));

		// when & then
		mockMvc.perform(post("/api/members/my/metadata")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andDo(document("member-metadata-save-or-update",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("deviceType").type(JsonFieldType.STRING)
						.attributes(enumFormat(DeviceType.class, Enum::name))
						.description("디바이스 타입"),
					fieldWithPath("version").type(JsonFieldType.STRING).description("앱 버전")
				),

				resource(builder()
					.tag("👤 회원 메타데이터 API")
					.summary("회원 메타데이터 저장/수정")
					.description("회원의 디바이스별 메타데이터(앱 버전 등)를 저장하거나 수정합니다.")
					.build())
			));
	}

	@Test
	void 회원_메타데이터_전체_조회() throws Exception {
		authenticationSetUp();

		// given
		MemberMetadataFindResponse response = MemberMetadataFindResponse.builder()
			.id(1L)
			.version("1.0.0")
			.build();

		willReturn(response).given(memberMetadataFacade).find(anyLong(), any(String.class));

		// when & then
		mockMvc.perform(get("/api/members/my/metadata")
				.param(DEVICE_TYPE_STR, DeviceType.WEB.name())
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				))
			.andExpect(status().isOk())
			.andDo(document("member-metadata-find",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(DEVICE_TYPE_STR)
						.attributes(enumFormat(DeviceType.class, Enum::name))
						.description("기기 타입")
				),

				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER).description("메타데이타 아이디"),
					fieldWithPath("version").type(JsonFieldType.STRING).description("버전")
				),

				resource(builder()
					.tag("👤 회원 메타데이터 API")
					.summary("회원 메타데이터 조회")
					.description("회원의 디바이스별 메타데이터(앱 버전 등)를 조회합니다.")
					.build())
			));
	}
}
