package com.motd.be.module.member.story.controller;

import static com.motd.be.Constants.*;
import static com.motd.be.common.constants.PageSizeConstants.*;
import static com.motd.be.provider.module.member.MemberTokenProvider.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.exception.exceptions.StoryException;
import com.motd.be.module.member.jwt.Jwt;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.story.dto.response.StoryFindAllResponse;
import com.motd.be.module.member.story.dto.response.StoryFindDetailResponse;
import com.motd.be.module.member.story.dto.response.StoryResponse;
import com.motd.be.module.member.story.entity.Story;

import jakarta.servlet.http.Cookie;

@ControllerIntegrationTest
public class ServiceControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("스토리 목록을 정상적으로 조회할 수 있다.")
	void findAll_success() throws Exception {
		// given
		Story story1 = storyProvider.save(0);
		Story story2 = storyProvider.save(1);
		Story story3 = storyProvider.save(2);

		entityManager.flush();
		entityManager.clear();

		// when & then
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/stories")
				.param(PAGE_STR, ZERO_STR)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		StoryFindAllResponse response = objectMapper.readValue(responseJson, StoryFindAllResponse.class);

		assertThat(response.getPage()).isEqualTo(0);
		assertThat(response.getStories()).hasSize(3);

		List<Long> storyIds = response.getStories().stream()
			.map(StoryResponse::getId)
			.toList();

		assertThat(storyIds).containsExactly(story1.getId(), story2.getId(), story3.getId());
	}

	@Test
	@DisplayName("스토리 목록을 정상적으로 조회할 수 있다. (페이지네이션 적용)")
	void findAll_withPagination() throws Exception {
		// given
		for (int i = 1; i <= 15; i++) {
			storyProvider.save(i);
		}

		entityManager.flush();
		entityManager.clear();

		// when & then - 첫 번째 페이지
		String firstPageJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/stories")
				.param(PAGE_STR, ZERO_STR)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		StoryFindAllResponse firstPage = objectMapper.readValue(firstPageJson, StoryFindAllResponse.class);

		assertThat(firstPage.getPage()).isEqualTo(0);
		assertThat(firstPage.getHasNext()).isTrue();
		assertThat(firstPage.getStories()).hasSize(STORY_FIND_ALL_SIZE);

		// when & then - 두 번째 페이지
		String secondPageJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/stories")
				.param(PAGE_STR, "1")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		StoryFindAllResponse secondPage = objectMapper.readValue(secondPageJson, StoryFindAllResponse.class);

		assertThat(secondPage.getPage()).isEqualTo(1);
		assertThat(secondPage.getHasNext()).isFalse();
		assertThat(secondPage.getStories()).hasSize(5);
	}

	@Test
	@DisplayName("스토리 상세를 정상적으로 조회할 수 있다.")
	void findDetail_success() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		Story story = storyProvider.save(0);

		entityManager.flush();
		entityManager.clear();

		// when & then
		String responseJson = mockMvc.perform(MockMvcRequestBuilders.get("/api/stories/{storyId}", story.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		StoryFindDetailResponse response = objectMapper.readValue(responseJson, StoryFindDetailResponse.class);

		assertThat(response.getId()).isEqualTo(story.getId());
	}

	@Test
	@DisplayName("스토리 상세를 정상적으로 조회할 수 있다. (존재하지 않을떄)")
	void findDetail_notFound() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.get("/api/stories/{storyId}", 999999L)
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath(ERROR_STATUS).value(StoryException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(StoryException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(StoryException.NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("스토리 상세를 정상적으로 조회할 수 있다. (삭제되어 있을때)")
	void findDetail_Deleted() throws Exception {
		// given
		Member member = memberProvider.saveMember(SignInPlatform.KAKAO);
		Jwt jwt = generateTokenWithMemberIdRoleMember(member.getId());

		Story story = storyProvider.saveWithIsDeletedTrue(0);

		entityManager.flush();
		entityManager.clear();

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.get("/api/stories/{storyId}", story.getId())
				.cookie(new Cookie(ACCESS_TOKEN_STR, jwt.getAccessToken()))
				.cookie(new Cookie(REFRESH_TOKEN_STR, jwt.getRefreshToken()))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath(ERROR_STATUS).value(StoryException.NOT_FOUND.getHttpStatus().toString()))
			.andExpect(jsonPath(ERROR_MESSAGE).value(StoryException.NOT_FOUND.getErrorMessage()))
			.andExpect(jsonPath(ERROR_CODE).value(StoryException.NOT_FOUND.getCode()));
	}
}
