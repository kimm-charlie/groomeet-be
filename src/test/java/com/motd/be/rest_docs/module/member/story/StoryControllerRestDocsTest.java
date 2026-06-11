package com.motd.be.rest_docs.module.member.story;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static com.epages.restdocs.apispec.ResourceSnippetParameters.*;
import static com.motd.be.Constants.*;
import static com.motd.be.common.utils.DateFormatUtils.*;
import static com.motd.be.rest_docs.Utils.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.member.story.dto.response.StoryFindAllResponse;
import com.motd.be.module.member.story.dto.response.StoryFindDetailResponse;
import com.motd.be.module.member.story.dto.response.StoryResponse;

@RestDocsTest
public class StoryControllerRestDocsTest extends BaseRestDocsTest {

	@Test
	void 스토리_전체_조회() throws Exception {
		StoryFindAllResponse response = StoryFindAllResponse.builder()
			.page(0)
			.hasNext(true)
			.stories(Arrays.asList(
				StoryResponse.builder()
					.id(1L)
					.title("첫 번째 스토리")
					.thumbnailImageUrl("https://cdn.example.com/thumbnail1.jpg")
					.createdAt(formatToDateString(LocalDateTime.now().minusDays(1)))
					.build(),
				StoryResponse.builder()
					.id(2L)
					.title("두 번째 스토리")
					.thumbnailImageUrl("https://cdn.example.com/thumbnail2.jpg")
					.createdAt(formatToDateString(LocalDateTime.now().minusDays(2)))
					.build(),
				StoryResponse.builder()
					.id(3L)
					.title("세 번째 스토리")
					.thumbnailImageUrl("https://cdn.example.com/thumbnail3.jpg")
					.createdAt(formatToDateString(LocalDateTime.now().minusDays(3)))
					.build()
			))
			.build();

		given(storyFacade.findAll(anyInt())).willReturn(response);

		mockMvc.perform(get("/api/stories")
				.param(PAGE_STR, ZERO_STR)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("story-findAll",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				queryParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("page")
						.optional()
						.description("페이지 번호 (0부터 시작)")
				),

				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
					fieldWithPath("stories").type(JsonFieldType.ARRAY).description("스토리 목록"),
					fieldWithPath("stories[].id").type(JsonFieldType.NUMBER).description("스토리 ID"),
					fieldWithPath("stories[].title").type(JsonFieldType.STRING).description("스토리 제목"),
					fieldWithPath("stories[].thumbnailImageUrl").type(JsonFieldType.STRING).description("썸네일 이미지 URL"),
					fieldWithPath("stories[].createdAt").type(JsonFieldType.STRING)
						.attributes(getDateTimeFormat())
						.description("생성일시")
				),

				resource(builder()
					.tag("📖 스토리 API")
					.summary("스토리 목록 조회")
					.description("스토리 목록을 페이지네이션으로 조회합니다.")
					.build())
			));
	}

	@Test
	void 스토리_상세_조회() throws Exception {
		authenticationSetUp();
		
		StoryFindDetailResponse response = StoryFindDetailResponse.builder()
			.id(1L)
			.title("스토리 상세 제목")
			.contentImageUrl("https://cdn.example.com/content1.jpg")
			.build();

		given(storyFacade.findDetail(anyLong(), anyLong())).willReturn(response);

		mockMvc.perform(get("/api/stories/{storyId}", 1L)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("story-findDetail",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				pathParameters(
					org.springframework.restdocs.request.RequestDocumentation.parameterWithName("storyId")
						.description("스토리 ID")
				),

				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER).description("스토리 ID"),
					fieldWithPath("title").type(JsonFieldType.STRING).description("스토리 제목"),
					fieldWithPath("contentImageUrl").type(JsonFieldType.STRING).description("컨텐츠 이미지 URL")
				),

				resource(builder()
					.tag("📖 스토리 API")
					.summary("스토리 상세 조회")
					.description("특정 스토리의 상세 정보를 조회합니다.")
					.build())
			));
	}
}
