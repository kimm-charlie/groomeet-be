package com.motd.be.module.member.story.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.StoryException;
import com.motd.be.module.member.story.entity.Story;
import com.motd.be.module.member.story.repository.StoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoryQueryService {

	private final StoryRepository storyRepository;

	public Slice<Story> findAll(Pageable pageable) {
		return storyRepository.findAllWithPageable(pageable);
	}

	public Story findById(Long storyId) {
		return storyRepository.findByIdAndIsDeletedFalse(storyId).orElseThrow(() -> new CustomRuntimeException(
			StoryException.NOT_FOUND));
	}
}
