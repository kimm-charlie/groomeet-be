package com.motd.be.module.director.consulting_request.repository;

import static com.motd.be.common.constants.PageSizeConstants.*;
import static com.motd.be.common.constants.TimePolicy.*;
import static com.motd.be.module.member.consulting_request.entity.QConsultingRequest.*;
import static com.motd.be.module.member.member.entity.QMember.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_request.enums.ConsultingRequestStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ConsultingRequestQueryDslRepositoryForDirector {

	private final JPAQueryFactory query;

	public Slice<ConsultingRequest> findAllAvailable(Long cursorId) {
		List<ConsultingRequest> results = query
			.selectFrom(consultingRequest)
			.join(consultingRequest.member, member).fetchJoin()
			.where(
				availableCondition(),
				consultingRequest.isDeleted.isFalse(),
				filterByCursor(cursorId)
			)
			.orderBy(consultingRequest.createdAt.desc(), consultingRequest.id.desc())
			.limit(CONSULTING_REQUEST_PAGE_SIZE + 1)
			.fetch();

		boolean hasNext = results.size() > CONSULTING_REQUEST_PAGE_SIZE;
		List<ConsultingRequest> content = hasNext ? results.subList(0, CONSULTING_REQUEST_PAGE_SIZE) : results;

		return new SliceImpl<>(content, Pageable.unpaged(), hasNext);
	}

	public long countAvailable() {
		Long count = query
			.select(consultingRequest.count())
			.from(consultingRequest)
			.where(
				availableCondition(),
				consultingRequest.isDeleted.isFalse()
			)
			.fetchOne();

		return count != null ? count : 0;
	}

	private BooleanExpression availableCondition() {
		LocalDateTime expiryThreshold = LocalDateTime.now().minusMinutes(CONSULTING_REQUEST_RESERVATION_MINUTES);

		return consultingRequest.status.eq(ConsultingRequestStatus.PENDING)
			.or(consultingRequest.status.eq(ConsultingRequestStatus.RESERVED)
				.and(consultingRequest.reservedAt.before(expiryThreshold)));
	}

	private BooleanExpression filterByCursor(Long cursorId) {
		if (cursorId == null) {
			return null;
		}

		LocalDateTime cursorCreatedAt = query
			.select(consultingRequest.createdAt)
			.from(consultingRequest)
			.where(consultingRequest.id.eq(cursorId))
			.fetchOne();

		if (cursorCreatedAt == null) {
			return null;
		}

		return consultingRequest.createdAt.lt(cursorCreatedAt)
			.or(consultingRequest.createdAt.eq(cursorCreatedAt).and(consultingRequest.id.lt(cursorId)));
	}
}
