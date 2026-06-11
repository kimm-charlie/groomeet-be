package com.motd.be.module.member.director_profile_detail.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.director_profile_detail.dto.response.DirectorProfileFindDetailResponseForPublic;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectorProfileDetailFacade {

	private final MemberQueryService memberQueryService;

	public DirectorProfileFindDetailResponseForPublic findDetail(Long memberId) {
		// 1. 디렉터 조회
		Member member = memberQueryService.findByIdWithDirector(memberId);

		// 2. 디렉터 프로필 상세 조회
		return DirectorProfileFindDetailResponseForPublic.from(member.getDirectorInfo().getDirectorProfileDetail());
	}
}
