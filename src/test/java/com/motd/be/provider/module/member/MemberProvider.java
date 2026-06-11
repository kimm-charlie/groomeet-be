package com.motd.be.provider.module.member;

import static com.motd.be.Constants.*;
import static com.motd.be.Constants.EMAIL;
import static com.motd.be.common.utils.ReferralCodeUtils.*;
import static com.motd.be.common.utils.Utils.*;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.MemberTerms;
import com.motd.be.module.member.member.entity.Role;
import com.motd.be.module.member.member.entity.SignInPlatform;
import com.motd.be.module.member.member.repository.MemberRepository;

@Component
public class MemberProvider {

	@Autowired
	private MemberRepository memberRepository;

	private static Member memberDummy(SignInPlatform signInPlatform) {
		String identifier = UUID.randomUUID().toString();
		return Member.builder()
			.terms(MemberTerms.builder()
				.serviceAgreed(true)
				.privacyPolicyAgreed(true)
				.build())
			.nickname(NICKNAME)
			.email(EMAIL)
			.role(Role.MEMBER)
			.signInPlatform(signInPlatform)
			.identifier(identifier)
			.activeUniqueKey(generateMemberActiveUniqueKey(signInPlatform, identifier))
			.isAuthenticated(Boolean.TRUE)
			.referralCode(createRandomCode())
			.build();
	}

	private static Member memberDummyWithIdentifier(SignInPlatform signInPlatform, String identifier) {
		return Member.builder()
			.terms(MemberTerms.builder()
				.serviceAgreed(true)
				.privacyPolicyAgreed(true)
				.build())
			.nickname(NICKNAME)
			.email(EMAIL)
			.role(Role.MEMBER)
			.signInPlatform(signInPlatform)
			.identifier(identifier)
			.activeUniqueKey(generateMemberActiveUniqueKey(signInPlatform, identifier))
			.isAuthenticated(Boolean.TRUE)
			.referralCode(createRandomCode())
			.build();
	}

	private static Member memberDummyWithIdentifierAndBannedMember(SignInPlatform signInPlatform,
		LocalDate unbannedAt) {
		String identifier = UUID.randomUUID().toString();
		return Member.builder()
			.terms(MemberTerms.builder()
				.serviceAgreed(true)
				.privacyPolicyAgreed(true)
				.build())
			.nickname(NICKNAME)
			.email(EMAIL)
			.role(Role.MEMBER)
			.signInPlatform(signInPlatform)
			.identifier(identifier)
			.isBanned(Boolean.TRUE)
			.bannedAt(LocalDateTime.now())
			.unbannedAt(unbannedAt)
			.activeUniqueKey(generateMemberActiveUniqueKey(signInPlatform, identifier))
			.referralCode(createRandomCode())
			.build();
	}

	private static Member memberDummyWithIsAuthenticatedFalse(SignInPlatform signInPlatform) {
		String identifier = UUID.randomUUID().toString();
		return Member.builder()
			.terms(MemberTerms.builder()
				.serviceAgreed(true)
				.privacyPolicyAgreed(true)
				.build())
			.nickname(NICKNAME)
			.email(EMAIL)
			.role(Role.MEMBER)
			.signInPlatform(signInPlatform)
			.identifier(identifier)
			.activeUniqueKey(generateMemberActiveUniqueKey(signInPlatform, identifier))
			.isAuthenticated(Boolean.FALSE)
			.referralCode(createRandomCode())
			.build();
	}

	private static Member memberDummyWithIsWithdrawalTrue(LocalDateTime localDateTime, SignInPlatform signInPlatform) {
		return Member.builder()
			.terms(MemberTerms.builder()
				.serviceAgreed(true)
				.privacyPolicyAgreed(true)
				.build())
			.nickname(NICKNAME)
			.email(EMAIL)
			.role(Role.MEMBER)
			.signInPlatform(signInPlatform)
			.identifier(IDENTIFIER)
			.isWithdrawal(Boolean.TRUE)
			.withdrawalAt(localDateTime)
			.referralCode(createRandomCode())
			.build();
	}

	private static Member memberDummyWithDirectorInfoAndIsWithdrawalTrue(SignInPlatform signInPlatform,
		DirectorInfo directorInfo) {
		return Member.builder()
			.terms(MemberTerms.builder()
				.serviceAgreed(true)
				.privacyPolicyAgreed(true)
				.build())
			.nickname(NICKNAME)
			.email(EMAIL)
			.role(Role.MEMBER)
			.signInPlatform(signInPlatform)
			.identifier(IDENTIFIER)
			.isWithdrawal(Boolean.TRUE)
			.withdrawalAt(LocalDateTime.now())
			.directorInfo(directorInfo)
			.referralCode(createRandomCode())
			.build();
	}

	private static Member memberDummyWithDirector(SignInPlatform signInPlatform, DirectorInfo directorInfo) {
		String identifier = UUID.randomUUID().toString();
		return Member.builder()
			.terms(MemberTerms.builder()
				.serviceAgreed(true)
				.privacyPolicyAgreed(true)
				.build())
			.nickname(NICKNAME)
			.email(EMAIL)
			.role(Role.DIRECTOR)
			.signInPlatform(signInPlatform)
			.identifier(identifier)
			.activeUniqueKey(generateMemberActiveUniqueKey(signInPlatform, identifier))
			.isDirector(Boolean.TRUE)
			.directorInfo(directorInfo)
			.isAuthenticated(Boolean.TRUE)
			.referralCode(createRandomCode())
			.build();
	}

	private static Member memberDummyWithDirectorAndIsAuthenticatedFalse(SignInPlatform signInPlatform,
		DirectorInfo directorInfo) {
		String identifier = UUID.randomUUID().toString();
		return Member.builder()
			.terms(MemberTerms.builder()
				.serviceAgreed(true)
				.privacyPolicyAgreed(true)
				.build())
			.nickname(NICKNAME)
			.email(EMAIL)
			.role(Role.DIRECTOR)
			.signInPlatform(signInPlatform)
			.identifier(identifier)
			.activeUniqueKey(generateMemberActiveUniqueKey(signInPlatform, identifier))
			.isDirector(Boolean.TRUE)
			.directorInfo(directorInfo)
			.isAuthenticated(Boolean.FALSE)
			.referralCode(createRandomCode())
			.build();
	}

	private static Member memberDummyWithIsAuthenticatedTrue(SignInPlatform signInPlatform) {
		String identifier = UUID.randomUUID().toString();
		return Member.builder()
			.terms(MemberTerms.builder()
				.serviceAgreed(true)
				.privacyPolicyAgreed(true)
				.build())
			.nickname(NICKNAME)
			.email(EMAIL)
			.role(Role.MEMBER)
			.signInPlatform(signInPlatform)
			.identifier(identifier)
			.activeUniqueKey(generateMemberActiveUniqueKey(signInPlatform, identifier))
			.referralCode(createRandomCode())
			.isAuthenticated(Boolean.TRUE)
			.build();
	}

	public Member saveMember(SignInPlatform signInPlatform) {
		return memberRepository.save(memberDummy(signInPlatform));
	}

	public Member saveMemberWithIsAuthenticatedFalse(SignInPlatform signInPlatform) {
		return memberRepository.save(memberDummyWithIsAuthenticatedFalse(signInPlatform));
	}

	public Member saveMemberWithBanned(SignInPlatform signInPlatform, LocalDate unbannedAt) {
		return memberRepository.save(memberDummyWithIdentifierAndBannedMember(signInPlatform, unbannedAt));
	}

	public Member saveMemberWithIdentifier(SignInPlatform signInPlatform, String identifier) {
		return memberRepository.save(memberDummyWithIdentifier(signInPlatform, identifier));
	}

	public Member saveMemberWithdrawalTrue(LocalDateTime localDateTime, SignInPlatform signInPlatform) {
		return memberRepository.save(memberDummyWithIsWithdrawalTrue(localDateTime, signInPlatform));
	}

	public Member saveMemberWithDirectorAndWithdrawalTrue(SignInPlatform signInPlatform, DirectorInfo directorInfo) {
		return memberRepository.save(memberDummyWithDirectorInfoAndIsWithdrawalTrue(signInPlatform, directorInfo));
	}

	public List<Member> findAll() {
		return memberRepository.findAll();
	}

	public Member saveMemberWithDirectorInfo(SignInPlatform signInPlatform, DirectorInfo directorInfo) {
		return memberRepository.save(memberDummyWithDirector(signInPlatform, directorInfo));
	}

	public Member saveMemberWithDirectorInfoAndIsAuthenticatedFalse(SignInPlatform signInPlatform,
		DirectorInfo directorInfo) {
		return memberRepository.save(memberDummyWithDirectorAndIsAuthenticatedFalse(signInPlatform, directorInfo));
	}

	public Member findById(Long id) {
		return memberRepository.findById(id).orElseThrow();
	}
}
