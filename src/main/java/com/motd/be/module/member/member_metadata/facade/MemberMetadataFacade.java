package com.motd.be.module.member.member_metadata.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member.service.MemberQueryService;
import com.motd.be.module.member.member_metadata.dto.request.MemberMetadataSaveOrUpdateRequest;
import com.motd.be.module.member.member_metadata.dto.response.MemberMetadataFindResponse;
import com.motd.be.module.member.member_metadata.service.MemberMetadataService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberMetadataFacade {

	private final MemberQueryService memberQueryService;
	private final MemberMetadataService memberMetadataService;

	@Transactional
	public void saveOrUpdate(Long memberId, MemberMetadataSaveOrUpdateRequest request) {
		// 1. 회원 조회
		Member member = memberQueryService.findById(memberId);

		// 2. 회원 메타데이터 저장/갱신
		memberMetadataService.saveOrUpdate(member, request);
	}

	public MemberMetadataFindResponse find(Long memberId, String deviceType) {
		// 1. 회원 조회
		Member member = memberQueryService.findById(memberId);

		// 2. 회원 메타데이터 조회
		return memberMetadataService.find(member, deviceType);
	}
}
