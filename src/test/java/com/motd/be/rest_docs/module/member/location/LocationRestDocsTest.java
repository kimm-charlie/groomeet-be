package com.motd.be.rest_docs.module.member.location;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.rest_docs.Utils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
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
import com.motd.be.module.member.location.dto.response.LocationFindAllResponse;
import com.motd.be.module.member.location.entity.LocationType;

@com.motd.be.annotation.RestDocsTest
public class LocationRestDocsTest extends BaseRestDocsTest {

	@Test
	void 지역_전체_조회() throws Exception {
		LocationFindAllResponse response1 = LocationFindAllResponse.builder()
			.id(1L)
			.name("서울")
			.locationType("CITY")
			.parentId(null)
			.build();
		LocationFindAllResponse response2 = LocationFindAllResponse.builder()
			.id(2L)
			.name("강남구")
			.locationType("DISTRICT")
			.parentId(1L)
			.build();
		List<LocationFindAllResponse> responseList = List.of(response1, response2);

		willReturn(responseList).given(locationFacade).findAll(anyLong());

		mockMvc.perform(get("/api/locations")
				.param(LOCATION_PARENT_ID, "1")
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andDo(document("location-find-all",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName(LOCATION_PARENT_ID)
						.optional()
						.description("상위 지역 ID (없으면 최상위 지역(도시) 조회)")
				),

				responseFields(
					fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("지역 ID"),
					fieldWithPath("[].name").type(JsonFieldType.STRING).description("지역 이름"),
					fieldWithPath("[].locationType").type(JsonFieldType.STRING)
						.attributes(enumFormat(LocationType.class, Enum::name))
						.description("지역 타입"),
					fieldWithPath("[].parentId").optional().type(JsonFieldType.NUMBER).description("부모 아이디")
				),

				resource(builder()
					.tag("📍 지역 API")
					.summary("지역 전체 조회")
					.description("지역 목록을 조회합니다. 상위 지역 ID를 전달하면 하위 지역을 조회합니다.")
					.build())
			));
	}
}
