package com.motd.be.module.admin.service_request.repository;

import static com.motd.be.module.member.service_request.entity.QServiceRequest.*;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ServiceRequestQueryDslRepositoryForAdmin {

	private final JPAQueryFactory queryFactory;

	public Slice<ServiceRequest> findAll(String search, ServiceRequestStatus status, Pageable pageable) {
		List<ServiceRequest> content = queryFactory.selectFrom(serviceRequest)
			.leftJoin(serviceRequest.member).fetchJoin()
			.leftJoin(serviceRequest.directorService).fetchJoin()
			.leftJoin(serviceRequest.directRequestedMember).fetchJoin()
			.where(
				filterBySearch(search),
				statusEq(status)
			)
			.orderBy(serviceRequest.createdAt.desc())
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
		if (!search.matches("^[0-9]*$")) {
			return serviceRequest.member.nickname.containsIgnoreCase(search);
		}
		return serviceRequest.member.nickname.containsIgnoreCase(search)
			.or(serviceRequest.member.id.stringValue().contains(search));
	}

	private BooleanExpression statusEq(ServiceRequestStatus status) {
		return status != null ? serviceRequest.status.eq(status) : null;
	}
}

