package com.motd.be.provider.module.member;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.director.business_registration.repository.BusinessRegistrationRepositoryForDirector;
import com.motd.be.module.member.business_registration.entity.BusinessRegistration;
import com.motd.be.module.member.member.entity.Member;

@Component
public class BusinessRegistrationProvider {

	@Autowired
	private BusinessRegistrationRepositoryForDirector businessRegistrationRepositoryForDirector;

	public List<BusinessRegistration> findAll() {
		return businessRegistrationRepositoryForDirector.findAll();
	}

	public BusinessRegistration save(Member member, String businessRegistrationNumber,
		String residentRegistrationNumber) {
		return businessRegistrationRepositoryForDirector.save(BusinessRegistration.builder()
			.member(member)
			.businessRegistrationNumber(businessRegistrationNumber)
			.residentRegistrationNumber(residentRegistrationNumber)
			.build());
	}

}
