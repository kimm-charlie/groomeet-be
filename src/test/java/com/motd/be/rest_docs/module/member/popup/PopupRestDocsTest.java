package com.motd.be.rest_docs.module.member.popup;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.member.popup.dto.response.PopupFindAllResponse;
import com.motd.be.module.member.popup.dto.response.PopupResponse;

@RestDocsTest
public class PopupRestDocsTest extends BaseRestDocsTest {

	@Test
	void 팝업_전체_조회() throws Exception {
		// given
		List<PopupResponse> popUpResponses = List.of(
			PopupResponse.builder()
				.id(1L)
				.title("신년 이벤트 팝업")
				.thumbnailImageUrl(THUMBNAIL_IMAGE_URL)
				.linkUrl(LINK_URL)
				.build(),
			PopupResponse.builder()
				.id(2L)
				.title("할인 이벤트 팝업")
				.thumbnailImageUrl(THUMBNAIL_IMAGE_URL)
				.linkUrl(LINK_URL)
				.build()
		);

		PopupFindAllResponse response = PopupFindAllResponse.builder()
			.popUps(popUpResponses)
			.totalCount(2)
			.build();

		// when
		willReturn(response).given(popupFacade).findAll();

		// then
		mockMvc.perform(get("/api/popups"))
			.andExpect(status().isOk())
			.andDo(document("popup-find-all",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				responseFields(
					fieldWithPath("popUps").type(JsonFieldType.ARRAY)
						.description("팝업 목록"),
					fieldWithPath("popUps[].id").type(JsonFieldType.NUMBER)
						.description("팝업 ID"),
					fieldWithPath("popUps[].title").type(JsonFieldType.STRING)
						.description("팝업 제목"),
					fieldWithPath("popUps[].thumbnailImageUrl").type(JsonFieldType.STRING)
						.description("썸네일 이미지 URL"),
					fieldWithPath("popUps[].linkUrl").type(JsonFieldType.STRING)
						.description("팝업 링크 URL"),
					fieldWithPath("totalCount").type(JsonFieldType.NUMBER)
						.description("전체 팝업 개수")
				),

				resource(builder()
					.tag("🔥 팝업 관련 API")
					.summary("팝업 전체 조회")
					.description("현재 등록된 모든 팝업 목록을 조회합니다.")
					.build()
				)
			));
	}
}

