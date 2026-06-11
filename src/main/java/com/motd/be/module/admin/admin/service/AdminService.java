package com.motd.be.module.admin.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.admin.admin.dto.response.AdminFindDetailResponse;
import com.motd.be.module.admin.member.service.MemberQueryServiceForAdmin;
import com.motd.be.shared.hackle.service.HackleEventPublisher;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

	private final AdminQueryService adminQueryService;
	private final HackleEventPublisher hackleEventPublisher;
	private final MemberQueryServiceForAdmin memberQueryServiceForAdmin;

	public AdminFindDetailResponse findInfo(Long adminId) {
		return AdminFindDetailResponse.from(adminQueryService.findById(adminId));
	}
}
