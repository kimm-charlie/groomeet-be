package com.motd.be.module.admin.director_service.repository;

import static com.motd.be.module.member.director_service.entity.QDirectorService.*;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.motd.be.module.member.director_service.entity.DirectorService;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DirectorServiceQueryDslRepositoryForAdmin {

	private final JPAQueryFactory query;

	public Slice<DirectorService> findAll(Pageable pageable, Boolean showIsDeleted, Long parentId) {
		JPAQuery<DirectorService> sql = query
			.select(directorService)
			.from(directorService)
			.where(
				filterByIsDeleted(showIsDeleted, parentId),
				filterByParentId(parentId)
			)
			.orderBy(getOrderSpecifier(showIsDeleted))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1);

		List<DirectorService> results = sql.fetch();
		boolean hasNext = results.size() > pageable.getPageSize();

		if (hasNext) {
			results.remove(results.size() - 1);
		}

		return new SliceImpl<>(results, pageable, hasNext);
	}

	private BooleanExpression filterByIsDeleted(Boolean showIsDeleted, Long parentId) {
		if (Boolean.TRUE.equals(showIsDeleted)) {
			return null;
		}
		if (parentId != null) {
			return directorService.isDeleted.eq(false);
		}
		return directorService.isDeleted.eq(false).and(directorService.isActive.isTrue());
	}

	private BooleanExpression filterByParentId(Long parentId) {
		if (parentId == null) {
			return directorService.parent.isNull();
		}
		return directorService.parent.id.eq(parentId);
	}

	private OrderSpecifier<?> getOrderSpecifier(Boolean showIsDeleted) {
		if (Boolean.TRUE.equals(showIsDeleted)) {
			return directorService.createdAt.desc();
		}
		return directorService.sortOrder.asc();
	}
}
