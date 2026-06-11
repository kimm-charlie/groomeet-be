package com.motd.be.module.director.member.facade;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.director.member.dto.response.MemberProfileSummaryResponseForDirector;
import com.motd.be.module.director.member.service.MemberQueryServiceForDirector;
import com.motd.be.module.director.service_request.service.ServiceRequestQueryServiceForDirector;
import com.motd.be.module.member.service_request.entity.ServiceRequestStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberFacadeForDirector {

	private final MemberQueryServiceForDirector memberQueryServiceForDirector;
	private final ServiceRequestQueryServiceForDirector serviceRequestQueryServiceForDirector;

	public MemberProfileSummaryResponseForDirector findProfileSummary(Long targetMemberId) {
		// 회원 조회
		memberQueryServiceForDirector.findById(targetMemberId);

		// 요청 요약 정보 조회
		Map<ServiceRequestStatus, Integer> serviceRequestCountMap = serviceRequestQueryServiceForDirector.countByMemberId(
			targetMemberId);

		return MemberProfileSummaryResponseForDirector.from(serviceRequestCountMap);
	}
}
