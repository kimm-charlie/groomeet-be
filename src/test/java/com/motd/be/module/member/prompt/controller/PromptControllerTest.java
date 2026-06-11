package com.motd.be.module.member.prompt.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.ValidationMessages.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.AiProviderException;
import com.motd.be.exception.exceptions.HandlerException;
import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.entity.LocationType;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.prompt.dto.request.PromptGenerateRequest;
import com.motd.be.module.member.prompt.dto.request.PromptServiceRecommendRequest;
import com.motd.be.module.member.prompt.dto.response.PromptGenerateResponse;
import com.motd.be.module.member.prompt.dto.response.PromptServiceRecommendResponse;
import com.motd.be.module.member.prompt.entity.PromptMessage;
import com.motd.be.module.member.prompt.entity.PromptMessageRole;
import com.motd.be.module.member.prompt.entity.PromptRoom;
import com.motd.be.module.member.service_request_file.entity.ServiceRequestFile;
import com.motd.be.shared.ai.dto.response.AiChatCompletionResponse;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class PromptControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("서비스 추천이 가능하다 (텍스트만 정상 요청)")
	void 서비스_추천이_가능하다_텍스트만_정상_요청() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);

		PromptServiceRecommendRequest request = PromptServiceRecommendRequest.builder()
			.prompt("요즘 머리가 많이 빠져서 고민이에요")
			.build();

		String aiResponse = "{\"matched\":true,\"recommendations\":[{\"serviceId\":1,\"serviceName\":\"탈모 관리\",\"reason\":\"탈모 초기 관리가 적합합니다.\"}]}";
		mockAiChatResponse(aiResponse);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/prompt/recommend")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andReturn();

		// then
		PromptServiceRecommendResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(), PromptServiceRecommendResponse.class);
		assertThat(response.getRoomId()).isNotNull();
		assertThat(response.isMatched()).isTrue();
		assertThat(response.getRecommendations()).isNotEmpty();

		List<PromptRoom> rooms = promptRoomProvider.findAll();
		assertThat(rooms).hasSize(1);
		assertThat(rooms.get(0).getTurnCount()).isEqualTo(1);
		assertThat(rooms.get(0).getIsServiceRecommendSuccess()).isTrue();

		List<PromptMessage> messages = promptMessageProvider.findAll();
		assertThat(messages).hasSize(2);
		assertThat(messages.get(0).getRole()).isEqualTo(PromptMessageRole.USER);
		assertThat(messages.get(1).getRole()).isEqualTo(PromptMessageRole.ASSISTANT);
	}

	@Test
	@DisplayName("서비스 추천이 가능하다 (이미지 포함)")
	void 서비스_추천이_가능하다_이미지_포함() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);

		ServiceRequestFile image1 = serviceRequestFileProvider.saveWithoutServiceRequest(member);
		ServiceRequestFile image2 = serviceRequestFileProvider.saveWithoutServiceRequest(member);

		PromptServiceRecommendRequest request = PromptServiceRecommendRequest.builder()
			.prompt("이 상태 어떻게 하면 좋을까요?")
			.fileIds(Arrays.asList(image1.getId(), image2.getId()))
			.build();

		String aiResponse = "{\"matched\":true,\"recommendations\":[{\"serviceId\":1,\"serviceName\":\"탈모 관리\",\"reason\":\"이미지 분석 결과 탈모 초기 증상이 보입니다.\"}]}";
		mockAiChatResponse(aiResponse);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/prompt/recommend")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andReturn();

		// then
		PromptServiceRecommendResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(), PromptServiceRecommendResponse.class);
		assertThat(response.getRoomId()).isNotNull();
		assertThat(response.isMatched()).isTrue();
	}

	@Test
	@DisplayName("서비스 추천이 가능하다 (매칭 실패)")
	void 서비스_추천이_가능하다_매칭_실패() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);

		PromptServiceRecommendRequest request = PromptServiceRecommendRequest.builder()
			.prompt("오늘 날씨가 좋네요")
			.build();

		String aiResponse = "{\"matched\":false,\"message\":\"현재 저희가 제공하는 서비스는 두피/모발 관리 등이 있습니다. 관련된 고민이 있으시면 다시 질문해 주세요!\"}";
		mockAiChatResponse(aiResponse);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/prompt/recommend")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andReturn();

		// then
		PromptServiceRecommendResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(), PromptServiceRecommendResponse.class);
		assertThat(response.getRoomId()).isNotNull();
		assertThat(response.isMatched()).isFalse();
		assertThat(response.getMessage()).isNotEmpty();

		List<PromptRoom> rooms = promptRoomProvider.findAll();
		assertThat(rooms).hasSize(1);
		assertThat(rooms.get(0).getIsServiceRecommendSuccess()).isFalse();
	}

	@Test
	@DisplayName("서비스 추천이 가능하다 (fileIds 4개 이상이면 400)")
	void 서비스_추천이_가능하다_fileIds_4개_이상이면_400() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		PromptServiceRecommendRequest request = PromptServiceRecommendRequest.builder()
			.prompt("요즘 머리가 많이 빠져서 고민이에요")
			.fileIds(Arrays.asList(1L, 2L, 3L, 4L))
			.build();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/prompt/recommend")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(PROMPT_FILE_IDS_SIZE))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("서비스 추천이 가능하다 (멀티턴 대화)")
	void 서비스_추천이_가능하다_멀티턴_대화() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);

		// 첫 번째 턴 - 정보 부족
		PromptServiceRecommendRequest firstRequest = PromptServiceRecommendRequest.builder()
			.prompt("고민이 있어요")
			.build();

		String firstAiResponse = "{\"matched\":false,\"message\":\"더 자세히 알려주세요\"}";
		mockAiChatResponse(firstAiResponse);

		entityManager.flush();
		entityManager.clear();

		MvcResult firstResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/prompt/recommend")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(firstRequest)))
			.andExpect(status().isOk())
			.andReturn();

		PromptServiceRecommendResponse firstResponse = objectMapper.readValue(
			firstResult.getResponse().getContentAsString(), PromptServiceRecommendResponse.class);
		Long roomId = firstResponse.getRoomId();
		assertThat(roomId).isNotNull();
		assertThat(firstResponse.isMatched()).isFalse();

		// 두 번째 턴 - 추가 정보 제공
		PromptServiceRecommendRequest secondRequest = PromptServiceRecommendRequest.builder()
			.roomId(roomId)
			.prompt("머리가 많이 빠져서 두피 관리를 받고 싶어요")
			.build();

		String secondAiResponse = "{\"matched\":true,\"recommendations\":[{\"serviceId\":1,\"serviceName\":\"탈모 관리\"}]}";
		mockAiChatResponse(secondAiResponse);

		// when
		MvcResult secondResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/prompt/recommend")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(secondRequest)))
			.andExpect(status().isOk())
			.andReturn();

		// then
		PromptServiceRecommendResponse secondResponse = objectMapper.readValue(
			secondResult.getResponse().getContentAsString(), PromptServiceRecommendResponse.class);
		assertThat(secondResponse.getRoomId()).isEqualTo(roomId);
		assertThat(secondResponse.isMatched()).isTrue();

		List<PromptRoom> rooms = promptRoomProvider.findAll();
		assertThat(rooms).hasSize(1);
		assertThat(rooms.get(0).getTurnCount()).isEqualTo(2);

		List<PromptMessage> messages = promptMessageProvider.findAll();
		assertThat(messages).hasSize(4);
	}

	@Test
	@DisplayName("서비스 추천이 가능하다 (recommend → generate 전체 흐름)")
	void 서비스_추천이_가능하다_recommend에서_generate까지_전체_흐름() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService childService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);

		Location city = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location district = locationProvider.saveWithParent(LOCATION_NAME_2_STR, LocationType.DISTRICT, city);

		// recommend 턴
		PromptServiceRecommendRequest recommendRequest = PromptServiceRecommendRequest.builder()
			.prompt("머리가 많이 빠져서 고민이에요")
			.build();

		String recommendAiResponse = "{\"matched\":true,\"recommendations\":[{\"serviceId\":" + childService.getId() + ",\"serviceName\":\"" + SERVICE_NAME_2_STR + "\"}]}";
		mockAiChatResponse(recommendAiResponse);

		entityManager.flush();
		entityManager.clear();

		MvcResult recommendResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/prompt/recommend")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(recommendRequest)))
			.andExpect(status().isOk())
			.andReturn();

		PromptServiceRecommendResponse recommendResponse = objectMapper.readValue(
			recommendResult.getResponse().getContentAsString(), PromptServiceRecommendResponse.class);
		Long roomId = recommendResponse.getRoomId();

		// generate 턴 - 같은 roomId 사용
		PromptGenerateRequest generateRequest = PromptGenerateRequest.builder()
			.roomId(roomId)
			.prompt("자연스럽게 관리받고 싶어요")
			.directorServiceId(childService.getId())
			.locationIds(List.of(district.getId()))
			.build();

		String generateAiContent = "안녕하세요, 최근 탈모가 진행되고 있어서 자연스러운 관리를 받고 싶습니다.";
		mockAiChatResponse(generateAiContent);

		// when
		MvcResult generateResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/prompt/generate")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(generateRequest)))
			.andExpect(status().isOk())
			.andReturn();

		// then
		PromptGenerateResponse generateResponse = objectMapper.readValue(
			generateResult.getResponse().getContentAsString(), PromptGenerateResponse.class);
		assertThat(generateResponse.getRoomId()).isEqualTo(roomId);
		assertThat(generateResponse.getAiContent()).isNotEmpty();

		List<PromptRoom> rooms = promptRoomProvider.findAll();
		assertThat(rooms).hasSize(1);
		assertThat(rooms.get(0).getTurnCount()).isEqualTo(2);
		assertThat(rooms.get(0).getDirectorService()).isNotNull();

		List<PromptMessage> messages = promptMessageProvider.findAll();
		assertThat(messages).hasSize(4);
	}

	@Test
	@DisplayName("서비스 추천이 가능하다 (턴 제한 초과 시 400)")
	void 서비스_추천이_가능하다_턴_제한_초과_시_400() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);

		PromptRoom room = promptRoomProvider.save(member);
		for (int i = 0; i < 10; i++) {
			room.incrementTurnCount();
		}
		entityManager.flush();
		entityManager.clear();

		PromptServiceRecommendRequest request = PromptServiceRecommendRequest.builder()
			.roomId(room.getId())
			.prompt("추가 질문입니다")
			.build();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/prompt/recommend")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_MESSAGE).value(AiProviderException.MAX_TURNS_EXCEEDED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(AiProviderException.MAX_TURNS_EXCEEDED.getCode()));
	}

	@Test
	@DisplayName("서비스 추천이 가능하다 (다른 사용자 방 접근 시 403)")
	void 서비스_추천이_가능하다_다른_사용자_방_접근_시_403() throws Exception {
		// given
		Member member1 = memberProvider.saveMember(SignInPlatform.KAKAO);
		Member member2 = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt2 = generateTokenWithMemberIdRoleMember(member2.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);

		PromptRoom room = promptRoomProvider.save(member1);

		entityManager.flush();
		entityManager.clear();

		PromptServiceRecommendRequest request = PromptServiceRecommendRequest.builder()
			.roomId(room.getId())
			.prompt("다른 사용자 방에 접근")
			.build();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/prompt/recommend")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt2.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt2.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath(ERROR_MESSAGE).value(AiProviderException.ROOM_ACCESS_DENIED.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(AiProviderException.ROOM_ACCESS_DENIED.getCode()));
	}

	@Test
	@DisplayName("요청서 생성이 가능하다 (정상)")
	void 요청서_생성이_가능하다_정상() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		DirectorService parentService = directorCategoryProvider.save(SERVICE_NAME_1_STR, null);
		DirectorService childService = directorCategoryProvider.save(SERVICE_NAME_2_STR, parentService);

		Location city = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location district = locationProvider.saveWithParent(LOCATION_NAME_2_STR, LocationType.DISTRICT, city);

		PromptGenerateRequest request = PromptGenerateRequest.builder()
			.prompt("요즘 머리가 많이 빠져서 자연스럽게 관리받고 싶어요")
			.directorServiceId(childService.getId())
			.locationIds(List.of(district.getId()))
			.build();

		String aiContent = "안녕하세요, 최근 탈모가 진행되고 있어서 자연스러운 관리를 받고 싶습니다.";
		mockAiChatResponse(aiContent);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/prompt/generate")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andReturn();

		// then
		PromptGenerateResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(), PromptGenerateResponse.class);
		assertThat(response.getRoomId()).isNotNull();
		assertThat(response.getAiContent()).isNotEmpty();

		List<PromptRoom> rooms = promptRoomProvider.findAll();
		assertThat(rooms).hasSize(1);
		assertThat(rooms.get(0).getTurnCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("요청서 생성이 가능하다 (존재하지 않는 directorServiceId)")
	void 요청서_생성이_가능하다_존재하지_않는_directorServiceId() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		Location city = locationProvider.save(LOCATION_NAME_1_STR, LocationType.CITY);
		Location district = locationProvider.saveWithParent(LOCATION_NAME_2_STR, LocationType.DISTRICT, city);

		PromptGenerateRequest request = PromptGenerateRequest.builder()
			.prompt("요즘 머리가 많이 빠져서 자연스럽게 관리받고 싶어요")
			.directorServiceId(9999L)
			.locationIds(List.of(district.getId()))
			.build();

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/prompt/generate")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("요청서 생성이 가능하다 (fileIds 4개 이상이면 400)")
	void 요청서_생성이_가능하다_fileIds_4개_이상이면_400() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		PromptGenerateRequest request = PromptGenerateRequest.builder()
			.prompt("요즘 머리가 많이 빠져서 자연스럽게 관리받고 싶어요")
			.directorServiceId(1L)
			.locationIds(List.of(1L))
			.fileIds(Arrays.asList(1L, 2L, 3L, 4L))
			.build();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/prompt/generate")
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath(ERROR_STATUS).value(HandlerException.ARGUMENT_NOT_VALID.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(PROMPT_FILE_IDS_SIZE))
			.andExpect(jsonPath(ERROR_CODE).value(HandlerException.ARGUMENT_NOT_VALID.getCode()));
	}

	@Test
	@DisplayName("비로그인 접근 시 401")
	void 비로그인_접근_시_401() throws Exception {
		PromptServiceRecommendRequest request = PromptServiceRecommendRequest.builder()
			.prompt("요즘 머리가 많이 빠져서 고민이에요")
			.build();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.post("/api/prompt/recommend")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized());
	}

	private void mockAiChatResponse(String content) {
		AiChatCompletionResponse response = AiChatCompletionResponse.builder()
			.id("chatcmpl-test")
			.model("gpt-test")
			.choices(List.of(
				AiChatCompletionResponse.Choice.builder()
					.index(0)
					.message(AiChatCompletionResponse.Message.builder()
						.role("assistant")
						.content(content)
						.build())
					.finishReason("stop")
					.build()
			))
			.usage(AiChatCompletionResponse.Usage.builder()
				.promptTokens(100)
				.completionTokens(50)
				.totalTokens(150)
				.build())
			.build();

		given(aiChatProvider.chat(any())).willReturn(response);
	}
}
