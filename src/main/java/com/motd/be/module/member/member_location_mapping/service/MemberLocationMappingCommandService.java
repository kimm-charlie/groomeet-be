package com.motd.be.module.member.member_location_mapping.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member_location_mapping.entity.MemberLocationMapping;
import com.motd.be.module.member.member_location_mapping.repository.MemberLocationMappingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberLocationMappingCommandService {

	private final MemberLocationMappingRepository memberLocationMappingRepository;

	public void save(MemberLocationMapping newMapping) {
		memberLocationMappingRepository.save(newMapping);
	}

	public void delete(MemberLocationMapping mapping) {
		memberLocationMappingRepository.delete(mapping);
	}

	public void deleteAllByMember(Member member) {
		memberLocationMappingRepository.deleteAllByMember(member);
	}

	public void deleteAllByMappings(List<MemberLocationMapping> toDelete) {
		memberLocationMappingRepository.deleteAllByMappings(toDelete);
	}
}
