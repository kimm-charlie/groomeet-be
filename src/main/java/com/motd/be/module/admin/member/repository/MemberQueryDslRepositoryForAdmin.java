package com.motd.be.module.admin.member.repository;

import static com.motd.be.module.member.member.entity.QMember.*;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.motd.be.module.admin.member.dto.response.MemberCountDto;
import com.motd.be.module.member.member.entity.Member;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberQueryDslRepositoryForAdmin {

	private final JPAQueryFactory query;

	public Slice<Member> findAll(Pageable pageable, String search, Boolean showOnlyDirector, Boolean showOnlyMember) {
		JPAQuery<Member> sql = query
			.select(member)
			.from(member)
			.where(
				filterBySearch(search),
				filterByRole(showOnlyDirector, showOnlyMember),
				member.isWithdrawal.eq(false)
			)
			.orderBy(member.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1);

		List<Member> results = sql.fetch();
		boolean hasNext = results.size() > pageable.getPageSize();

		if (hasNext) {
			results.remove(results.size() - 1);
		}

		return new SliceImpl<>(results, pageable, hasNext);
	}

	public MemberCountDto countMembersBySearch(String search) {
		Long totalCount = query
			.select(member.count())
			.from(member)
			.where(
				filterBySearch(search),
				member.isWithdrawal.eq(false)
			)
			.fetchOne();

		Long directorCount = query
			.select(member.count())
			.from(member)
			.where(
				filterBySearch(search),
				member.isDirector.eq(true),
				member.isWithdrawal.eq(false)
			)
			.fetchOne();

		Long memberCount = query
			.select(member.count())
			.from(member)
			.where(
				filterBySearch(search),
				member.isDirector.eq(false),
				member.isWithdrawal.eq(false)
			)
			.fetchOne();

		return MemberCountDto.builder()
			.totalCount(totalCount != null ? totalCount : 0L)
			.directorCount(directorCount != null ? directorCount : 0L)
			.memberCount(memberCount != null ? memberCount : 0L)
			.build();
	}

	private BooleanExpression filterBySearch(String search) {
		if (search == null || search.isBlank()) {
			return null;
		}
		if (!search.matches("^[0-9]*$")) {
			return member.nickname.containsIgnoreCase(search);
		}
		return member.nickname.containsIgnoreCase(search).or(member.id.stringValue().contains(search));
	}

	private BooleanExpression filterByRole(Boolean showOnlyDirector, Boolean showOnlyMember) {
		if (Boolean.TRUE.equals(showOnlyDirector)) {
			return member.isDirector.eq(true);
		}
		if (Boolean.TRUE.equals(showOnlyMember)) {
			return member.isDirector.eq(false);
		}
		return null;
	}
}
