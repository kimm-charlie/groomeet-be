package com.motd.be.module.director.business_registration.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motd.be.module.director.business_registration.dto.request.BusinessRegistrationCreateRequestForDirector;
import com.motd.be.module.director.business_registration.dto.response.BusinessRegistrationFindResponseForDirector;
import com.motd.be.module.director.business_registration.service.BusinessRegistrationServiceForDirector;
import com.motd.be.module.director.business_registration_file.service.BusinessRegistrationFileServiceForDirector;
import com.motd.be.module.director.member.service.MemberQueryServiceForDirector;
import com.motd.be.module.member.business_registration.entity.BusinessRegistration;
import com.motd.be.module.member.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessRegistrationFacadeForDirector {

	private final MemberQueryServiceForDirector memberQueryServiceForDirector;
	private final BusinessRegistrationServiceForDirector businessRegistrationServiceForDirector;
	private final BusinessRegistrationFileServiceForDirector businessRegistrationFileServiceForDirector;

	@Transactional
	public void register(Long memberId, BusinessRegistrationCreateRequestForDirector request) {
		Member director = memberQueryServiceForDirector.findByIdWithDirector(memberId);

		BusinessRegistration businessRegistration = businessRegistrationServiceForDirector.register(director, request);

		businessRegistrationFileServiceForDirector.mapRegistration(businessRegistration,
			request.getFileIds(), director);
	}

	public BusinessRegistrationFindResponseForDirector find(Long memberId) {
		Member director = memberQueryServiceForDirector.findByIdWithDirector(memberId);

		return businessRegistrationServiceForDirector.find(director);
	}
}
