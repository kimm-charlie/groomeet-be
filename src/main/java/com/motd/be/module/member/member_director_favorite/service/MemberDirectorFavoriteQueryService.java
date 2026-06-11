package com.motd.be.module.member.member_director_favorite.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member_director_favorite.entity.MemberDirectorFavorite;
import com.motd.be.module.member.member_director_favorite.repositroy.MemberDirectorFavoriteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberDirectorFavoriteQueryService {

	private final MemberDirectorFavoriteRepository memberDirectorFavoriteRepository;

	public boolean existsByMemberIdAndTargetId(Long memberId, Long targetId) {
		return memberDirectorFavoriteRepository.existsByMemberIdAndDirectorInfoId(memberId, targetId);
	}

	public Slice<MemberDirectorFavorite> findAllByMember(Member member, Pageable pageable) {
		return memberDirectorFavoriteRepository.findAllByMember(member, pageable);
	}
}
