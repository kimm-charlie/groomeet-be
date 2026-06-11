package com.motd.be.module.member.member.service;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.constants.DefaultConstants.*;
import static com.motd.be.common.utils.ReferralCodeUtils.*;
import static com.motd.be.shared.aws.util.ImageUrlConverter.*;

import java.time.LocalDate;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.AuthException;
import com.motd.be.module.member.auth.dto.request.AuthSignUpRequest;
import com.motd.be.module.member.auth.dto.request.AuthWithdrawalRequest;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.member.dto.response.CheckNicknameDuplicateResponse;
import com.motd.be.module.member.member.dto.response.MemberFindAccountInfoResponse;
import com.motd.be.module.member.member.dto.response.MemberFindInfoResponse;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.MemberTerms;
import com.motd.be.module.member.member.entity.Role;
import com.motd.be.module.member.member.entity.WithdrawalReason;
import com.motd.be.module.member.member.validator.MemberValidator;
import com.motd.be.module.member.profile_file.entity.ProfileFile;
import com.motd.be.module.member.profile_file.service.ProfileFileCommandService;
import com.motd.be.redis.domain.sign_up_information.entity.SignUpInformation;
import com.motd.be.shared.mobile_ok.dto.response.MobileOkResultDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

	private final MemberValidator memberValidator;
	private final MemberQueryService memberQueryService;
	private final MemberCommandService memberCommandService;
	private final ProfileFileCommandService profileFileCommandService;

	public void updateProfileImage(Member member, ProfileFile profileFile) {

		// 1. profileFile이 null이면 기본 이미지로 변경
		if (profileFile == null) {
			member.updateProfileImage(toCdnUrl(DEFAULT_PROFILE_IMAGE_URL));

			// 모든 profileFile 엔티티 논리 삭제 처리
			profileFileCommandService.deleteAllProfileFilesOfMember(member);
		} else {
			// 2. 새 이미지로 변경
			member.updateProfileImage(profileFile.getCdnUrl());

			// 3. profilefile 엔티티 업데이트 (현재 업데이트한 profileFile 제외하고는 모두 논리 삭제 처리)
			profileFileCommandService.deleteProfileFileWhenUpdate(member, profileFile);
		}
	}

	public void registerAsDirector(Member member, DirectorInfo directorInfo) {
		member.registerAsDirector(directorInfo);
	}

	public void updateNickname(Member member, String nickname) {
		// 1. 닉네임 중복 검증
		memberValidator.validateDuplicateNickname(member, nickname);

		// 2. 닉네임 변경
		if (!member.getNickname().equals(nickname)) {
			member.updateNickname(nickname);
		}
	}

	public CheckNicknameDuplicateResponse checkNicknameDuplicate(Long memberId, String nickname) {
		return CheckNicknameDuplicateResponse.from(
			memberQueryService.existsByNicknameExcludingMemberId(memberId, nickname));
	}

	public MemberFindInfoResponse findInfo(Long memberId) {
		return MemberFindInfoResponse.from(memberQueryService.findById(memberId));
	}

	public MemberFindAccountInfoResponse findAccountInfo(Long memberId) {
		return MemberFindAccountInfoResponse.from(memberQueryService.findById(memberId));
	}

	public Member createMember(AuthSignUpRequest request, SignUpInformation signUpInformation) {
		MemberTerms terms = request.toMemberTermsEntity();
		String referralCode = generateReferralCode();

		try {
			Member member = memberCommandService.save(
				request.toMemberEntity(signUpInformation.getIdentifier(), signUpInformation.getEmail(),
					DEFAULT_NICKNAME, Role.MEMBER, signUpInformation.getPlatform(), terms, referralCode));
			member.updateNickname(DEFAULT_NICKNAME + member.getId());
			return member;
		} catch (DataIntegrityViolationException e) {
			log.error("회원가입 중 데이터 무결성 위반 발생\n identifier : {}", signUpInformation.getIdentifier());
			throw new CustomRuntimeException(AuthException.DUPLICATED_MEMBER);
		}
	}

	public String generateReferralCode() {
		while (true) {
			String candidate = createRandomCode();
			if (!memberQueryService.existsByReferralCode(candidate)) {
				return candidate;
			}
		}
	}

	public void updateWithdrawalInfo(Member member, AuthWithdrawalRequest request) {
		member.updateWithdrawalInfo(WithdrawalReason.valueOf(request.getWithdrawalReason()));
	}

	public int unbanMembers(LocalDate today) {
		return memberCommandService.unbanMembers(today);
	}

	public void updateAuthenticationInfo(Long memberId, MobileOkResultDto mobileOkResultDto) {
		Member member = memberQueryService.findById(memberId);
		memberValidator.validateDuplicateAuthenticationCi(memberId, mobileOkResultDto.getCi());
		member.setAuthenticated(mobileOkResultDto);
	}

	@Transactional
	public Boolean toggleAuthentication(Long memberId) {
		Member member = memberQueryService.findById(memberId);
		member.toggleAuthentication();
		return member.getIsAuthenticated();
	}
}
