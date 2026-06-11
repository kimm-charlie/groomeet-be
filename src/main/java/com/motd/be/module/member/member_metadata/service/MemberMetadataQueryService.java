package com.motd.be.module.member.member_metadata.service;

import org.springframework.stereotype.Service;

import com.motd.be.exception.CustomRuntimeException;
import com.motd.be.exception.exceptions.MemberMetadataException;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member_metadata.entity.DeviceType;
import com.motd.be.module.member.member_metadata.entity.MemberMetadata;
import com.motd.be.module.member.member_metadata.repository.MemberMetadataRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberMetadataQueryService {

	private final MemberMetadataRepository memberMetadataRepository;

	public MemberMetadata findByMemberAndDeviceType(Member member, DeviceType deviceType) {
		return memberMetadataRepository.findByMemberAndDeviceType(member, deviceType)
			.orElseThrow(() -> new CustomRuntimeException(MemberMetadataException.NOT_FOUND));
	}
}
