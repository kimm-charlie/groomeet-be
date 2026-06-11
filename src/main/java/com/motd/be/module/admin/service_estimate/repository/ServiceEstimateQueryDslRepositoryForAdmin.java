package com.motd.be.module.admin.service_estimate.repository;

import static com.motd.be.module.member.service_estimate.entity.QServiceEstimate.*;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.motd.be.module.member.member.entity.QMember;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimate;
import com.motd.be.module.member.service_estimate.entity.ServiceEstimateStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ServiceEstimateQueryDslRepositoryForAdmin {

	private final JPAQueryFactory queryFactory;

	public Slice<ServiceEstimate> findAll(String search, ServiceEstimateStatus status, Pageable pageable) {
		List<ServiceEstimate> content = queryFactory.selectFrom(serviceEstimate)
			.leftJoin(serviceEstimate.directorInfo).fetchJoin()
			.leftJoin(serviceEstimate.directorInfo.member).fetchJoin()
			.leftJoin(serviceEstimate.serviceRequest).fetchJoin()
			.leftJoin(serviceEstimate.serviceRequest.member).fetchJoin()
			.leftJoin(serviceEstimate.serviceRequest.directorService).fetchJoin()
			.where(
				filterBySearch(search),
				statusEq(status),
				serviceEstimate.isDeleted.isFalse()
			)
			.orderBy(serviceEstimate.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		boolean hasNext = content.size() > pageable.getPageSize();
		if (hasNext) {
			content = content.subList(0, pageable.getPageSize());
		}

		return new SliceImpl<>(content, pageable, hasNext);
	}

	private BooleanExpression filterBySearch(String search) {
		if (search == null || search.isBlank()) {
			return null;
		}
		QMember directorMember = serviceEstimate.directorInfo.member;
		if (!search.matches("^[0-9]*$")) {
			return directorMember.nickname.containsIgnoreCase(search);
		}
		return directorMember.nickname.containsIgnoreCase(search)
			.or(directorMember.id.stringValue().contains(search));
	}

	private BooleanExpression statusEq(ServiceEstimateStatus status) {
		return status != null ? serviceEstimate.status.eq(status) : null;
	}
}
