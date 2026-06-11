package com.motd.be.module.member.portfolio.repository;

import static com.motd.be.common.constants.PageSizeConstants.*;
import static com.motd.be.module.member.director_info.entity.QDirectorInfo.*;
import static com.motd.be.module.member.director_location_mapping.entity.QDirectorLocationMapping.*;
import static com.motd.be.module.member.portfolio.entity.QPortfolio.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.location.entity.LocationType;
import com.motd.be.module.member.portfolio.entity.Portfolio;
import com.motd.be.module.member.portfolio.entity.PortfolioSortType;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PortfolioQueryDslRepository {

	private final JPAQueryFactory query;

	public Slice<Portfolio> findAll(Long cursorId, Location location, List<Long> directorServiceIds,
		Long targetDirectorInfoId, List<Long> excludedDirectorMemberIds, String sortType, Long excludePortfolioId) {
		List<Portfolio> results = query
			.select(portfolio)
			.from(portfolio)
			.join(portfolio.directorInfo, directorInfo)
			.fetchJoin()
			.join(directorInfo.member)
			.fetchJoin()
			.where(filterByDirectorServiceIds(directorServiceIds), filterByLocation(location),
				filterByTargetDirectorInfoId(targetDirectorInfoId), filterIsDeletedTrue(),
				excludeBlockedDirectors(excludedDirectorMemberIds), filterExcludePortfolio(excludePortfolioId),
				filterByCursor(cursorId, sortType))
			.orderBy(sortBy(sortType))
			.limit(PORTFOLIO_PAGE_SIZE + 1)
			.fetch();

		boolean hasNext = results.size() > PORTFOLIO_PAGE_SIZE;
		List<Portfolio> content = hasNext ? results.subList(0, PORTFOLIO_PAGE_SIZE) : results;

		return new SliceImpl<>(content, Pageable.unpaged(), hasNext);
	}

	private OrderSpecifier<?>[] sortBy(String sortType) {
		if (sortType == null) {
			return new OrderSpecifier<?>[] {
				portfolio.createdAt.desc(),
				portfolio.id.desc()
			};
		}

		return switch (PortfolioSortType.from(sortType)) {
			case MOST_HIRED -> new OrderSpecifier<?>[] {
				portfolio.directorInfo.completedEstimateCount.desc(),
				portfolio.createdAt.desc(),
				portfolio.id.desc()
			};
			case LATEST -> new OrderSpecifier<?>[] {
				portfolio.createdAt.desc(),
				portfolio.id.desc()
			};
		};
	}

	// 커서 기반 페이지네이션: OFFSET 대신 WHERE 조건으로 "커서 이후 데이터"를 필터링한다.
	// 정렬 기준 값이 동일한 행이 있을 수 있으므로, 정렬 컬럼 조합(count → createdAt → id)으로 비교해야 정확한 위치를 특정할 수 있다.
	private BooleanExpression filterByCursor(Long cursorId, String sortType) {
		if (cursorId == null) {
			return null;
		}

		PortfolioSortType sort = sortType == null ? PortfolioSortType.LATEST : PortfolioSortType.from(sortType);

		if (sort == PortfolioSortType.MOST_HIRED) {
			Tuple cursorData = query
				.select(portfolio.directorInfo.completedEstimateCount, portfolio.createdAt)
				.from(portfolio)
				.join(portfolio.directorInfo)
				.where(portfolio.id.eq(cursorId))
				.fetchOne();

			if (cursorData == null) {
				return null;
			}

			Integer cursorCount = cursorData.get(portfolio.directorInfo.completedEstimateCount);
			LocalDateTime cursorCreatedAt = cursorData.get(portfolio.createdAt);

			return portfolio.directorInfo.completedEstimateCount.lt(cursorCount)
				.or(portfolio.directorInfo.completedEstimateCount.eq(cursorCount)
					.and(portfolio.createdAt.lt(cursorCreatedAt)))
				.or(portfolio.directorInfo.completedEstimateCount.eq(cursorCount)
					.and(portfolio.createdAt.eq(cursorCreatedAt))
					.and(portfolio.id.lt(cursorId)));
		}

		// LATEST: createdAt DESC, id DESC
		LocalDateTime cursorCreatedAt = query
			.select(portfolio.createdAt)
			.from(portfolio)
			.where(portfolio.id.eq(cursorId))
			.fetchOne();

		if (cursorCreatedAt == null) {
			return null;
		}

		return portfolio.createdAt.lt(cursorCreatedAt)
			.or(portfolio.createdAt.eq(cursorCreatedAt).and(portfolio.id.lt(cursorId)));
	}

	private BooleanExpression filterIsDeletedTrue() {
		return portfolio.isDeleted.isFalse();
	}

	private BooleanExpression filterByDirectorServiceIds(List<Long> directorServiceIds) {
		if (directorServiceIds != null && !directorServiceIds.isEmpty()) {
			return portfolio.directorService.id.in(directorServiceIds);
		}
		return null;
	}

	private BooleanExpression filterExcludePortfolio(Long excludePortfolioId) {
		if (excludePortfolioId != null) {
			return portfolio.id.ne(excludePortfolioId);
		}

		return null;
	}

	private BooleanExpression excludeBlockedDirectors(List<Long> excludedDirectorMemberIds) {
		if (excludedDirectorMemberIds == null || excludedDirectorMemberIds.isEmpty()) {
			return null;
		}

		return directorInfo.member.id.notIn(excludedDirectorMemberIds);
	}

	private BooleanExpression filterByTargetDirectorInfoId(Long targetDirectorInfoId) {
		if (targetDirectorInfoId != null) {
			return directorInfo.id.eq(targetDirectorInfoId);
		}
		return null;
	}

	private BooleanExpression filterByLocation(Location location) {
		if (location == null || location.getType() == LocationType.ALL_CITY) {
			return null;
		}

		return JPAExpressions
			.selectOne()
			.from(directorLocationMapping)
			.join(directorLocationMapping.location)
			.where(
				directorLocationMapping.directorInfo.eq(directorInfo),
				locationCondition(location)
			)
			.exists();
	}

	private BooleanExpression locationCondition(Location location) {
		return switch (location.getType()) {
			case ALL_CITY -> null;

			case CITY -> directorLocationMapping.location.id.eq(location.getId())
				.or(directorLocationMapping.location.parent.id.eq(location.getId()))
				.or(directorLocationMapping.location.type.eq(LocationType.ALL_CITY));

			case DISTRICT -> directorLocationMapping.location.id.eq(location.getId())
				.or(directorLocationMapping.location.id.eq(location.getParent().getId()))
				.or(directorLocationMapping.location.type.eq(LocationType.ALL_CITY));
		};
	}
}
