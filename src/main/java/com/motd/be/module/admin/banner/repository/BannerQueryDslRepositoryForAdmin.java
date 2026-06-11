package com.motd.be.module.admin.banner.repository;

import static com.motd.be.module.member.banner.entity.QBanner.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.motd.be.module.member.banner.entity.Banner;
import com.motd.be.module.member.banner.entity.BannerType;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BannerQueryDslRepositoryForAdmin {

	private final JPAQueryFactory query;

	public Slice<Banner> findAll(Pageable pageable, Boolean showIsDeleted, BannerType type) {
		JPAQuery<Banner> sql = query
			.select(banner)
			.from(banner)
			.where(
				filterByIsDeleted(showIsDeleted),
				filterByType(type)
			)
			.orderBy(getOrderSpecifier(showIsDeleted))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1);

		List<Banner> results = sql.fetch();
		boolean hasNext = results.size() > pageable.getPageSize();

		if (hasNext) {
			results.remove(results.size() - 1);
		}

		return new SliceImpl<>(results, pageable, hasNext);
	}

	/**
	 * 삭제 뿐만아니라 비활성화 된것도 필터링 한다.
	 *
	 * @param showIsDeleted
	 * @return
	 */
	private BooleanExpression filterByIsDeleted(Boolean showIsDeleted) {
		if (Boolean.TRUE.equals(showIsDeleted)) {
			return null; // 전체 조회
		}

		LocalDateTime now = LocalDateTime.now();
		return banner.isDeleted.eq(false)
			.and(banner.endAt.goe(now));
	}

	private BooleanExpression filterByType(BannerType type) {
		if (type == null) {
			return null;
		}
		return banner.type.eq(type);
	}

	private OrderSpecifier<?> getOrderSpecifier(Boolean showIsDeleted) {
		if (Boolean.TRUE.equals(showIsDeleted)) {
			return banner.createdAt.desc();
		}
		return banner.sortOrder.asc();
	}
}
