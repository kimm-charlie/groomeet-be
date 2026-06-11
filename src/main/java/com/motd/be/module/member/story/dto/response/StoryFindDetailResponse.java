package com.motd.be.module.member.story.dto.response;

import com.motd.be.module.member.story.entity.Story;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoryFindDetailResponse {

	private Long id;
	private String title;
	private String contentImageUrl;

	public static StoryFindDetailResponse from(Story story) {
		return StoryFindDetailResponse.builder()
			.id(story.getId())
			.title(story.getTitle())
			.contentImageUrl(story.getContentImageCdnUrl())
			.build();
	}
}
