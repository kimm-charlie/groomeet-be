package com.motd.be.module.member.service_request.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

	@Query("""
			SELECT sr
			FROM ServiceRequest sr
			JOIN FETCH sr.directorService
			WHERE sr.id = :serviceRequestId
			AND sr.isDeleted = false
		""")
	Optional<ServiceRequest> findByIdWithDirectorService(Long serviceRequestId);

	@Query("""
		        SELECT sr
		        FROM ServiceRequest sr
		        WHERE sr.id IN :expiredRequestIds
		        AND sr.status = :pending
		        AND sr.isDeleted = false
		""")
	List<ServiceRequest> findAllByIdWithIsDeletedFalse(@Param("expiredRequestIds") List<Long> expiredRequestIds,
		@Param("pending") ServiceRequestStatus pending);

	@Query("""
		        SELECT sr
		        FROM ServiceRequest sr
		        WHERE sr.expiredAt <= :now
		        AND sr.status = :pending
		        AND sr.isDeleted = false
		""")
	List<ServiceRequest> findAllExpiredBefore(@Param("now") LocalDateTime now,
		@Param("pending") ServiceRequestStatus pending);

	@Query("""
		    SELECT sr
		    FROM ServiceRequest sr
		    WHERE sr.member = :member
		    AND sr.isDeleted = false
		    AND sr.status NOT IN (:endedStatus)
		""")
	List<ServiceRequest> findAllByMemberNotYetEnded(@Param("member") Member member,
		@Param("endedStatus") List<ServiceRequestStatus> endedStatus);

	@Query("""
		    SELECT sr.status, COUNT(sr)
		    FROM ServiceRequest sr
		    WHERE sr.member.id = :memberId
		    AND sr.isDeleted = false
		    GROUP BY sr.status
		""")
	List<Object[]> countByDirectorInfoGroupByStatus(@Param("memberId") Long memberId);

	@Modifying
	@Query("""
			UPDATE ServiceRequest sr
			SET sr.status = :expired,
				sr.isReceivingEstimate = false,
				sr.expiredAt = :now
			WHERE sr IN :serviceRequests
		""")
	void updateStatusToExpired(@Param("serviceRequests") List<ServiceRequest> serviceRequests,
		@Param("expired") ServiceRequestStatus expired, @Param("now") LocalDateTime now);

	@Modifying
	@Query("""
			UPDATE ServiceRequest sr
			SET sr.status = :canceled,
				sr.canceledAt = :now
			WHERE sr IN :serviceRequests
		""")
	void updateStatusToCancel(@Param("serviceRequests") List<ServiceRequest> serviceRequests,
		@Param("canceled") ServiceRequestStatus canceledStatus, @Param("now") LocalDateTime now);

	@Modifying
	@Query("""
			UPDATE ServiceRequest sr
			SET sr.status = :completed,
				sr.completedAt = :now
			WHERE sr.id IN :serviceRequestIds
		""")
	void updateStatusToCompleted(@Param("serviceRequestIds") List<Long> serviceRequestIds,
		@Param("completed") ServiceRequestStatus serviceRequestStatus, @Param("now") LocalDateTime now);

	@Query("""
		  SELECT EXISTS(
			  SELECT 1
			  FROM ServiceRequest sr
			WHERE sr.member = :member
			  AND sr.directorService = :directorService
			  AND sr.createdAt >= :oneDaysAgo
		    )
		""")
	boolean existsByMemberAndDirectorServiceInLast24Hours(@Param("member") Member member,
		@Param("directorService") DirectorService directorService, @Param("oneDaysAgo") LocalDateTime oneDaysAgo);

	@Query("""
			SELECT DISTINCT sr
			FROM ServiceRequest sr
			JOIN FETCH sr.requestLocationMappings rlm
			JOIN FETCH rlm.location l
			LEFT JOIN FETCH l.parent
			WHERE sr.id IN :serviceRequestIds
			AND sr.status = :pending
			AND sr.isReceivingEstimate = true
			AND sr.receivedEstimateCount < :maxEstimateCount
			AND sr.isLocationExpanded = false
			AND sr.isDeleted = false
			AND l.parent IS NOT NULL
		""")
	List<ServiceRequest> findAllForLocationExpansion(@Param("serviceRequestIds") List<Long> serviceRequestIds,
		@Param("pending") ServiceRequestStatus pending, @Param("maxEstimateCount") int maxEstimateCount);

	@Query("""
			SELECT DISTINCT sr.id
			FROM ServiceRequest sr
			JOIN sr.requestLocationMappings rlm
			JOIN rlm.location l
			WHERE sr.status = :pending
			AND sr.isReceivingEstimate = true
			AND sr.receivedEstimateCount < :maxEstimateCount
			AND sr.isLocationExpanded = false
			AND sr.isDeleted = false
			AND l.parent IS NOT NULL
			AND sr.createdAt <= :expandThreshold
		""")
	List<Long> findIdsForLocationExpansionBefore(@Param("expandThreshold") LocalDateTime expandThreshold,
		@Param("pending") ServiceRequestStatus pending, @Param("maxEstimateCount") int maxEstimateCount);

	@Query("""
		SELECT CASE WHEN COUNT(sr) > 0 THEN true ELSE false END
		FROM ServiceRequest sr
		WHERE sr.member.id = :memberId
		AND sr.isDeleted = false
		AND sr.status NOT IN (:endedStatuses)
		AND (
		    sr.directRequestedMember.id = :targetMemberId
		    OR
		    EXISTS (
		        SELECT 1
		        FROM ServiceEstimate se
		        JOIN se.directorInfo di
		        JOIN di.member director
		        WHERE se.serviceRequest = sr
		        AND director.id = :targetMemberId
		        AND se.isDeleted = false
		        AND se.status != :canceled
		    )
		)
		""")
	boolean existsNotEndedRequestBetweenMemberAndDirector(@Param("memberId") Long memberId,
		@Param("targetMemberId") Long targetMemberId, @Param("endedStatuses") List<ServiceRequestStatus> endedStatuses,
		@Param("canceled") ServiceEstimateStatus canceled);

	@Query("""
			SELECT COUNT(sr)
			FROM ServiceRequest sr
			WHERE sr.status = :status
			AND sr.ongoingAt >= :startOfDay
			AND sr.ongoingAt < :endOfDay
			AND sr.isDeleted = false
		""")
	long countByStatusAndOngoingAtBetween(@Param("status") ServiceRequestStatus status,
		@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

	@Query("""
			SELECT COUNT(sr)
			FROM ServiceRequest sr
			WHERE sr.status = :status
			AND sr.isReceivingEstimate = true
			AND sr.receivedEstimateCount = 0
			AND sr.isDeleted = false
		""")
	long countByStatusAndNoEstimate(@Param("status") ServiceRequestStatus status);
}
