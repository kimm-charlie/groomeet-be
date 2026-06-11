package com.motd.be.module.admin.consulting_request.repository;

import static com.motd.be.module.member.consulting_request.entity.QConsultingRequest.*;
import static com.motd.be.module.member.consulting_sheet.entity.QConsultingSheet.*;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.motd.be.module.member.consulting_request.entity.ConsultingRequest;
import com.motd.be.module.member.consulting_sheet.enums.ConsultingSheetStatus;
import com.motd.be.module.member.member.entity.QMember;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ConsultingRequestQueryDslRepositoryForAdmin {

	private final JPAQueryFactory queryFactory;

	public Slice<ConsultingRequest> findAll(String search, Boolean showAll, Pageable pageable) {
		List<ConsultingRequest> content = queryFactory.selectFrom(consultingRequest)
			.leftJoin(consultingRequest.member).fetchJoin()
			.leftJoin(consultingRequest.reservedBy).fetchJoin()
			.leftJoin(consultingRequest.reservedBy.member).fetchJoin()
			.where(
				filterBySearch(search),
				statusFilter(showAll),
				consultingRequest.isDeleted.isFalse()
			)
			.orderBy(consultingRequest.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		boolean hasNext = content.size() > pageable.getPageSize();
		if (hasNext) {
			content = content.subList(0, pageable.getPageSize());
		}

		return new SliceImpl<>(content, pageable, hasNext);
	}

	public Long count(String search, Boolean showAll) {
		return queryFactory.select(consultingRequest.count())
			.from(consultingRequest)
			.leftJoin(consultingRequest.member)
			.leftJoin(consultingRequest.reservedBy)
			.leftJoin(consultingRequest.reservedBy.member)
			.where(
				filterBySearch(search),
				statusFilter(showAll),
				consultingRequest.isDeleted.isFalse()
			)
			.fetchOne();
	}

	private BooleanExpression filterBySearch(String search) {
		if (search == null || search.isBlank()) {
			return null;
		}
		QMember requestMember = consultingRequest.member;
		BooleanExpression memberSearch = requestMember.nickname.containsIgnoreCase(search);

		// 선점 디렉터 닉네임 검색
		BooleanExpression reservedDirectorSearch = consultingRequest.reservedBy.member.nickname
			.containsIgnoreCase(search);

		// 컨설팅지 작성 디렉터 닉네임 검색
		BooleanExpression sheetDirectorSearch = JPAExpressions
			.selectOne()
			.from(consultingSheet)
			.where(
				consultingSheet.consultingRequest.id.eq(consultingRequest.id),
				consultingSheet.directorInfo.member.nickname.containsIgnoreCase(search),
				consultingSheet.isDeleted.isFalse()
			)
			.exists();

		return memberSearch.or(reservedDirectorSearch).or(sheetDirectorSearch);
	}

	private BooleanExpression statusFilter(Boolean showAll) {
		if (Boolean.TRUE.equals(showAll)) {
			return null;
		}

		// 컨설팅지가 아예 없는 요청서 (PENDING/RESERVED 상태)
		BooleanExpression noSheetExists = JPAExpressions
			.selectOne()
			.from(consultingSheet)
			.where(
				consultingSheet.consultingRequest.id.eq(consultingRequest.id),
				consultingSheet.isDeleted.isFalse()
			)
			.notExists();

		// 승인 대기 중인 컨설팅지가 있는 요청서
		BooleanExpression pendingApprovalSheetExists = JPAExpressions
			.selectOne()
			.from(consultingSheet)
			.where(
				consultingSheet.consultingRequest.id.eq(consultingRequest.id),
				consultingSheet.status.eq(ConsultingSheetStatus.PENDING_APPROVAL),
				consultingSheet.isDeleted.isFalse()
			)
			.exists();

		return noSheetExists.or(pendingApprovalSheetExists);
	}
}
