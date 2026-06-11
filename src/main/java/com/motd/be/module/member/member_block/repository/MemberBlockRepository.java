package com.motd.be.module.member.member_block.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member_block.entity.MemberBlock;

public interface MemberBlockRepository extends JpaRepository<MemberBlock, Long> {

	@Query("""
		    select (count(mb) > 0)
		    from MemberBlock mb
		    where (mb.blocker = :sender and mb.blocked = :receiver)
		       or (mb.blocker = :receiver and mb.blocked = :sender)
		""")
	boolean existsByBlockerAndBlocked(@Param("sender") Member sender,
		@Param("receiver") Member receiver);

	@Query("""
		    SELECT CASE 
		             WHEN mb.blocker.id = :memberId THEN mb.blocked.id
		             ELSE mb.blocker.id
		           END
		    FROM MemberBlock mb
		    WHERE mb.blocker.id = :memberId
		       OR mb.blocked.id = :memberId
		""")
	List<Long> findAllBlockRelatedMemberIds(@Param("memberId") Long memberId);

	@Query("""
			SELECT CASE WHEN COUNT(mb) > 0 THEN true ELSE false END
			FROM MemberBlock mb
			WHERE mb.blocker.id = :blockerId AND mb.blocked.id = :blockedId
		""")
	boolean existsByBlockerIdAndBlockedId(@Param("blockerId") Long blockerId, @Param("blockedId") Long blockedId);

	@Modifying
	@Query("""
			DELETE FROM MemberBlock mb
			WHERE mb.blocker.id = :blockerId AND mb.blocked.id = :blockedId
		""")
	void deleteByBlockerIdAndBlockedId(@Param("blockerId") Long blockerId, @Param("blockedId") Long blockedId);

	@Query("""
		        SELECT mb
		        FROM MemberBlock mb
		        WHERE mb.blocker = :blocker
		        ORDER BY mb.createdAt DESC
		""")
	Slice<MemberBlock> findAllByBlocker(Member blocker, Pageable pageable);

	@Modifying
	@Query("""
			DELETE FROM MemberBlock mb
			WHERE mb.blocker.id = :memberId
			OR mb.blocked.id = :memberId
		""")
	void deleteAllByMemberId(@Param("memberId") Long memberId);
}
