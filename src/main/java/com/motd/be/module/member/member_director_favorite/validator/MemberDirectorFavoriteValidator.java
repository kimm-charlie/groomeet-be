package com.motd.be.module.member.member_director_favorite.validator;

import org.springframework.stereotype.Component;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.MemberDirectorFavoriteException;
import com.motd.be.module.member.member_director_favorite.service.MemberDirectorFavoriteQueryService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberDirectorFavoriteValidator {

	private final MemberDirectorFavoriteQueryService memberDirectorFavoriteQueryService;

	public void validateCannotFavoriteSelf(Long memberId, Long targetMemberId) {
		if (memberId.equals(targetMemberId)) {
			throw new CustomRuntimeException(MemberDirectorFavoriteException.CANNOT_FAVORITE_SELF);
		}
	}

	public void validateFavoriteNotExists(Long memberId, Long targetMemberId) {
		if (memberDirectorFavoriteQueryService.existsByMemberIdAndTargetId(memberId, targetMemberId)) {
			throw new CustomRuntimeException(MemberDirectorFavoriteException.ALREADY_FAVORITE);
		}
	}

	public void validateFavoriteExist(Long memberId, Long targetMemberId) {
		if (!memberDirectorFavoriteQueryService.existsByMemberIdAndTargetId(memberId, targetMemberId)) {
			throw new CustomRuntimeException(MemberDirectorFavoriteException.ALREADY_FAVORITE);
		}
	}

}
