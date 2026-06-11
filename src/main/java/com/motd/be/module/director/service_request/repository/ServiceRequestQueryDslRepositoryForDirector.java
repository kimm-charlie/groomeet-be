package com.motd.be.module.director.service_request.repository;

import static com.motd.be.common.constants.Constants.*;
import static com.motd.be.common.constants.ValidationConstants.*;
import static com.motd.be.module.member.director_location_mapping.entity.QDirectorLocationMapping.*;
import static com.motd.be.module.member.director_service.entity.QDirectorService.*;
import static com.motd.be.module.member.request_location_mapping.entity.QRequestLocationMapping.*;
import static com.motd.be.module.member.service_estimate.entity.QServiceEstimate.*;
import static com.motd.be.module.member.service_request.entity.QServiceRequest.*;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.motd.be.module.member.director_info.entity.DirectorInfo;
import com.motd.be.module.member.location.entity.LocationType;
import com.motd.be.module.member.location.entity.QLocation;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.service_request.entity.ServiceRequest;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ServiceRequestQueryDslRepositoryForDirector {

	private final JPAQueryFactory query;

	public Slice<ServiceRequest> findAllForDirector(Member director, List<Long> directorServiceIds, Pageable pageable,
		List<Long> blockedMemberIds, Boolean showOnlyDirectRequest, Set<Long> hiddenRequestIds) {

		JPAQuery<ServiceRequest> sql = query
			.select(serviceRequest)
			.from(serviceRequest)
			.join(serviceRequest.directorService, directorService)
			.fetchJoin()
			.join(directorService.parent)
			.fetchJoin()
			.join(serviceRequest.member)
			.fetchJoin()
			.where(
				filterByDirectorServices(directorServiceIds),
				filterOnlyPendingAndIsReceivingEstimateTrue(),
				filterIsDeletedTrue(),
				filterDirectRequest(director),
				excludeOwnRequests(director),
				excludeBlockedMembers(blockedMemberIds),
				excludeAlreadyEstimatedByDirector(director.getDirectorInfo()),
				excludeRequestsWithSentEstimate(director.getDirectorInfo()),
				filterOnlyDirectRequest(showOnlyDirectRequest, director),
				filterEstimateCountUnderLimit(),
				filterHiddenRequests(hiddenRequestIds),
				filterMatchedLocations(director.getDirectorInfo()))
			.orderBy(serviceRequest.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1);

		List<ServiceRequest> results = sql.fetch();

		boolean hasNext = results.size() > pageable.getPageSize();

		if (hasNext) {
			results.remove(results.size() - 1);
		}

		return new SliceImpl<>(results, pageable, hasNext);
	}

	private BooleanExpression filterEstimateCountUnderLimit() {
		return serviceRequest.receivedEstimateCount.lt(MAX_RECEIVED_ESTIMATE_COUNT);
	}

	private BooleanExpression filterOnlyDirectRequest(Boolean showOnlyDirectRequest, Member director) {
		if (showOnlyDirectRequest) {
			return serviceRequest.isDirectRequest.isTrue().and(serviceRequest.directRequestedMember.eq(director));
		}
		return null;
	}

	private Predicate filterHiddenRequests(Set<Long> hiddenRequestIds) {
		if (hiddenRequestIds == null || hiddenRequestIds.isEmpty()) {
			return null;
		}

		return serviceRequest.id.notIn(hiddenRequestIds);
	}

	private Predicate filterIsDeletedTrue() {
		return serviceRequest.isDeleted.isFalse();
	}

	private BooleanExpression excludeOwnRequests(Member member) {
		return serviceRequest.member.ne(member);
	}

	private BooleanExpression filterDirectRequest(Member member) {
		return serviceRequest.isDirectRequest.isFalse().or(serviceRequest.directRequestedMember.eq(member));
	}

	private BooleanExpression filterOnlyPendingAndIsReceivingEstimateTrue() {
		return serviceRequest.status.eq(ServiceRequestStatus.PENDING).and(serviceRequest.isReceivingEstimate.isTrue());
	}

	private BooleanExpression filterByDirectorServices(List<Long> directorServiceIds) {
		return serviceRequest.directorService.id.in(directorServiceIds);
	}

	private BooleanExpression excludeBlockedMembers(List<Long> blockedMemberIds) {
		if (blockedMemberIds == null || blockedMemberIds.isEmpty()) {
			return null;
		}

		return serviceRequest.member.id.notIn(blockedMemberIds);
	}

	// 요청를 올린 회원과, 디렉터간의 종료되지 않은 제안이 있다면, 해당 회원의 요청는 디렉터의 요청시장에 보이지 않는다.
	private BooleanExpression excludeAlreadyEstimatedByDirector(DirectorInfo directorInfo) {
		return serviceRequest.member.id.notIn(
			JPAExpressions
				.select(serviceEstimate.serviceRequest.member.id)
				.from(serviceEstimate)
				.where(
					serviceEstimate.directorInfo.eq(directorInfo)
						.and(serviceEstimate.isDeleted.isFalse())
						.and(serviceEstimate.status.notIn(ENDED_ESTIMATE_STATUSES))
				)
		);
	}

	// 디렉터가 이미 제안을 보낸 특정 요청는 보이지 않는다 (취소된 제안 포함)
	private BooleanExpression excludeRequestsWithSentEstimate(DirectorInfo directorInfo) {
		return serviceRequest.id.notIn(
			JPAExpressions
				.select(serviceEstimate.serviceRequest.id)
				.from(serviceEstimate)
				.where(
					serviceEstimate.directorInfo.eq(directorInfo)
						.and(serviceEstimate.isDeleted.isFalse())
				)
		);
	}

	/**
	 * 요청(ServiceRequest)과 디렉터(Director)의 지역 매칭 조건
	 * <p>
	 * EXISTS (
	 * 요청의 location 과 디렉터의 location 이 아래 조건 중 하나라도 만족하면 매칭
	 * <p>
	 * 1) 둘 중 하나라도 ALL_CITY(전국) 인 경우
	 * 2) 요청 location 과 디렉터 location 이 동일한 경우
	 * 3) 디렉터 location 이 요청 location 의 상위인 경우
	 * - 예) 디렉터: 서울시 / 요청: 송파구
	 * 4) 요청 location 이 디렉터 location 의 상위인 경우
	 * - 예) 요청: 서울시 / 디렉터: 강남구
	 * )
	 * <p>
	 * 즉, 요청과 디렉터가 동일한 지역이거나
	 * 같은 지역 트리(전국 → 시 → 구) 내에서 상·하위 관계이면 매칭된다.
	 */
	private BooleanExpression filterMatchedLocations(DirectorInfo directorInfo) {

		BooleanExpression originalExists =
			JPAExpressions
				.selectOne()
				.from(requestLocationMapping)
				.join(directorLocationMapping)
				.on(directorLocationMapping.directorInfo.eq(directorInfo))
				.where(
					requestLocationMapping.serviceRequest.eq(serviceRequest),
					matchLocationCondition(
						directorLocationMapping.location,
						requestLocationMapping.location
					)
				)
				.exists();

		BooleanExpression expandedExists =
			JPAExpressions
				.selectOne()
				.from(directorLocationMapping)
				.where(
					directorLocationMapping.directorInfo.eq(directorInfo),
					serviceRequest.isLocationExpanded.isTrue(),
					serviceRequest.expandedLocation.isNotNull(),
					matchLocationCondition(
						directorLocationMapping.location,
						serviceRequest.expandedLocation
					)
				)
				.exists();

		return originalExists.or(expandedExists);
	}

	private BooleanExpression matchLocationCondition(
		QLocation directorLocation,
		QLocation requestLocation
	) {
		return
			// 0) 둘 중 하나라도 전국
			directorLocation.type.eq(LocationType.ALL_CITY)
				.or(requestLocation.type.eq(LocationType.ALL_CITY))

				// 1) 동일 location
				.or(directorLocation.eq(requestLocation))

				// 2) 디렉터가 상위
				.or(directorLocation.eq(requestLocation.parent))

				// 3) 요청이 상위
				.or(directorLocation.parent.eq(requestLocation));
	}

	public Integer findCountsByStatusPendingAndDirectorServiceIds(
		List<Long> directorServiceIds,
		Member director,
		List<Long> blockedMemberIds,
		Set<Long> hiddenRequestIds
	) {
		Long count = query
			.select(serviceRequest.count())
			.from(serviceRequest)
			.where(
				filterByDirectorServices(directorServiceIds),
				filterOnlyPendingAndIsReceivingEstimateTrue(),
				filterIsDeletedTrue(),
				filterDirectRequest(director),
				excludeOwnRequests(director),
				excludeBlockedMembers(blockedMemberIds),
				excludeAlreadyEstimatedByDirector(director.getDirectorInfo()),
				excludeRequestsWithSentEstimate(director.getDirectorInfo()),
				filterEstimateCountUnderLimit(),
				filterHiddenRequests(hiddenRequestIds),
				filterMatchedLocations(director.getDirectorInfo())
			)
			.fetchOne();

		return count == null ? 0 : count.intValue();
	}

	public Integer findCountsByStatusPendingAndDirectorServiceIdsAndDirectRequest(
		List<Long> directorServiceIds,
		Member director,
		List<Long> blockedMemberIds,
		Set<Long> hiddenRequestIds
	) {
		Long count = query
			.select(serviceRequest.count())
			.from(serviceRequest)
			.where(
				filterByDirectorServices(directorServiceIds),
				filterOnlyPendingAndIsReceivingEstimateTrue(),
				filterIsDeletedTrue(),
				excludeOwnRequests(director),
				excludeBlockedMembers(blockedMemberIds),
				excludeAlreadyEstimatedByDirector(director.getDirectorInfo()),
				excludeRequestsWithSentEstimate(director.getDirectorInfo()),
				filterEstimateCountUnderLimit(),
				filterHiddenRequests(hiddenRequestIds),
				filterMatchedLocations(director.getDirectorInfo()),
				filterOnlyDirectRequest(true, director)
			)
			.fetchOne();

		return count == null ? 0 : count.intValue();
	}
}
