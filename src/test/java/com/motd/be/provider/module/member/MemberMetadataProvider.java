package com.motd.be.provider.module.member;

import static com.motd.be.common.utils.Utils.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member_metadata.entity.DeviceType;
import com.motd.be.module.member.member_metadata.entity.MemberMetadata;
import com.motd.be.module.member.member_metadata.repository.MemberMetadataRepository;

@Component
public class MemberMetadataProvider {

	@Autowired
	private MemberMetadataRepository memberMetadataRepository;

	public List<MemberMetadata> findAllByMember(Member member) {
		return memberMetadataRepository.findAllByMember(member);
	}

	public MemberMetadata save(Member member, DeviceType deviceType, String version) {
		return memberMetadataRepository.save(MemberMetadata.builder()
			.member(member)
			.deviceType(deviceType)
			.version(version)
			.activeUniqueKey(generateMemberMetadataUniqueKey(member.getId(), deviceType))
			.build());
	}
}
