package com.motd.be.module.admin.member.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.member.entity.Member;

public interface MemberRepositoryForAdmin extends JpaRepository<Member, Long> {

	long countByIsWithdrawalFalse();

	@Query("""
			SELECT COUNT(m)
			FROM Member m
			WHERE m.createdAt >= :startOfDay
			AND m.createdAt < :endOfDay
			AND m.isWithdrawal = false
		""")
	long countByCreatedAtBetweenAndIsWithdrawalFalse(@Param("startOfDay") LocalDateTime startOfDay,
		@Param("endOfDay") LocalDateTime endOfDay);

	Optional<Member> findByIdAndIsWithdrawalFalse(Long id);

	@Query("""
			SELECT m
			FROM Member m
			WHERE m.isWithdrawal = false
		""")
	List<Member> findAllWithIsWithdrawalFalse();
}

