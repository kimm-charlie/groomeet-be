package com.motd.be.module.member.member.facade;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.member.dto.request.MemberUpdateAndCheckDuplicateNicknameRequest;
import com.motd.be.module.member.member.dto.request.MemberUpdateProfileImageRequest;
import com.motd.be.module.member.member.dto.request.MemberUpdatePushSettingRequest;
import com.motd.be.module.member.member.dto.response.CheckNicknameDuplicateResponse;
import com.motd.be.module.member.member.dto.response.MemberFindAccountInfoResponse;
import com.motd.be.module.member.member.dto.response.MemberFindInfoResponse;
import com.motd.be.module.member.member.dto.response.MemberIsAuthenticatedResponse;
import com.motd.be.module.member.member.dto.response.MemberProfileResponse;
import com.motd.be.module.member.member.dto.response.MemberPushSettingResponse;
import com.motd.be.module.member.member.dto.response.MemberReferralCodeResponse;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.entity.PushType;
import com.motd.be.module.member.member.service.MemberQueryService;
import com.motd.be.module.member.member.service.MemberService;
import com.motd.be.module.member.member_director_favorite.service.MemberDirectorFavoriteService;
import com.motd.be.module.member.member_nickname_history.service.MemberNicknameService;
import com.motd.be.module.member.profile_file.entity.ProfileFile;
import com.motd.be.module.member.profile_file.service.ProfileFileService;
import com.motd.be.module.member.service_request.service.ServiceRequestService;
import com.motd.be.shared.hackle.dto.request.HackleUpdatePushSubscriptionRequest;
import com.motd.be.shared.hackle.service.HackleEventPublisher;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberFacade {

	private final MemberQueryService memberQueryService;
	private final MemberService memberService;
	private final MemberDirectorFavoriteService memberDirectorFavoriteService;
	private final MemberNicknameService memberNicknameService;
	private final ProfileFileService profileFileService;
	private final ServiceRequestService serviceRequestService;
	private final HackleEventPublisher hackleEventPublisher;

	@Transactional
	public void updateNickname(Long memberId, MemberUpdateAndCheckDuplicateNicknameRequest request) {
		// 1. 회원 조회
		Member member = memberQueryService.findById(memberId);

		//닉네임 변경 기록 저장
		memberNicknameService.recordNicknameChange(member, member.getNickname(), request.getNickname());

		// 2. 닉네임 업데이트
		memberService.updateNickname(member, request.getNickname());
	}

	public CheckNicknameDuplicateResponse checkNicknameDuplicate(Long memberId,
		MemberUpdateAndCheckDuplicateNicknameRequest request) {

		//1. 닉네임 중복 검증
		return memberService.checkNicknameDuplicate(memberId, request.getNickname());
	}

	@Transactional
	public void updateProfileImage(Long memberId, MemberUpdateProfileImageRequest request) {
		// 1. 회원 조회
		Member member = memberQueryService.findById(memberId);

		// 프로필 이미지 조회
		ProfileFile profileFile = profileFileService.find(member, request);

		// 2. 프로필 이미지 업데이트
		memberService.updateProfileImage(member, profileFile);
	}

	public MemberFindInfoResponse findInfo(Long memberId) {
		// 1. 회원 정보 조회
		return memberService.findInfo(memberId);
	}

	public MemberFindAccountInfoResponse findAccountInfo(Long memberId) {
		// 1. 회원 정보 조회
		return memberService.findAccountInfo(memberId);
	}

	public MemberProfileResponse findProfile(Long memberId, Long targetMemberId) {
		// 1. targetMember 프로필 조회
		Member targetMember = memberQueryService.findByIdWithDirector(targetMemberId);

		// 2. 로그인한 회원이 즐겨찾기 했는지 조회
		boolean isFavorited = memberDirectorFavoriteService.existsByMemberIdAndTargetId(memberId, targetMemberId);

		// 로그인한 회원이 디렉터와 진행중인 요청이 있는지 조회
		boolean hasNotEndedRequest = serviceRequestService.existsNotEndedRequestBetweenMemberAndDirector(memberId,
			targetMemberId);

		return MemberProfileResponse.of(targetMember, isFavorited, hasNotEndedRequest);
	}

	public MemberPushSettingResponse findPushSettings(Long memberId) {
		// 회원 조회
		Member member = memberQueryService.findById(memberId);

		return MemberPushSettingResponse.from(member);
	}

	@Transactional
	public void updatePushSetting(Long memberId, MemberUpdatePushSettingRequest request) {
		// 회원 조회
		Member member = memberQueryService.findById(memberId);

		// 업데이트
		PushType pushType = PushType.valueOf(request.getPushType());
		member.updatePushSetting(pushType);

		// Hackle 푸시 구독 정보 업데이트
		hackleEventPublisher.updatePushSubscription(HackleUpdatePushSubscriptionRequest.from(member));
	}

	@Transactional
	public int unbanMembers() {
		return memberService.unbanMembers(LocalDate.now());
	}

	public MemberReferralCodeResponse findMyReferralCode(Long memberId) {
		Member member = memberQueryService.findById(memberId);

		return MemberReferralCodeResponse.from(member);
	}

	public MemberIsAuthenticatedResponse isAuthenticated(Long memberId) {
		Member member = memberQueryService.findById(memberId);

		return MemberIsAuthenticatedResponse.from(member);
	}
}
