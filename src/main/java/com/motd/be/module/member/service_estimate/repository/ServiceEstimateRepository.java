package com.motd.be.module.member.service_estimate.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateReminderStatus;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.motd.be.module.member.service_request.entity.ServiceRequest;

public interface ServiceEstimateRepository extends JpaRepository<ServiceEstimate, Long> {

	@Query("""
			SELECT se.serviceRequest.id, COUNT(se)
			FROM ServiceEstimate se
			WHERE se.serviceRequest IN :serviceRequests
			AND se.status != :canceled
			AND se.isDeleted = false
			GROUP BY se.serviceRequest.id
		""")
	List<Object[]> countEstimatesByServiceRequests(@Param("serviceRequests") List<ServiceRequest> serviceRequests,
		@Param("canceled") ServiceEstimateStatus canceled);

	@Query("""
			SELECT se
			FROM ServiceEstimate se
			JOIN FETCH se.serviceRequest sr
			JOIN FETCH sr.directorService
			JOIN FETCH sr.member
			WHERE se.id = :id
			AND se.isDeleted = false
		""")
	Optional<ServiceEstimate> findByIdWithServiceRequest(Long id);

	@Query("""
			SELECT se
			FROM ServiceEstimate se
			JOIN FETCH se.serviceRequest sr
			JOIN FETCH se.directorInfo
			WHERE se.id = :serviceEstimateId
			AND se.isDeleted = false
		""")
	Optional<ServiceEstimate> findByIdWithRequestAndDirector(Long serviceEstimateId);

	@Query("""
			SELECT se
			FROM ServiceEstimate se
			JOIN se.serviceRequest sr
			WHERE se.directorInfo = :directorInfo
			AND se.status NOT IN :endedStatus
			AND (:directorServiceId IS NULL OR sr.directorService.id = :directorServiceId)
			AND se.isDeleted = false
		""")
	List<ServiceEstimate> findAllByDirectorInfoAndStatus(@Param("directorInfo") DirectorInfo directorInfo,
		@Param("endedStatus") List<ServiceEstimateStatus> endedStatus,
		@Param("directorServiceId") Long directorServiceId);

	@Modifying
	@Query("""
			UPDATE ServiceEstimate se
			SET se.status = :expired,
				se.expiredAt = :now
			WHERE se.serviceRequest IN :serviceRequests
			AND se.status = :pending
			AND se.isDeleted = false
		""")
	void updateToExpiredStatusByServiceRequests(@Param("serviceRequests") List<ServiceRequest> serviceRequests,
		@Param("expired") ServiceEstimateStatus expired, @Param("pending") ServiceEstimateStatus pending,
		@Param("now") LocalDateTime now);

	@Query("""
		        SELECT se
		        FROM ServiceEstimate se
		        JOIN FETCH se.serviceRequest sr
		        WHERE se.id = :serviceEstimateId
		        AND se.isDeleted = false
		""")
	Optional<ServiceEstimate> findByIdWithServiceRequestLock(Long serviceEstimateId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT se
			FROM ServiceEstimate se
			JOIN FETCH se.serviceRequest sr
			JOIN FETCH sr.member
			JOIN FETCH sr.directorService
			JOIN FETCH se.directorInfo di
			JOIN FETCH di.member
			WHERE se.id = :serviceEstimateId
			AND se.isDeleted = false
		""")
	Optional<ServiceEstimate> findByIdWithRequestAndDirectorLock(Long serviceEstimateId);

	@Query("""
		    SELECT CASE WHEN COUNT(se) > 0 THEN true ELSE false END
		    FROM ServiceEstimate se
		    WHERE se.isDeleted = false
		      AND se.status IN :status
		      AND (
		           (se.directorInfo.member = :member AND se.serviceRequest.member = :target)
		        OR (se.directorInfo.member = :target AND se.serviceRequest.member = :member)
		      )
		""")
	boolean existsByDirectorInfoAndMemberAndStatus(
		@Param("member") Member member,
		@Param("target") Member target,
		@Param("status") List<ServiceEstimateStatus> status
	);

	@Query("""
			  SELECT COUNT(se)
			  FROM ServiceEstimate se
			  WHERE se.serviceRequest.member = :writer
			  AND se.directorInfo = :directorInfo
			  AND se.status IN :completedEstimateStatuses
			  AND se.isDeleted = false
		""")
	int countCompletedEstimatesByRequesterIdAndDirectorInfo(@Param("writer") Member writer,
		@Param("directorInfo") DirectorInfo directorInfo,
		@Param("completedEstimateStatuses") List<ServiceEstimateStatus> completedEstimateStatuses);

	@Query("""
		        SELECT se.serviceRequest.member.id, COUNT(se)
		        FROM ServiceEstimate se
		        WHERE se.serviceRequest.member IN :members
		        AND se.directorInfo = :directorInfo
		        AND se.status IN :completedEstimateStatuses
		        AND se.isDeleted = false
		        GROUP BY se.serviceRequest.member.id
		""")
	List<Object[]> countCompletedEstimatesByRequesterIdsAndDirectorInfo(List<Member> members,
		DirectorInfo directorInfo, List<ServiceEstimateStatus> completedEstimateStatuses);

	@Modifying
	@Query("""
			UPDATE ServiceEstimate se
			SET se.status = :canceled,
					se.canceledAt = :now
			WHERE se.serviceRequest IN :serviceRequests
			AND se.isDeleted = false
			AND se.status != :canceled
		""")
	void updateStatusToCanceledByServiceRequests(@Param("serviceRequests") List<ServiceRequest> serviceRequests,
		@Param("canceled") ServiceEstimateStatus canceled, @Param("now") LocalDateTime now);

	@Query("""
			SELECT se
			FROM ServiceEstimate se
			WHERE se.directorInfo = :directorInfo
			AND se.status NOT IN :endedEstimateStatuses
			AND se.isDeleted = false
		""")
	List<ServiceEstimate> findAllByDirectorInfoNotYetEnded(@Param("directorInfo") DirectorInfo directorInfo,
		@Param("endedEstimateStatuses") List<ServiceEstimateStatus> endedEstimateStatuses);

	@Modifying
	@Query("""
			UPDATE ServiceEstimate se
			SET se.status = :canceled,
				se.canceledAt = :now
			WHERE se IN :serviceEstimates
			AND se.isDeleted = false
			AND se.status != :canceled
		""")
	void updateStatusToCanceledByServiceEstimates(@Param("serviceEstimates") List<ServiceEstimate> serviceEstimates,
		@Param("canceled") ServiceEstimateStatus canceledStatus, @Param("now") LocalDateTime now);

	@Query("""
		    SELECT se.serviceRequest.id, m
		    FROM ServiceEstimate se
		    JOIN se.directorInfo di
		    JOIN di.member m
		    WHERE se.serviceRequest IN :serviceRequests
		      AND se.isDeleted = false
		      AND se.status != :canceledStatus
		      AND (
		            se.isHired = true
		            OR NOT EXISTS (
		                SELECT 1
		                FROM ServiceEstimate se2
		                WHERE se2.serviceRequest = se.serviceRequest
		                  AND se2.isDeleted = false
		                  AND se2.isHired = true
		            )
		      )
		""")
	List<Object[]> findDirectorsByServiceRequests(
		@Param("serviceRequests") List<ServiceRequest> serviceRequests,
		@Param("canceledStatus") ServiceEstimateStatus canceledStatus
	);

	@Query("""
			SELECT se 
			FROM ServiceEstimate se
			WHERE se.id = :serviceEstimateId
			AND se.isDeleted = false
		""")
	Optional<ServiceEstimate> findByIdWithIsDeletedFalse(Long serviceEstimateId);

	@Modifying
	@Query("""
			UPDATE ServiceEstimate se
			SET se.status = :expiredStatus,
				se.expiredAt = :now
			WHERE
				se.serviceRequest = :serviceRequest
			AND se.isDeleted = false
			AND se.status NOT IN :endedEstimateStatuses
			AND se != :ongoingEstimate
		""")
	void updateToExpiredStatusByServiceRequest(@Param("ongoingEstimate") ServiceEstimate serviceEstimate,
		@Param("serviceRequest") ServiceRequest serviceRequest,
		@Param("expiredStatus") ServiceEstimateStatus expiredStatus, @Param("now") LocalDateTime now,
		@Param("endedEstimateStatuses") List<ServiceEstimateStatus> endedEstimateStatuses);

	@Modifying
	@Query("""
			UPDATE ServiceEstimate se
			SET se.status = :directorCompletedStatus,
				se.directorDoneAt = :now
			WHERE se IN :serviceEstimates
			AND se.isDeleted = false
		""")
	void updateToDirectorCompleted(@Param("serviceEstimates") List<ServiceEstimate> serviceEstimates,
		@Param("directorCompletedStatus") ServiceEstimateStatus directorCompletedStatus,
		@Param("now") LocalDateTime now);

	@Modifying
	@Query("""
			UPDATE ServiceEstimate se
			SET se.status = :memberCompletedStatus,
				se.memberCompletedAt = :now
			WHERE se IN :targetEstimates
			AND se.isDeleted = false
		""")
	void updateToMemberCompleted(@Param("targetEstimates") List<ServiceEstimate> targetEstimates,
		@Param("memberCompletedStatus") ServiceEstimateStatus memberCompletedStatus, @Param("now") LocalDateTime now);

	@Query("""
			SELECT CASE WHEN COUNT(se) > 0 THEN true ELSE false END
			FROM ServiceEstimate se
			JOIN se.serviceRequest sr
			WHERE sr.member = :member
			AND se.directorInfo.id = :directorInfoId
			AND se.status IN :ongoingEstimateStatuses
			AND se.isDeleted = false
		""")
	boolean existsByMemberAndDirectorInfoAndOngoingStatus(@Param("member") Member member,
		@Param("directorInfoId") Long directorInfoId,
		@Param("ongoingEstimateStatuses") List<ServiceEstimateStatus> ongoingEstimateStatuses);

	@Query("""
			SELECT CASE WHEN COUNT(se) > 0 THEN true ELSE false END
			FROM ServiceEstimate se
			JOIN se.serviceRequest sr
			WHERE sr.member = :member
			AND se.directorInfo.id = :directorInfoId
			AND se.status NOT IN :endedStatus
			AND se.isDeleted = false
		""")
	boolean existsByMemberAndDirectorNotEndedStatus(@Param("member") Member member,
		@Param("directorInfoId") Long directorInfoId,
		@Param("endedStatus") List<ServiceEstimateStatus> endedStatus);

	@Modifying
	@Query("""
				UPDATE ServiceEstimate se
				SET se.reviewReminderSentAt = :now
				WHERE se IN :serviceEstimates
				AND se.isDeleted = false
			""")
	void updateReviewReminderSentAt(@Param("serviceEstimates") List<ServiceEstimate> serviceEstimates,
		@Param("now") LocalDateTime now);

	@Query("""
			SELECT se
			FROM ServiceEstimate se
			JOIN FETCH se.serviceRequest sr
			JOIN FETCH sr.member
			JOIN FETCH se.directorInfo di
			JOIN FETCH di.member
			WHERE se.isDeleted = false
			AND se.status = :ongoingStatus
			AND se.reminderStatus = :pendingStatus
			AND se.reminderNeedAt <= :reminderNeedAt
			AND se.scheduledAt > :tomorrowStartAt
		""")
	List<ServiceEstimate> findReminderTargets(
		@Param("reminderNeedAt") LocalDateTime reminderNeedAt,
		@Param("pendingStatus") ServiceEstimateReminderStatus pendingStatus,
		@Param("ongoingStatus") ServiceEstimateStatus ongoingStatus,
		@Param("tomorrowStartAt") LocalDateTime tomorrowStartAt);

	@Query("""
			SELECT se.scheduledAt
			FROM ServiceEstimate se
			WHERE se.directorInfo.member.id = :directorMemberId
			AND se.status = 'ONGOING'
			AND se.isDeleted = false
			AND se.scheduledAt IS NOT NULL
			AND se.scheduledAt >= :startOfDay
			AND se.scheduledAt < :startOfNextDay
		""")
	List<LocalDateTime> findScheduledAtByDirectorMemberIdAndDate(@Param("directorMemberId") Long directorMemberId,
		@Param("startOfDay") LocalDateTime startOfDay, @Param("startOfNextDay") LocalDateTime startOfNextDay);

	@Query("""
			SELECT se.scheduledAt
			FROM ServiceEstimate se
			WHERE se.directorInfo.member.id = :directorMemberId
			AND se.status = 'ONGOING'
			AND se.isDeleted = false
			AND se.scheduledAt IS NOT NULL
			AND se.scheduledAt >= :startOfDay
			AND se.scheduledAt < :startOfNextDay
			AND se.id != :excludeEstimateId
		""")
	List<LocalDateTime> findScheduledAtByDirectorMemberIdAndDateExcluding(
		@Param("directorMemberId") Long directorMemberId,
		@Param("startOfDay") LocalDateTime startOfDay,
		@Param("startOfNextDay") LocalDateTime startOfNextDay,
		@Param("excludeEstimateId") Long excludeEstimateId);
}
