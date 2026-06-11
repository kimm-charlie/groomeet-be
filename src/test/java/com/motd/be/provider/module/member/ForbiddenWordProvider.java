package com.motd.be.provider.module.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.shared.forbidden_word.entity.ForbiddenWord;
import com.motd.be.shared.forbidden_word.repository.ForbiddenWordRepository;

@Component
public class ForbiddenWordProvider {

	@Autowired
	private ForbiddenWordRepository forbiddenWordRepository;

	public ForbiddenWord save(String word) {
		return forbiddenWordRepository.save(
			ForbiddenWord.builder()
				.word(word)
				.isActive(true)
				.build()
		);
	}

	public ForbiddenWord save(String word, Boolean isActive) {
		return forbiddenWordRepository.save(
			ForbiddenWord.builder()
				.word(word)
				.isActive(isActive)
				.build()
		);
	}
}
