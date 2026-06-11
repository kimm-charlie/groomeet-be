package com.motd.be.module.member.member_director_favorite.repositroy;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member_director_favorite.entity.MemberDirectorFavorite;

public interface MemberDirectorFavoriteRepository extends JpaRepository<MemberDirectorFavorite, Long> {

	@Query("""
			SELECT CASE WHEN COUNT(mdf) > 0 THEN true ELSE false END
			FROM MemberDirectorFavorite mdf
			WHERE mdf.member.id = :memberId
			AND mdf.targetMember.id = :targetMemberId
		""")
	boolean existsByMemberIdAndDirectorInfoId(@Param("memberId") Long memberId,
		@Param("targetMemberId") Long targetMemberId);

	@Modifying
	@Query("""
		        DELETE FROM MemberDirectorFavorite mdf
		        WHERE mdf.member.id = :memberId
		        AND mdf.targetMember.id = :targetMemberId
		""")
	void deleteByMemberIdAndTargetMemberId(@Param("memberId") Long memberId,
		@Param("targetMemberId") Long targetMemberId);

	@Modifying
	@Query("""
			DELETE FROM MemberDirectorFavorite mdf
			WHERE mdf.member.id = :memberId
			OR mdf.targetMember.id = :memberId
		""")
	void deleteAllByMemberIdOrTargetMemberId(@Param("memberId") Long memberId);

	@Query("""
			SELECT mdf
			FROM MemberDirectorFavorite mdf
			WHERE mdf.member = :member
			ORDER BY mdf.createdAt DESC
		""")
	Slice<MemberDirectorFavorite> findAllByMember(Member member, Pageable pageable);
}
