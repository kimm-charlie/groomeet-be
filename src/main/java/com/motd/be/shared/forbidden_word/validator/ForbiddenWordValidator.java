package com.motd.be.shared.forbidden_word.validator;

import java.util.List;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ForbiddenWordException;
import com.motd.be.shared.forbidden_word.repository.ForbiddenWordQueryService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ForbiddenWordValidator {

	private final ForbiddenWordQueryService forbiddenWordQueryService;

	/**
	 * 텍스트에 금칙어가 포함되어 있는지 검증합니다.
	 * 금칙어가 포함된 경우 예외를 발생시킵니다.
	 *
	 * @param text 검증할 텍스트
	 */
	public void validate(String text) {
		if (text == null || text.isBlank()) {
			return;
		}

		List<String> forbiddenWords = forbiddenWordQueryService.findAllActiveWords();

		for (String word : forbiddenWords) {
			if (text.contains(word)) {
				throw new CustomRuntimeException(
					ForbiddenWordException.CONTAINS_FORBIDDEN_WORD,
					"금칙어가 포함되어 있습니다: " + word
				);
			}
		}
	}

	/**
	 * 여러 텍스트에 금칙어가 포함되어 있는지 검증합니다.
	 *
	 * @param texts 검증할 텍스트 목록
	 */
	public void validateAll(String... texts) {
		// 금칙어 목록을 한 번만 조회
		List<String> forbiddenWords = forbiddenWordQueryService.findAllActiveWords();

		for (String text : texts) {
			if (text == null || text.isBlank()) {
				continue;
			}

			for (String word : forbiddenWords) {
				if (text.contains(word)) {
					throw new CustomRuntimeException(
						ForbiddenWordException.CONTAINS_FORBIDDEN_WORD,
						"금칙어가 포함되어 있습니다: " + word
					);
				}
			}
		}
	}
}
