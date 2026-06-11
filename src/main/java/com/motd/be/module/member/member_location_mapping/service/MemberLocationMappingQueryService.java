package com.motd.be.module.member.member_location_mapping.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.member_location_mapping.entity.MemberLocationMapping;
import com.motd.be.module.member.member_location_mapping.repository.MemberLocationMappingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberLocationMappingQueryService {

	private final MemberLocationMappingRepository memberLocationMappingRepository;

	public List<MemberLocationMapping> findAllByMemberIdWithLocation(Long memberId) {
		return memberLocationMappingRepository.findAllByMemberIdWithLocation(memberId);
	}
}
