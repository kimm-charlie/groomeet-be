package com.motd.be.module.member.service_estimate.repository;

import static com.motd.be.module.member.director_info.entity.QDirectorInfo.*;
import static com.motd.be.module.member.member.entity.QMember.*;
import static com.motd.be.module.member.service_estimate.entity.QServiceEstimate.*;
import static com.motd.be.module.member.service_request.entity.QServiceRequest.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ServiceEstimateQueryDslRepository {

	private final JPAQueryFactory query;

	public Slice<ServiceEstimate> findAllByServiceRequest(ServiceRequest requestedServiceRequest, Pageable pageable) {
		JPAQuery<ServiceEstimate> sql = query
			.select(serviceEstimate)
			.from(serviceEstimate)
			.join(serviceEstimate.serviceRequest, serviceRequest)
			.fetchJoin()
			.join(serviceRequest.directorService).fetchJoin()
			.join(serviceRequest.member).fetchJoin()
			.where(
				serviceRequest.id.eq(requestedServiceRequest.getId()),
				filterByServiceRequestStatus(requestedServiceRequest.getStatus()),
				filterIsDeletedTrue()
			)
			.orderBy(serviceEstimate.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1);

		List<ServiceEstimate> results = sql.fetch();

		boolean hasNext = results.size() > pageable.getPageSize();

		if (hasNext) {
			results.remove(results.size() - 1);
		}

		return new SliceImpl<>(results, pageable, hasNext);
	}

	private Predicate filterIsDeletedTrue() {
		return serviceEstimate.isDeleted.isFalse();
	}

	private BooleanExpression filterByServiceRequestStatus(ServiceRequestStatus status) {
		return switch (status) {
			case PENDING -> serviceEstimate.status.eq(ServiceEstimateStatus.PENDING);
			case ONGOING -> serviceEstimate.status.eq(ServiceEstimateStatus.ONGOING)
				.or(serviceEstimate.status.eq(ServiceEstimateStatus.DIRECTOR_DONE));
			case COMPLETED -> serviceEstimate.status.eq(ServiceEstimateStatus.COMPLETED_BY_MEMBER)
				.or(serviceEstimate.status.eq(ServiceEstimateStatus.REVIEW_COMPLETED));
			case CANCELED, EXPIRED -> null;
		};
	}

	public List<ServiceEstimate> findAllOngoingFilterByScheduleCompleted(LocalDateTime scheduledBefore) {
		return query
			.select(serviceEstimate)
			.from(serviceEstimate)
			.join(serviceEstimate.serviceRequest, serviceRequest)
			.join(serviceEstimate.directorInfo, directorInfo).fetchJoin()
			.join(directorInfo.member).fetchJoin()
			.where(
				serviceEstimate.status.eq(ServiceEstimateStatus.ONGOING),
				serviceEstimate.isDeleted.isFalse(),
				serviceRequest.status.eq(ServiceRequestStatus.ONGOING),
				serviceEstimate.scheduledAt.before(scheduledBefore)
			)
			.fetch();
	}

	public List<ServiceEstimate> findAllDirectorCompletedBefore(LocalDateTime completedBefore) {
		return query
			.select(serviceEstimate)
			.from(serviceEstimate)
			.join(serviceEstimate.serviceRequest, serviceRequest).fetchJoin()
			.join(serviceRequest.member, member).fetchJoin()
			.join(serviceEstimate.directorInfo, directorInfo).fetchJoin()
			.join(directorInfo.member).fetchJoin()
			.where(
				serviceEstimate.status.eq(ServiceEstimateStatus.DIRECTOR_DONE),
				serviceEstimate.isDeleted.isFalse(),
				serviceEstimate.directorDoneAt.before(completedBefore)
			)
			.fetch();
	}

	public Slice<ServiceEstimate> findServiceEstimateHistoriesForPublic(Member member, Pageable pageable,
		List<ServiceEstimateStatus> completedStatuses) {
		JPAQuery<ServiceEstimate> sql = query
			.select(serviceEstimate)
			.from(serviceEstimate)
			.join(serviceEstimate.serviceRequest, serviceRequest).fetchJoin()
			.join(serviceEstimate.directorInfo, directorInfo).fetchJoin()
			.join(directorInfo.member).fetchJoin()
			.where(
				serviceRequest.member.eq(member),
				serviceEstimate.status.in(completedStatuses),
				filterIsDeletedTrue()
			)
			.orderBy(serviceEstimate.memberCompletedAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1);

		List<ServiceEstimate> results = sql.fetch();

		boolean hasNext = results.size() > pageable.getPageSize();

		if (hasNext) {
			results.remove(results.size() - 1);
		}

		return new SliceImpl<>(results, pageable, hasNext);
	}

	/**
	 * 리뷰 작성 장려 Push 대상 조회
	 * - 사용자 완료(COMPLETED_BY_MEMBER) 상태 (리뷰 미작성 상태)
	 * - memberCompletedAt < memberCompletedBefore (1일 이상 경과)
	 * - reviewReminderSentAt IS NULL (푸시 미발송)
	 * - scheduledAt의 hour가 현재 시간 ± toleranceHours 범위 내
	 */
	public List<ServiceEstimate> findReviewReminderTargets(
		LocalDateTime memberCompletedBefore,
		int currentHour,
		int toleranceHours
	) {
		int minHour = (currentHour - toleranceHours + 24) % 24;
		int maxHour = (currentHour + toleranceHours) % 24;

		BooleanExpression hourCondition;
		if (minHour <= maxHour) {
			hourCondition = serviceEstimate.scheduledAt.hour().between(minHour, maxHour);
		} else {
			hourCondition = serviceEstimate.scheduledAt.hour().goe(minHour)
				.or(serviceEstimate.scheduledAt.hour().loe(maxHour));
		}

		return query
			.select(serviceEstimate)
			.from(serviceEstimate)
			.join(serviceEstimate.serviceRequest, serviceRequest).fetchJoin()
			.join(serviceRequest.member, member).fetchJoin()
			.join(serviceRequest.directorService).fetchJoin()
			.where(
				serviceEstimate.status.eq(ServiceEstimateStatus.COMPLETED_BY_MEMBER),
				serviceEstimate.isDeleted.isFalse(),
				serviceEstimate.memberCompletedAt.before(memberCompletedBefore),
				serviceEstimate.reviewReminderSentAt.isNull(),
				serviceEstimate.scheduledAt.isNotNull(),
				hourCondition
			)
			.fetch();
	}
}
