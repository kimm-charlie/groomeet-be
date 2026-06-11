package com.motd.be.module.member.code_usage_history.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.code_usage_history.entity.CodeUsageHistory;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.promotion_code.entity.PromotionCode;

public interface CodeUsageHistoryRepository extends JpaRepository<CodeUsageHistory, Long> {

	@Query("""
			SELECT CASE WHEN COUNT(cuh) > 0 THEN true ELSE false END
			FROM CodeUsageHistory cuh
			WHERE cuh.promotionCode = :promotionCode
			AND cuh.inviteeMember = :member
		""")
	boolean existsByPromotionCodeIdAndInviteeMemberId(@Param("promotionCode") PromotionCode promotionCode,
		@Param("member") Member member);

	@Query("""
			SELECT CASE WHEN COUNT(cuh) > 0 THEN true ELSE false END
			FROM CodeUsageHistory cuh
			WHERE cuh.inviteeMember = :member
			OR cuh.inviterMember = :member
		""")
	boolean existsByInviteeMemberOrInviterMember(@Param("member") Member member);
}
