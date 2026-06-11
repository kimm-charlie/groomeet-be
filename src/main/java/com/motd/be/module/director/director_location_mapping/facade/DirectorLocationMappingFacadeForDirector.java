package com.motd.be.module.director.director_location_mapping.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.director.director_location_mapping.dto.request.DirectorLocationMappingUpdateRequestForDirector;
import com.motd.be.module.director.director_location_mapping.service.DirectorLocationMappingServiceForDirector;
import com.motd.be.module.director.member.service.MemberQueryServiceForDirector;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectorLocationMappingFacadeForDirector {

	private final MemberQueryServiceForDirector memberQueryServiceForDirector;
	private final DirectorLocationMappingServiceForDirector directorLocationMappingServiceForDirector;

	@Transactional
	public void updateLocation(Long memberId, DirectorLocationMappingUpdateRequestForDirector request) {
		// 1. 디렉터 회원 조회
		Member member = memberQueryServiceForDirector.findByIdWithDirector(memberId);

		// 2. 지역 매핑 업데이트
		directorLocationMappingServiceForDirector.updateMappings(member.getDirectorInfo(), request.getLocationIds());
	}
}
