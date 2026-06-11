package com.motd.be.module.member.member_director_favorite.service;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.member_director_favorite.entity.MemberDirectorFavorite;
import com.motd.be.module.member.member_director_favorite.repositroy.MemberDirectorFavoriteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberDirectorFavoriteCommandService {

	private final MemberDirectorFavoriteRepository memberDirectorFavoriteRepository;

	public void save(MemberDirectorFavorite entity) {
		memberDirectorFavoriteRepository.save(entity);
	}

	public void deleteByMemberIdAndTargetMemberId(Long memberId, Long targetMemberId) {
		memberDirectorFavoriteRepository.deleteByMemberIdAndTargetMemberId(memberId, targetMemberId);
	}

	public void deleteAllByMemberId(Long memberId) {
		memberDirectorFavoriteRepository.deleteAllByMemberIdOrTargetMemberId(memberId);
	}
}
