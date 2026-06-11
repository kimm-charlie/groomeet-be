package com.motd.be.module.member.director_info.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.director_info.entity.DirectorInfo;

public interface DirectorInfoRepository extends JpaRepository<DirectorInfo, Long> {

	@Query("""
				SELECT d
				FROM DirectorInfo d
				JOIN d.member m
				WHERE m.isWithdrawal = false
				ORDER BY d.completedEstimateCount DESC
		""")
	Slice<DirectorInfo> findDirectorRank(Pageable pageable);

	@Query("""
			SELECT COUNT(d)
			FROM DirectorInfo d
			JOIN d.member m
			WHERE m.isWithdrawal = false
		""")
	long countByMemberIsWithdrawalFalse();

	@Query("""
			SELECT COUNT(d)
			FROM DirectorInfo d
			JOIN d.member m
			WHERE d.createdAt >= :startOfDay
			AND d.createdAt < :endOfDay
			AND m.isWithdrawal = false
		""")
	long countByCreatedAtBetweenAndMemberIsWithdrawalFalse(@Param("startOfDay") LocalDateTime startOfDay,
		@Param("endOfDay") LocalDateTime endOfDay);
}
