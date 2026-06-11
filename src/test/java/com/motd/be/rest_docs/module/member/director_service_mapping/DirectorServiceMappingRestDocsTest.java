package com.motd.be.rest_docs.module.member.director_service_mapping;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.director.director_service_mapping.dto.response.DirectorServiceFindAllResponseForDirector;

@RestDocsTest
public class DirectorServiceMappingRestDocsTest extends BaseRestDocsTest {

	@Test
	void 디렉터_서비스_목록_조회() throws Exception {

		// given
		List<DirectorServiceFindAllResponseForDirector> responseList = Arrays.asList(
			DirectorServiceFindAllResponseForDirector.builder()
				.id(1L)
				.name("서비스명1")
				.parentName("부모서비스명1")
				.build(),
			DirectorServiceFindAllResponseForDirector.builder()
				.id(2L)
				.name("서비스명2")
				.parentName("부모서비스명2")
				.build()
		);

		willReturn(responseList).given(directorServiceMappingFacade).findAll(anyLong());

		// when & then
		mockMvc.perform(get("/api/directors/{targetMemberId}/services", 1L)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("director-service-mapping-find-all-for-public",

				getRequestPreProcessor(),
				getResponsePreProcessor(),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("targetMemberId")
						.description("조회할 디렉터의 memberId")
				),

				responseFields(
					fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("서비스 ID"),
					fieldWithPath("[].name").type(JsonFieldType.STRING).description("서비스명"),
					fieldWithPath("[].parentName").type(JsonFieldType.STRING).description("부모 서비스명")
				),

				resource(builder()
					.tag("🎬 특정 디렉터 서비스 매핑 API (회원용)")
					.summary("특정 디렉터 서비스 목록 조회 (회원용)")
					.description("특정 디렉터의 서비스 목록을 조회합니다.(회원용)")
					.build())
			));
	}
}

