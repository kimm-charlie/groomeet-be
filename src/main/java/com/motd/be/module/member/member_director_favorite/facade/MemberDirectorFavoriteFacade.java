package com.motd.be.module.member.member_director_favorite.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;
import com.motd.be.module.member.member_director_favorite.dto.request.MemberDirectorFavoriteRequest;
import com.motd.be.module.member.member_director_favorite.dto.response.MemberDirectorFavoriteFindAllResponse;
import com.motd.be.module.member.member_director_favorite.service.MemberDirectorFavoriteService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberDirectorFavoriteFacade {

	private final MemberDirectorFavoriteService memberDirectorFavoriteService;
	private final MemberQueryService memberQueryService;

	@Transactional
	public void save(Long memberId, MemberDirectorFavoriteRequest request) {
		// 회원 조회
		Member member = memberQueryService.findById(memberId);

		// 디렉터 정보 조회
		Member target = memberQueryService.findByIdWithDirector(request.getTargetMemberId());

		// 즐겨찾기 저장
		memberDirectorFavoriteService.save(member, target);
	}

	@Transactional
	public void delete(Long memberId, MemberDirectorFavoriteRequest request) {
		// 회원 조회
		Member member = memberQueryService.findById(memberId);

		// 즐겨찾기 삭제
		memberDirectorFavoriteService.deleteWithValidation(member, request.getTargetMemberId());
	}

	public MemberDirectorFavoriteFindAllResponse findAll(Long memberId, int page) {
		// 회원 조회
		Member member = memberQueryService.findById(memberId);

		// 즐겨찾기 목록 조회
		return memberDirectorFavoriteService.findAll(member, page);
	}
}
