package com.motd.be.provider.module.member;

import static com.motd.be.Constants.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.story.entity.Story;
import com.motd.be.module.member.story.repository.StoryRepository;

@Component
public class StoryProvider {

	@Autowired
	private StoryRepository storyRepository;

	public Story save(int sortOrder) {
		return storyRepository.save(Story.builder()
			.title(TITLE_STR)
			.thumbnailImageUrl(THUMBNAIL_IMAGE_URL)
			.thumbnailImageCdnUrl(CDN_URL_STR)
			.contentImageUrl(CONTENT_IMAGE_URL)
			.contentImageCdnUrl(CDN_URL_STR)
			.sortOrder(sortOrder)
			.build());
	}

	public Story saveWithIsDeletedTrue(int sortOrder) {
		return storyRepository.save(Story.builder()
			.title(TITLE_STR)
			.thumbnailImageUrl(THUMBNAIL_IMAGE_URL)
			.thumbnailImageCdnUrl(CDN_URL_STR)
			.contentImageUrl(CONTENT_IMAGE_URL)
			.contentImageCdnUrl(CDN_URL_STR)
			.sortOrder(sortOrder)
			.isDeleted(Boolean.TRUE)
			.build());
	}
}
