package com.motd.be.module.admin.portfolio.repository;

import static com.motd.be.common.constants.PageSizeConstants.*;
import static com.motd.be.module.member.portfolio.entity.QPortfolio.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.motd.be.module.member.portfolio.entity.Portfolio;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PortfolioQueryDslRepositoryForAdmin {

	private final JPAQueryFactory query;

	public Slice<Portfolio> findAll(Long cursorId, String search, Boolean isPopular, Boolean showIsDeleted) {
		List<Portfolio> portfolios = query
			.select(portfolio)
			.from(portfolio)
			.join(portfolio.directorService).fetchJoin()
			.join(portfolio.directorInfo).fetchJoin()
			.join(portfolio.directorInfo.member).fetchJoin()
			.where(
				filterByIsDeleted(showIsDeleted),
				filterByCursor(cursorId, isPopular),
				filterBySearch(search),
				filterByIsPopular(isPopular)
			)
			.orderBy(getOrderSpecifiers(isPopular))
			.limit(ADMIN_PORTFOLIO_PAGE_SIZE + 1)
			.fetch();

		boolean hasNext = portfolios.size() > ADMIN_PORTFOLIO_PAGE_SIZE;
		List<Portfolio> content = hasNext ? portfolios.subList(0, ADMIN_PORTFOLIO_PAGE_SIZE) : portfolios;

		return new SliceImpl<>(content, Pageable.unpaged(), hasNext);
	}

	private OrderSpecifier<?>[] getOrderSpecifiers(Boolean isPopular) {
		if (Boolean.TRUE.equals(isPopular)) {
			return new OrderSpecifier<?>[] {portfolio.popularAt.desc(), portfolio.id.desc()};
		}
		return new OrderSpecifier<?>[] {portfolio.id.desc()};
	}

	private BooleanExpression filterByCursor(Long cursorId, Boolean isPopular) {
		if (cursorId == null) {
			return null;
		}

		if (Boolean.TRUE.equals(isPopular)) {
			LocalDateTime cursorPopularAt = query
				.select(portfolio.popularAt)
				.from(portfolio)
				.where(portfolio.id.eq(cursorId))
				.fetchOne();

			if (cursorPopularAt == null) {
				return portfolio.id.lt(cursorId);
			}

			return portfolio.popularAt.lt(cursorPopularAt)
				.or(portfolio.popularAt.eq(cursorPopularAt).and(portfolio.id.lt(cursorId)));
		}

		return portfolio.id.lt(cursorId);
	}

	private BooleanExpression filterBySearch(String search) {
		if (search == null || search.isBlank()) {
			return null;
		}
		return portfolio.directorInfo.member.nickname.containsIgnoreCase(search)
			.or(portfolio.directorInfo.member.id.stringValue().contains(search));
	}

	private BooleanExpression filterByIsPopular(Boolean isPopular) {
		if (isPopular == null) {
			return null;
		}
		return portfolio.isPopular.eq(isPopular);
	}

	private BooleanExpression filterByIsDeleted(Boolean showIsDeleted) {
		if (Boolean.TRUE.equals(showIsDeleted)) {
			return null;
		}
		return portfolio.isDeleted.eq(false);
	}
}
