package com.motd.be.module.member.member.validator;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.MemberException;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberValidator {

	private final MemberQueryService memberQueryService;

	public void validateDuplicateNickname(Member member, String newNickname) {
		if (memberQueryService.existsByNicknameExcludingMemberId(member.getId(), newNickname)) {
			throw new CustomRuntimeException(MemberException.DUPLICATE_NICKNAME);
		}
	}

	public void isAuthenticatedMember(Member director) {
		if (!Boolean.TRUE.equals(director.getIsAuthenticated())) {
			throw new CustomRuntimeException(MemberException.NOT_AUTHENTICATED);
		}
	}

	public Member isWithdrawalMember(Member receiver) {
		if (receiver.getIsWithdrawal()) {
			throw new CustomRuntimeException(MemberException.WITHDRAWAL_MEMBER);
		}
		return receiver;
	}

	public void isOwnReferralCode(Member inviter, Member invitee) {
		if (inviter.getId().equals(invitee.getId())) {
			throw new CustomRuntimeException(MemberException.INVALID_REFERRAL_CODE);
		}
	}

	public void validateDuplicateAuthenticationCi(Long memberId, String authenticationCi) {
		if (authenticationCi == null) {
			return;
		}

		memberQueryService.findByAuthenticationCi(authenticationCi)
			.filter(member -> !member.getId().equals(memberId))
			.ifPresent(member -> {
				throw new CustomRuntimeException(MemberException.DUPLICATED_AUTHENTICATION_CI);
			});
	}
}
