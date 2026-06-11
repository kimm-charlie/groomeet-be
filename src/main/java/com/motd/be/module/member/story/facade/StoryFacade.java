package com.motd.be.module.member.story.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.story.dto.response.StoryFindAllResponse;
import com.motd.be.module.member.story.dto.response.StoryFindDetailResponse;
import com.motd.be.module.member.story.service.StoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoryFacade {

	private final StoryService storyService;

	public StoryFindAllResponse findAll(int page) {
		return storyService.findAll(page);
	}

	public StoryFindDetailResponse findDetail(Long memberId, Long storyId) {
		//todo 추후에 memberId 를 통한 조회수 구현
		return storyService.findDetail(storyId);
	}
}
