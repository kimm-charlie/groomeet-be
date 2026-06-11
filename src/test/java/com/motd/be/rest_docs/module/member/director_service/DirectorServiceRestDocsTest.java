package com.motd.be.rest_docs.module.member.director_service;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static org.mockito.BDDMockito.*;
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
import com.motd.be.module.member.director_service.dto.response.DirectorServiceFindAllResponse;

@RestDocsTest
public class DirectorServiceRestDocsTest extends BaseRestDocsTest {
	@Test
	void 디렉터_서비스_전체_조회() throws Exception {
		// given
		Long parentId = 1L;
		DirectorServiceFindAllResponse response1 = DirectorServiceFindAllResponse.builder()
			.id(1L)
			.name("서비스명1")
			.parentId(parentId)
			.isActive(true)
			.build();
		DirectorServiceFindAllResponse response2 = DirectorServiceFindAllResponse.builder()
			.id(2L)
			.name("서비스명2")
			.parentId(parentId)
			.isActive(true)
			.build();

		given(directorServiceFacade.findAll(anyLong())).willReturn(Arrays.asList(response1, response2));

		// when & then
		mockMvc.perform(get("/api/director-services")
				.param(DIRECTOR_SERVICE_PARENT_ID, parentId.toString())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("director-service-find-all",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(
							DIRECTOR_SERVICE_PARENT_ID)
						.optional()
						.description("부모 아이디")
				),

				responseFields(
					fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("서비스 ID"),
					fieldWithPath("[].name").type(JsonFieldType.STRING).description("서비스명"),
					fieldWithPath("[].parentId").optional().type(JsonFieldType.NUMBER).description("부모 서비스 ID"),
					fieldWithPath("[].isActive").type(JsonFieldType.BOOLEAN).description("활성 여부")
				),

				resource(builder()
					.tag("🎬 디렉터 서비스 API")
					.summary("디렉터 서비스 전체 조회")
					.description("디렉터 서비스 전체 목록을 조회합니다. parentId가 있으면 하위 서비스만 조회합니다.")
					.build())
			));
	}
}
