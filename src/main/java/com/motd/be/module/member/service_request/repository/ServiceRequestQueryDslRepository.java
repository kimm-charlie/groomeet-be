package com.motd.be.module.member.service_request.repository;

import static com.motd.be.module.member.director_service.entity.QDirectorService.*;
import static com.motd.be.module.member.service_request.entity.QServiceRequest.*;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ServiceRequestQueryDslRepository {

	private final JPAQueryFactory query;

	public Slice<ServiceRequest> findAllForMember(Member member, Long directorServiceId,
		Boolean showOnlyPending, Pageable pageable) {

		JPAQuery<ServiceRequest> sql = query.select(serviceRequest)
			.from(serviceRequest)
			.join(serviceRequest.directorService, directorService)
			.fetchJoin()
			.join(directorService.parent)
			.fetchJoin()
			.where(includeRequestsBy(member), filterByDirectorService(directorServiceId),
				filterByStatusIfPending(showOnlyPending),
				filterIsDeletedTrue())
			.orderBy(getOrderSpecifier())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1);

		List<ServiceRequest> results = sql.fetch();

		boolean hasNext = results.size() > pageable.getPageSize();

		if (hasNext) {
			results.remove(results.size() - 1);
		}

		return new SliceImpl<>(results, pageable, hasNext);
	}

	private BooleanExpression includeRequestsBy(Member member) {
		return serviceRequest.member.eq(member);
	}

	private BooleanExpression filterByDirectorService(Long directorServiceId) {
		if (directorServiceId == null) {
			return null;
		}
		return serviceRequest.directorService.id.eq(directorServiceId);
	}

	private OrderSpecifier<?> getOrderSpecifier() {
		return serviceRequest.createdAt.desc();
	}

	private Predicate filterIsDeletedTrue() {
		return serviceRequest.isDeleted.isFalse();
	}

	public List<ServiceRequest> findDirectorServicesByMember(Member member, Boolean showOnlyPending) {
		JPAQuery<ServiceRequest> sql = query.select(serviceRequest)
			.from(serviceRequest)
			.join(serviceRequest.directorService, directorService)
			.fetchJoin()
			.join(directorService.parent)
			.fetchJoin()
			.join(serviceRequest.member)
			.where(includeRequestsBy(member), filterByStatusIfPending(showOnlyPending),
				filterIsDeletedTrue())
			.orderBy(serviceRequest.createdAt.desc());

		return sql.fetch();
	}

	private Predicate filterByStatusIfPending(Boolean showOnlyPending) {
		if (showOnlyPending != null && showOnlyPending) {
			return serviceRequest.status.eq(ServiceRequestStatus.PENDING);
		}
		return null;
	}
}
