package com.motd.be.module.member.member_location_mapping.facade;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.location.dto.response.LocationResponse;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;
import com.motd.be.module.member.member_location_mapping.dto.request.MemberLocationMappingUpdateRequest;
import com.motd.be.module.member.member_location_mapping.service.MemberLocationMappingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberLocationMappingFacade {

	private final MemberQueryService memberQueryService;
	private final MemberLocationMappingService memberLocationMappingService;

	public List<LocationResponse> findAll(Long memberId) {
		Member member = memberQueryService.findById(memberId);
		return memberLocationMappingService.findAll(member);
	}

	@Transactional
	public void saveOrUpdate(Long memberId, MemberLocationMappingUpdateRequest request) {
		Member member = memberQueryService.findById(memberId);
		memberLocationMappingService.updateMappings(member, request.getLocationIds());
	}
}
