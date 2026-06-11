package com.motd.be.module.member.story.service;

import static com.motd.be.common.constants.PageSizeConstants.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.motd.be.module.member.story.dto.response.StoryFindAllResponse;
import com.motd.be.module.member.story.dto.response.StoryFindDetailResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoryService {

	private final StoryQueryService storyQueryService;

	public StoryFindAllResponse findAll(int page) {
		Pageable pageable = PageRequest.of(page, STORY_FIND_ALL_SIZE);

		return StoryFindAllResponse.from(storyQueryService.findAll(pageable));
	}

	public StoryFindDetailResponse findDetail(Long storyId) {
		return StoryFindDetailResponse.from(storyQueryService.findById(storyId));
	}
}
