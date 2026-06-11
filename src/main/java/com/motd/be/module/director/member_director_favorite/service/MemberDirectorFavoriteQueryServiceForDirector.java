package com.motd.be.module.director.member_director_favorite.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.director.member_director_favorite.repository.MemberDirectorFavoriteRepositoryForDirector;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberDirectorFavoriteQueryServiceForDirector {

	private final MemberDirectorFavoriteRepositoryForDirector memberDirectorFavoriteRepositoryForDirector;

	public List<Member> findAllMembersByTargetMemberId(Long targetMemberId) {
		return memberDirectorFavoriteRepositoryForDirector.findAllMembersByTargetMemberId(targetMemberId);
	}
}
