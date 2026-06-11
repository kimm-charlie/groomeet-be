package com.motd.be.module.member.story.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.story.entity.Story;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoryFindAllResponse {

	private int page;
	private Boolean hasNext;
	private List<StoryResponse> stories;

	public static StoryFindAllResponse from(Slice<Story> stories) {
		return StoryFindAllResponse.builder()
			.page(stories.getNumber())
			.hasNext(stories.hasNext())
			.stories(StoryResponse.fromList(stories.getContent()))
			.build();
	}
}
