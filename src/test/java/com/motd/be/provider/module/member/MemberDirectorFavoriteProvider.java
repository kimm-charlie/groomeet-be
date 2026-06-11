package com.motd.be.provider.module.member;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member_director_favorite.entity.MemberDirectorFavorite;
import com.motd.be.module.member.member_director_favorite.repositroy.MemberDirectorFavoriteRepository;

@Component
public class MemberDirectorFavoriteProvider {

	@Autowired
	private MemberDirectorFavoriteRepository memberDirectorFavoriteRepository;

	public List<MemberDirectorFavorite> findAll() {
		return memberDirectorFavoriteRepository.findAll();
	}

	public MemberDirectorFavorite save(Member member, Member targetMember) {
		return memberDirectorFavoriteRepository.save(MemberDirectorFavorite.builder()
			.member(member)
			.targetMember(targetMember)
			.build());
	}

	public MemberDirectorFavorite findById(Long id) {
		return memberDirectorFavoriteRepository.findById(id).orElse(null);
	}
}
