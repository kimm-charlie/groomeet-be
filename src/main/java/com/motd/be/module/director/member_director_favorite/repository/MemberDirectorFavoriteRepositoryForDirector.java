package com.motd.be.module.director.member_director_favorite.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member_director_favorite.entity.MemberDirectorFavorite;

public interface MemberDirectorFavoriteRepositoryForDirector extends JpaRepository<MemberDirectorFavorite, Long> {

	@Query("""
			SELECT mdf.member
			FROM MemberDirectorFavorite mdf
			WHERE mdf.targetMember.id = :targetMemberId
		""")
	List<Member> findAllMembersByTargetMemberId(@Param("targetMemberId") Long targetMemberId);
}
