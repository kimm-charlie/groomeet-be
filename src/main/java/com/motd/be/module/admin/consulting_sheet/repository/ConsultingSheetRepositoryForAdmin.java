package com.motd.be.module.admin.consulting_sheet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;

import jakarta.persistence.LockModeType;

public interface ConsultingSheetRepositoryForAdmin extends JpaRepository<ConsultingSheet, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT cs
			FROM ConsultingSheet cs
			JOIN FETCH cs.consultingRequest cr
			WHERE cs.id = :id
			AND cs.isDeleted = false
		""")
	Optional<ConsultingSheet> findByIdWithLock(@Param("id") Long id);
}
