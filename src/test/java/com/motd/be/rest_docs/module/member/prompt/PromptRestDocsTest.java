package com.motd.be.rest_docs.module.member.prompt;

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
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.motd.be.BaseRestDocsTest;
import com.motd.be.annotation.RestDocsTest;
import com.motd.be.module.member.prompt.dto.request.PromptGenerateRequest;
import com.motd.be.module.member.prompt.dto.request.PromptServiceRecommendRequest;
import com.motd.be.module.member.prompt.dto.response.PromptGenerateResponse;
import com.motd.be.module.member.prompt.dto.response.PromptServiceRecommendResponse;
import com.motd.be.module.member.prompt.dto.response.PromptServiceRecommendResponse.ServiceRecommendation;

import jakarta.servlet.http.Cookie;

@RestDocsTest
public class PromptRestDocsTest extends BaseRestDocsTest {

	@Test
	void 서비스_추천_성공_텍스트만() throws Exception {
		authenticationSetUp();

		PromptServiceRecommendRequest request = PromptServiceRecommendRequest.builder()
			.prompt("요즘 머리가 많이 빠져서 고민이에요")
			.build();

		PromptServiceRecommendResponse response = PromptServiceRecommendResponse.ofMatched(
			1L,
			Arrays.asList(
				ServiceRecommendation.builder()
					.serviceId(1L)
					.serviceName("탈모 관리")
					.build(),
				ServiceRecommendation.builder()
					.serviceId(5L)
					.serviceName("두피 케어")
					.build()
			)
		);

		given(promptFacade.recommendServices(anyLong(), any(PromptServiceRecommendRequest.class)))
			.willReturn(response);

		mockMvc.perform(post("/api/prompt/recommend")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andDo(document("prompt-recommend-success-text-only",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("roomId")
						.type(JsonFieldType.NUMBER)
						.optional()
						.description("대화방 ID (첫 요청 시 null, 후속 턴에서 이전 응답의 roomId 전달)"),
					fieldWithPath("prompt")
						.type(JsonFieldType.STRING)
						.description("사용자의 고민/요청 텍스트"),
					fieldWithPath("fileIds")
						.type(JsonFieldType.ARRAY)
						.optional()
						.description("첨부 이미지 파일 ID 목록 (최대 3개)")
				),

				responseFields(
					fieldWithPath("roomId").type(JsonFieldType.NUMBER)
						.description("대화방 ID (후속 턴에서 재사용)"),
					fieldWithPath("matched").type(JsonFieldType.BOOLEAN)
						.description("서비스 매칭 성공 여부"),
					fieldWithPath("message").type(JsonFieldType.STRING)
						.optional()
						.description("matched=false 일 때 AI 안내 메시지"),
					fieldWithPath("recommendations").type(JsonFieldType.ARRAY)
						.description("추천 서비스 목록 (최대 3개)"),
					fieldWithPath("recommendations[].serviceId").type(JsonFieldType.NUMBER)
						.description("추천 서비스 ID"),
					fieldWithPath("recommendations[].serviceName").type(JsonFieldType.STRING)
						.description("추천 서비스명")
				),

				resource(builder()
					.tag("🤖 AI 프롬프트 API")
					.summary("서비스 추천 (텍스트만)")
					.description("사용자의 고민을 기반으로 적합한 서비스를 최대 3개 추천합니다.")
					.build())
			));
	}

	@Test
	void 서비스_추천_성공_이미지포함() throws Exception {
		authenticationSetUp();

		PromptServiceRecommendRequest request = PromptServiceRecommendRequest.builder()
			.prompt("이 상태 어떻게 하면 좋을까요?")
			.fileIds(Arrays.asList(1L, 2L))
			.build();

		PromptServiceRecommendResponse response = PromptServiceRecommendResponse.ofMatched(
			1L,
			List.of(
				ServiceRecommendation.builder()
					.serviceId(1L)
					.serviceName("탈모 관리")
					.build()
			)
		);

		given(promptFacade.recommendServices(anyLong(), any(PromptServiceRecommendRequest.class)))
			.willReturn(response);

		mockMvc.perform(post("/api/prompt/recommend")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andDo(document("prompt-recommend-success-with-images",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("roomId")
						.type(JsonFieldType.NUMBER)
						.optional()
						.description("대화방 ID (첫 요청 시 null, 후속 턴에서 이전 응답의 roomId 전달)"),
					fieldWithPath("prompt")
						.type(JsonFieldType.STRING)
						.description("사용자의 고민/요청 텍스트"),
					fieldWithPath("fileIds")
						.type(JsonFieldType.ARRAY)
						.description("첨부 이미지 파일 ID 목록 (최대 3개)")
				),

				responseFields(
					fieldWithPath("roomId").type(JsonFieldType.NUMBER)
						.description("대화방 ID (후속 턴에서 재사용)"),
					fieldWithPath("matched").type(JsonFieldType.BOOLEAN)
						.description("서비스 매칭 성공 여부"),
					fieldWithPath("message").type(JsonFieldType.STRING)
						.optional()
						.description("matched=false 일 때 AI 안내 메시지"),
					fieldWithPath("recommendations").type(JsonFieldType.ARRAY)
						.description("추천 서비스 목록 (최대 3개)"),
					fieldWithPath("recommendations[].serviceId").type(JsonFieldType.NUMBER)
						.description("추천 서비스 ID"),
					fieldWithPath("recommendations[].serviceName").type(JsonFieldType.STRING)
						.description("추천 서비스명")
				),

				resource(builder()
					.tag("🤖 AI 프롬프트 API")
					.summary("서비스 추천 (이미지 포함)")
					.description("사용자의 고민과 첨부 이미지를 기반으로 적합한 서비스를 추천합니다.")
					.build())
			));
	}

	@Test
	void 서비스_추천_매칭실패() throws Exception {
		authenticationSetUp();

		PromptServiceRecommendRequest request = PromptServiceRecommendRequest.builder()
			.prompt("오늘 날씨가 좋네요")
			.build();

		PromptServiceRecommendResponse response = PromptServiceRecommendResponse.ofUnmatched(
			1L,
			"현재 저희가 제공하는 서비스는 두피/모발 관리, 피부 케어 등이 있습니다. 관련된 고민이 있으시면 다시 질문해 주세요!");

		given(promptFacade.recommendServices(anyLong(), any(PromptServiceRecommendRequest.class)))
			.willReturn(response);

		mockMvc.perform(post("/api/prompt/recommend")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andDo(document("prompt-recommend-unmatched",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("roomId")
						.type(JsonFieldType.NUMBER)
						.optional()
						.description("대화방 ID (첫 요청 시 null, 후속 턴에서 이전 응답의 roomId 전달)"),
					fieldWithPath("prompt")
						.type(JsonFieldType.STRING)
						.description("사용자의 고민/요청 텍스트"),
					fieldWithPath("fileIds")
						.type(JsonFieldType.ARRAY)
						.optional()
						.description("첨부 이미지 파일 ID 목록 (최대 3개)")
				),

				responseFields(
					fieldWithPath("roomId").type(JsonFieldType.NUMBER)
						.description("대화방 ID (후속 턴에서 재사용)"),
					fieldWithPath("matched").type(JsonFieldType.BOOLEAN)
						.description("서비스 매칭 성공 여부"),
					fieldWithPath("message").type(JsonFieldType.STRING)
						.description("AI가 생성한 안내 메시지"),
					fieldWithPath("recommendations").type(JsonFieldType.ARRAY)
						.description("빈 배열")
				),

				resource(builder()
					.tag("🤖 AI 프롬프트 API")
					.summary("서비스 추천 (매칭 실패)")
					.description("서비스와 무관한 프롬프트인 경우 안내 메시지를 반환합니다.")
					.build())
			));
	}

	@Test
	void 요청서_생성_성공() throws Exception {
		authenticationSetUp();

		PromptGenerateRequest request = PromptGenerateRequest.builder()
			.prompt("요즘 머리가 많이 빠져서 자연스럽게 관리받고 싶어요")
			.directorServiceId(1L)
			.locationIds(Arrays.asList(10L, 11L))
			.fileIds(Arrays.asList(1L, 2L))
			.build();

		PromptGenerateResponse response = PromptGenerateResponse.from(
			1L,
			"안녕하세요, 최근 탈모가 진행되고 있어서 자연스러운 관리를 받고 싶습니다. " +
				"사진에서 보시는 것처럼 앞머리 쪽이 많이 얇아진 상태인데, " +
				"어느 정도 기간이 필요할지 상담 부탁드립니다. " +
				"시술 방법이나 관리 주기에 대해서도 안내해 주시면 감사하겠습니다.");

		given(promptFacade.generateRequest(anyLong(), any(PromptGenerateRequest.class)))
			.willReturn(response);

		mockMvc.perform(post("/api/prompt/generate")
				.cookie(
					new Cookie(ACCESS_TOKEN_STR, ACCESS_TOKEN_STR),
					new Cookie(REFRESH_TOKEN_STR, REFRESH_TOKEN_STR)
				)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andDo(document("prompt-generate-success",
				getRequestPreProcessor(),
				getResponsePreProcessor(),

				requestCookies(
					cookieWithName(ACCESS_TOKEN_STR).description("HttpOnly accessToken 쿠키"),
					cookieWithName(REFRESH_TOKEN_STR).description("HttpOnly refreshToken 쿠키")
				),

				requestFields(
					fieldWithPath("roomId")
						.type(JsonFieldType.NUMBER)
						.optional()
						.description("대화방 ID (recommend 후 전달받은 roomId, null이면 새 방 생성)"),
					fieldWithPath("prompt")
						.type(JsonFieldType.STRING)
						.description("AI에게 보낼 텍스트 프롬프트"),
					fieldWithPath("directorServiceId")
						.type(JsonFieldType.NUMBER)
						.description("디렉터 서비스 ID"),
					fieldWithPath("locationIds")
						.type(JsonFieldType.ARRAY)
						.description("희망 지역 ID 목록 (최소 1개, 최대 3개)"),
					fieldWithPath("fileIds")
						.type(JsonFieldType.ARRAY)
						.optional()
						.description("첨부 이미지 파일 ID 목록 (최대 3개)")
				),

				responseFields(
					fieldWithPath("roomId").type(JsonFieldType.NUMBER)
						.description("대화방 ID"),
					fieldWithPath("aiContent").type(JsonFieldType.STRING)
						.description("AI가 생성한 요청서 본문")
				),

				resource(builder()
					.tag("🤖 AI 프롬프트 API")
					.summary("요청서 생성")
					.description("추천된 서비스 기반으로 AI가 요청서를 자동 생성합니다.")
					.build())
			));
	}
}
