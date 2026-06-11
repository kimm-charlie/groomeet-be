package com.motd.be.module.member.member_director_favorite.service;

import static com.motd.be.common.constants.PageSizeConstants.*;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.MemberDirectorFavoriteException;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member_director_favorite.dto.response.MemberDirectorFavoriteFindAllResponse;
import com.motd.be.module.member.member_director_favorite.entity.MemberDirectorFavorite;
import com.motd.be.module.member.member_director_favorite.validator.MemberDirectorFavoriteValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberDirectorFavoriteService {

	private final MemberDirectorFavoriteQueryService memberDirectorFavoriteQueryService;
	private final MemberDirectorFavoriteCommandService memberDirectorFavoriteCommandService;
	private final MemberDirectorFavoriteValidator memberDirectorFavoriteValidator;

	public void save(Member member, Member targetMember) {
		// 1. 이미 즐겨찾기된 상태인지 확인
		memberDirectorFavoriteValidator.validateCannotFavoriteSelf(member.getId(), targetMember.getId());

		// 2. 자기 자신 즐겨찾기 불가
		memberDirectorFavoriteValidator.validateFavoriteNotExists(member.getId(), targetMember.getId());

		// 3. 즐겨찾기 저장
		try {
			memberDirectorFavoriteCommandService.save(
				MemberDirectorFavorite.builder()
					.member(member)
					.targetMember(targetMember)
					.build()
			);
		} catch (DataIntegrityViolationException e) {
			throw new CustomRuntimeException(MemberDirectorFavoriteException.ALREADY_FAVORITE);
		}
	}

	public void deleteWithValidation(Member member, Long targetMemberId) {
		// 1. 존재하는지 확인
		memberDirectorFavoriteValidator.validateFavoriteExist(member.getId(), targetMemberId);

		// 2. 즐겨찾기 삭제
		memberDirectorFavoriteCommandService.deleteByMemberIdAndTargetMemberId(member.getId(), targetMemberId);
	}

	public void deleteIfExist(Member member, Long targetMemberId) {
		// 1. 존재하는지 확인
		if (memberDirectorFavoriteQueryService.existsByMemberIdAndTargetId(member.getId(), targetMemberId)) {
			// 2. 즐겨찾기 삭제
			memberDirectorFavoriteCommandService.deleteByMemberIdAndTargetMemberId(member.getId(), targetMemberId);
		}
	}

	public MemberDirectorFavoriteFindAllResponse findAll(Member member, int page) {
		Pageable pageable = PageRequest.of(page, MEMBER_DIRECTOR_FAVORITE_FIND_ALL_SIZE);

		return MemberDirectorFavoriteFindAllResponse.from(
			memberDirectorFavoriteQueryService.findAllByMember(member, pageable));
	}

	public boolean existsByMemberIdAndTargetId(Long memberId, Long targetMemberId) {
		if (memberId == null) {
			return false;
		}
		return memberDirectorFavoriteQueryService.existsByMemberIdAndTargetId(memberId, targetMemberId);
	}
}
