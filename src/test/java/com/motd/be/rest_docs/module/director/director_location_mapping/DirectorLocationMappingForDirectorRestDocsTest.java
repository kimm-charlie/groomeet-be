package com.motd.be.rest_docs.module.director.director_location_mapping;

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

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.director.director_location_mapping.dto.request.DirectorLocationMappingUpdateRequestForDirector;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class DirectorLocationMappingForDirectorRestDocsTest extends BaseRestDocsTest {

	@Test
	void 활동_위치_수정() throws Exception {
		authenticationSetUp();

		// given
		DirectorLocationMappingUpdateRequestForDirector request = DirectorLocationMappingUpdateRequestForDirector.builder()
			.locationIds(Arrays.asList(1L, 2L, 3L))
			.build();

		willDoNothing().given(directorLocationMappingFacadeForDirector)
			.updateLocation(anyLong(), any(DirectorLocationMappingUpdateRequestForDirector.class));

		// when & then
		mockMvc.perform(patch("/api/directors/my/location")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent())
			.andDo(document("director-update-location",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR)
						.description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR)
						.description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("locationIds").type(JsonFieldType.ARRAY)
						.description("지역 ID 리스트")
				),

				resource(builder()
					.tag("🎬 디렉터 API")
					.summary("디렉터 위치 수정")
					.description("디렉터가 자신의 활동 위치를 수정합니다.")
					.build())
			));
	}
}
