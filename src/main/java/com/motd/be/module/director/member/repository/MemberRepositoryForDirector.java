package com.motd.be.module.director.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.motd.be.module.member.member.entity.Member;

public interface MemberRepositoryForDirector extends JpaRepository<Member, Long> {

	@Query("""
		    SELECT m FROM Member m 
		    WHERE m.id = :memberId 
		     AND m.isWithdrawal = false
		""")
	Optional<Member> findById(Long memberId);

	@Query("""
			SELECT m FROM Member m
			LEFT JOIN FETCH m.directorInfo d
			WHERE m.id = :memberId
			AND m.isWithdrawal = false
		""")
	Optional<Member> findByIdWithDirector(Long memberId);

}
