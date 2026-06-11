package com.motd.be.module.admin.member.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.admin.member.dto.request.BanRequestForAdmin;
import com.motd.be.module.admin.member.dto.response.MemberDetailResponseForAdmin;
import com.motd.be.module.admin.member.dto.response.MemberFindAllResponseForAdmin;
import com.motd.be.module.admin.member.service.MemberServiceForAdmin;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberFacadeForAdmin {

	private final MemberServiceForAdmin memberServiceForAdmin;

	public MemberFindAllResponseForAdmin findAll(int page, String search, Boolean showOnlyDirector,
		Boolean showOnlyMember) {
		return memberServiceForAdmin.findAll(page, search, showOnlyDirector, showOnlyMember);
	}

	public MemberDetailResponseForAdmin findDetail(Long memberId) {
		return memberServiceForAdmin.findDetail(memberId);
	}

	@Transactional
	public void ban(Long memberId, BanRequestForAdmin request) {
		memberServiceForAdmin.ban(memberId, request.getBanPeriod());
	}
}
