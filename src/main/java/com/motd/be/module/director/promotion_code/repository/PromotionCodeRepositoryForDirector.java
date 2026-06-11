package com.motd.be.module.director.promotion_code.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.promotion_code.entity.PromotionCode;

import jakarta.persistence.LockModeType;

public interface PromotionCodeRepositoryForDirector extends JpaRepository<PromotionCode, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT pc FROM PromotionCode pc
			WHERE pc.code = :code
			AND pc.isDeleted = false
		""")
	Optional<PromotionCode> findByCodeWithLock(@Param("code") String code);
}
