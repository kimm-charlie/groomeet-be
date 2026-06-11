package com.motd.be.module.admin.member.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.MemberException;
import com.motd.be.module.admin.member.dto.response.MemberCountDto;
import com.motd.be.module.admin.member.repository.MemberQueryDslRepositoryForAdmin;
import com.motd.be.module.admin.member.repository.MemberRepositoryForAdmin;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryServiceForAdmin {

	private final MemberRepositoryForAdmin memberRepositoryForAdmin;
	private final MemberQueryDslRepositoryForAdmin memberQueryDslRepositoryForAdmin;

	public long countTotalMembers() {
		return memberRepositoryForAdmin.countByIsWithdrawalFalse();
	}

	public long countTodayMembers(LocalDateTime startOfDay, LocalDateTime endOfDay) {
		return memberRepositoryForAdmin.countByCreatedAtBetweenAndIsWithdrawalFalse(startOfDay, endOfDay);
	}

	public Member findById(Long memberId) {
		return memberRepositoryForAdmin.findByIdAndIsWithdrawalFalse(memberId)
			.orElseThrow(() -> new CustomRuntimeException(MemberException.NOT_FOUND));
	}

	public Slice<Member> findAll(Pageable pageable, String search, Boolean showOnlyDirector, Boolean showOnlyMember) {
		return memberQueryDslRepositoryForAdmin.findAll(pageable, search, showOnlyDirector, showOnlyMember);
	}

	public MemberCountDto countMembersBySearch(String search) {
		return memberQueryDslRepositoryForAdmin.countMembersBySearch(search);
	}

	public List<Member> findAllWithIsWithdrawalFalse() {
		return memberRepositoryForAdmin.findAllWithIsWithdrawalFalse();
	}
}

