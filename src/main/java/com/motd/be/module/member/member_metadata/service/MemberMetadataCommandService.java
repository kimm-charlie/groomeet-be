package com.motd.be.module.member.member_metadata.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member_metadata.entity.MemberMetadata;
import com.motd.be.module.member.member_metadata.repository.MemberMetadataRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberMetadataCommandService {

	private final MemberMetadataRepository memberMetadataRepository;

	public MemberMetadata save(MemberMetadata memberMetadata) {
		return memberMetadataRepository.save(memberMetadata);
	}

	public List<MemberMetadata> findAllByMember(Member member) {
		return memberMetadataRepository.findAllByMember(member);
	}
}
