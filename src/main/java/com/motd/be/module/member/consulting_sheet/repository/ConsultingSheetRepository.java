package com.motd.be.module.member.consulting_sheet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.consulting_sheet.entity.ConsultingSheet;
import com.motd.be.module.member.consulting_sheet.enums.ConsultingSheetStatus;
import com.motd.be.module.member.member.entity.Member;

public interface ConsultingSheetRepository extends JpaRepository<ConsultingSheet, Long> {

	@Query("""
			SELECT cs FROM ConsultingSheet cs
			WHERE cs.consultingRequest.member = :member
			AND cs.status = :status
			AND cs.isDeleted = false
		""")
	Optional<ConsultingSheet> findByConsultingRequestMemberAndStatus(
		@Param("member") Member member,
		@Param("status") ConsultingSheetStatus status);

	@Query("""
			SELECT cs FROM ConsultingSheet cs
			JOIN FETCH cs.consultingRequest cr
			JOIN FETCH cs.directorInfo di
			JOIN FETCH di.member
			WHERE cs.id = :id
			AND cs.status = :status
			AND cs.isDeleted = false
		""")
	Optional<ConsultingSheet> findByIdAndStatusWithDetails(@Param("id") Long id,
		@Param("status") ConsultingSheetStatus status);
}
