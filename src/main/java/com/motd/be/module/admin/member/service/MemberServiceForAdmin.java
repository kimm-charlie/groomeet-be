package com.motd.be.module.admin.member.service;

import static com.motd.be.common.constants.PageSizeConstants.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.motd.be.module.admin.member.dto.response.MemberCountDto;
import com.motd.be.module.admin.member.dto.response.MemberDetailResponseForAdmin;
import com.motd.be.module.admin.member.dto.response.MemberFindAllResponseForAdmin;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

import com.motd.be.module.member.member.entity.BanPeriod;

@Service
@RequiredArgsConstructor
public class MemberServiceForAdmin {

	private final MemberQueryServiceForAdmin memberQueryServiceForAdmin;

	public MemberFindAllResponseForAdmin findAll(int page, String search, Boolean showOnlyDirector, Boolean showOnlyMember) {
		Pageable pageable = PageRequest.of(page, MEMBER_PAGE_SIZE);

		Slice<Member> members = memberQueryServiceForAdmin.findAll(pageable, search, showOnlyDirector, showOnlyMember);
		MemberCountDto counts = memberQueryServiceForAdmin.countMembersBySearch(search);

		return MemberFindAllResponseForAdmin.from(members, counts.getTotalCount(), counts.getDirectorCount(),
			counts.getMemberCount());
	}

	public MemberDetailResponseForAdmin findDetail(Long memberId) {
		Member member = memberQueryServiceForAdmin.findById(memberId);

		return MemberDetailResponseForAdmin.from(member);
	}

	public void ban(Long memberId, BanPeriod banPeriod) {
		Member member = memberQueryServiceForAdmin.findById(memberId);

		member.ban(banPeriod);
	}
}
