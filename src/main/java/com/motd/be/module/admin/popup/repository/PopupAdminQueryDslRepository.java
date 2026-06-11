package com.motd.be.module.admin.popup.repository;

import static com.motd.be.module.member.popup.entity.QPopup.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.motd.be.module.member.popup.entity.Popup;
import com.motd.be.module.member.popup.entity.PopupType;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PopupAdminQueryDslRepository {

	private final JPAQueryFactory query;

	public Slice<Popup> findAll(Pageable pageable, Boolean showIsDeleted, PopupType type) {
		JPAQuery<Popup> sql = query
			.select(popup)
			.from(popup)
			.where(filterByShowIsDeleted(showIsDeleted), filterByType(type))
			.orderBy(getOrderSpecifier(showIsDeleted))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1);

		List<Popup> results = sql.fetch();

		boolean hasNext = results.size() > pageable.getPageSize();

		if (hasNext) {
			results.remove(results.size() - 1);
		}

		return new SliceImpl<>(results, pageable, hasNext);
	}

	private BooleanExpression filterByShowIsDeleted(Boolean showIsDeleted) {
		if (Boolean.TRUE.equals(showIsDeleted)) {
			return null; // 전체 조회
		}

		LocalDateTime now = LocalDateTime.now();
		return popup.isDeleted.eq(false)
			.and(popup.endAt.goe(now));
	}

	private BooleanExpression filterByType(PopupType type) {
		if (type == null) {
			return null;
		}
		return popup.type.eq(type);
	}

	private OrderSpecifier<?> getOrderSpecifier(Boolean showIsDeleted) {
		if (Boolean.TRUE.equals(showIsDeleted)) {
			return popup.createdAt.desc();
		} else {
			return popup.sortOrder.asc();
		}

	}
}

