package com.motd.be.module.member.story.controller;

import static com.motd.be.common.constants.Constants.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motd.be.module.member.story.dto.response.StoryFindAllResponse;
import com.motd.be.module.member.story.dto.response.StoryFindDetailResponse;
import com.motd.be.module.member.story.facade.StoryFacade;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class StoryController {

	private final StoryFacade storyFacade;

	@GetMapping("/stories")
	public ResponseEntity<StoryFindAllResponse> findAll(
		@RequestParam(name = PAGE, required = false, defaultValue = ZERO) int page) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(storyFacade.findAll(page));
	}

	@GetMapping("/stories/{storyId}")
	public ResponseEntity<StoryFindDetailResponse> findDetail(@AuthenticationPrincipal Long memberId,
		@PathVariable(STORY_ID) Long storyId) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(storyFacade.findDetail(memberId, storyId));
	}
}
