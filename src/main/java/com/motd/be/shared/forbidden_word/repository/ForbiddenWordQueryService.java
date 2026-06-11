package com.motd.be.shared.forbidden_word.repository;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.shared.forbidden_word.entity.ForbiddenWord;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ForbiddenWordQueryService {

	private final ForbiddenWordRepository forbiddenWordRepository;

	public List<String> findAllActiveWords() {
		return forbiddenWordRepository.findAllByIsActiveTrue()
			.stream()
			.map(ForbiddenWord::getWord)
			.toList();
	}
}
