package com.motd.be.provider.module.member;

import static com.motd.be.common.utils.Utils.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.location.entity.Location;
import com.motd.be.module.member.member.entity.Member;
import com.motd.be.module.member.member_location_mapping.entity.MemberLocationMapping;
import com.motd.be.module.member.member_location_mapping.repository.MemberLocationMappingRepository;

@Component
public class MemberLocationMappingProvider {

	@Autowired
	private MemberLocationMappingRepository memberLocationMappingRepository;

	public MemberLocationMapping save(Member member, Location location) {
		return memberLocationMappingRepository.save(MemberLocationMapping.builder()
			.member(member)
			.location(location)
			.activeUniqueKey(generateMemberLocationMappingUniqueKey(member, location))
			.build());
	}

	public List<MemberLocationMapping> findAll() {
		return memberLocationMappingRepository.findAll();
	}
}
