package com.motd.be.module.director.consulting_request.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_request.enums.ConsultingRequestStatus;
import com.motd.be.module.member.director_info.entity.DirectorInfo;

import jakarta.persistence.LockModeType;

public interface ConsultingRequestRepositoryForDirector extends JpaRepository<ConsultingRequest, Long> {

	@Query("""
			SELECT cr
			FROM ConsultingRequest cr
			WHERE cr.id = :consultingRequestId
			AND cr.isDeleted = false
		""")
	Optional<ConsultingRequest> findByIdAndNotDeleted(@Param("consultingRequestId") Long consultingRequestId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT cr
			FROM ConsultingRequest cr
			WHERE cr.id = :consultingRequestId
			AND cr.isDeleted = false
		""")
	Optional<ConsultingRequest> findByIdWithLock(@Param("consultingRequestId") Long consultingRequestId);

	@Modifying
	@Query("""
			UPDATE ConsultingRequest cr
			SET cr.status = :pendingStatus,
				cr.reservedBy = null,
				cr.reservedAt = null
			WHERE cr.reservedBy = :directorInfo
			AND cr.status = :reservedStatus
			AND cr.id <> :consultingRequestId
			AND cr.isDeleted = false
		""")
	int releaseOtherReservations(@Param("directorInfo") DirectorInfo directorInfo,
		@Param("reservedStatus") ConsultingRequestStatus reservedStatus,
		@Param("pendingStatus") ConsultingRequestStatus pendingStatus,
		@Param("consultingRequestId") Long consultingRequestId);
}
