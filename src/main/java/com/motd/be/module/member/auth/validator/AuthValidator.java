package com.motd.be.module.member.auth.validator;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.MemberException;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;
import com.motd.be.redis.domain.sign_up_information.entity.SignUpInformation;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthValidator {

	private final MemberQueryService memberQueryService;

	public void validateIdentifier(SignUpInformation info) {
		if (info.getIdentifier() == null) {
			throw new CustomRuntimeException(MemberException.IDENTIFIER_NOT_FOUND);
		}
	}

	public void validateDuplicateEmail(String email) {
		if (email == null) {
			return;
		}

		Optional<Member> existing = memberQueryService.findByEmailWithIsWithdrawalFalse(email);

		if (existing.isPresent()) {
			throw new CustomRuntimeException(MemberException.DUPLICATED_EMAIL);
		}
	}
}
