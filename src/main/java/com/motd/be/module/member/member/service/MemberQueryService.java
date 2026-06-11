package com.motd.be.module.member.member.service;

import static com.motd.be.common.constants.ValidationConstants.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.DirectorInfoException;
import com.motd.be.exception.exceptions.MemberException;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberQueryService {

	private final MemberRepository memberRepository;

	public Optional<Member> findByIdentifierAndPlatform(String identifier, SignInPlatform signInPlatform) {
		return memberRepository.findByIdentifierAndPlatform(identifier,
			LocalDateTime.now().minusMonths(WITHDRAWAL_RESTRICTION_MONTHS), signInPlatform);
	}

	public Optional<Member> findByEmailWithIsWithdrawalFalse(String email) {
		return memberRepository.findByEmailWithIsWithdrawalFalse(email);
	}

	public Optional<Member> findByAuthenticationCi(String authenticationCi) {
		return memberRepository.findByAuthenticationCi(authenticationCi,
			LocalDateTime.now().minusMonths(WITHDRAWAL_RESTRICTION_MONTHS));
	}

	public Member findById(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomRuntimeException(MemberException.NOT_FOUND));
	}

	public Member findByIdWithLock(Long memberId) {
		return memberRepository.findByIdWithLock(memberId)
			.orElseThrow(() -> new CustomRuntimeException(MemberException.NOT_FOUND));
	}

	public Member findByIdWithDirector(Long memberId) {
		Member member = memberRepository.findByIdWithDirector(memberId)
			.orElseThrow(() -> new CustomRuntimeException(MemberException.NOT_FOUND));

		if (member.getDirectorInfo() == null || !member.isDirector()) {
			throw new CustomRuntimeException(DirectorInfoException.DIRECTOR_INFO_NOT_FOUND);
		}
		return member;
	}

	public Boolean existsByNicknameExcludingMemberId(Long memberId, String nickname) {
		return memberRepository.existsByNicknameExcludingMemberId(nickname, memberId);
	}

	public Member findByIdWithDirectorAndLock(Long memberId) {
		Member member = memberRepository.findByIdWithDirectorAndLock(memberId)
			.orElseThrow(() -> new CustomRuntimeException(MemberException.NOT_FOUND));

		if (member.getDirectorInfo() == null || !member.isDirector()) {
			throw new CustomRuntimeException(DirectorInfoException.DIRECTOR_INFO_NOT_FOUND);
		}
		return member;
	}

	public boolean existsByReferralCode(String candidate) {
		return memberRepository.existsByReferralCode(candidate);
	}

	public Member findByReferralCode(String referralCode) {
		return memberRepository.findByReferralCode(referralCode)
			.orElseThrow(() -> new CustomRuntimeException(MemberException.INVALID_REFERRAL_CODE));
	}
}
