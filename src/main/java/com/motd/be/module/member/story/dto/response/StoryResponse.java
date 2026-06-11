package com.motd.be.module.member.story.dto.response;

import static com.motd.be.common.utils.DateFormatUtils.*;

import java.util.List;

import com.motd.be.module.member.story.entity.Story;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoryResponse {

	private Long id;
	private String title;
	private String thumbnailImageUrl;
	private String createdAt;

	public static List<StoryResponse> fromList(List<Story> stories) {
		return stories.stream()
			.map(StoryResponse::from)
			.toList();
	}

	public static StoryResponse from(Story story) {
		return StoryResponse.builder()
			.id(story.getId())
			.title(story.getTitle())
			.thumbnailImageUrl(story.getThumbnailImageCdnUrl())
			.createdAt(formatToDateString(story.getCreatedAt()))
			.build();
	}
}
