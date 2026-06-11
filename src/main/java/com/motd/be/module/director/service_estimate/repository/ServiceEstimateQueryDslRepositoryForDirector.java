package com.motd.be.module.director.service_estimate.repository;

import static com.motd.be.module.member.service_estimate.entity.QServiceEstimate.*;
import static com.motd.be.module.member.service_request.entity.QServiceRequest.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.ServiceEstimateException;
import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ServiceEstimateQueryDslRepositoryForDirector {

	private final JPAQueryFactory query;

	public Slice<ServiceEstimate> findAll(ServiceEstimateStatus status, DirectorInfo directorInfo, Pageable pageable,
		Long directorServiceId, Boolean showOnlyDirectRequest) {

		JPAQuery<ServiceEstimate> sql = query
			.select(serviceEstimate)
			.from(serviceEstimate)
			.join(serviceEstimate.serviceRequest, serviceRequest).fetchJoin()
			.join(serviceRequest.directorService).fetchJoin()
			.join(serviceRequest.member).fetchJoin()
			.where(filterByDirectorServiceId(directorServiceId),
				filterByDirectorInfo(directorInfo), filterByStatus(status), filterIsDeletedTrue(),
				filterShowOnlyDirectRequest(showOnlyDirectRequest, directorInfo))
			.orderBy(getOrderSpecifier(status))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1);

		List<ServiceEstimate> results = sql.fetch();

		boolean hasNext = results.size() > pageable.getPageSize();

		if (hasNext) {
			results.remove(results.size() - 1);
		}

		return new SliceImpl<>(results, pageable, hasNext);
	}

	public Slice<ServiceEstimate> findServiceEstimateHistoriesForDirector(DirectorInfo directorInfo, Pageable pageable,
		List<ServiceEstimateStatus> completedStatuses) {
		JPAQuery<ServiceEstimate> sql = query
			.select(serviceEstimate)
			.from(serviceEstimate)
			.join(serviceEstimate.serviceRequest, serviceRequest).fetchJoin()
			.join(serviceRequest.member).fetchJoin()
			.where(
				filterByDirectorInfo(directorInfo),
				serviceEstimate.status.in(completedStatuses),
				filterIsDeletedTrue()
			)
			.orderBy(serviceEstimate.ongoingAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1);

		List<ServiceEstimate> results = sql.fetch();

		boolean hasNext = results.size() > pageable.getPageSize();

		if (hasNext) {
			results.remove(results.size() - 1);
		}

		return new SliceImpl<>(results, pageable, hasNext);
	}

	private Predicate filterShowOnlyDirectRequest(Boolean showOnlyDirectRequest, DirectorInfo directorInfo) {
		if (Boolean.TRUE.equals(showOnlyDirectRequest)) {
			return serviceEstimate.serviceRequest.isDirectRequest.isTrue()
				.and(serviceEstimate.serviceRequest.directRequestedMember.id.eq(directorInfo.getMember().getId()));
		}
		return null;
	}

	private Predicate filterIsDeletedTrue() {
		return serviceEstimate.isDeleted.isFalse();
	}

	private BooleanExpression filterByStatus(ServiceEstimateStatus status) {
		return switch (status) {
			case PENDING -> serviceEstimate.status.eq(ServiceEstimateStatus.PENDING);
			case ONGOING, DIRECTOR_DONE -> serviceEstimate.status.eq(ServiceEstimateStatus.ONGOING)
				.or(serviceEstimate.status.eq(ServiceEstimateStatus.DIRECTOR_DONE));
			case COMPLETED_BY_MEMBER, REVIEW_COMPLETED ->
				serviceEstimate.status.eq(ServiceEstimateStatus.COMPLETED_BY_MEMBER)
					.or(serviceEstimate.status.eq(ServiceEstimateStatus.REVIEW_COMPLETED));
			case CANCELED -> serviceEstimate.status.eq(ServiceEstimateStatus.CANCELED);
			case EXPIRED -> serviceEstimate.status.eq(ServiceEstimateStatus.EXPIRED);
		};
	}

	private BooleanExpression filterByDirectorServiceId(Long directorServiceId) {
		if (directorServiceId != null) {
			return serviceRequest.directorService.id.eq(directorServiceId);
		}
		return null;
	}

	private BooleanExpression filterByDirectorInfo(DirectorInfo directorInfo) {
		if (directorInfo != null) {
			return serviceEstimate.directorInfo.eq(directorInfo);
		}
		return null;
	}

	private OrderSpecifier<?> getOrderSpecifier(ServiceEstimateStatus status) {
		return switch (status) {
			case PENDING -> serviceEstimate.createdAt.desc();
			case ONGOING, DIRECTOR_DONE -> {
				DateTimeExpression<?> orderExpr = Expressions.dateTimeTemplate(
					LocalDateTime.class,
					"coalesce({0}, {1})",
					serviceEstimate.directorDoneAt,
					serviceEstimate.ongoingAt
				);
				yield orderExpr.desc();
			}
			case COMPLETED_BY_MEMBER -> serviceEstimate.memberCompletedAt.desc();
			case CANCELED, EXPIRED -> {
				DateTimeExpression<?> orderExpr = Expressions.dateTimeTemplate(
					LocalDateTime.class,
					"coalesce({0}, {1})",
					serviceEstimate.canceledAt,
					serviceEstimate.expiredAt
				);
				yield orderExpr.desc();
			}
			default -> throw new CustomRuntimeException(ServiceEstimateException.STATUS_NOT_FOUND);
		};
	}
}
